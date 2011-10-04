package org.obm.push.protocol.bean;

import java.util.List;

import org.obm.push.bean.MeetingResponse;

public class MeetingHandlerRequest {

	private final List<MeetingResponse> meetingResponses;

	public MeetingHandlerRequest(List<MeetingResponse> meetingResponses) {
		this.meetingResponses = meetingResponses;
	}
	
	public List<MeetingResponse> getMeetingResponses() {
		return meetingResponses;
	}
	
}
