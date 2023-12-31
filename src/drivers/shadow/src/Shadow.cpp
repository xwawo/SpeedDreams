/***************************************************************************

    file        : shadow.cpp
    created     : 30 Dec 2019
    copyright   : (C) 2019 Xavier Bertaux

 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#ifdef _WIN32
#include <windows.h>
#endif

#include <cstdio>
#include <cstdlib>
#include <string>
#include <vector>
#include <utility>
#include <math.h>

#include <portability.h>
#include <tgf.h>
#include <track.h>
#include <car.h>
#include <raceman.h>
#include <robottools.h>
#include <timeanalysis.h>
#include <robot.h>

#include "Driver.h"

using ::std::string;
using ::std::vector;
using ::std::pair;

class Shared;
class Driver;

// The "SHADOW" logger instance
GfLogger* PLogSHADOW = 0;

static Shared       s_shared;

// TORCS interface
static void initTrack(int index, tTrack* track, void *carHandle, void **carParmHandle, tSituation *s);
static void newRace(int index, tCarElt* car, tSituation *s);
static void drive(int index, tCarElt* car, tSituation *s);
static int  pitcmd(int index, tCarElt* car, tSituation *s);
static void shutdown(int index);
static int  InitFuncPt(int index, void *pt);
static void endRace(int index, tCarElt *car, tSituation *s);

// SD interface
static const int BUFSIZE = 512;
static const int MAXNBBOTS = 100;
static const string defaultBotName[MAXNBBOTS] =                  // NOLINT(runtime/string)
{
        "driver 1",  "driver 2",  "driver 3",  "driver 4",  "driver 5",
        "driver 6",  "driver 7",  "driver 8",  "driver 9",  "driver 10",
        "driver 11", "driver 12", "driver 13", "driver 14", "driver 15",
        "driver 16", "driver 17", "driver 18", "driver 19", "driver 20"
        };

static const string defaultBotDesc[MAXNBBOTS] =                  // NOLINT(runtime/string)
{
        "driver 1",  "driver 2",  "driver 3",  "driver 4",  "driver 5",
        "driver 6",  "driver 7",  "driver 8",  "driver 9",  "driver 10",
        "driver 11", "driver 12", "driver 13", "driver 14", "driver 15",
        "driver 16", "driver 17", "driver 18", "driver 19", "driver 20"
        };

// Drivers info: pair(first:Name, second:Desc)
static vector< pair<string, string> > Drivers;
static Driver *driver[MAXNBBOTS];  // Array of drivers

// Number of drivers defined in robot's xml-file
static int NBBOTS = 0;      // Still unknown
static string nameBuffer;   // Robot's name // NOLINT(runtime/string)
static string pathBuffer;   // Robot's xml-filename // NOLINT(runtime/string)

// Save start index offset from robot's xml file
static int indexOffset = 0;
// Marker for undefined drivers to be able to comment out drivers
// in the robot's xml-file between others, not only at the end of the list
const char *sUndefined = "undefined";

////////////////////////////////
// Utility
////////////////////////////////
// Set robots's name and xml file pathname
static void setRobotName(const string &name)
{
    char buffer[BUFSIZE];
    snprintf(buffer, BUFSIZE, "drivers/%s/%s.xml", name.c_str(), name.c_str());
    nameBuffer = name;
    pathBuffer = buffer;
}

////////////////////////////////////////////////////////////////
// SD Interface (new, fixed name scheme, from Andrew's USR code)
////////////////////////////////////////////////////////////////

// Module entry point (new fixed name scheme).
// Extended for use with schismatic robots

extern "C" int moduleWelcome(const tModWelcomeIn* welcomeIn, tModWelcomeOut* welcomeOut)
{
    // Save module name and loadDir, and determine module XML file pathname.
    setRobotName(welcomeIn->name);

    // Filehandle for robot's xml-file
    void *pRobotSettings = GfParmReadFile(pathBuffer, GFPARM_RMODE_STD);

    PLogSHADOW = GfLogger::instance("SHADOW");

    if (pRobotSettings)               // robot settings XML could be read
    {
        NBBOTS = 0;

        char SectionBuffer[BUFSIZE];
        snprintf(SectionBuffer, BUFSIZE, "%s/%s/%d", ROB_SECT_ROBOTS, ROB_LIST_INDEX, 0);

        // Try to get first driver from index 0
        string sDriverName = GfParmGetStrNC(pRobotSettings,
                                            SectionBuffer,
                                            ROB_ATTR_NAME,
                                            const_cast<char*>(sUndefined));

        // Check whether index 0 is used as start index
        if (sDriverName != sUndefined)
        {
            // Teams xml file uses index 0, 1, ..., N - 1
            indexOffset = 0;
        }
        else
        {
            // Teams xml file uses index 1, 2, ..., N
            indexOffset = 1;
        }

        // Loop over all possible drivers, clear all buffers,
        // save defined driver names and descriptions.
        Drivers.clear();

        for (int i = indexOffset; i < MAXNBBOTS + indexOffset; ++i)
        {
            snprintf(SectionBuffer, BUFSIZE, "%s/%s/%d", ROB_SECT_ROBOTS, ROB_LIST_INDEX, i);
            sDriverName = GfParmGetStr(pRobotSettings, SectionBuffer, ROB_ATTR_NAME, sUndefined);

            if (sDriverName != sUndefined)
            {
                // This driver is defined in robot's xml-file
                string sDriverDesc = GfParmGetStr(pRobotSettings, SectionBuffer, ROB_ATTR_DESC, defaultBotDesc[i].c_str());
                Drivers.push_back(make_pair(sDriverName, sDriverDesc));
                ++NBBOTS;
            }
        }  // for i

        GfParmReleaseHandle(pRobotSettings);
    }
    else        // if robot settings XML could not be read
    {
        // For schismatic robots NBBOTS is unknown! Handle error here
        NBBOTS = 0;
        // But this is not considered a real failure of moduleWelcome !
    }

    // Set max nb of interfaces to return.
    welcomeOut->maxNbItf = NBBOTS;

    return 0;
}

// Module entry point (new fixed name scheme).
extern "C" int moduleInitialize(tModInfo *modInfo)
{
    // Clear all structures.
    memset(modInfo, 0, NBBOTS * sizeof(tModInfo));

    for (int i = 0; i < NBBOTS; i++)
    {
        modInfo[i].name = Drivers[i].first.c_str();
        modInfo[i].desc = Drivers[i].second.c_str();
        modInfo[i].fctInit = InitFuncPt;       // Init function.
        modInfo[i].gfId    = ROB_IDENT;        // Supported framework version.
        modInfo[i].index   = i + indexOffset;  // Indices from robot's xml-file.
    }  // for i

    return 0;
}

// Module exit point (new fixed name scheme).
extern "C" int moduleTerminate()
{
    return 0;
}

////////////////////////////////////////////////////////////////
// TORCS backward compatibility scheme, from Andrew's USR code
////////////////////////////////////////////////////////////////

// Module entry point
extern "C" int shadow(tModInfo *modInfo)
{
    NBBOTS = 10;
    Drivers.clear();
    pathBuffer = "drivers/shadow/shadow.xml";
    nameBuffer = "shadow";

    // Filehandle for robot's xml-file
    void *pRobotSettings = GfParmReadFile(pathBuffer, GFPARM_RMODE_STD);

    if (pRobotSettings)                  // Let's look what we have to provide here
    {
        char SectionBuffer[BUFSIZE];

        for (int i = 0; i < NBBOTS; i++)
        {
            snprintf(SectionBuffer, BUFSIZE, "%s/%s/%d", ROB_SECT_ROBOTS, ROB_LIST_INDEX, i);
            string sDriverName = GfParmGetStr(pRobotSettings, SectionBuffer, ROB_ATTR_NAME, defaultBotName[i].c_str());
            string sDriverDesc = GfParmGetStr(pRobotSettings, SectionBuffer, ROB_ATTR_DESC, defaultBotDesc[i].c_str());
            Drivers.push_back(make_pair(sDriverName, sDriverDesc));
        }

        GfParmReleaseHandle(pRobotSettings);
    }

    return moduleInitialize(modInfo);
}

// Module exit point (TORCS backward compatibility scheme).
extern "C" int shadowShut()
{
    return moduleTerminate();
}

// Module interface initialization.
static int InitFuncPt(int index, void *pt)
{
    tRobotItf *itf = static_cast<tRobotItf *>(pt);

    // Create robot instance for index.
    driver[index] = new Driver(index);
    driver[index]->MyBotName = nameBuffer.c_str();

    itf->rbNewTrack = initTrack;    // Give the robot the track view called.
    itf->rbNewRace  = newRace;      // Start a new race.
    itf->rbDrive    = drive;        // Drive during race.
    itf->rbPitCmd   = pitcmd;       // Pit commands.
    itf->rbEndRace  = endRace;      // End of the current race.
    itf->rbShutdown = shutdown;     // Called before the module is unloaded.
    itf->index      = index;        // Index used if multiple interfaces.

    return 0;
}

// Called for every track change or new race.
static void initTrack(int index, tTrack* track, void *carHandle,
                      void **carParmHandle, tSituation *s)
{
    driver[index]->SetShared( &s_shared );
    driver[index]->InitTrack(index, track, carHandle, carParmHandle, s);
}


// Start a new race.
static void newRace(int index, tCarElt* car, tSituation *s)
{
    driver[index]->NewRace(index, car, s);
}

// Drive during race.
static void drive(int index, tCarElt* car, tSituation *s)
{
    driver[index]->Drive(index, car, s);
}

// Pitstop callback.
static int pitcmd(int index, tCarElt* car, tSituation *s)
{
    return driver[index]->PitCmd(index, car, s);
}

// End of the current race.
static void endRace(int index, tCarElt *car, tSituation *s)
{
    driver[index]->EndRace(index, car, s);
}

// Called before the module is unloaded.
static void shutdown(int index)
{
    driver[index]->Shutdown(index);
    delete driver[index];
}
