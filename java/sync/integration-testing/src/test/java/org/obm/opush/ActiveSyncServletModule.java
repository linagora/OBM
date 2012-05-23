package org.obm.opush;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.thread.QueuedThreadPool;
import org.obm.push.OpushModule;
import org.obm.push.utils.DOMUtils;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.util.Modules;
import com.google.inject.util.Modules.OverriddenModuleBuilder;

public abstract class ActiveSyncServletModule extends AbstractModule {

	@Inject DOMUtils domUtils;
	
	protected abstract Module overrideModule() throws Exception;
	
	protected void configure() {
		OverriddenModuleBuilder override = Modules.override(new OpushModule());
		try {
			install(override.with(overrideModule()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Provides @Singleton
	@PortNumber int getPortNumber() {
		return FreePortFinder.findFreePort();
	}

	@Provides @Singleton
	protected OpushServer buildOpushServer(@PortNumber int portNumber) {
		return new OpushServer(portNumber);
	}
	
	public static class OpushServer {
		private Server server;

		public OpushServer(int portNumber) {
			server = new Server();
			server.setThreadPool(new QueuedThreadPool(2));
			SelectChannelConnector selectChannelConnector = new SelectChannelConnector();
			selectChannelConnector.setPort(portNumber);
			server.setConnectors(new Connector[] {selectChannelConnector});
			Context root = new Context(server, "/", Context.SESSIONS);
			root.addFilter(GuiceFilter.class, "/*", 0);
			root.addServlet(DefaultServlet.class, "/");
		}

		public void start() throws Exception {
			server.start();
		}

		public void stop() throws Exception {
			server.stop();
		}
	}

}
