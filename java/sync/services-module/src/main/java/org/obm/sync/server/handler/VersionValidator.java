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
package org.obm.sync.server.handler;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ClientInformations;
import org.obm.sync.auth.LightningVersion;
import org.obm.sync.auth.OBMConnectorVersionException;
import org.obm.sync.auth.Version;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class VersionValidator {

	private final Version obmConnectorRequiredVersion;
	private final int linagoraVersion; 
	private final ClientInformations.Parser parser;

	@Inject
	protected VersionValidator(ClientInformations.Parser parser) {
		this.obmConnectorRequiredVersion = new Version(2, 4, 1, 8);
		this.linagoraVersion = 2;
		this.parser = parser;
	}

	public void checkObmConnectorVersion(AccessToken token)
			throws OBMConnectorVersionException {
		String origin = token.getOrigin();
		if (origin == null) {
			//don't filter if origin is null
			return;
		}
		ClientInformations actualVersion = parser.parse(origin);
		if (actualVersion == null) {
			//don't filter if origin is not parsable
			return;
		}
		Version connectorVersion = actualVersion.getObmConnectorVersion();
		if (connectorVersion == null || connectorVersion.compareTo(obmConnectorRequiredVersion) < 0) {
			throw new OBMConnectorVersionException(token, obmConnectorRequiredVersion);
		}
		LightningVersion lightningVersion = actualVersion.getLightningVersion();
		if (lightningVersion == null) {
			throw new OBMConnectorVersionException(token, obmConnectorRequiredVersion);
		}
		Integer linagoraVersion = lightningVersion.getLinagoraVersion();
		if (linagoraVersion == null || linagoraVersion < this.linagoraVersion) {
			throw new OBMConnectorVersionException(token, obmConnectorRequiredVersion);
		}
	}

}
