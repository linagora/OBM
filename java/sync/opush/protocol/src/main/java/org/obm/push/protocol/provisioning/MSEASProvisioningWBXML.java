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
package org.obm.push.protocol.provisioning;

import org.obm.push.ProtocolVersion;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;

/**
 * Policy type for protocol 12.x (windows mobile 6, iPhone, etc)
 */

public class MSEASProvisioningWBXML extends Policy {

	private final ProtocolVersion protocolVersion;

	public MSEASProvisioningWBXML(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	private void p(Element provDoc, String field, String value) {
		DOMUtils.createElementAndText(provDoc, field, value);
	}

	@VisibleForTesting ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}
	
	@Override
	public void serialize(Element data) {

		Element provDoc = DOMUtils.createElement(data, "EASProvisionDoc");

		p(provDoc, "DevicePasswordEnabled", "0");
		p(provDoc, "AlphanumericDevicePasswordRequired", "0");

		p(provDoc, "PasswordRecoveryEnabled", "0");
		p(provDoc, "DeviceEncryptionEnabled", "0");
		p(provDoc, "AttachmentsEnabled", "1");
		p(provDoc, "MinDevicePasswordLength", "4");

		p(provDoc, "MaxInactivityTimeDeviceLock", "900");
		p(provDoc, "MaxDevicePasswordFailedAttempts", "8");
		DOMUtils.createElement(provDoc, "MaxAttachmentSize");

		p(provDoc, "AllowSimpleDevicePassword", "1");
		DOMUtils.createElement(provDoc, "DevicePasswordExpiration");
		p(provDoc, "DevicePasswordHistory", "0");

		if (protocolVersion.compareTo(ProtocolVersion.V120) > 0) {
			p(provDoc, "AllowStorageCard", "1");
			p(provDoc, "AllowCamera", "1");
			p(provDoc, "RequireDeviceEncryption", "0");
			p(provDoc, "AllowUnsignedApplications", "1");
			p(provDoc, "AllowUnsignedInstallationPackages", "1");

			p(provDoc, "MinDevicePasswordComplexCharacters", "3");
			p(provDoc, "AllowWiFi", "1");
			p(provDoc, "AllowTextMessaging", "1");
			p(provDoc, "AllowPOPIMAPEmail", "1");
			p(provDoc, "AllowBluetooth", "2");
			p(provDoc, "AllowIrDA", "1");
			p(provDoc, "RequireManualSyncWhenRoaming", "0");
			p(provDoc, "AllowDesktopSync", "1");
			p(provDoc, "MaxCalendarAgeFilter", "0");
			p(provDoc, "AllowHTMLEmail", "1");
			p(provDoc, "MaxEmailAgeFilter", "0");
			p(provDoc, "MaxEmailBodyTruncationSize", "-1");
			p(provDoc, "MaxEmailHTMLBodyTruncationSize", "-1");

			p(provDoc, "RequireSignedSMIMEMessages", "0");
			p(provDoc, "RequireEncryptedSMIMEMessages", "0");
			p(provDoc, "RequireSignedSMIMEAlgorithm", "0");
			p(provDoc, "RequireEncryptionSMIMEAlgorithm", "0");
			p(provDoc, "AllowSMIMEEncryptionAlgorithmNegotiation", "2");
			p(provDoc, "AllowSMIMESoftCerts", "1");
			p(provDoc, "AllowBrowser", "1");
			p(provDoc, "AllowConsumerEmail", "1");

			p(provDoc, "AllowRemoteDesktop", "1");
			p(provDoc, "AllowInternetSharing", "1");
			DOMUtils.createElement(provDoc, "UnapprovedInROMApplicationList");
			DOMUtils.createElement(provDoc, "ApprovedApplicationList");
		}
	}

}
