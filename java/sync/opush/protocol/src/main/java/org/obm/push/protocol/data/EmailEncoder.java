package org.obm.push.protocol.data;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.Recurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.SyncCollection;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;

public class EmailEncoder implements IDataEncoder {

	private static final Logger logger = LoggerFactory
			.getLogger(EmailEncoder.class);
	
	private SimpleDateFormat sdf;
	private HTMLBodyFormatter htmlFormatter;
	private PlainBodyFormatter plainFormatter;

	private final IntEncoder intEncoder;

	@Inject
	/* package */ EmailEncoder(IntEncoder intEncoder) {
		this.intEncoder = intEncoder;
		sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		htmlFormatter = new HTMLBodyFormatter();
		plainFormatter = new PlainBodyFormatter();
	}

	@Override
	public void encode(BackendSession bs, Element parent,
			IApplicationData data, SyncCollection c, boolean isResponse) {
		MSEmail mail = (MSEmail) data;

		DOMUtils.createElementAndText(parent, "Email:To",
				buildStringAdresses(mail.getTo()));
		if (mail.getCc().size() > 0) {
			DOMUtils.createElementAndText(parent, "Email:CC",
					buildStringAdresses(mail.getCc()));
		}
		if (mail.getFrom() != null) {
			DOMUtils.createElementAndText(parent, "Email:From",
					formatEmail(mail.getFrom()));
		} else {
			DOMUtils.createElementAndText(parent, "Email:From", "");
		}
		DOMUtils.createElementAndText(parent, "Email:Subject",
				mail.getSubject());

		if (mail.getDate() != null) {
			DOMUtils.createElementAndText(parent, "Email:DateReceived",
					sdf.format(mail.getDate()));
		} else {
			logger.warn("date received is null for mail " + mail.getUid()
					+ " s: " + mail.getSubject());
		}

		if (mail.getTo().size() > 0 && mail.getTo().get(0) != null
				&& mail.getTo().get(0).getDisplayName() != null
				&& !"".equals(mail.getTo().get(0).getDisplayName())) {
			DOMUtils.createElementAndText(parent, "Email:DisplayTo", mail
					.getTo().get(0).getDisplayName());
		}
		// DOMUtils.createElementAndText(parent, "Email:ThreadTopic", mail
		// .getSubject());
		DOMUtils.createElementAndText(parent, "Email:Importance", mail
				.getImportance().asIntString());
		DOMUtils.createElementAndText(parent, "Email:Read", mail.isRead() ? "1"
				: "0");

		if (bs.getProtocolVersion().compareTo(new BigDecimal("2.5")) == 0) {
			appendBody25(parent, mail, c);
		} else {
			appendBody(parent, mail, c);
		}

		if (bs.getProtocolVersion().compareTo(new BigDecimal("2.5")) == 0) {
			appendAttachments25(parent, mail.getAttachements());
		} else {
			appendAttachments(parent, mail.getAttachements());
		}

		DOMUtils.createElementAndText(parent, "Email:MessageClass", mail
				.getMessageClass().toString());
		if (mail.getInvitation() != null) {
			appendMeetintRequest(parent, mail);
		}

		DOMUtils.createElementAndText(parent, "Email:InternetCPID", "65001");
	}

	private void appendBody25(Element parent, MSEmail mail, SyncCollection c) {
		if (c.getOptions().getTruncation() != null && c.getOptions().getTruncation().equals(1)) {
			DOMUtils.createElementAndText(parent, "Email:BodyTruncated", "1");
		} else {
			MSEmailBodyType availableFormat = getAvailableFormat(c, mail);
			String mailBody = getBodyData(mail, availableFormat);
			if (MSEmailBodyType.MIME.equals(availableFormat)) {
				DOMUtils.createElementAndText(parent, "Email:MIMETruncated",
						"0");
				DOMUtils.createElementAndText(parent, "Email:MIME", mailBody);
			} else {
				DOMUtils.createElementAndText(parent, "Email:BodyTruncated",
						"0");
				if (mailBody != null && mailBody.length() > 0) {
					DOMUtils.createElementAndText(parent, "Email:Body",
							mailBody);
				}
			}
		}
	}

	protected void appendBody(Element parent, MSEmail mail, SyncCollection c) {
		Element elemBody = DOMUtils.createElement(parent, "AirSyncBase:Body");
		MSEmailBodyType availableFormat = getAvailableFormat(c, mail);
		String data = getBodyData(mail, availableFormat);

		DOMUtils.createElementAndText(elemBody, "AirSyncBase:Type",
				availableFormat.asIntString());
		if (data != null) {
			DOMUtils.createElementAndText(elemBody,
					"AirSyncBase:EstimatedDataSize", ""
							+ data.getBytes().length);

			if (c.getOptions().getBodyPreference(availableFormat) != null
					&& c.getOptions().getBodyPreference(availableFormat).getTruncationSize() != null) {
				if (c.getOptions().getBodyPreference(availableFormat).getTruncationSize() < data
						.length()) {
					data = data.substring(0, c.getOptions().getBodyPreference(availableFormat)
							.getTruncationSize());
					DOMUtils.createElementAndText(elemBody,
							"AirSyncBase:Truncated", "1");
				}
			}

		}

		if (data != null && data.length() > 0) {
			DOMUtils.createElementAndText(elemBody, "AirSyncBase:Data", data);
		}

		if (mail.getInvitation() != null) {
			DOMUtils.createElementAndText(parent, "Email:ContentClass",
					"urn:content-classes:calendarmessage");
		} else {
			DOMUtils.createElementAndText(parent, "Email:ContentClass",
					"urn:content-classes:message");
		}

		DOMUtils.createElementAndText(parent, "AirSyncBase:NativeBodyType",
				availableFormat.asIntString());

	}

	private MSEmailBodyType getAvailableFormat(SyncCollection c, MSEmail mail) {
		if (c.getOptions().getMimeSupport() != null && c.getOptions().getMimeSupport().equals(2)) {
			return MSEmailBodyType.MIME;
		} else if (c.getOptions().getBodyPreferences().size() > 1) {
			if (c.getOptions().getBodyPreference(MSEmailBodyType.HTML) != null
					&& mail.getBody().getValue(MSEmailBodyType.HTML) != null) {
				return MSEmailBodyType.HTML;
			} else if (c.getOptions().getBodyPreference(MSEmailBodyType.PlainText) != null
					&& mail.getBody().getValue(MSEmailBodyType.PlainText) != null) {
				return MSEmailBodyType.PlainText;
			} else if (c.getOptions().getBodyPreference(MSEmailBodyType.MIME) != null
					&& mail.getBody().getValue(MSEmailBodyType.MIME) != null) {
				return MSEmailBodyType.MIME;
			}
		} else if (c.getOptions().getBodyPreferences().size() == 1) {
			BodyPreference pref = c.getOptions().getBodyPreferences().values().iterator()
					.next();
			if (pref != null && pref.getType() != null) {
				return pref.getType();
			}
		}
		return MSEmailBodyType.PlainText;
	}

	private String getBodyData(MSEmail mail,
			MSEmailBodyType type) {
		switch (type) {
		case PlainText:
			String body = mail.getBody().getValue(MSEmailBodyType.PlainText);
			if (body != null && body.length() != 0) {
				return body.trim();
			} else {
				body = mail.getBody().getValue(MSEmailBodyType.HTML);
				if (body != null && body.length() != 0) {
					return plainFormatter.convert(body).trim();
				}
			}
			break;
		case HTML:
			body = mail.getBody().getValue(MSEmailBodyType.HTML);
			if (body != null && body.length() != 0) {
				return body.trim();
			} else {
				body = mail.getBody().getValue(MSEmailBodyType.PlainText);
				if (body != null && body.length() != 0) {
					return htmlFormatter.convert(body).trim();
				}
			}
			break;
		case RTF:
			return mail.getBody().getValue(MSEmailBodyType.RTF);
		case MIME:
			try {
				return FileUtils.streamString(mail.getMimeData(), false);
			} catch (Exception e) {
				return "";
			}
		}
		return "";
	}

	private void appendMeetintRequest(Element parent, MSEmail mail) {
		if (mail.getInvitation() != null) {
			Element mr = DOMUtils.createElement(parent, "Email:MeetingRequest");

			MSEvent invi = mail.getInvitation();
			DOMUtils.createElementAndText(mr, "Email:AllDayEvent",
					invi.getAllDayEvent() ? "1" : "0");
			DOMUtils.createElementAndText(mr, "Email:StartTime",
					formatDate(invi.getStartTime()));
			DOMUtils.createElementAndText(mr, "Email:DTStamp",
					formatDate(invi.getDtStamp()));
			DOMUtils.createElementAndText(mr, "Email:EndTime",
					formatDate(invi.getEndTime()));

			DOMUtils.createElementAndText(mr, "Email:InstanceType", "0");

			if (invi.getLocation() != null && !"".equals(invi.getLocation())) {
				DOMUtils.createElementAndText(mr, "Email:Location",
						invi.getLocation());
			}
			if (invi.getOrganizerEmail() != null
					&& !"".equals(invi.getOrganizerEmail())) {
				DOMUtils.createElementAndText(mr, "Email:Organizer",
						invi.getOrganizerEmail());
			} else if (invi.getOrganizerName() != null
					&& !"".equals(invi.getOrganizerName())) {
				DOMUtils.createElementAndText(mr, "Email:Organizer",
						invi.getOrganizerName());
			}
			if (invi.getReminder() != null) {
				DOMUtils.createElementAndText(mr, "Email:Reminder", invi
						.getReminder().toString());
			}

			DOMUtils.createElementAndText(mr, "Email:ResponseRequested", "0");

			if (invi.getSensitivity() != null) {
				DOMUtils.createElementAndText(mr, "Email:Sensitivity", invi
						.getSensitivity().asIntString());
			}

			if (invi.getBusyStatus() != null) {
				DOMUtils.createElementAndText(mr, "Email:IntDBusyStatus", invi
						.getBusyStatus().asIntString());
			}

			Element tz = DOMUtils.createElement(mr, "Email:TimeZone");
			// taken from exchange 2k7 : eastern greenland, gmt+0, no dst
			tz.setTextContent("xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==");
			MSEventUid eventUid = invi.getUid();
			if (eventUid != null) {
				DOMUtils.createElementAndText(mr, "Email:GlobalObjId",
						msEventUidToGlobalObjId(eventUid));
			} else {
				throw new InvalidParameterException("a MSEvent must have an UID");
			}
			appendRecurence(mr, invi);
		}
	}

	/* package */ String msEventUidToGlobalObjId(MSEventUid msEventUid) {
		String eventUidAsString = msEventUid.serializeToString();
		byte[] eventUidAsBytes = eventUidAsString.getBytes(Charsets.US_ASCII);
		byte[] preambule = buildByteSequence(
				0x4, 0x0, 0x0, 0x0, 0x82, 0x0, 0xE0, 0, 0x74, 0xC5, 0xB7, 0x10, 0x1A, 0x82, 0xE0, 0x08,
				0x0, 0x0, 0x0, 0x0,
				0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0);
		
		byte[] marker = "vCal-Uid".getBytes(Charsets.US_ASCII);
		byte[] markerEnd = buildByteSequence(0x1, 0x0, 0x0, 0x0);
		byte[] dataEnd = buildByteSequence(0x0);
		byte[] length = intEncoder.toByteArray(
				marker.length + markerEnd.length + eventUidAsBytes.length + dataEnd.length);
		byte[] result = Bytes.concat(preambule, length, marker, markerEnd, eventUidAsBytes, dataEnd);
		return Base64.encodeBase64String(result);
	}
	
	/* package*/ byte[] buildByteSequence(int... bytes) {
		byte[] byteArray = new byte[bytes.length];
		int i = 0;
		for (int b: bytes) {
			byteArray[i++] = (byte) b;
		}
		return byteArray;
	}
	
	private void appendRecurence(Element parent, MSEvent invi) {
		Recurrence recur = invi.getRecurrence();
		if (recur != null) {
			Element ers = DOMUtils.createElement(parent, "Email:Recurrences");
			Element r = DOMUtils.createElement(ers, "Email:Recurrence");
			if (recur.getInterval() != null) {
				DOMUtils.createElementAndText(r, "Email:Recurrence_Interval",
						recur.getInterval().toString());
			}
			if (recur.getUntil() != null) {
				DOMUtils.createElementAndText(r, "Email:Recurrence_Until",
						formatDate(recur.getUntil()));
			}
			if (recur.getOccurrences() != null) {
				DOMUtils.createElementAndText(r,
						"Email:Recurrence_Occurrences", recur.getOccurrences()
								.toString());
			}

			DOMUtils.createElementAndText(r, "Email:Recurrence_Type", recur
					.getType().asIntString());
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.setTimeInMillis(invi.getStartTime().getTime());
			switch (recur.getType()) {
			case DAILY:
				break;
			case MONTHLY:
				DOMUtils.createElementAndText(r, "Email:Recurrence_DayOfMonth",
						"" + cal.get(Calendar.DAY_OF_MONTH));
				break;
			case MONTHLY_NDAY:
				DOMUtils.createElementAndText(r,
						"Email:Recurrence_WeekOfMonth",
						"" + cal.get(Calendar.WEEK_OF_MONTH));
				DOMUtils.createElementAndText(
						r,
						"Email:Recurrence_DayOfWeek",
						""
								+ RecurrenceDayOfWeek.dayOfWeekToInt(cal
										.get(Calendar.DAY_OF_WEEK)));
				break;
			case WEEKLY:
				DOMUtils.createElementAndText(r, "Email:Recurrence_DayOfWeek",
						"" + RecurrenceDayOfWeek.asInt(recur.getDayOfWeek()));
				break;
			case YEARLY:
				DOMUtils.createElementAndText(r, "Email:Recurrence_DayOfMonth",
						"" + cal.get(Calendar.DAY_OF_MONTH));
				DOMUtils.createElementAndText(r,
						"Email:Recurrence_MonthOfYear",
						"" + (cal.get(Calendar.MONTH) + 1));
				break;
			case YEARLY_NDAY:
				break;

			}
		}
	}

	private void appendAttachments(Element parent, Set<MSAttachement> attachments) {
		if (attachments.size() > 0) {
			Element atts = DOMUtils.createElement(parent,
					"AirSyncBase:Attachments");

			for (MSAttachement msAtt : attachments) {
				Element att = DOMUtils.createElement(atts, "AirSyncBase:Attachment");
				DOMUtils.createElementAndText(att, "AirSyncBase:DisplayName", msAtt.getDisplayName());
				DOMUtils.createElementAndText(att, "AirSyncBase:FileReference", msAtt.getFileReference());
				DOMUtils.createElementAndText(att, "AirSyncBase:Method", msAtt.getMethod().asIntString());
				DOMUtils.createElementAndText(att, 
						"AirSyncBase:EstimatedDataSize", msAtt.getEstimatedDataSize().toString());
			}
		}
	}

	private void appendAttachments25(Element parent, Set<MSAttachement> attachments) {
		if (attachments.size() > 0) {
			Element atts = DOMUtils.createElement(parent, "Email:Attachments");

			for (MSAttachement msAtt : attachments) {
				Element att = DOMUtils.createElement(atts, "Email:Attachment");
				DOMUtils.createElementAndText(att, "Email:DisplayName", msAtt.getDisplayName());
				DOMUtils.createElementAndText(att, "Email:AttName", msAtt.getFileReference());
				DOMUtils.createElementAndText(att, "Email:AttMethod", msAtt.getMethod().asIntString());
				DOMUtils.createElementAndText(att, "Email:AttSize", msAtt.getEstimatedDataSize().toString());
			}
		}
	}

	private String formatDate(Date d) {
		return sdf.format(d);
	}

	private String buildStringAdresses(List<MSAddress> addresses) {
		StringBuilder sb = new StringBuilder();
		for (Iterator<MSAddress> it = addresses.iterator(); it.hasNext();) {
			MSAddress addr = it.next();
			sb.append(formatEmail(addr));
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	private String formatEmail(MSAddress addr) {
		StringBuilder sb = new StringBuilder();
		if (addr.getDisplayName() != null && addr.getDisplayName().length() > 0) {
			sb.append("\"");
			sb.append(addr.getDisplayName());
			sb.append("\"");
		}
		if (addr.getMail() != null && addr.getMail().length() > 0) {
			sb.append(" <");
			sb.append(addr.getMail());
			sb.append("> ");
		}
		return sb.toString();
	}

}
