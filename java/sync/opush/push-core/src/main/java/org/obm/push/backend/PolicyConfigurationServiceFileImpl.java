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

import org.joda.time.Duration;
import org.obm.configuration.utils.IniFile;
import org.obm.push.DefaultPolicy;
import org.obm.push.Policy;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class PolicyConfigurationServiceFileImpl implements PolicyConfigurationService {

	private final IniFile iniFile;
	private final DefaultPolicy defaultPolicy;

	@Inject
	@VisibleForTesting PolicyConfigurationServiceFileImpl(IniFile.Factory iniFileFactory, @Named("opushPolicyConfigurationFile") String opushPolicyConfiguration) {
		iniFile = iniFileFactory.build(opushPolicyConfiguration);
		defaultPolicy = new DefaultPolicy();
	}
	
	@Override
	public Policy getPolicy() {
		return new Policy() {
			
			@Override
			public boolean requireStorageCardEncryption() {
				return iniFile.getBooleanValue("requireStorageCardEncryption", defaultPolicy.requireStorageCardEncryption());
			}
			
			@Override
			public boolean requireSignedSMIMEMessages() {
				return iniFile.getBooleanValue("requireSignedSMIMEMessages", defaultPolicy.requireSignedSMIMEMessages());
			}
			
			@Override
			public boolean requireSignedSMIMEAlgorithm() {
				return iniFile.getBooleanValue("requireSignedSMIMEAlgorithm", defaultPolicy.requireSignedSMIMEAlgorithm());
			}
			
			@Override
			public boolean requireManualSyncWhenRoaming() {
				return iniFile.getBooleanValue("requireManualSyncWhenRoaming", defaultPolicy.requireManualSyncWhenRoaming());
			}
			
			@Override
			public boolean requireEncryptionSMIMEAlgorithm() {
				return iniFile.getBooleanValue("requireEncryptionSMIMEAlgorithm", defaultPolicy.requireEncryptionSMIMEAlgorithm());
			}
			
			@Override
			public boolean requireEncryptedSMIMEMessages() {
				return iniFile.getBooleanValue("requireEncryptedSMIMEMessages", defaultPolicy.requireEncryptedSMIMEMessages());
			}
			
			@Override
			public boolean requireDeviceEncryption() {
				return iniFile.getBooleanValue("requireDeviceEncryption", defaultPolicy.requireDeviceEncryption());
			}
			
			@Override
			public boolean passwordRecoveryEnabled() {
				return iniFile.getBooleanValue("passwordRecoveryEnabled", defaultPolicy.passwordRecoveryEnabled());
			}
			
			@Override
			public int minDevicePasswordLength() {
				return iniFile.getIntValue("minDevicePasswordLength", defaultPolicy.minDevicePasswordLength());
			}
			
			@Override
			public int minDevicePasswordComplexCharacters() {
				return iniFile.getIntValue("minDevicePasswordComplexCharacters", defaultPolicy.minDevicePasswordComplexCharacters());
			}
			
			@Override
			public Duration maxInactivityTimeDeviceLock() {
				return 
						Duration.standardSeconds(
								iniFile.getIntValue("maxInactivityTimeDeviceLock", defaultPolicy.maxInactivityTimeDeviceLock().toStandardSeconds().getSeconds()));
			}
			
			@Override
			public int maxEmailHTMLBodyTruncationSize() {
				return iniFile.getIntValue("maxEmailHTMLBodyTruncationSize", defaultPolicy.maxEmailHTMLBodyTruncationSize());
			}
			
			@Override
			public int maxEmailBodyTruncationSize() {
				return iniFile.getIntValue("maxEmailBodyTruncationSize", defaultPolicy.maxEmailBodyTruncationSize());
			}
			
			@Override
			public int maxEmailAgeFilter() {
				return iniFile.getIntValue("maxEmailAgeFilter", defaultPolicy.maxEmailAgeFilter());
			}
			
			@Override
			public int maxDevicePasswordFailedAttempts() {
				return iniFile.getIntValue("maxDevicePasswordFailedAttempts", defaultPolicy.maxDevicePasswordFailedAttempts());
			}
			
			@Override
			public int maxCalendarAgeFilter() {
				return iniFile.getIntValue("maxCalendarAgeFilter", defaultPolicy.maxCalendarAgeFilter());
			}
			
			@Override
			public Integer maxAttachmentSize() {
				return iniFile.getIntegerValue("maxAttachmentSize", defaultPolicy.maxAttachmentSize());
			}
			
			@Override
			public boolean devicePasswordHistory() {
				return iniFile.getBooleanValue("devicePasswordHistory", defaultPolicy.devicePasswordHistory());
			}
			
			@Override
			public Boolean devicePasswordExpiration() {
				return iniFile.getNullableBooleanValue("devicePasswordExpiration", defaultPolicy.devicePasswordExpiration());
			}
			
			@Override
			public boolean devicePasswordEnabled() {
				return iniFile.getBooleanValue("devicePasswordEnabled", defaultPolicy.devicePasswordEnabled());
			}
			
			@Override
			public boolean deviceEncryptionEnabled() {
				return iniFile.getBooleanValue("deviceEncryptionEnabled", defaultPolicy.deviceEncryptionEnabled());
			}
			
			@Override
			public boolean attachmentsEnabled() {
				return iniFile.getBooleanValue("attachmentsEnabled", defaultPolicy.attachmentsEnabled());
			}
			
			@Override
			public boolean alphaNumericDevicePasswordRequired() {
				return iniFile.getBooleanValue("alphaNumericDevicePasswordRequired", defaultPolicy.alphaNumericDevicePasswordRequired());
			}
			
			@Override
			public boolean allowWiFi() {
				return iniFile.getBooleanValue("allowWiFi", defaultPolicy.allowWiFi());
			}
			
			@Override
			public boolean allowUnsignedInstallationPackages() {
				return iniFile.getBooleanValue("allowUnsignedInstallationPackages", defaultPolicy.allowUnsignedInstallationPackages());
			}
			
			@Override
			public boolean allowUnsignedApplications() {
				return iniFile.getBooleanValue("allowUnsignedApplications", defaultPolicy.allowUnsignedApplications());
			}
			
			@Override
			public boolean allowTextMessaging() {
				return iniFile.getBooleanValue("allowTextMessaging", defaultPolicy.allowTextMessaging());
			}
			
			@Override
			public boolean allowStorageCard() {
				return iniFile.getBooleanValue("allowStorageCard", defaultPolicy.allowStorageCard());
			}
			
			@Override
			public boolean allowSimpleDevicePassword() {
				return iniFile.getBooleanValue("allowSimpleDevicePassword", defaultPolicy.allowSimpleDevicePassword());
			}
			
			@Override
			public boolean allowSMIMESoftCerts() {
				return iniFile.getBooleanValue("allowSMIMESoftCerts", defaultPolicy.allowSMIMESoftCerts());
			}
			
			@Override
			public int allowSMIMEEncryptionAlgorithmNegotiation() {
				return iniFile.getIntValue("allowSMIMEEncryptionAlgorithmNegotiation", defaultPolicy.allowSMIMEEncryptionAlgorithmNegotiation());
			}
			
			@Override
			public boolean allowRemoteDesktop() {
				return iniFile.getBooleanValue("allowRemoteDesktop", defaultPolicy.allowRemoteDesktop());
			}
			
			@Override
			public boolean allowPOPIMAPEmail() {
				return iniFile.getBooleanValue("allowPOPIMAPEmail", defaultPolicy.allowPOPIMAPEmail());
			}
			
			@Override
			public boolean allowIrDA() {
				return iniFile.getBooleanValue("allowIrDA", defaultPolicy.allowIrDA());
			}
			
			@Override
			public boolean allowInternetSharing() {
				return iniFile.getBooleanValue("allowInternetSharing", defaultPolicy.allowInternetSharing());
			}
			
			@Override
			public boolean allowHTMLEmail() {
				return iniFile.getBooleanValue("allowHTMLEmail", defaultPolicy.allowHTMLEmail());
			}
			
			@Override
			public boolean allowDesktopSync() {
				return iniFile.getBooleanValue("allowDesktopSync", defaultPolicy.allowDesktopSync());
			}
			
			@Override
			public boolean allowConsumerEmail() {
				return iniFile.getBooleanValue("allowConsumerEmail", defaultPolicy.allowConsumerEmail());
			}
			
			@Override
			public boolean allowCamera() {
				return iniFile.getBooleanValue("allowCamera", defaultPolicy.allowCamera());
			}
			
			@Override
			public boolean allowBrowser() {
				return iniFile.getBooleanValue("allowBrowser", defaultPolicy.allowBrowser());
			}
			
			@Override
			public int allowBluetooth() {
				return iniFile.getIntValue("allowBluetooth", defaultPolicy.allowBluetooth());
			}
			
		};
	}
	
}
