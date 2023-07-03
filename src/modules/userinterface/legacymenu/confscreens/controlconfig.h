/***************************************************************************

    file        : controlconfig.h
    created     : Wed Mar 12 22:09:01 CET 2003
    copyright   : (C) 2003 by Eric Espie
    email       : eric.espie@torcs.org   
    version     : $Id: controlconfig.h 8068 2022-03-05 21:03:42Z iobyte $

***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
 
/** @file    
    		
    @author	<a href=mailto:torcs@free.fr>Eric Espie</a>
    @version	$Id: controlconfig.h 8068 2022-03-05 21:03:42Z iobyte $
*/

#ifndef _CONTROLCONFIG_H_
#define _CONTROLCONFIG_H_

#include <playerpref.h>

#include "confscreens.h"


extern void *ControlMenuInit(void *prevMenu, void *prefHdle, unsigned index, tGearChangeMode gearChangeMode, int saveOnExit);

/* Load control settings for player of given index (the current one if 0) 
   from preferences (if given parm handle is null, use current) */
extern void ControlGetSettings(void *prefHdle = 0, unsigned index = 0);

/* Save control settings for player of given index (the current one if 0) 
   into preference, according to its selected gear change mode
   (if given parm handle is null, use current) */
extern void ControlPutSettings(void *prefHdle = 0, unsigned index = 0, tGearChangeMode gearChangeMode = GEAR_MODE_NONE);


typedef struct
{
    const char  *name;
    tCtrlRef    ref;
    int	        Id;
    int         labelId;
    const char  *minName;
    float       min;
    const char  *maxName;
    float       max;
    const char  *powName;
    float       pow;
    int         keyboardPossible;
    int         pref;
    int         butIgnore;
} tCmdInfo;

#endif /* _CONTROLCONFIG_H_ */ 



