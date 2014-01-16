/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.protocol.bean;

import org.obm.push.bean.MeetingResponseStatus;

import com.google.common.base.Objects;

public class ItemChangeMeetingResponse {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private String calId;
		private String reqId;
		private MeetingResponseStatus status;
		
		private Builder() {}
		
		public Builder calId(String calId) {
			this.calId = calId;
			return this;
		}
		
		public Builder reqId(String reqId) {
			this.reqId = reqId;
			return this;
		}
		
		public Builder status(MeetingResponseStatus status) {
			this.status = status;
			return this;
		}
		
		public ItemChangeMeetingResponse build() {
			return new ItemChangeMeetingResponse(calId, reqId, status);
		}
	}
	
	private final String calId;
	private final String reqId;
	private final MeetingResponseStatus status;
	
	private ItemChangeMeetingResponse(String calId, String reqId, MeetingResponseStatus status) {
		this.calId = calId;
		this.reqId = reqId;
		this.status = status;
	}
	
	public String getCalId() {
		return calId;
	}
	
	public String getReqId() {
		return reqId;
	}

	public MeetingResponseStatus getStatus() {
		return status;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(calId, reqId, status);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof ItemChangeMeetingResponse) {
			ItemChangeMeetingResponse that = (ItemChangeMeetingResponse) object;
			return Objects.equal(this.calId, that.calId)
					&& Objects.equal(this.reqId, that.reqId)
					&& Objects.equal(this.status, that.status);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("calId", calId)
			.add("reqId", reqId)
			.add("status", status)
			.toString();
	}
}
