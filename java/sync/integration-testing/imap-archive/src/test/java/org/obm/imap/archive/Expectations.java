/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */

package org.obm.imap.archive;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static org.obm.imap.archive.DBData.admin;

import javax.ws.rs.core.MediaType;

import org.hamcrest.Matchers;

import com.github.restdriver.clientdriver.ClientDriverRequest.Method;
import com.github.restdriver.clientdriver.ClientDriverRule;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class Expectations {

	private final ClientDriverRule driver;

	public Expectations(ClientDriverRule driver) {
		this.driver = driver;
	}
	
	public Expectations expectTrustedLogin(ObmDomain domain) {
		return expectTrustedLoginForUser(domain, admin);
	}
	
	public Expectations expectTrustedLoginForUser(ObmDomain domain, ObmUser user) {
		driver.addExpectation(
				onRequestTo("/obm-sync/login/trustedLogin").withMethod(Method.POST)
					.withBody(Matchers.allOf(
								Matchers.containsString("login=" + user.getLogin() + "%40" + domain.getName()),
								Matchers.containsString("password=" + user.getPassword().getStringValue())),
					MediaType.APPLICATION_FORM_URLENCODED),
				giveResponse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<token xmlns=\"http://www.obm.org/xsd/sync/token.xsd\">"
						+ "<sid>06ae323a-0fa1-42ea-9ee8-313a023e4fd4</sid>"
						+ "<domain uuid=\"" + domain.getUuid().get() + "\">" + domain.getName() + "</domain>"
						+ "</token>",
					MediaType.APPLICATION_XML)
				);
		return this;
	}
	
	public Expectations expectTrustedLoginThrowAuthFault() {
		driver.addExpectation(
				onRequestTo("/obm-sync/login/trustedLogin").withMethod(Method.POST)
					.withBody(Matchers.allOf(
								Matchers.containsString("login=admin%40mydomain.org"),
								Matchers.containsString("password=trust3dToken")),
					MediaType.APPLICATION_FORM_URLENCODED),
				giveResponse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<error xmlns=\"http://www.obm.org/xsd/sync/error.xsd\">"
						+ "<message>Login failed for user 'admin%40mydomain.org'</message>"
						+ "</error>",
					MediaType.APPLICATION_XML)
				);
		return this;
	}
	
	public Expectations expectGetDomain(ObmDomain domain) {
		expectDomain(domain);
		return this;
	}

	private void expectDomain(ObmDomain domain) {
		driver.addExpectation(
				onRequestTo("/obm-sync/provisioning/v1/domains/" + domain.getUuid().get()),
				giveResponse("{\"id\":\"" + domain.getUuid().get() + "\","
							+ "\"name\":\"" + domain.getName() + "\","
							+ "\"label\":\"" + domain.getName() + "\","
							+ "\"aliases\":[]}",
					MediaType.APPLICATION_JSON)
				);
	}
}
