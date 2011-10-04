/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.server.handler;

import org.obm.sync.calendar.EventType;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.calendar.CalendarBindingImpl;
import fr.aliacom.obm.common.session.SessionManagement;

@Singleton
public class CalendarHandler extends EventHandler {

	@Inject
	private CalendarHandler(SessionManagement sessionManagement, CalendarBindingImpl calendarBindingImpl) {
		super(sessionManagement, calendarBindingImpl);
		calendarBindingImpl.setEventType(EventType.VEVENT);
	}

}
