/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.opush;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.mortbay.component.LifeCycle;
import org.mortbay.component.LifeCycle.Listener;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.thread.QueuedThreadPool;
import org.obm.push.OpushModule;
import org.obm.push.utils.DOMUtils;

import bitronix.tm.TransactionManagerServices;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.util.Modules;
import com.google.inject.util.Modules.OverriddenModuleBuilder;

public abstract class ActiveSyncServletModule extends AbstractModule {

	@Inject DOMUtils domUtils;
	
	protected abstract Module overrideModule() throws Exception;
	protected abstract void onModuleInstalled();
	
	protected void configure() {
		OverriddenModuleBuilder override = Modules.override(new OpushModule());
		try {
			install(override.with(overrideModule()));
			onModuleInstalled();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Provides @Singleton
	protected OpushServer buildOpushServer(Injector injector) {
		return new OpushServer(injector);
	}
	
	public static class OpushServer {
		
		private static final int WAIT_TO_BE_STARTED_MAX_TIME = 10;
		private static final int WAIT_TO_BE_STARTED_LATCH_COUNT = 1;

		private final Server server;
		private final SelectChannelConnector selectChannelConnector;
		private final CountDownLatch serverStartedLatch = new CountDownLatch(WAIT_TO_BE_STARTED_LATCH_COUNT);

		public OpushServer(Injector injector) {
			server = new Server();
			server.setThreadPool(new QueuedThreadPool(2));
			selectChannelConnector = new SelectChannelConnector();
			server.setConnectors(new Connector[] {selectChannelConnector});
			Context root = new Context(server, "/", Context.SESSIONS);
			root.addFilter(GuiceFilter.class, "/*", 0);
			root.addServlet(DefaultServlet.class, "/");
			root.addLifeCycleListener(buildServerStartedListener());
			root.addEventListener(buildTransactionManagerListener(injector));
		}

		private ServletContextListener buildTransactionManagerListener(final Injector injector) {
			return new ServletContextListener() {

				@Override
				public void contextInitialized(ServletContextEvent sce) {
				}

				@Override
				public void contextDestroyed(ServletContextEvent sce) {
					injector.getInstance(org.obm.push.store.ehcache.ObjectStoreManager.class).shutdown();
					injector.getInstance(org.obm.push.jaxb.store.ehcache.ObjectStoreManager.class).shutdown();
					TransactionManagerServices.getTransactionManager().shutdown();
				}
			};
		}

		private Listener buildServerStartedListener() {
			return new Listener() {
				@Override
				public void lifeCycleStopping(LifeCycle event) {
				}
				@Override
				public void lifeCycleStopped(LifeCycle event) {
				}
				@Override
				public void lifeCycleStarting(LifeCycle event) {
				}
				@Override
				public void lifeCycleStarted(LifeCycle event) {
					serverStartedLatch.countDown();
				}
				@Override
				public void lifeCycleFailure(LifeCycle event, Throwable cause) {
				}
			};
		}

		public void start() throws Exception {
			server.start();
		}

		public void stop() throws Exception {
			server.stop();
		}
		
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
	}

}
