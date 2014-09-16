/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014  Linagora
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
package org.obm.servlet.filter.resource;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.servlet.filter.resource.ResourcesFilterTest.Servlet.IntResource;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ResourcesFilterTest {

	@Singleton
	public static class Servlet extends HttpServlet {
		
		public static class IntResource implements Resource {
		
			public static List<IntResource> resources = Lists.newArrayList();
			
			private int value;
			private boolean closed;
			
			public IntResource(int value) {
				this.value = value;
				resources.add(this);
			}
			
			public int getValue() {
				return value;
			}
			
			@Override
			public void closeResource() {
				closed = true;
			}
		}
		
		@Inject Provider<ResourcesHolder> holder;
		
		int count = 0;
		
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			ResourcesHolder holder = this.holder.get();
			super.doGet(req, resp);
			holder.put(IntResource.class, new IntResource(count++));
		}
	}
	
	public static class TestModule extends ResourceFilterTestModule {
		@Override
		protected void configureServlets() {
			super.configureServlets();
			serve("/*").with(Servlet.class);
		}
	}
	
	@Rule
	public JUnitGuiceRule guiceModule = new JUnitGuiceRule(TestModule.class);

	@Inject EmbeddedServer server;
	@Inject Servlet servlet;
	
	@Before
	public void setup() throws Exception {
		server.start();
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void test() throws ClientProtocolException, IOException {
		Request.Get("http://localhost:" + server.getPort() + "/test").execute();
		Request.Get("http://localhost:" + server.getPort() + "/test").execute();
		assertThat(IntResource.resources).hasSize(2);
		assertThat(IntResource.resources.get(0).closed).isTrue();
		assertThat(IntResource.resources.get(1).closed).isTrue();
	}
	
}
