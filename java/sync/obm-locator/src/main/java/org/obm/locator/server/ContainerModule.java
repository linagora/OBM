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
package org.obm.locator.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.mortbay.component.LifeCycle;
import org.mortbay.component.LifeCycle.Listener;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.thread.QueuedThreadPool;
import org.obm.configuration.LocatorConfiguration;
import org.obm.configuration.VMArgumentsUtils;
import org.obm.locator.LocatorServerLauncher;
import org.slf4j.Logger;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;

public class ContainerModule extends AbstractModule {

	private static final Logger logger = LocatorServerLauncher.logger;

	private static final int POOL_THREAD_SIZE = Objects.firstNonNull( 
			VMArgumentsUtils.integerArgumentValue("obmLocatorPoolSize"), 50);

	private static final int WAIT_TO_BE_STARTED_MAX_TIME = 10;
	private static final int WAIT_TO_BE_STARTED_LATCH_COUNT = 1;
	private final CountDownLatch serverStartedLatch = new CountDownLatch(WAIT_TO_BE_STARTED_LATCH_COUNT);

	@Override
	protected void configure() {
		install(new LocatorModule());
	}

	@Provides @Singleton
	protected LocatorServer buildServer(LocatorConfiguration locatorConfiguration) {
		final SelectChannelConnector selectChannelConnector = new SelectChannelConnector();
		selectChannelConnector.setPort(locatorConfiguration.getLocatorPort());

		final Server server = new Server();
		server.setThreadPool(new QueuedThreadPool(POOL_THREAD_SIZE));
		server.setConnectors(new Connector[] {selectChannelConnector});
		
		Context root = new Context(server, "/", Context.SESSIONS);
		root.addFilter(GuiceFilter.class, "/*", 0);
		root.addServlet(DefaultServlet.class, "/");
		root.addLifeCycleListener(buildServerStartedListener());
		return new LocatorServer() {

			@Override
			public void join() throws Exception {
				server.join();
			}
			
			@Override
			public void stop() throws Exception {
				server.stop();
			}
			
			@Override
			public void start() throws Exception {
				server.start();
			}

			@Override
			public int getPort() {
				if (server.isRunning()) {
					return waitServerStartsThenGetPorts();
				}
				throw new IllegalStateException("Could not get server's listening port. Start the server first.");
			}

			private int waitServerStartsThenGetPorts() {
				try {
					if (serverStartedLatch.await(WAIT_TO_BE_STARTED_MAX_TIME, TimeUnit.SECONDS)) {
						return getLocalPort();
					}
				} catch (InterruptedException e) {
					Throwables.propagate(e);
				}
				throw new IllegalStateException("Could not get server's listening port. Illegal concurrent state.");
			}

			private int getLocalPort() {
				int port = selectChannelConnector.getLocalPort();
				if (port > 0) {
					return port;
				}
				throw new IllegalStateException("Could not get server's listening port. Received port is " + port);
			}
		};
	}

	private Listener buildServerStartedListener() {
		return new Listener() {
			@Override
			public void lifeCycleStopping(LifeCycle event) {
				logger.info("Application stopping");
			}
			@Override
			public void lifeCycleStopped(LifeCycle event) {
				logger.info("Application stopped");
			}
			@Override
			public void lifeCycleStarting(LifeCycle event) {
				logger.info("Application starting");
			}
			@Override
			public void lifeCycleStarted(LifeCycle event) {
				serverStartedLatch.countDown();
				logger.info("Application started");
			}
			@Override
			public void lifeCycleFailure(LifeCycle event, Throwable cause) {
				logger.error("Application failure", cause);
			}
		};
	}
}
