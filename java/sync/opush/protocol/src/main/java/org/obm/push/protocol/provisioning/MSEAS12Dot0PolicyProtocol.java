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

import org.obm.push.Policy;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class MSEAS12Dot0PolicyProtocol implements PolicyProtocol {

	@Override
	public void appendPolicy(Element data, Policy policy) {
		Element provDoc = DOMUtils.createElement(data, "EASProvisionDoc");

		DOMUtils.createElementAndText(provDoc, "DevicePasswordEnabled", policy.devicePasswordEnabled());
		DOMUtils.createElementAndText(provDoc, "AlphanumericDevicePasswordRequired", policy.alphaNumericDevicePasswordRequired());
		DOMUtils.createElementAndText(provDoc, "PasswordRecoveryEnabled", policy.passwordRecoveryEnabled());
		DOMUtils.createElementAndText(provDoc, "DeviceEncryptionEnabled", policy.deviceEncryptionEnabled());
		DOMUtils.createElementAndText(provDoc, "AttachmentsEnabled", policy.attachmentsEnabled());
		DOMUtils.createElementAndText(provDoc, "MinDevicePasswordLength", policy.minDevicePasswordLength());
		DOMUtils.createElementAndText(provDoc, "MaxInactivityTimeDeviceLock", policy.maxInactivityTimeDeviceLock().toStandardSeconds().getSeconds());
		DOMUtils.createElementAndText(provDoc, "MaxDevicePasswordFailedAttempts", policy.maxDevicePasswordFailedAttempts());
		if (policy.maxAttachmentSize() != null) {
			DOMUtils.createElementAndText(provDoc, "MaxAttachmentSize", policy.maxAttachmentSize()); 
		} else {
			DOMUtils.createElement(provDoc, "MaxAttachmentSize");
		}

		DOMUtils.createElementAndText(provDoc, "AllowSimpleDevicePassword", policy.allowSimpleDevicePassword());
		if (policy.devicePasswordExpiration() != null) {
			DOMUtils.createElementAndText(provDoc, "DevicePasswordExpiration", policy.devicePasswordExpiration());
		} else {
			DOMUtils.createElement(provDoc, "DevicePasswordExpiration");
		}
		DOMUtils.createElementAndText(provDoc, "DevicePasswordHistory", policy.devicePasswordHistory());
	}
	
}
