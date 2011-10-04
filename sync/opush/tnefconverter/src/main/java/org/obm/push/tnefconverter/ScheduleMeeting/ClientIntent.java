package org.obm.push.tnefconverter.ScheduleMeeting;

public enum ClientIntent {
	ciManager, // 1
	ciDelegate,// 2
	ciDeletedWithNoResponse,// 4
	ciDeletedExceptionWithNoResponse,// 8
	ciRespondedTentative,// 10
	ciRespondedAccept,// 20
	ciRespondedDecline,// 40
	ciModifiedStartTime,// 80
	ciModifiedEndTime,// 100
	ciModifiedLocation,// 200
	ciRespondedExceptionDecline,// 400
	ciCanceled,// 800
	ciExceptionCanceled;// 1000
	
	public static ClientIntent getClientIntent(String val){
		if("1".equals(val)){
			return ciManager;
		} else if("2".equals(val)){
			return ciDelegate;
		} else if("4".equals(val)){
			return ciDeletedWithNoResponse;
		} else if("8".equals(val)){
			return ciDeletedExceptionWithNoResponse;
		} else if("10".equals(val)){
			return ciRespondedTentative;
		} else if("20".equals(val)){
			return ciRespondedAccept;
		} else if("40".equals(val)){
			return ciRespondedDecline;
		} else if("80".equals(val)){
			return ciModifiedStartTime;
		} else if("100".equals(val)){
			return ciModifiedEndTime;
		} else if("200".equals(val)){
			return ciModifiedLocation;
		} else if("400".equals(val)){
			return ciRespondedExceptionDecline;
		} else if("800".equals(val)){
			return ciCanceled;
		} else if("1000".equals(val)){
			return ciExceptionCanceled;
		} 
		return null;
	}

}
