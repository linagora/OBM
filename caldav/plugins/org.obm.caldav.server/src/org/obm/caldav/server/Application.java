package org.obm.caldav.server;

import java.util.Hashtable;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.equinox.http.jetty.JettyConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		System.out.println("CalDAV server started...");
		Hashtable<String, Object> settings = new Hashtable<String, Object>();
		settings.put(JettyConstants.HTTP_PORT, 8083);
		settings.put(JettyConstants.CONTEXT_PATH, "");

		System.setProperty("org.mortbay.http.HttpRequest.maxFormContentSize",
				"" + (20 * 1024 * 1024));

		JettyConfigurator.startServer("obm_caldav", settings);

		loadBundle("org.eclipse.equinox.http.registry");

		return EXIT_OK;
	}

	private void loadBundle(String bundleName) throws BundleException {
		Bundle bundle = Platform.getBundle(bundleName);
		if (bundle != null) {
			if (bundle.getState() == Bundle.RESOLVED) {
				bundle.start(Bundle.START_TRANSIENT);
			}
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
