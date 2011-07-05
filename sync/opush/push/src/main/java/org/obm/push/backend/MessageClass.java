package org.obm.push.backend;

public enum MessageClass {
	Note {
		@Override
		public String toString() {
			return "IPM.Note";
		}
	}, NoteRulesOofTemplateMicrosoft {
		@Override
		public String toString() {
			return "IPM.Note.Rules.OofTemplate.Microsoft";
		}
	}, NoteSMIME {
		@Override
		public String toString() {
			return "IPM.Note.SMIME";
		}
	}, NoteSMIMEMultipartSigned {
		@Override
		public String toString() {
			return "IPM.Note.SMIME.MultipartSigned";
		}
	}, ScheduleMeetingRequest {
		@Override
		public String toString() {
			return "IPM.Schedule.Meeting.Request";
		}
	}, ScheduleMeetingCanceled {
		@Override
		public String toString() {
			return "IPM.Schedule.Meeting.Canceled";
		}
	}, ScheduleMeetingRespPos {
		@Override
		public String toString() {
			return "IPM.Schedule.Meeting.Resp.Pos";
		}
	}, ScheduleMeetingRespTent {
		@Override
		public String toString() {
			return "IPM.Schedule.Meeting.Resp.Tent";
		}
	}, ScheduleMeetingRespNeg {
		@Override
		public String toString() {
			return "IPM.Schedule.Meeting.Resp.Neg";
		}
	}, Post {
		@Override
		public String toString() {
			return "IPM.Post";
		}
	};

	public abstract String toString();

}
