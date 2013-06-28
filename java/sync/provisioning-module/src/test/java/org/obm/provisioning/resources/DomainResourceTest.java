/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.provisioning.resources;

import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collections;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;

import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class DomainResourceTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testEmptyList() throws Exception {
		expect(domainDao.list()).andReturn(Collections.<ObmDomain> emptyList());
		mocksControl.replay();

		HttpResponse response = get("/domains");

		mocksControl.verify();

		assertThat(EntityUtils.toString(response.getEntity())).isEqualTo("[]");
	}

	@Test
	public void testNullList() throws Exception {
		expect(domainDao.list()).andReturn(null);
		mocksControl.replay();

		HttpResponse response = get("/domains");

		mocksControl.verify();

		assertThat(EntityUtils.toString(response.getEntity())).isEqualTo("[]");
	}

	@Test
	public void testList() throws Exception {
		expect(domainDao.list()).andReturn(ImmutableList.<ObmDomain> of(domain, ToolBox.getDefaultObmDomain()));
		mocksControl.replay();

		HttpResponse response = get("/domains");

		mocksControl.verify();

		assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(
				"[" +
						"{" +
							"\"id\":\"a3443822-bb58-4585-af72-543a287f7c0e\"," +
							"\"url\":\"/domains/a3443822-bb58-4585-af72-543a287f7c0e\"" +
						"}," +
						"{" +
							"\"id\":\"ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6\"," +
							"\"url\":\"/domains/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6\"" +
						"}" +
				"]");
	}

	@Test
	public void testListOnError() throws Exception {
		expect(domainDao.list()).andThrow(new RuntimeException());
		mocksControl.replay();

		HttpResponse response = get("/domains");

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}

	@Test
	public void testGet() throws Exception {
		expectDomain();
		mocksControl.replay();

		HttpResponse response = get("/domains/" + domain.getUuid().get());

		mocksControl.verify();

		assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(
				"{" +
					"\"id\":\"a3443822-bb58-4585-af72-543a287f7c0e\"," +
					"\"name\":\"domain\"," +
					"\"label\":null," +
					"\"aliases\":[]" +
				"}");
	}

	@Test
	public void testGetOnUnknownDomain() throws Exception {
		expectNoDomain();
		mocksControl.replay();

		HttpResponse response = get("/domains/" + domain.getUuid().get());

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}

	@Override
	protected HttpResponse get(String path) throws Exception {
		return Request.Get(baseUrl + path).execute().returnResponse();
	}

}
