/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.icalendar;

import java.util.Collection;
import java.util.List;

import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;

import org.obm.sync.calendar.Event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ICSParsingResults {

	private List<Event> parsedEvents;
	private Collection<Reject<VEvent>> rejectedEvents;
	private Collection<Reject<VToDo>> rejectedTodos;

	public ICSParsingResults() {
		this.parsedEvents = ImmutableList.of();
		this.rejectedEvents = ImmutableList.of();
		this.rejectedTodos = ImmutableList.of();
	}

	public ICSParsingResults(ParsingResults<Event, VEvent> eventParsingResults,
			ParsingResults<Event, VToDo> todoParsingResults) {
		this.parsedEvents = ImmutableList.copyOf(Iterables.concat(
				eventParsingResults.getParsedItems(),
				todoParsingResults.getParsedItems()));
		this.rejectedEvents = eventParsingResults.getRejectedItems();
		this.rejectedTodos = todoParsingResults.getRejectedItems();
	}

	public List<Event> getParsedEvents() {
		return parsedEvents;
	}

	public Collection<Reject<VEvent>> getRejectedEvents() {
		return rejectedEvents;
	}

	public Collection<Reject<VToDo>> getRejectedTodos() {
		return rejectedTodos;
	}
}
