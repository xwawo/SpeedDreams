/***************************************************************************

    file                 : easymesh.h
    created              : Sun Feb 25 22:50:07 /etc/localtime 2001
    copyright            : Bojan NICENO & Eric Espi�
    email                : niceno@univ.trieste.it Eric.Espie@torcs.org
    version              : $Id: easymesh.h 8524 2022-09-19 17:20:34Z iobyte $

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
    @version	$Id: easymesh.h 8524 2022-09-19 17:20:34Z iobyte $
*/

#ifndef _EASYMESH_H_
#define _EASYMESH_H_

void GenerateTerrain(tTrack *track, void *TrackHandle, const std::string &outfile, FILE *AllFd, int noElevation, bool useBorder);
int getPointCount();
void addPoint(double x, double y, double z, double F, int mark);
void addSegment(int n0, int n1, int mark);

#endif /* _EASYMESH_H_ */ 



