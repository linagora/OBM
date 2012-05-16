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
package org.obm.push.bean.autodiscover;

import com.google.common.base.Objects;

public class AutodiscoverResponseServer {

	private final String type;
	private final String url;
	private final String name;
	private final String serverData;
	
	public AutodiscoverResponseServer(String type, String url, String name,
			String serverData) {
		
		this.type = type;
		this.url = url;
		this.name = name;
		this.serverData = serverData;
	}

	/**
	 *  Indicates that the URL that is returned by the URL element
	 *  
	 * @return MobileSync or CertEnroll
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Specifies a URL string that conveys the protocol, port, resource location, and other
	 * information.
	 *
	 * @return url
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * If the Type element value is "MobileSync", then the Name element specifies the URL that conveys
	 * the protocol. If the Type element value is "CertEnroll", then the Name element value is NULL.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * The ServerData element is a string value that is present only when the Type element (section
	 * 2.2.3.159.1) value is set to "CertEnroll".
	 *
	 * @return
	 */
	public String getServerData() {
		return serverData;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(type, url, name, serverData);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof AutodiscoverResponseServer) {
			AutodiscoverResponseServer that = (AutodiscoverResponseServer) object;
			return Objects.equal(this.type, that.type)
				&& Objects.equal(this.url, that.url)
				&& Objects.equal(this.name, that.name)
				&& Objects.equal(this.serverData, that.serverData);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("type", type)
			.add("url", url)
			.add("name", name)
			.add("serverData", serverData)
			.toString();
	}
	
}
