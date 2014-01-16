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
package org.obm.push.bean.autodiscover;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class AutodiscoverResponse {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private String responseCulture;
		private AutodiscoverResponseUser responseUser;
		private String actionRedirect;
		private List<AutodiscoverResponseServer> listActionServer;
		private AutodiscoverResponseError actionError;
		private AutodiscoverResponseError responseError;
		
		private Builder() {
			this.listActionServer = Lists.newArrayList();
		}
		
		public Builder responseCulture(String responseCulture) {
			this.responseCulture = responseCulture;
			return this;
		}
		
		public Builder responseUser(AutodiscoverResponseUser responseUser) {
			this.responseUser = responseUser;
			return this;
		}
		
		public Builder actionRedirect(String actionRedirect) {
			this.actionRedirect = actionRedirect;
			return this;
		}
		
		public Builder listActionServer(List<AutodiscoverResponseServer> listActionServer) {
			this.listActionServer = listActionServer;
			return this;
		}
		
		public Builder add(AutodiscoverResponseServer autodiscoverResponseServer) {
			this.listActionServer.add(autodiscoverResponseServer);
			return this;
		}
		
		public Builder actionError(AutodiscoverResponseError actionError) {
			this.actionError = actionError;
			return this;
		}
		
		public Builder responseError(AutodiscoverResponseError responseError) {
			this.responseError = responseError;
			return this;
		}
		
		public AutodiscoverResponse build() {
			return new AutodiscoverResponse(responseCulture, responseUser, actionRedirect, listActionServer, actionError, responseError);
		}
	}
	
	private final String responseCulture;
	private final AutodiscoverResponseUser responseUser;
	private final String actionRedirect;
	private final List<AutodiscoverResponseServer> listActionServer;
	private final AutodiscoverResponseError actionError;
	private final AutodiscoverResponseError responseError;
	
	private AutodiscoverResponse(String responseCulture, AutodiscoverResponseUser responseUser, String actionRedirect,
			List<AutodiscoverResponseServer> listActionServer, AutodiscoverResponseError actionError, 
			AutodiscoverResponseError responseError) {
		
		this.responseCulture = responseCulture;
		this.responseUser = responseUser;
		this.actionRedirect = actionRedirect;
		this.listActionServer = listActionServer;
		this.actionError = actionError;
		this.responseError = responseError;
	}
	
	public String getResponseCulture() {
		return responseCulture;
	}
	
	public AutodiscoverResponseUser getResponseUser() {
		return responseUser;
	}
	
	public String getActionRedirect() {
		return actionRedirect;
	}
	
	public List<AutodiscoverResponseServer> getListActionServer() {
		return listActionServer;
	}
	
	public AutodiscoverResponseError getActionError() {
		return actionError;
	}
	
	public AutodiscoverResponseError getResponseError() {
		return responseError;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(responseCulture, responseUser, actionRedirect, listActionServer, actionError, responseError);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof AutodiscoverResponse) {
			AutodiscoverResponse that = (AutodiscoverResponse) object;
			return Objects.equal(this.responseCulture, that.responseCulture)
				&& Objects.equal(this.responseUser, that.responseUser)
				&& Objects.equal(this.actionRedirect, that.actionRedirect)
				&& Objects.equal(this.listActionServer, that.listActionServer)
				&& Objects.equal(this.actionError, that.actionError)
				&& Objects.equal(this.responseError, that.responseError);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("responseCulture", responseCulture)
			.add("responseUser", responseUser)
			.add("actionRedirect", actionRedirect)
			.add("actionServer", listActionServer)
			.add("actionError", actionError)
			.add("responseError", responseError)
			.toString();
	}
	
}
