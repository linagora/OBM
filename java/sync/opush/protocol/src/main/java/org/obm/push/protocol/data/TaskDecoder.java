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

import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.MSTask;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.tnefconverter.RTFUtils;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

import com.google.inject.Inject;

public class TaskDecoder extends Decoder implements IDataDecoder {

	@Inject
	public TaskDecoder(Base64ASTimeZoneDecoder base64AsTimeZoneDecoder, ASTimeZoneConverter asTimeZoneConverter) {
		super(base64AsTimeZoneDecoder, asTimeZoneConverter);
	}
	
	@Override
	public IApplicationData decode(Element syncData) {
		MSTask task = new MSTask();

		task.setSubject(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"Subject")));
		appendDescription(syncData, task);
		
		task.setImportance(parseDOMInt(DOMUtils.getUniqueElement(syncData,
				"Importance")));
		task.setUtcStartDate(parseDOMDate(DOMUtils.getUniqueElement(syncData,
				"UTCStartDate")));
		task.setStartDate(parseDOMDate(DOMUtils.getUniqueElement(syncData,
				"StartDate")));
		task.setUtcDueDate(parseDOMDate(DOMUtils.getUniqueElement(syncData,
				"UTCDueDate")));
		task.setDueDate(parseDOMDate(DOMUtils.getUniqueElement(syncData,
				"DueDate")));
		task.setCategories(parseDOMStringCollection(DOMUtils.getUniqueElement(
				syncData, "Categories"), "Category"));
		appendRecurrence(syncData, task);
		task.setComplete(parseDOMInt2Boolean(DOMUtils.getUniqueElement(
				syncData, "Complete")));
		task.setDateCompleted(parseDOMDate(DOMUtils.getUniqueElement(syncData,
				"DateCompleted")));
		task.setSensitivity(getCalendarSensitivity(syncData));
		task.setReminderTime(parseDOMDate(DOMUtils.getUniqueElement(syncData,
				"ReminderTime")));
		task.setReminderSet(parseDOMInt2Boolean(DOMUtils.getUniqueElement(
				syncData, "ReminderSet")));

		logger.info("Decode task to "+task);
		return task;
	}

	private void appendDescription(Element syncData, MSTask task) {
		// description
		Element body = DOMUtils.getUniqueElement(syncData, "Body");
		if (body != null) {
			Element data = DOMUtils.getUniqueElement(body, "Data");
			if (data != null) {
				Type bodyType = Type.fromInt(Integer.parseInt(DOMUtils
						.getUniqueElement(body, "Type").getTextContent()));
				String txt = data.getTextContent();
				if (bodyType == Type.PLAIN_TEXT) {
					task.setDescription(data.getTextContent());
				} else if (bodyType == Type.RTF) {
					task.setDescription(RTFUtils.extractB64CompressedRTF(txt));
				} else {
					logger.warn("Unsupported body type: " + bodyType + "\n"
							+ txt);
				}
			}
		}
		Element rtf = DOMUtils.getUniqueElement(syncData, "Compressed_RTF");
		if (rtf != null) {
			String txt = rtf.getTextContent();
			task.setDescription(RTFUtils.extractB64CompressedRTF(txt));
		}
	}

	private void appendRecurrence(Element syncData, MSTask task) {
		MSRecurrence recurrence = null;
		Element containerNode = DOMUtils.getUniqueElement(syncData, "Recurrence");
		if (containerNode != null) {
			
			recurrence = new MSRecurrence();

			recurrence.setStart(parseDOMDate(DOMUtils.getUniqueElement(syncData,
				"Start")));
			recurrence.setRegenerate(parseDOMInt2Boolean(DOMUtils.getUniqueElement(
				syncData, "Regenerate")));
			recurrence.setDeadOccur(parseDOMInt2Boolean(DOMUtils.getUniqueElement(
				syncData, "DeadOccur")));
			
			recurrence.setUntil(parseDOMDate(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceUntil")));
			recurrence.setWeekOfMonth(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceWeekOfMonth")));
			recurrence.setMonthOfYear(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceMonthOfYear")));
			recurrence.setDayOfMonth(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceDayOfMonth")));
			recurrence.setOccurrences(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceOccurrences")));
			recurrence.setInterval(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceInterval")));
			Integer i = parseDOMInt(DOMUtils.getUniqueElement(containerNode,
					"RecurrenceDayOfWeek"));
			if (i != null) {
				recurrence.setDayOfWeek(RecurrenceDayOfWeek.fromInt(i));
			}

			switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(containerNode,
					"RecurrenceType"))) {
			case 0:
				recurrence.setType(RecurrenceType.DAILY);
				break;
			case 1:
				recurrence.setType(RecurrenceType.WEEKLY);
				break;
			case 2:
				recurrence.setType(RecurrenceType.MONTHLY);
				break;
			case 3:
				recurrence.setType(RecurrenceType.MONTHLY_NDAY);
				break;
			case 5:
				recurrence.setType(RecurrenceType.YEARLY);
				break;
			case 6:
				recurrence.setType(RecurrenceType.YEARLY_NDAY);
				break;
			}

			if (recurrence.getType() != null) {
				logger.info("parse type: " + recurrence.getType());
			}
		}
		task.setRecurrence(recurrence);
	}

	private CalendarSensitivity getCalendarSensitivity(Element domSource) {
		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource,
				"Sensitivity"))) {
		case 0:
			return CalendarSensitivity.NORMAL;
		case 1:
			return CalendarSensitivity.PERSONAL;
		case 2:
			return CalendarSensitivity.PRIVATE;
		case 3:
			return CalendarSensitivity.CONFIDENTIAL;
		}
		return null;
	}
}
