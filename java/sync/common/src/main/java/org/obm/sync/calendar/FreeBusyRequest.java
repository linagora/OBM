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
package org.obm.sync.calendar;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * BEGIN:VCALENDAR PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN
 * VERSION:2.0 METHOD:REQUEST BEGIN:VFREEBUSY DTSTAMP:20091029T082350Z
 * ORGANIZER:mailto:adrien@test.tlse.lng DTSTART:20091011T220000Z
 * DTEND:20091027T230000Z UID:11b66915-d01c-4562-b35d-fe222770a95c
 * ATTENDEE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL:
 * mailto:adrien@test.tlse.lng END:VFREEBUSY END:VCALENDAR
 * 
 * @author adrienp
 * 
 */
public class FreeBusyRequest {

	private String uid;
	private String owner;
	private Date start;
	private Date end;
	private List<Attendee> attendees;

	public FreeBusyRequest() {
		attendees = new LinkedList<Attendee>();

	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public List<Attendee> getAttendees() {
		return attendees;
	}

	public void addAttendee(Attendee att) {
		this.attendees.add(att);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
