/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.items;

import java.util.Date;
import java.util.List;

import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;

public class EventChanges {

	private DeletedEvent[] deletedEvents;
	private Event[] updatedEvents;
	private ParticipationChanges[] participationUpdated;
	private Date lastSync;

	public EventChanges() {
		deletedEvents = new DeletedEvent[0];
		updatedEvents = new Event[0];
		participationUpdated = new ParticipationChanges[0];
	}

	public DeletedEvent[] getDeletedEvents() {
		return deletedEvents;
	}

	public void setDeletedEvents(List<DeletedEvent> deleted) {
		this.deletedEvents = deleted.toArray(new DeletedEvent[0]);
	}

	public Event[] getUpdated() {
		return updatedEvents;
	}

	public void setUpdated(Event[] updated) {
		this.updatedEvents = updated;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	public ParticipationChanges[] getParticipationUpdated() {
		return participationUpdated;
	}
	
	public void setParticipationUpdated(ParticipationChanges[] participationUpdated) {
		this.participationUpdated = participationUpdated;
	}
	
}
