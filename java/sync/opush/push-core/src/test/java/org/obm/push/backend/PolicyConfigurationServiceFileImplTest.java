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
package org.obm.push.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.Duration;
import org.junit.Test;
import org.obm.configuration.utils.IniFile.Factory;
import org.obm.push.Policy;


public class PolicyConfigurationServiceFileImplTest {

	@Test
	public void testDefaultConfiguration() {
		String configurationPath = getClass().getResource("configuration.properties").getFile();
		
		Policy policy = new PolicyConfigurationServiceFileImpl(new Factory(){}, configurationPath).getPolicy();
		
		assertThat(policy.passwordRecoveryEnabled()).isTrue();
		assertThat(policy.devicePasswordEnabled()).isTrue();
		assertThat(policy.alphaNumericDevicePasswordRequired()).isTrue();
		assertThat(policy.passwordRecoveryEnabled()).isTrue();
		assertThat(policy.deviceEncryptionEnabled()).isFalse();
		assertThat(policy.attachmentsEnabled()).isTrue();
		assertThat(policy.minDevicePasswordLength()).isEqualTo(2);
		assertThat(policy.maxInactivityTimeDeviceLock()).isEqualTo(Duration.standardSeconds(999));
		assertThat(policy.maxDevicePasswordFailedAttempts()).isEqualTo(1);
		assertThat(policy.maxAttachmentSize()).isEqualTo(12);
		assertThat(policy.allowSimpleDevicePassword()).isFalse();
		assertThat(policy.devicePasswordExpiration()).isTrue();
		assertThat(policy.devicePasswordHistory()).isTrue();
		assertThat(policy.allowStorageCard()).isFalse();
		assertThat(policy.allowCamera()).isFalse();
		assertThat(policy.requireStorageCardEncryption()).isTrue();
		assertThat(policy.requireDeviceEncryption()).isTrue();
		assertThat(policy.allowUnsignedApplications()).isFalse();
		assertThat(policy.allowUnsignedInstallationPackages()).isFalse();
		assertThat(policy.minDevicePasswordComplexCharacters()).isEqualTo(5);
		assertThat(policy.allowWiFi()).isFalse();
		assertThat(policy.allowTextMessaging()).isFalse();
		assertThat(policy.allowPOPIMAPEmail()).isFalse();
		assertThat(policy.allowBluetooth()).isEqualTo(1);
		assertThat(policy.allowIrDA()).isFalse();
		assertThat(policy.requireManualSyncWhenRoaming()).isTrue();
		assertThat(policy.allowDesktopSync()).isFalse();
		assertThat(policy.maxCalendarAgeFilter()).isEqualTo(1);
		assertThat(policy.allowHTMLEmail()).isFalse();
		assertThat(policy.maxEmailAgeFilter()).isEqualTo(12);
		assertThat(policy.maxEmailBodyTruncationSize()).isEqualTo(5);
		assertThat(policy.maxEmailHTMLBodyTruncationSize()).isEqualTo(33);
		assertThat(policy.requireSignedSMIMEMessages()).isTrue();
		assertThat(policy.requireEncryptedSMIMEMessages()).isTrue();
		assertThat(policy.requireSignedSMIMEAlgorithm()).isTrue();
		assertThat(policy.requireEncryptionSMIMEAlgorithm()).isTrue();
		assertThat(policy.allowSMIMEEncryptionAlgorithmNegotiation()).isEqualTo(1);
		assertThat(policy.allowSMIMESoftCerts()).isFalse();
		assertThat(policy.allowBrowser()).isFalse();
		assertThat(policy.allowConsumerEmail()).isFalse();
		assertThat(policy.allowRemoteDesktop()).isFalse();
		assertThat(policy.allowInternetSharing()).isFalse();
	}
	
}
