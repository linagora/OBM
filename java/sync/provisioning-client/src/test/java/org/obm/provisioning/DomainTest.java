/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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

import static com.github.restdriver.clientdriver.RestClientDriver.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.common.base.Optional;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;


public class DomainTest {

	@Rule public ClientDriverRule driver = new ClientDriverRule();
	
	@Test
	public void getById() {
		driver.addExpectation(
				onRequestTo("/obm-sync/provisioning/v1/domains/d6b64792-b093-d85a-1b4f-d2a9e05d4011"), 
				giveResponse("{\"id\":\"d6b64792-b093-d85a-1b4f-d2a9e05d4011\",\"name\":\"global.virt\",\"label\":\"Global Domain\",\"aliases\":[]}",
						MediaType.APPLICATION_JSON));
		
		Optional<ObmDomain> domain = new Domain(driver.getBaseUrl() + "/obm-sync/").getById(UUID.fromString("d6b64792-b093-d85a-1b4f-d2a9e05d4011"));
		
		assertThat(domain.isPresent()).isTrue();
		assertThat(domain.get()).isEqualTo(ObmDomain.builder().uuid(ObmDomainUuid.of("d6b64792-b093-d85a-1b4f-d2a9e05d4011")).name("global.virt").label("Global Domain").build());
	}
	
	@Test
	public void getByIdNotFound() {
		driver.addExpectation(
				onRequestTo("/obm-sync/provisioning/v1/domains/d6b64792-b093-d85a-1b4f-d2a9e05d4011"), 
				giveEmptyResponse().withStatus(404));
		
		Optional<ObmDomain> actual = new Domain(driver.getBaseUrl() + "/obm-sync/").getById(UUID.fromString("d6b64792-b093-d85a-1b4f-d2a9e05d4011"));
		assertThat(actual.isPresent()).isFalse();
	}
	
	@Test
	public void getByIdServerError() {
		driver.addExpectation(
				onRequestTo("/obm-sync/provisioning/v1/domains/d6b64792-b093-d85a-1b4f-d2a9e05d4011"), 
				giveEmptyResponse().withStatus(500));
		
		Optional<ObmDomain> actual = new Domain(driver.getBaseUrl() + "/obm-sync/").getById(UUID.fromString("d6b64792-b093-d85a-1b4f-d2a9e05d4011"));
		assertThat(actual.isPresent()).isFalse();
	}
}
