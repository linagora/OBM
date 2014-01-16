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
package org.obm.push;

import org.joda.time.Duration;

public class DefaultPolicy implements Policy {

	@Override
	public boolean devicePasswordEnabled() {
		return false;
	}

	@Override
	public boolean alphaNumericDevicePasswordRequired() {
		return false;
	}

	@Override
	public boolean passwordRecoveryEnabled() {
		return false;
	}

	@Override
	public boolean deviceEncryptionEnabled() {
		return false;
	}

	@Override
	public boolean attachmentsEnabled() {
		return true;
	}

	@Override
	public int minDevicePasswordLength() {
		return 4;
	}

	@Override
	public Duration maxInactivityTimeDeviceLock() {
		return Duration.standardSeconds(900);
	}

	@Override
	public int maxDevicePasswordFailedAttempts() {
		return 8;
	}

	@Override
	public Integer maxAttachmentSize() {
		return null;
	}

	@Override
	public boolean allowSimpleDevicePassword() {
		return true;
	}

	@Override
	public Boolean devicePasswordExpiration() {
		return null;
	}

	@Override
	public boolean devicePasswordHistory() {
		return false;
	}

	@Override
	public boolean allowStorageCard() {
		return true;
	}

	@Override
	public boolean allowCamera() {
		return true;
	}

	@Override
	public boolean requireStorageCardEncryption() {
		return false;
	}

	@Override
	public boolean requireDeviceEncryption() {
		return false;
	}

	@Override
	public boolean allowUnsignedApplications() {
		return true;
	}

	@Override
	public boolean allowUnsignedInstallationPackages() {
		return true;
	}

	@Override
	public int minDevicePasswordComplexCharacters() {
		return 3;
	}

	@Override
	public boolean allowWiFi() {
		return true;
	}

	@Override
	public boolean allowTextMessaging() {
		return true;
	}

	@Override
	public boolean allowPOPIMAPEmail() {
		return true;
	}

	@Override
	public int allowBluetooth() {
		return 2;
	}

	@Override
	public boolean allowIrDA() {
		return true;
	}

	@Override
	public boolean requireManualSyncWhenRoaming() {
		return false;
	}

	@Override
	public boolean allowDesktopSync() {
		return true;
	}

	@Override
	public int maxCalendarAgeFilter() {
		return 0;
	}

	@Override
	public boolean allowHTMLEmail() {
		return true;
	}

	@Override
	public int maxEmailAgeFilter() {
		return 0;
	}

	@Override
	public int maxEmailBodyTruncationSize() {
		return -1;
	}

	@Override
	public int maxEmailHTMLBodyTruncationSize() {
		return -1;
	}

	@Override
	public boolean requireSignedSMIMEMessages() {
		return false;
	}

	@Override
	public boolean requireEncryptedSMIMEMessages() {
		return false;
	}

	@Override
	public boolean requireSignedSMIMEAlgorithm() {
		return false;
	}

	@Override
	public boolean requireEncryptionSMIMEAlgorithm() {
		return false;
	}

	@Override
	public int allowSMIMEEncryptionAlgorithmNegotiation() {
		return 2;
	}

	@Override
	public boolean allowSMIMESoftCerts() {
		return true;
	}

	@Override
	public boolean allowBrowser() {
		return true;
	}

	@Override
	public boolean allowConsumerEmail() {
		return true;
	}

	@Override
	public boolean allowRemoteDesktop() {
		return true;
	}

	@Override
	public boolean allowInternetSharing() {
		return true;
	}

}
