/***************************************************************************

    file        : snddefault.h
    copyright   : (C) 2011 by Jean-Philippe Meuret                        
    email       : pouillot@users.sourceforge.net   
    version     : $Id: snddefault.h 8380 2022-08-08 01:15:06Z beaglejoe $

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
    		The "snddefault" sound engine module
    @version    $Id: snddefault.h 8380 2022-08-08 01:15:06Z beaglejoe $
*/

#ifndef _SNDDEFAULT_H_
#define _SNDDEFAULT_H_

#include <isoundengine.h>

#include <tgf.hpp>


// DLL exported symbols declarator for Windows.
#ifdef WIN32
# ifdef SNDDEFAULT_DLL
#  define SNDDEFAULT_API __declspec(dllexport)
# else
#  define SNDDEFAULT_API __declspec(dllimport)
# endif
#else
# define SNDDEFAULT_API
#endif


// The C interface of the module.
extern "C" int SNDDEFAULT_API openGfModule(const char* pszShLibName, void* hShLibHandle);
extern "C" int SNDDEFAULT_API closeGfModule();

// The module main class
// (Singleton, inherits GfModule, and implements IGraphicsEngine and ISoundEngine).
class SNDDEFAULT_API SndDefault : public GfModule, public ISoundEngine
{
 public:

	virtual void init(Situation* s);
        virtual void shutdown();
        virtual void refresh(Situation* s, Camera* camera);
        virtual void mute(bool bOn = true);
        virtual float getVolume() const;
        virtual void setVolume(float vol);

	// Accessor to the singleton.
	static SndDefault& self();

	// Destructor.
	virtual ~SndDefault();

 protected:

	// Protected constructor to avoid instanciation outside (but friends).
	SndDefault(const std::string& strShLibName, void* hShLibHandle);
	
	// Make the C interface functions nearly member functions.
	friend int openGfModule(const char* pszShLibName, void* hShLibHandle);
	friend int closeGfModule();

 protected:

	// The singleton.
	static SndDefault* _pSelf;
};

#endif /* _SNDDEFAULT_H_ */ 
