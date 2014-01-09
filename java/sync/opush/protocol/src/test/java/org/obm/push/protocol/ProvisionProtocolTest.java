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
package org.obm.push.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.parsers.FactoryConfigurationError;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.ProvisionStatus;
import org.obm.push.exception.InvalidPolicyKeyException;
import org.obm.push.protocol.bean.ProvisionRequest;
import org.obm.push.protocol.bean.ProvisionResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ProvisionProtocolTest {
	
	private static final long _3378841480L = 3378841480L;
	private static final String MS_EAS_PROVISIONING_WBXML = "MS-EAS-Provisioning-WBXML";
	private ProvisionProtocol provisionProtocol;
	
	@Before
	public void init() {
		provisionProtocol = new ProvisionProtocol.Factory().createProtocol(ProtocolVersion.V121);
	}
	
	@Test
	public void parseRequest() throws InvalidPolicyKeyException {
		Document document = buildRequestDocument(String.valueOf(_3378841480L));
		ProvisionRequest request = provisionProtocol.decodeRequest(document);
		assertThat(request.getPolicyKey()).isEqualTo(_3378841480L);
	}

	@Test(expected=InvalidPolicyKeyException.class)
	public void parseRequestWithWrongPolicyKey() throws InvalidPolicyKeyException {
		provisionProtocol.decodeRequest(buildRequestDocument("3378841480ZZD"));
	}

	public void parseRequestWithoutPolicyKey() throws InvalidPolicyKeyException {
		ProvisionRequest request = provisionProtocol.decodeRequest( buildRequestDocumentWithoutPolicyKey() );
		assertThat(request.getPolicyType()).isEqualTo(MS_EAS_PROVISIONING_WBXML);
		assertThat(request.getPolicyKey()).isNull();
	}
	
	private Document buildRequestDocumentWithoutPolicyKey() throws FactoryConfigurationError {
		Document document = DOMUtils.createDoc(null, "Provision");
		Element root = document.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", ProvisionStatus.SUCCESS.getSpecificationValue());
		Element policies = DOMUtils.createElement(root, "Policies");
		Element policyNode = DOMUtils.createElement(policies, "Policy");
		DOMUtils.createElementAndText(policyNode, "PolicyType", MS_EAS_PROVISIONING_WBXML);
		DOMUtils.createElementAndText(policyNode, "Status", ProvisionStatus.SUCCESS.getSpecificationValue());
		return document;
	}	

	
	private Document buildRequestDocument(String policyKey) throws FactoryConfigurationError {
		Document document = DOMUtils.createDoc(null, "Provision");
		Element root = document.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", ProvisionStatus.SUCCESS.getSpecificationValue());
		Element policies = DOMUtils.createElement(root, "Policies");
		Element policyNode = DOMUtils.createElement(policies, "Policy");
		DOMUtils.createElementAndText(policyNode, "PolicyType", MS_EAS_PROVISIONING_WBXML);
		DOMUtils.createElementAndText(policyNode, "Status", ProvisionStatus.SUCCESS.getSpecificationValue());
		DOMUtils.createElementAndText(policyNode, "PolicyKey", policyKey);
		return document;
	}	
	
	@Test
	public void testLoopWithinResponseProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<Provision>" +
				"<Status>2</Status>" +
				"<Policies>" +
				"<Policy>" +
				"<PolicyType>MS-EAS-Provisioning-WBXML</PolicyType>" +
				"<Status>2</Status>" +
				"<PolicyKey>3378841480</PolicyKey>" +
				"<Data>" +
				"<EASProvisionDoc>" +
				"<DevicePasswordEnabled>0</DevicePasswordEnabled>" +
				"<AlphanumericDevicePasswordRequired>0</AlphanumericDevicePasswordRequired>" +
				"<PasswordRecoveryEnabled>0</PasswordRecoveryEnabled>" +
				"<DeviceEncryptionEnabled>0</DeviceEncryptionEnabled>" +
				"<AttachmentsEnabled>1</AttachmentsEnabled>" +
				"<MinDevicePasswordLength>4</MinDevicePasswordLength>" +
				"<MaxInactivityTimeDeviceLock>900</MaxInactivityTimeDeviceLock>" +
				"<MaxDevicePasswordFailedAttempts>8</MaxDevicePasswordFailedAttempts>" +
				"<MaxAttachmentSize/>" +
				"<AllowSimpleDevicePassword>1</AllowSimpleDevicePassword>" +
				"<DevicePasswordExpiration/>" +
				"<DevicePasswordHistory>0</DevicePasswordHistory>" +
				"<AllowStorageCard>1</AllowStorageCard>" +
				"<AllowCamera>1</AllowCamera>" +
				"<RequireDeviceEncryption>0</RequireDeviceEncryption>" +
				"<AllowUnsignedApplications>1</AllowUnsignedApplications>" +
				"<AllowUnsignedInstallationPackages>1</AllowUnsignedInstallationPackages>" +
				"<MinDevicePasswordComplexCharacters>3</MinDevicePasswordComplexCharacters>" +
				"<AllowWiFi>1</AllowWiFi>" +
				"<AllowTextMessaging>1</AllowTextMessaging>" +
				"<AllowPOPIMAPEmail>1</AllowPOPIMAPEmail>" +
				"<AllowBluetooth>2</AllowBluetooth>" +
				"<AllowIrDA>1</AllowIrDA>" +
				"<RequireManualSyncWhenRoaming>0</RequireManualSyncWhenRoaming>" +
				"<AllowDesktopSync>1</AllowDesktopSync>" +
				"<MaxCalendarAgeFilter>0</MaxCalendarAgeFilter>" +
				"<AllowHTMLEmail>1</AllowHTMLEmail>" +
				"<MaxEmailAgeFilter>0</MaxEmailAgeFilter>" +
				"<MaxEmailBodyTruncationSize>-1</MaxEmailBodyTruncationSize>" +
				"<MaxEmailHTMLBodyTruncationSize>-1</MaxEmailHTMLBodyTruncationSize>" +
				"<RequireSignedSMIMEMessages>0</RequireSignedSMIMEMessages>" +
				"<RequireEncryptedSMIMEMessages>0</RequireEncryptedSMIMEMessages>" +
				"<RequireSignedSMIMEAlgorithm>0</RequireSignedSMIMEAlgorithm>" +
				"<RequireEncryptionSMIMEAlgorithm>0</RequireEncryptionSMIMEAlgorithm>" +
				"<AllowSMIMEEncryptionAlgorithmNegotiation>2</AllowSMIMEEncryptionAlgorithmNegotiation>" +
				"<AllowSMIMESoftCerts>1</AllowSMIMESoftCerts>" +
				"<AllowBrowser>1</AllowBrowser>" +
				"<AllowConsumerEmail>1</AllowConsumerEmail>" +
				"<AllowRemoteDesktop>1</AllowRemoteDesktop>" +
				"<AllowInternetSharing>1</AllowInternetSharing>" +
				"<UnapprovedInROMApplicationList/>" +
				"<ApprovedApplicationList/>" +
				"</EASProvisionDoc>" +
				"</Data>" +
				"</Policy>" +
				"</Policies>" +
				"</Provision>";
		
		ProvisionResponse provisionResponse = provisionProtocol.decodeResponse(DOMUtils.parse(initialDocument));
		Document encodeResponse = provisionProtocol.encodeResponse(provisionResponse);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
	
	@Test
	public void testLoopWithinRequestProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<Provision>" +
				"<Policies>" +
				"<Policy>" +
				"<PolicyType>MS-EAS-Provisioning-WBXML</PolicyType>" +
				"<PolicyKey>3378841480</PolicyKey>" +
				"</Policy>" +
				"</Policies>" +
				"</Provision>";
		
		ProvisionRequest provisionRequest = provisionProtocol.decodeRequest(DOMUtils.parse(initialDocument));
		Document encodeResponse = provisionProtocol.encodeRequest(provisionRequest);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
}
