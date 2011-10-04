package org.obm.push.protocol.bean;

import java.util.List;

import org.obm.push.bean.MeetingResponseStatus;

public class MeetingHandlerResponse {

	public static class ItemChangeMeetingResponse {
		private String calId;
		private String reqId;
		private MeetingResponseStatus status;
		
		public ItemChangeMeetingResponse() {
		}
		
		public String getCalId() {
			return calId;
		}
		
		public void setCalId(String calId) {
			this.calId = calId;
		}

		public void setReqId(String reqId) {
			this.reqId = reqId;
		}
		
		public String getReqId() {
			return reqId;
		}

		public void setStatus(MeetingResponseStatus status) {
			this.status = status;
		}
		
		public MeetingResponseStatus getStatus() {
			return status;
		}
		
	}
	
	private List<ItemChangeMeetingResponse> itemChanges;
	
	public MeetingHandlerResponse(List<ItemChangeMeetingResponse> itemChanges) {
		this.itemChanges = itemChanges;
	}

	public List<ItemChangeMeetingResponse> getItemChanges() {
		return itemChanges;
	}

	public void setItemChangeMeetingResponse(List<ItemChangeMeetingResponse> itemChanges) {
		this.itemChanges = itemChanges;
	}
	
}
