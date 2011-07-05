package org.obm.push.tnefconverter.ScheduleMeeting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import net.freeutils.tnef.Attr;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.Message;
import net.freeutils.tnef.RawInputStream;

import org.obm.push.tnefconverter.MAPIExtendedProp;
import org.obm.push.tnefconverter.RTFUtils;

public class ScheduleMeeting {

	private Message tnefMsg;
	private GlobalObjectId gloObjId;

	public ScheduleMeeting(Message tnefMsg) throws IOException {
		this.tnefMsg = tnefMsg;
		RawInputStream in = TNEFExtractorUtils.getMAPIPropInputStream(
				this.tnefMsg, MAPIExtendedProp.PR_PID_LID_GLOBAL_OBJECT_ID);
		int recurTime = TNEFExtractorUtils.getMAPIPropInt(
				this.tnefMsg, MAPIExtendedProp.PR_PID_LID_START_RECURRENCE_TIME);
		if (in != null) {
			this.gloObjId = new GlobalObjectId(new ByteArrayInputStream(in
					.toByteArray()), recurTime);
		}
	}

	public PidTagMessageClass getMethod() {
		return PidTagMessageClass.getPidTagMessageClass(TNEFExtractorUtils
				.getAttrString(this.tnefMsg, Attr.attMessageClass));
	}

	public String getUID() {
		if (gloObjId != null) {
			return gloObjId.getUid();
		}
		return null;
	}

	public String getDescription() {
		InputStream t = TNEFExtractorUtils.getMAPIPropInputStream(this.tnefMsg,
				MAPIProp.PR_RTF_COMPRESSED);
		if (t == null) {
			return "";
		}
		return RTFUtils.extractCompressedRTF(t);
	}

	public Integer getClazz() {
		String sensitivity = TNEFExtractorUtils.getMAPIPropString(this.tnefMsg,
				MAPIProp.PR_SENSITIVITY);
		try {
			return Integer.valueOf(sensitivity);
		} catch (Throwable e) {
			return 0;
		}
	}

	public Date getDTStamp() {
		return TNEFExtractorUtils.getMAPIPropDate(this.tnefMsg,
				MAPIExtendedProp.PR_PID_LID_OWNER_CRITICAL_CHANGE);
	}

	public Date getStartDate() {
		return TNEFExtractorUtils.getMAPIPropDate(this.tnefMsg,
				MAPIProp.PR_START_DATE);
	}

	public Date getEndDate() {
		return TNEFExtractorUtils.getMAPIPropDate(this.tnefMsg,
				MAPIProp.PR_END_DATE);
	}

	public Boolean getResponseRequested() {
		return TNEFExtractorUtils.getMAPIPropBoolean(this.tnefMsg,
				MAPIProp.PR_RESPONSE_REQUESTED);
	}

	public String getLocation() {
		return TNEFExtractorUtils.getMAPIPropString(this.tnefMsg,
				MAPIExtendedProp.PR_PID_LID_WHERE);
	}

	public Boolean isAllDay() {
		return TNEFExtractorUtils.getMAPIPropBoolean(this.tnefMsg,
				MAPIExtendedProp.PR_PID_LID_APPOINTMENT_SUB_TYPE);
	}

	public Boolean isRecurring() {
		return TNEFExtractorUtils.getMAPIPropBoolean(this.tnefMsg,
				MAPIExtendedProp.PR_PID_LID_IS_RECURRING);
	}

	public Boolean isException() {
		return TNEFExtractorUtils.getMAPIPropBoolean(this.tnefMsg,
				MAPIExtendedProp.PR_PID_LID_IS_EXCEPTION);
	}

	public OldRecurrenceType getOldRecurrenceType() {
		return OldRecurrenceType.getRecurrenceType(TNEFExtractorUtils
				.getMAPIPropString(this.tnefMsg,
						MAPIExtendedProp.PR_PID_LID_OLD_RECURRENCE_TYPE));
	}

	public Integer getInterval() {
		OldRecurrenceType recur = getOldRecurrenceType();
		String frequency = "";
		if (OldRecurrenceType.MONTHLY.equals(recur)
				|| OldRecurrenceType.MONTHLY_NDAY.equals(recur)) {
			frequency = TNEFExtractorUtils.getMAPIPropString(this.tnefMsg,
					MAPIExtendedProp.PR_PID_LID_MONTH_INTERVAL);
		} else if (OldRecurrenceType.WEEKLY.equals(recur)) {
			frequency = TNEFExtractorUtils.getMAPIPropString(this.tnefMsg,
					MAPIExtendedProp.PR_PID_LID_WEEK_INTERVAL);
		} else if (OldRecurrenceType.YEARLY.equals(recur)
				|| OldRecurrenceType.YEARLY_NDAY.equals(recur)) {
			frequency = TNEFExtractorUtils.getMAPIPropString(this.tnefMsg,
					MAPIExtendedProp.PR_PID_LID_YEAR_INTERVAL);
		}

		try {
			return Integer.valueOf(frequency);
		} catch (Throwable e) {
			return 0;
		}
	}

	public ClientIntent getClientIntent() {
		return ClientIntent.getClientIntent(TNEFExtractorUtils
				.getMAPIPropString(this.tnefMsg,
						MAPIExtendedProp.PR_PID_LID_CLIENT_INTENT));
	}

	public Date getRecurrenceId() {
		if (isException() && this.gloObjId != null) {
			return this.gloObjId.getRecurrenceId();
		}
		return null;
	}

	@Override
	public String toString() {
		return this.tnefMsg.toString();
	}

}
