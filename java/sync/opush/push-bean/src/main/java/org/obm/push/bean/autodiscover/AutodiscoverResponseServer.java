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

import com.google.common.base.Objects;

public class AutodiscoverResponseServer {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private String type;
		private String url;
		private String name;
		private String serverData;
		
		private Builder() {}
		
		public Builder type(String type) {
			this.type = type;
			return this;
		}
		
		public Builder url(String url) {
			this.url = url;
			return this;
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder serverData(String serverData) {
			this.serverData = serverData;
			return this;
		}
		
		public AutodiscoverResponseServer build() {
			return new AutodiscoverResponseServer(type, url, name, serverData);
		}
	}
	
	private final String type;
	private final String url;
	private final String name;
	private final String serverData;
	
	private AutodiscoverResponseServer(String type, String url, String name, String serverData) {
		this.type = type;
		this.url = url;
		this.name = name;
		this.serverData = serverData;
	}

	public String getType() {
		return type;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getName() {
		return name;
	}
	
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
