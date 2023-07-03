/***************************************************************************

    file                 : relief.h
    created              : Sat Jun  1 18:03:02 CEST 2002
    copyright            : (C) 2001 by Eric Espi�
    email                : Eric.Espie@torcs.org
    version              : $Id: relief.h 8532 2022-09-20 02:45:58Z iobyte $

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
    @version	$Id: relief.h 8532 2022-09-20 02:45:58Z iobyte $
*/

#ifndef _RELIEF_H_
#define _RELIEF_H_

extern void LoadRelief(tTrack *track, void * TrackHandle, const std::string &reliefFile);
extern void CountRelief(bool interior, int *nb_vert, int *nb_seg);
extern void GenRelief(bool interior);

#endif /* _RELIEF_H_ */ 



