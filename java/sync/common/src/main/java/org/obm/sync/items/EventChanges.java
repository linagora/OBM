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
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;

public class EventChanges {

	private EventObmId[] removed;
	private EventExtId[] removedExtIds;
	private Event[] updated;
	private ParticipationChanges[] participationUpdated;
	private Date lastSync;

	public EventChanges() {
		removed = new EventObmId[0];
		updated = new Event[0];
		participationUpdated = new ParticipationChanges[0];
		removedExtIds = new EventExtId[0];
	}
	
	public EventObmId[] getRemoved() {
		return removed;
	}

	public void setDeletions(List<DeletedEvent> deletions) {
		this.removed = new EventObmId[deletions.size()];
		this.removedExtIds = new EventExtId[deletions.size()];
		int i = 0;
		for (DeletedEvent de : deletions) {
			removed[i] = de.getId();
			removedExtIds[i] = de.getExtId();
			i++;
		}
	}

	public Event[] getUpdated() {
		return updated;
	}

	public void setUpdated(Event[] updated) {
		this.updated = updated;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	public EventExtId[] getRemovedExtIds() {
		return removedExtIds;
	}

	public ParticipationChanges[] getParticipationUpdated() {
		return participationUpdated;
	}
	
	public void setParticipationUpdated(ParticipationChanges[] participationUpdated) {
		this.participationUpdated = participationUpdated;
	}
	
}
