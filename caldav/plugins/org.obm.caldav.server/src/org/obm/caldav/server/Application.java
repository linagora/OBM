/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

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
