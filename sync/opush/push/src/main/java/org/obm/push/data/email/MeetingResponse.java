package org.obm.push.data.email;

import org.obm.push.data.calendarenum.AttendeeStatus;

public class MeetingResponse {
	
	private AttendeeStatus userResponse;
	private Integer collectionId;
	private String reqId;
	private String longId;
	
	public MeetingResponse(){
		
	}

	public AttendeeStatus getUserResponse() {
		return userResponse;
	}

	public void setUserResponse(AttendeeStatus userResponse) {
		this.userResponse = userResponse;
	}

	public Integer getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(Integer collectionId) {
		this.collectionId = collectionId;
	}

	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

	public String getLongId() {
		return longId;
	}

	public void setLongId(String longId) {
		this.longId = longId;
	}
}
