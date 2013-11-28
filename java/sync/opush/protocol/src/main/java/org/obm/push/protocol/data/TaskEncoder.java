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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.MSTask;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

public class TaskEncoder extends Encoder {
	
	private final SimpleDateFormat sdf;
	
	@Inject
	private TaskEncoder() {
		super();
		//2010-07-08T22:00:00.000Z
		this.sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");
		this.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	public void encode(Element p, IApplicationData data) {
		MSTask ta = (MSTask) data;

		s(p, "Tasks:Subject", ta.getSubject());
		if (ta.getImportance() != null) {
			s(p, "Tasks:Importance", ta.getImportance().toString());
		}
		s(p, "Tasks:UTCStartDate", ta.getUtcStartDate(),sdf);
		s(p, "Tasks:StartDate", ta.getStartDate(),sdf);
		s(p, "Tasks:UTCDueDate", ta.getUtcDueDate(),sdf);
		s(p, "Tasks:DueDate", ta.getDueDate(),sdf);
//		DOMUtils.createElement(p, "Tasks:Categories");
		if (ta.getRecurrence() != null) {
			encodeRecurrence(p, ta);
		}
		s(p, "Tasks:Complete", ta.getComplete());
		s(p, "Tasks:DateCompleted", ta.getDateCompleted(),sdf);
		s(p, "Tasks:Sensitivity", ta.getSensitivity().asIntString());
		if(ta.getReminderSet()!= null && ta.getReminderSet()){
			s(p, "Tasks:ReminderTime", ta.getReminderTime(),sdf);
			s(p, "Tasks:ReminderSet", ta.getReminderSet());
		}
		encodeBody(p, ta);
	}

	public Element encodedApplicationData(IApplicationData data) {
		Document doc = DOMUtils.createDoc(null, null);
		Element root = doc.getDocumentElement();
		encode(root, data);
		return root;
	}

	private void encodeRecurrence(Element p, MSTask task) {
		MSRecurrence rec = task.getRecurrence();
		Element r = DOMUtils.createElement(p, "Tasks:Recurrence");

		s(r, "Tasks:RecurrenceType", rec.getType().asIntString());
		s(r, "Tasks:RecurrenceStart", rec.getStart(),sdf);
		s(r, "Tasks:RecurrenceRegenerate", rec.getRegenerate());
		s(r, "Tasks:RecurrenceDeadOccur", rec.getDeadOccur());
		s(r, "Tasks:RecurrenceInterval", rec.getInterval());
		s(r, "Tasks:RecurrenceUntil", rec.getUntil(),sdf);

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(task.getStartDate().getTime());
		switch (rec.getType()) {
		case DAILY:
			break;
		case MONTHLY:
			DOMUtils.createElementAndText(r, "Tasks:RecurrenceDayOfMonth", ""
					+ cal.get(Calendar.DAY_OF_MONTH));
			break;
		case MONTHLY_NDAY:
			DOMUtils.createElementAndText(r, "Tasks:RecurrenceWeekOfMonth", ""
					+ cal.get(Calendar.WEEK_OF_MONTH));
			DOMUtils.createElementAndText(r, "Tasks:RecurrenceDayOfWeek", ""
					+ RecurrenceDayOfWeek.dayOfWeekToInt(cal
							.get(Calendar.DAY_OF_WEEK)));
			break;
		case WEEKLY:
			DOMUtils.createElementAndText(r, "Tasks:RecurrenceDayOfWeek", ""
					+ RecurrenceDayOfWeek.asInt(rec.getDayOfWeek()));
			break;
		case YEARLY:
			DOMUtils.createElementAndText(r, "Tasks:RecurrenceDayOfMonth", ""
					+ cal.get(Calendar.DAY_OF_MONTH));
			DOMUtils.createElementAndText(r, "Tasks:RecurrenceMonthOfYear", ""
					+ (cal.get(Calendar.MONTH) + 1));
			break;
		case YEARLY_NDAY:
			break;
		}
	}

	private void encodeBody(Element p, MSTask task) {
		String body = "";
		if (task.getDescription() != null) {
			body = task.getDescription().trim();
		}
		Element d = DOMUtils.createElement(p, "AirSyncBase:Body");
		s(d, "AirSyncBase:Type", Type.PLAIN_TEXT.toString());
		s(d, "AirSyncBase:EstimatedDataSize", "" + body.length());
		if (body.length() > 0) {
			DOMUtils.createElementAndText(d, "AirSyncBase:Data", body);
		}
	}
}
