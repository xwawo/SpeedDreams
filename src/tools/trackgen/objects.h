/***************************************************************************

    file                 : objects.h
    created              : Fri May 24 20:10:22 CEST 2002
    copyright            : (C) 2001 by Eric Espi�
    email                : Eric.Espie@torcs.org
    version              : $Id: objects.h 8113 2022-04-24 16:56:27Z iobyte $

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
    @version	$Id: objects.h 8113 2022-04-24 16:56:27Z iobyte $
*/

#ifndef _OBJECTS_H_
#define _OBJECTS_H_

extern void GenerateObjects(tTrack *track, void *TrackHandle, void *CfgHandle, FILE *save_fd, const std::string &meshFile, const std::string &outputFile);
extern int GetObjectsNb(void *TrackHandle);

#endif /* _OBJECTS_H_ */ 



