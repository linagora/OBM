/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.protocol.data;

import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.PIMDataType;
import org.obm.push.protocol.data.ms.MSEmailDecoder;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class DecoderFactory {

	private final Provider<CalendarDecoder> calendarProvider;
	private final Provider<ContactDecoder> contactProvider;
	private final Provider<TaskDecoder> taskProvider;
	private final Provider<MSEmailDecoder> emailProvider;

	@Inject
	protected DecoderFactory(Provider<CalendarDecoder> calendarProvider,
			Provider<ContactDecoder> contactProvider,
			Provider<TaskDecoder> taskProvider,
			Provider<MSEmailDecoder> emailProvider) {
		super();
		this.calendarProvider = calendarProvider;
		this.contactProvider = contactProvider;
		this.taskProvider = taskProvider;
		this.emailProvider = emailProvider;
	}
	
	public IApplicationData decode(Element data, PIMDataType dataType) {
		
		if (data == null || dataType == null) {
			return null;
		}
		IApplicationData decodedData = null;
		switch (dataType) {
		case CALENDAR:
			decodedData = calendarProvider.get().decode(data);
			break;
		case CONTACTS:
			decodedData = contactProvider.get().decode(data);
			break;
		case TASKS:
			decodedData = taskProvider.get().decode(data);
			break;
		case EMAIL:
			decodedData = emailProvider.get().decode(data);
			break;
		default:
		}
		return decodedData;
	}

}
