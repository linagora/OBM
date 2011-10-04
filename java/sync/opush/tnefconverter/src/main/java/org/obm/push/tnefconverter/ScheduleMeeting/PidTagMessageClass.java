package org.obm.push.tnefconverter.ScheduleMeeting;

public enum PidTagMessageClass {
	ScheduleMeetingRequest {
		@Override
		public String toString() {
			return "IPM.Microsoft Schedule.MtgReq";
		}
	},
	ScheduleMeetingCanceled {
		@Override
		public String toString() {
			return "IPM.Microsoft Schedule.MtgCncl";
		} 
	},
	ScheduleMeetingRespPos {
		@Override
		public String toString() {
			return "IPM.Microsoft Schedule.MtgRespP";
		}
	},
	ScheduleMeetingRespTent {
		@Override
		public String toString() {
			return "IPM.Microsoft Schedule.MtgRespA";
		}
	},
	ScheduleMeetingRespNeg {
		@Override
		public String toString() {
			return "IPM.Microsoft Schedule.MtgRespN";
		}
	};

	public abstract String toString();
	
	public static PidTagMessageClass getPidTagMessageClass(String val) {
		if ("IPM.Microsoft Schedule.MtgReq".equals(val)) {
			return ScheduleMeetingRequest;
		} else if ("IPM.Microsoft Schedule.MtgCncl".equals(val)) {
			return ScheduleMeetingCanceled;
		} else if ("IPM.Microsoft Schedule.MtgRespP".equals(val)) {
			return ScheduleMeetingRespPos;
		} else if ("IPM.Microsoft Schedule.MtgRespA".equals(val)) {
			return ScheduleMeetingRespTent;
		} else if ("IPM.Microsoft Schedule.MtgRespN".equals(val)) {
			return ScheduleMeetingRespNeg;
		} else {
			return null;
		}
	}
}
