/***************************************************************************

    file                 : objects.cpp
    created              : Fri May 24 20:09:20 CEST 2002
    copyright            : (C) 2001 by Eric Espie
    email                : eric.espie@torcs.org
    version              : $Id: objects.cpp 8616 2022-11-03 02:41:40Z iobyte $

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

    @author	<a href=mailto:eric.espie@torcs.org>Eric Espie</a>
    @version	$Id: objects.cpp 8616 2022-11-03 02:41:40Z iobyte $
*/


#include <algorithm>
#include <cstdio>
#include <cstdlib>
#include <cctype>
#include <cstring>
#ifndef WIN32
#include <unistd.h>
#endif
#include <cmath>
#include <plib/ssg.h>

#include <tgfclient.h>
#include <track.h>
#include <portability.h>

#include "trackgen.h"
#include "util.h"
#include "elevation.h"
#include "ac3d.h"
#include "objects.h"

struct group
{
    ssgBranch	*br;
};

static float		GroupSize;
static float		XGroupOffset;
static float		YGroupOffset;
static int		XGroupNb;
static int		ObjUniqId = 0;


typedef struct objdef
{
    GF_TAILQ_ENTRY(objdef)	link;
    bool			random, trackOriented, terrainOriented, borderOriented;
    unsigned int	color;
    ssgEntity		*obj;
    tdble		deltaHeight;
    tdble		deltaVert;
    float 		distance;
    bool        scaleRandom, scaleFixed;
    tdble       scale;
    tdble       scaleMin;
    tdble       scaleMax;
    const char  *filename;
} tobjdef;

GF_TAILQ_HEAD(objlist, objdef);
tobjlist objhead;

int
GetObjectsNb(void *TrackHandle)
{
    static const char *section = TRK_SECT_TERRAIN "/" TRK_SECT_OBJMAP;

    return GfParmGetEltNb(TrackHandle, section);
}

static void
ApplyTransform(sgMat4 m, ssgBase *node)
{
    if (node->isAKindOf(ssgTypeLeaf()))
    {
        ((ssgLeaf *)node)->transform(m);
    }
    else if (node->isAKindOf(ssgTypeBranch()))
    {
        ssgBranch *br = (ssgBranch *)node;

        for (int i = 0; i < br->getNumKids(); i++)
        {
            ApplyTransform(m, br->getKid(i));
        }
    }
}

static void sgMakeScaleMat4(sgMat4 dst, const SGfloat scale)
{
    sgSetVec4(dst[0], scale,   SG_ZERO, SG_ZERO, SG_ZERO);
    sgSetVec4(dst[1], SG_ZERO, scale,   SG_ZERO, SG_ZERO);
    sgSetVec4(dst[2], SG_ZERO, SG_ZERO, scale,   SG_ZERO);
    sgSetVec4(dst[3], SG_ZERO, SG_ZERO, SG_ZERO, SG_ONE);
}

static void scaleObject(ssgEntity *obj, tdble scale)
{
    sgMat4 m;
    sgMakeScaleMat4(m, scale);
    ApplyTransform(m, obj);
}

static void
InitObjects(tTrack *track, void *TrackHandle)
{
    ssgLoaderOptionsEx	options ;

    ObjUniqId = 0;

    srand((unsigned int)GfParmGetNum(TrackHandle, TRK_SECT_TERRAIN, TRK_ATT_SEED, nullptr, 1));

    ssgSetCurrentOptions ( &options ) ;

    GF_TAILQ_INIT(&objhead);

    std::string inputPath(track->filename);
    inputPath.resize(inputPath.find_last_of("/"));

    std::string modelPath(inputPath + ";" + GfDataDir() + "data/objects");
    ssgModelPath("");   // using our own search

    std::string texturePath(modelPath + ";" + GfDataDir() + "data/textures");
    ssgTexturePath(texturePath.c_str());
 
    int objnb = GfParmGetEltNb(TrackHandle, TRK_SECT_OBJECTS);
    GfParmListSeekFirst(TrackHandle, TRK_SECT_OBJECTS);

    for (int i = 0; i < objnb; i++)
    {
        objdef	*curObj = (struct objdef *)calloc(1, sizeof(struct objdef));
        curObj->color = (unsigned int)GfParmGetCurNum(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_COLOR, nullptr, 0);
        const char *objName = GfParmGetCurStr(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_OBJECT, nullptr);

        if (!objName)
        {
            GfOut("Missing %s in section %s/%s", TRK_ATT_OBJECT, TRK_SECT_OBJECTS, GfParmListGetCurEltName(TrackHandle, TRK_SECT_OBJECTS));
            exit(1);
        }

        char filename[1024];
        GetFilename(objName, modelPath.c_str(), filename, sizeof(filename));
        curObj->obj = ssgLoadAC(filename);

        if (!curObj->obj)
        {
            exit(1);
        }

        curObj->filename = strdup(filename);

        ssgFlatten(curObj->obj);

        std::string scaleType = GfParmGetCurStr(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_SCALE_TYPE, "");
        if (scaleType == "random")
        {
            curObj->scaleFixed = false;
            curObj->scaleRandom = true;
            curObj->scaleMin = GfParmGetCurNum(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_SCALE_MIN, nullptr, 0.5);
            curObj->scaleMax = GfParmGetCurNum(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_SCALE_MAX, nullptr, 2.0);
        }
        else if (scaleType == "fixed")
        {
            curObj->scaleFixed = true;
            curObj->scaleRandom = false;
            curObj->scale = GfParmGetCurNum(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_SCALE, nullptr, 1.0);
        }
        else
        {
            curObj->scaleFixed = false;
            curObj->scaleRandom = false;
        }

        if (strcmp(GfParmGetCurStr(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_ORIENTATION_TYPE, ""), "random") == 0)
        {
            curObj->deltaHeight = GfParmGetCurNum(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_DH, nullptr, 0);
            curObj->deltaVert = GfParmGetCurNum(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_DV, nullptr, 5.0);
            curObj->random = true;
        }
        else
        {
            curObj->random = false;
            sgMat4 m;
            sgMakeRotMat4(m, GfParmGetCurNum(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_ORIENTATION, "deg", 0), 0.0, 0.0);
            ApplyTransform(m, curObj->obj);
        }

        if (strcmp(GfParmGetCurStr(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_ORIENTATION_TYPE, ""), "track") == 0)
        {
            curObj->trackOriented = true;
        }
        else
        {
            curObj->trackOriented = false;
        }

        if (strcmp(GfParmGetCurStr(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_ORIENTATION_TYPE, ""), "terrain") == 0)
        {
            curObj->terrainOriented = true;
        }
        else
        {
            curObj->terrainOriented = false;
        }

        if (strcmp(GfParmGetCurStr(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_ORIENTATION_TYPE, ""), "border") == 0)
        {
            curObj->borderOriented = true;
            curObj->distance = GfParmGetCurNum(TrackHandle, TRK_SECT_OBJECTS, TRK_ATT_BORDER_DISTANCE, nullptr, 1.0);
        }
        else
        {
            curObj->borderOriented = false;
        }

        GF_TAILQ_INSERT_HEAD(&objhead, curObj, link);
        GfParmListSeekNext(TrackHandle, TRK_SECT_OBJECTS);
    }
}

/* Prune the group tree */
static void
AddToRoot(ssgRoot *Root, ssgEntity *node)
{
    if (node->isAKindOf(ssgTypeLeaf()))
    {
        Root->addKid(node);
    }
    else if (node->isAKindOf(ssgTypeBranch()))
    {
        ssgBranch *br = (ssgBranch *)node;

        for (int i = 0; i < br->getNumKids(); i++)
        {
            AddToRoot(Root, br->getKid(i));
        }
    }
}

static void
AddObject(tTrack *track, void *TrackHandle, ssgEntity *TrackRoot, ssgRoot *Root, unsigned int clr, tdble x, tdble y)
{
    for (struct objdef *curObj = GF_TAILQ_FIRST(&objhead); curObj; curObj = GF_TAILQ_NEXT(curObj, link))
    {
        if (clr == curObj->color)
        {
            sgMat4		m;
            tdble		dv=0, angle=0;
            float       z=0;
            float       xNeu, yNeu, zNeu;

            ssgEntity *obj = (ssgEntity*)curObj->obj->clone(SSG_CLONE_RECURSIVE | SSG_CLONE_GEOMETRY | SSG_CLONE_STATE);

            if (curObj->random)
            {
                /* random rotations */
                /* 		sgMakeCoordMat4 (m, 0.0, 0.0, curObj->deltaHeight * rand() / (RAND_MAX + 1.0), 0.0, 0.0, 0.0); */
                /* 		ApplyTransform (m, obj); */
                dv = curObj->deltaVert;
                angle = 360.0 * rand() / (RAND_MAX + 1.0);
            }

            if (curObj->scaleRandom)
            {
                tdble random = ((tdble)rand()) / (tdble)RAND_MAX;

                if (curObj->scaleMin > curObj->scaleMax)
                    std::swap(curObj->scaleMin, curObj->scaleMax);

                tdble scale = (random * (curObj->scaleMax - curObj->scaleMin)) + curObj->scaleMin;

                scaleObject(obj, scale);
            }
            else if (curObj->scaleFixed)
            {
                scaleObject(obj, curObj->scale);
            }

            if (curObj->terrainOriented)
            {
                /* NEW: calculate angle for terrain-aligned orientation */
                if (TrackRoot->isAKindOf(ssgTypeBranch()))
                    angle = getTerrainAngle(reinterpret_cast<ssgBranch*>(TrackRoot), x, y);
                else
                {
                    GfError("TrackRoot is not an ssgBranch\n");
                    exit(1);
                }
            }

            if (curObj->trackOriented)
            {
                /* NEW: calculate angle for track-aligned orientation */
                angle = getTrackAngle(track, TrackHandle, x, y);
            }

            if (curObj->borderOriented)
            {
                /* NEW: calculate angle for border-aligned orientation */
                angle= getBorderAngle(track, TrackHandle, x, y, curObj->distance, &xNeu, &yNeu, &zNeu);
                //noch was mit x und y machen
                x= xNeu;
                y= yNeu;
                z= zNeu;
                printf("tried to align to border: x: %g y: %g z: %g angle: %g \n", x, y, z, angle);
            }
            else
            {
                if (TrackRoot->isAKindOf(ssgTypeBranch()))
                {
                    z = getHOT(reinterpret_cast<ssgBranch*>(TrackRoot), x, y);
                    if (z == -1000000.0f)
                    {
                        printf("WARNING: failed to find elevation for object %s: x: %g y: %g z: %g\n", curObj->filename, x, y, z);
                        return;
                    }
                }
                else
                {
                    GfError("TrackRoot is not an ssgBranch\n");
                    exit(1);
                }
            }

            printf("placing object %s: x: %g y: %g z: %g \n", curObj->filename, x, y, z);
            sgMakeRotMat4(m, angle, dv / 2.0 - dv * rand() / (RAND_MAX + 1.0), dv / 2.0  - dv * rand() / (RAND_MAX + 1.0));
            ApplyTransform(m, obj);

            sgMakeTransMat4(m, x, y, z);
            ApplyTransform(m, obj);
            AddToRoot(Root, obj);

            return;
        }
    }
}

/* Code extracted from PLIB-ssg */
static const int writeTextureWithoutPath = TRUE;

struct saveTriangle
{
    int    v[3];
    sgVec2 t[3];
};

static bool
ssgSaveLeaf (ssgLeaf* vt, FILE *save_fd)
{
    int i;
    static sgVec3       *vlist;
    static saveTriangle *tlist;

    int num_verts = vt->getNumVertices();
    int num_tris  = vt->getNumTriangles();

    vlist = new sgVec3[num_verts];
    tlist = new saveTriangle[num_tris];

    for (i = 0; i < num_verts; i++)
    {
        sgCopyVec3 (vlist[i], vt->getVertex (i));
    }

    for (i = 0; i < num_tris; i++)
    {
        short vv0, vv1, vv2;

        vt->getTriangle (i, &vv0, &vv1, &vv2);

        tlist[i].v[0] = vv0;
        tlist[i].v[1] = vv1;
        tlist[i].v[2] = vv2;

        sgCopyVec2 (tlist[i].t[0], vt->getTexCoord (vv0));
        sgCopyVec2 (tlist[i].t[1], vt->getTexCoord (vv1));
        sgCopyVec2 (tlist[i].t[2], vt->getTexCoord (vv2));
    }

    fprintf (save_fd, "OBJECT poly\n");
    char buf[32];
    sprintf(buf, "obj%d", ObjUniqId++);
    fprintf (save_fd, "name \"%s\"\n", buf);

    ssgState* st = vt->getState ();

    if (st && st->isAKindOf (ssgTypeSimpleState()))
    {
        ssgSimpleState* ss = (ssgSimpleState*) vt->getState ();

        if (ss->isEnabled (GL_TEXTURE_2D))
        {
            const char* tfname = ss->getTextureFilename ();

            if ((tfname != nullptr) && (tfname[0] != 0))
            {
                if (writeTextureWithoutPath)
                {
                    char *s = strrchr ((char *)tfname, '\\');

                    if (s == nullptr)
                    {
                        s = strrchr ((char *)tfname, '/');
                    }

                    if (s == nullptr)
                    {
                        fprintf (save_fd, "texture \"%s\"\n", tfname);
                    }
                    else
                    {
                        fprintf (save_fd, "texture \"%s\"\n", ++s);
                    }
                }
                else
                {
                    fprintf (save_fd, "texture \"%s\"\n", tfname);
                }
            }
        }
    }

    fprintf (save_fd, "numvert %d\n", num_verts);

    for (i = 0; i < num_verts; i++)
    {
        fprintf (save_fd, "%g %g %g\n", vlist[i][0],vlist[i][2],-vlist[i][1]);
    }

    fprintf (save_fd, "numsurf %d\n", num_tris);

    for (i = 0; i < num_tris; i++)
    {
        fprintf (save_fd, "SURF 0x30\n");
        fprintf (save_fd, "mat 0\n");
        fprintf (save_fd, "refs 3\n");
        fprintf (save_fd, "%d %g %g\n",
                 tlist[i].v[0],tlist[i].t[0][0],tlist[i].t[0][1]);
        fprintf (save_fd, "%d %g %g\n",
                 tlist[i].v[1],tlist[i].t[1][0],tlist[i].t[1][1]);
        fprintf (save_fd, "%d %g %g\n",
                 tlist[i].v[2],tlist[i].t[2][0],tlist[i].t[2][1]);
    }

    fprintf (save_fd, "kids 0\n");

    delete[] vlist;
    delete[] tlist;

    return true;
}

static bool
ssgSaveACInner (ssgEntity *ent, FILE *save_fd)
{
    /* WARNING - RECURSIVE! */

    if (ent->isAKindOf (ssgTypeBranch()))
    {
        ssgBranch *br = (ssgBranch *) ent;
        char buf[32];
        sprintf(buf, "objg%d", ObjUniqId++);
        Ac3dGroup (save_fd, buf, ent->getNumKids());

        for (int i = 0; i < br->getNumKids (); i++)
        {
            if (! ssgSaveACInner(br->getKid (i), save_fd))
            {
                return false;
            }
        }

        return true;
    }
    else if (ent->isAKindOf(ssgTypeLeaf()))
    {
        ssgLeaf* vt = (ssgLeaf *)ent;

        return ssgSaveLeaf(vt, save_fd);
    }

    return false;
}

/* insert one leaf in group */
static void
InsertInGroup(ssgLeaf *leaf, ssgRoot* GroupRoot, std::vector<ssgBranch*>& Groups)
{
    int			grIdx;
    const float	*center;

    leaf->recalcBSphere();
    center = leaf->getBSphere()->getCenter();

    grIdx = (int)((center[0] - XGroupOffset) / GroupSize) +
            XGroupNb * (int)((center[1] - YGroupOffset) / GroupSize);

    if (Groups[grIdx] == nullptr)
    {
        Groups[grIdx] = new ssgBranch();
        GroupRoot->addKid(Groups[grIdx]);
    }

    Groups[grIdx]->addKid(leaf);
}

/* insert leaves in groups */
static void
InsertInner(ssgEntity *ent, ssgRoot* GroupRoot, std::vector<ssgBranch*>& Groups)
{
    /* WARNING - RECURSIVE! */

    if (ent->isAKindOf (ssgTypeBranch()))
    {
        ssgBranch *br = (ssgBranch *) ent;

        for (int i = 0; i < br->getNumKids (); i++)
        {
            InsertInner(br->getKid (i), GroupRoot, Groups);
        }

        return;
    }
    else if (ent->isAKindOf(ssgTypeLeaf()))
    {
        ssgLeaf* leaf = (ssgLeaf*)ent;

        InsertInGroup(leaf, GroupRoot, Groups);
    }
}


static void
Group(tTrack *track, void *TrackHandle, ssgRoot *Root, ssgRoot *GroupRoot, std::vector<ssgBranch*> &Groups)
{
    tdble	Margin;

    Margin    = GfParmGetNum(TrackHandle, TRK_SECT_TERRAIN, TRK_ATT_BMARGIN, nullptr, 100.0);
    GroupSize = GfParmGetNum(TrackHandle, TRK_SECT_TERRAIN, TRK_ATT_GRPSZ, nullptr, 100.0);
    XGroupOffset = track->min.x - Margin;
    YGroupOffset = track->min.y - Margin;

    XGroupNb = (int)((track->max.x + Margin - (track->min.x - Margin)) / GroupSize) + 1;

    int GroupNb = XGroupNb * ((int)((track->max.y + Margin - (track->min.y - Margin)) / GroupSize) + 1);

    Groups.resize(GroupNb);

    for (size_t i = 0; i < Groups.size(); ++i)
        Groups[i] = nullptr;

    InsertInner(Root, GroupRoot, Groups);
}


void
GenerateObjects(tTrack *track, void *TrackHandle, void *CfgHandle, FILE *save_fd, const std::string &meshFile, const std::string &outputFile)
{
    ssgLoaderOptionsEx	options;
    int			width, height;
    tdble		xmin, xmax, ymin, ymax;
    tdble 		Margin;
    tdble		kX, kY, dX, dY;
    unsigned int	clr;
    int			index;

    ssgSetCurrentOptions(&options);

    std::string inputPath(track->filename);
    inputPath.resize(inputPath.find_last_of("/"));

    std::string modelPath(inputPath + ";" + GfDataDir() + "data/objects");
    ssgModelPath("");   // don't need a search path because meshFile has a full path

    std::string texturePath(modelPath + ";" + GfDataDir() + "data/textures");
    ssgTexturePath(texturePath.c_str());

    ssgEntity *TrackRoot = ssgLoadAC(meshFile.c_str());

    InitObjects(track, TrackHandle);

    Margin = GfParmGetNum(TrackHandle, TRK_SECT_TERRAIN, TRK_ATT_BMARGIN, nullptr, 0);

    xmin = track->min.x - Margin;
    xmax = track->max.x + Margin;
    ymin = track->min.y - Margin;
    ymax = track->max.y + Margin;

    static const char * section = TRK_SECT_TERRAIN "/" TRK_SECT_OBJMAP;

    if (GfParmGetEltNb(TrackHandle, section) == 0)
    {
        return;
    }

    GfParmListSeekFirst(TrackHandle, section);

    index = 0;
    do
    {
        ssgRoot *Root = new ssgRoot();

        index++;
        const char *map = GfParmGetCurStr(TrackHandle, section, TRK_ATT_OBJMAP, "");

        std::string imageFile(inputPath + "/" + map);
        printf("Processing object map %s\n", imageFile.c_str());
        unsigned char *MapImage = GfTexReadImageFromPNG(imageFile.c_str(), 2.2, &width, &height, 0, 0, false);

        if (!MapImage)
        {
            return;
        }

        kX = (xmax - xmin) / width;
        dX = xmin;
        kY = (ymax - ymin) / height;
        dY = ymin;

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                clr = (MapImage[4 * (i + width * j)] << 16) + (MapImage[4 * (i + width * j) + 1] << 8) + MapImage[4 * (i + width * j) + 2];

                if (clr)
                {
                    printf("found color: 0x%X x: %d y: %d\n", clr, i, j);
                    AddObject(track, TrackHandle, TrackRoot, Root, clr, i * kX + dX, j * kY + dY);
                }
            }
        }

        free(MapImage);

        ssgRoot *GroupRoot = new ssgRoot();
        std::vector<ssgBranch*> Groups;

        Group(track, TrackHandle, Root, GroupRoot, Groups);

        const char *extName = GfParmGetStr(CfgHandle, "Files", "object", "obj");
        std::string objectFile(outputFile + "-" + extName + "-" + std::to_string(index) + ".ac");
        FILE *curFd = Ac3dOpen(objectFile, 1);
        ssgSaveACInner(GroupRoot, curFd);
        Ac3dClose(curFd);

        if (save_fd)
        {
            ssgSaveACInner(GroupRoot, save_fd);
        }

        delete Root;
        delete GroupRoot;
    } while (!GfParmListSeekNext(TrackHandle, section));

    delete TrackRoot;
}
