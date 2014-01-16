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
package org.obm.push.protocol.request;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.obm.push.bean.DeviceId;


public class SimpleQueryString extends AbstractActiveSyncRequest {

	public SimpleQueryString(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getParameter(String key) {
		return request.getParameter(key);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return request.getInputStream();
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {
		return request;
	}

	@Override
	public DeviceId getDeviceId() {
		return new DeviceId(p("DeviceId"));
	}

	@Override
	public String getDeviceType() {
		String deviceType = p("DeviceType");
		if (deviceType.startsWith("IMEI")) {
			return p("User-Agent");
		}
		return deviceType;
	}
	
	@Override
	public String getCommand() {
		return p("Cmd");
	}
	
	@Override
	public String getMsPolicyKey() {
		return request.getHeader("X-Ms-PolicyKey");
	}
	
	@Override
	public String getMSASProtocolVersion() {
		return request.getHeader("MS-ASProtocolVersion");
	}
}
