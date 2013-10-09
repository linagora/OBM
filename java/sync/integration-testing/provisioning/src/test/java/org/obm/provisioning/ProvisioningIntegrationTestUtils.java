/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
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
package org.obm.provisioning;

import java.net.URL;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ProvisioningIntegrationTestUtils {

	public static String getAdminUserJson(){
		return "{\"id\":\"Admin0ExtId\",\"login\":\"admin0\",\"lastname\":\"Lastname\",\"profile\":\"admin\","
				+ "\"firstname\":\"Firstname\",\"commonname\":null,\"password\":\"admin0\","
				+ "\"kind\":null,\"title\":null,\"description\":null,\"company\":null,\"service\":null,"
				+ "\"direction\":null,\"addresses\":[],\"town\":null,\"zipcode\":null,\"business_zipcode\":null,"
				+ "\"country\":\"0\",\"phones\":[],\"mobile\":null,\"faxes\":[],\"archived\":false,\"mail_quota\":\"0\","
				+ "\"mail_server\":null,\"mails\":[\"admin0@*\"],\"effectiveMails\":[\"admin0@test.tlse.lng\"],\"hidden\":false,\"timecreate\":null,\"timeupdate\":null,"
				+ "\"groups\":[]}";
	}
	
	public static String getAdminUserJsonWithGroup(){
		return "{\"id\":\"Admin0ExtId\",\"login\":\"admin0\",\"lastname\":\"Lastname\",\"profile\":\"admin\","
				+ "\"firstname\":\"Firstname\",\"commonname\":null,\"password\":\"admin0\",\"kind\":null,\"title\":null,"
				+ "\"description\":null,\"company\":null,\"service\":null,\"direction\":null,\"addresses\":[],\"town\":null,"
				+ "\"zipcode\":null,\"business_zipcode\":null,\"country\":\"0\",\"phones\":[],\"mobile\":null,\"faxes\":[],\"archived\":false,"
				+ "\"mail_quota\":\"0\",\"mail_server\":null,\"mails\":[\"admin0@*\"],\"effectiveMails\":[\"admin0@global.virt\"],\"hidden\":false,\"timecreate\":null,\"timeupdate\":null,"
				+ "\"groups\":[{\"id\":\"GroupWithUsers\",\"url\":\"/123456789/groups/GroupWithUsers\"}]}";
	}

	public static String groupUrl(URL baseURL, ObmDomainUuid domain) {
		return domainUrl(baseURL, domain) + "/groups/";
	}

	public static String userUrl(URL baseURL, ObmDomainUuid domain) {
		return domainUrl(baseURL, domain) + "/users/";
	}
	
	public static String profileUrl(URL baseURL, ObmDomainUuid domain) {
		return domainUrl(baseURL, domain) + "/profiles/";
	}

	public static String domainUrl(URL baseURL, ObmDomainUuid domain) {
		return baseUrl(baseURL) + "/" + domain.get();
	}
	
	public static String baseUrl(URL baseURL) {
		return baseURL.toExternalForm() + ProvisioningService.PROVISIONING_ROOT_PATH;
	}
	
	public static String batchUrl(URL baseURL, ObmDomainUuid domain, String batchId) {
		return domainUrl(baseURL, domain) + "/batches/" + batchId;
	}
}
