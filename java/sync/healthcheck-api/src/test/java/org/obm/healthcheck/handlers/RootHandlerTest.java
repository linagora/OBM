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
package org.obm.healthcheck.handlers;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.healthcheck.HealthCheckHandler;
import org.obm.healthcheck.handlers.RootHandler.EndpointDescription;
import org.obm.healthcheck.handlers.RootHandlerTest.Env;
import org.obm.test.GuiceModule;
import org.obm.test.SlowGuiceRunner;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(Env.class)
public class RootHandlerTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			Multibinder<HealthCheckHandler> multibinder = Multibinder.newSetBinder(binder(), HealthCheckHandler.class);
			
			multibinder.addBinding().to(Handler1.class);
			multibinder.addBinding().to(Handler2.class);
		}
		
	}
	
	@Path("/handler1")
	public static class Handler1 implements HealthCheckHandler {
		
		@GET
		@Path("method1")
		public Object method1() {
			return null;
		}
		
		@GET
		@Path("method2")
		public Object method2() {
			return null;
		}
		
	}
	
	@Path("/handler2")
	public static class Handler2 implements HealthCheckHandler {
		
		@GET
		@Path("method1")
		public Object method1() {
			return null;
		}
		
	}
	
	@Inject
	private RootHandler handler;
	
	@Test
	public void testRoot() {
		List<EndpointDescription> actual = handler.root();
		List<EndpointDescription> expected = ImmutableList.of(
																new EndpointDescription("GET", "/handler1/method1"),
																new EndpointDescription("GET", "/handler1/method2"),
																new EndpointDescription("GET", "/handler2/method1")
															);
		
		assertThat(actual).isEqualTo(expected);
	}

}
