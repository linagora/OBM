package org.obm.push.bean;

import com.google.common.base.Objects;


public class MeetingResponse {
	
	private AttendeeStatus userResponse;
	private Integer collectionId;
	private String reqId;
	private String longId;
	
	public MeetingResponse() {
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

	@Override
	public final int hashCode(){
		return Objects.hashCode(userResponse, collectionId, reqId, longId);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MeetingResponse) {
			MeetingResponse that = (MeetingResponse) object;
			return Objects.equal(this.userResponse, that.userResponse)
				&& Objects.equal(this.collectionId, that.collectionId)
				&& Objects.equal(this.reqId, that.reqId)
				&& Objects.equal(this.longId, that.longId);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("userResponse", userResponse)
			.add("collectionId", collectionId)
			.add("reqId", reqId)
			.add("longId", longId)
			.toString();
	}
	
}
