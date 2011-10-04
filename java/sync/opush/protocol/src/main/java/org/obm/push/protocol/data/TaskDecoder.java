package org.obm.push.protocol.data;

import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSTask;
import org.obm.push.bean.Recurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.tnefconverter.RTFUtils;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class TaskDecoder extends Decoder implements IDataDecoder {

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
		Recurrence recurrence = null;
		Element containerNode = DOMUtils.getUniqueElement(syncData, "Recurrence");
		if (containerNode != null) {
			
			recurrence = new Recurrence();

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
