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
package org.obm.push.bean;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

public class UserDataRequest {

	@Singleton
	public static class Factory {
		public UserDataRequest createUserDataRequest(Credentials credentials, String command, Device device) {
			return new UserDataRequest(credentials, command, device);
		}
	}
	
	private final Credentials credentials;
	private final Device device;
	private final String command;
	private final Map<String, Resource> resources;

	public UserDataRequest(Credentials credentials, String command, Device device) {
		super();
		this.credentials = credentials;
		this.command = command;
		this.device = device;
		this.resources = Maps.newHashMap();
	}
	
	public boolean checkHint(String key, boolean defaultValue) {
		return device.checkHint(key, defaultValue);
	}

	public User getUser() {
		return credentials.getUser();
	}

	public String getPassword() {
		return credentials.getPassword();
	}

	public DeviceId getDevId() {
		return device.getDevId();
	}

	public String getDevType() {
		return device.getDevType();
	}

	public String getCommand() {
		return command;
	}

	public Credentials getCredentials() {
		return credentials;
	}
	
	public Device getDevice(){
		return device;
	}

	public void putResource(String key, Resource resource) {
		if (key != null && resource != null) {
			this.resources.put(key, resource);
		}
	}
	
	public void putAllResources(Map<String, Resource> resources) {
		if (resources != null) {
			this.resources.putAll(resources);
		}
	}
	
	public Resource getResource(String key) {
		if (null != key) {
			return resources.get(key);
		}
		return null;
	}
	
	public Map<String, Resource> getResources() {
		return resources;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(credentials, device, command);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof UserDataRequest) {
			UserDataRequest that = (UserDataRequest) object;
			return Objects.equal(this.credentials, that.credentials)
				&& Objects.equal(this.device, that.device)
				&& Objects.equal(this.command, that.command);
		}
		return false;
	}

	@Override
	public final String toString() {
		return Objects.toStringHelper(this)
			.add("credentials", credentials)
			.add("device", device)
			.add("command", command)
			.add("resources", resources)
			.toString();
	}
}
