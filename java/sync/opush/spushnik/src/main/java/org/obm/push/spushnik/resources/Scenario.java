/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.spushnik.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.obm.push.bean.DeviceId;
import org.obm.push.spushnik.bean.CheckResult;
import org.obm.push.spushnik.bean.CheckStatus;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.sync.push.client.HttpRequestException;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.WBXMLOPClient;

import com.google.common.base.Throwables;

public abstract class Scenario {
	
	private final static DeviceId DEVICE_ID = new DeviceId("spushnik");
	private final static String DEV_TYPE = "spushnikProbe";
	private final static String USER_AGENT = "spushnikAgent";
	
	@GET
	@Produces("application/json")
	public CheckResult run(
			@QueryParam("loginAtDomain") String loginAtDomain, 
			@QueryParam("password") String password, 
			@QueryParam("serviceUrl") String serviceUrl) {
		
		OPClient client = new WBXMLOPClient(loginAtDomain, password, DEVICE_ID, 
				DEV_TYPE, USER_AGENT, serviceUrl, new WBXMLTools());
		
		try {
			return scenarii(client);
		} catch (Exception e) {
			return handleException(e);
		}
	}
	
	protected abstract CheckResult scenarii(OPClient client) throws Exception;
	
	protected CheckResult buildOK() {
		return buildResult(CheckStatus.OK, null);
	}
	
	protected CheckResult buildWarning(String message) {
		return buildResult(CheckStatus.WARNING, message);
	}
	
	protected CheckResult buildError(String message) {
		return buildResult(CheckStatus.ERROR, message);
	}
	
	private CheckResult buildResult(CheckStatus checkStatus, String message) {
		return CheckResult.builder()
				.checkStatus(checkStatus)
				.message(message)
				.build(); 
	}
	
	private CheckResult handleException(Exception exception) {
		if (exception instanceof HttpRequestException) {
			return buildError("HTTP error: " + ((HttpRequestException) exception).getStatusCode());
		}
		return buildError(Throwables.getStackTraceAsString(exception));
	}
	
}
