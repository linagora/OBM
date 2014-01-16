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
package org.obm.sync.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obm.sync.auth.Version.Factory;

public class ClientInformations {

	public static class Parser {
		
		private Pattern thunderbirdStringSplitter;
		private Pattern connectorVersionPattern;

		public static class ParserException extends Exception {
			public ParserException() {
				super();
			}
		}
		
		public Parser() {
			super();
			thunderbirdStringSplitter = Pattern.compile("thunderbird\\[ext: (.+), light: (.+)\\]");
			connectorVersionPattern = Pattern.compile("(\\d+)\\.(\\d+)?(\\.(\\d+))?(\\.(\\d+))?(.+)?");
		}

		public ClientInformations parse(String thunderbirdVersionAsString) {
			ClientInformations clientInformations = new ClientInformations();
			String[] parts = splitThunderbirdVersionString(thunderbirdVersionAsString);
			if (parts != null) {
				Version connectorVersion = parseConnectorVersion(parts[0]);
				LightningVersion lightningVersion = parseLightningVersion(parts[1]);
				clientInformations.setLightningVersion(lightningVersion);
				clientInformations.setObmConnectorVersion(connectorVersion);
				return clientInformations;
			}
			return null;
		}

		// thunderbird[ext: 2.4.1.8-rc11, light: 1.0b2]
		private String[] splitThunderbirdVersionString(CharSequence thunderbirdVersionAsString) {
			Matcher matcher = thunderbirdStringSplitter.matcher(thunderbirdVersionAsString);
			if (matcher.matches()) {
				return new String[] {matcher.group(1), matcher.group(2)}; 
			}
			return null;
		}
		
		private LightningVersion parseLightningVersion(String lightningVersion) {
			return parseVersion(lightningVersion, new LightningVersion.FactoryImpl());
		}

		private Version parseConnectorVersion(String connectorVersion) {
			return parseVersion(connectorVersion, new Version.FactoryImpl());
		}
		
		// 2.4.1.8-rc11
		private <T extends Version> T parseVersion(String connectorVersion, Factory<T> factory) {
			Matcher matcher = connectorVersionPattern.matcher(connectorVersion);
			if (matcher.matches()) {
				int major = Integer.valueOf(matcher.group(1));
				int minor = Integer.valueOf(matcher.group(2));
				Integer releaseAsInt = getIntegerFromString(matcher.group(4));
				Integer subReleaseAsInt = getIntegerFromString(matcher.group(6));
				String suffix = matcher.group(7);
				return factory.create(major, minor, releaseAsInt, subReleaseAsInt, suffix);
			}
			return null;
		}

		private Integer getIntegerFromString(String release) {
			return release != null ? Integer.valueOf(release) : null;
		}

	}

	private LightningVersion lightning;
	private Version obmConnector;
	
	public ClientInformations() {
		super();
	}
	
	public void setLightningVersion(LightningVersion lightning) {
		this.lightning = lightning;
	}
	
	public void setObmConnectorVersion(Version obmConnector) {
		this.obmConnector = obmConnector;
	}
	
	public LightningVersion getLightningVersion() {
		return lightning;
	}
	
	public Version getObmConnectorVersion() {
		return obmConnector;
	}
}
