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

package org.obm.caldav.server.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.StatusCodeConstant;
import org.obm.caldav.server.exception.CalDavException;
import org.obm.caldav.server.methodHandler.DavMethodHandler;
import org.obm.caldav.server.methodHandler.DeleteHandler;
import org.obm.caldav.server.methodHandler.GetHandler;
import org.obm.caldav.server.methodHandler.OptionsHandler;
import org.obm.caldav.server.methodHandler.PropFindHandler;
import org.obm.caldav.server.methodHandler.PropPatchHandler;
import org.obm.caldav.server.methodHandler.PutHandler;
import org.obm.caldav.server.methodHandler.ReportHandler;
import org.obm.caldav.server.share.Token;

/**
 * WebDAV for CalDAV implementation
 * 
 * @author tom, adrien
 * 
 */
public class WebdavServlet extends HttpServlet {

	private static final Log logger = LogFactory.getLog(WebdavServlet.class);

	private static final long serialVersionUID = 1410911584964336424L;

	private Map<String, DavMethodHandler> handlers;
	private AuthHandler authHandler;

	/**
	 * Handles the special WebDAV methods.
	 */
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		IProxy proxy = null;
		try {
			Token token = authHandler.doAuth(request);
			proxy = getProxy();

			if (!proxy.validateToken(token)) {
				String uri = request.getMethod() + " "
						+ request.getRequestURI() + " "
						+ request.getQueryString();
				logger.debug("invalid auth, sending http 401 (uri: " + uri
						+ ")");
				String s = "Basic realm=\"CalDavService\"";
				response.setHeader("WWW-Authenticate", s);

				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			String method = request.getMethod();

			if (logger.isInfoEnabled()) {
				logger.info("[" + method + "] " + request.getRequestURI());
			}

			DavMethodHandler handler = handlers.get(method.toLowerCase());
			if (handler != null) {
				proxy.login(token);
				DavRequest dr = new DavRequest(request);
				handler.process(token, proxy, dr, response);

			} else {
				super.service(request, response);
			}
		} catch (CalDavException e) {
			logger.error(e.getMessage());
			response.sendError(e.getHttpStatusCode());
		} catch (Exception e) {
			// rfc4791 1.3 Method Preconditions and Postconditions
			response.sendError(StatusCodeConstant.SC_INTERNAL_SERVER_ERROR);
			logger.error(e.getMessage(), e);
		} finally {
			appendHearder(response);
			if (proxy != null) {
				proxy.logout();
			}
		}
	}
	
	
	private void appendHearder(HttpServletResponse response){
//		head[Content-Length] => 147
//		head[Date] => Wed, 29 Jul 2009 11:29:03 GMT
//		head[Expires] => Wed, 29 Jul 2009 11:29:03 GMT
//		head[Cache-Control] => private, max-age=0
//		head[X-Content-Type-Options] => nosniff
//		head[DAV] => 1, calendar-access, calendar-schedule, calendar-proxy
		
		response.setHeader("DAV", "1, calendar-access, calendar-schedule, calendar-proxy");
		response.setHeader("Cache-Control", "private, max-age=0");
		
		
	}

	private IProxy getProxy() throws CoreException {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extPoint = extensionRegistry
				.getExtensionPoint("org.obm.caldav.server.proxy");
		IConfigurationElement[] configurationElements = extPoint
				.getConfigurationElements();

		for (IConfigurationElement current : configurationElements) {
			String[] attributeNames = current.getAttributeNames();
			for (String currentAttributeName : attributeNames) {
				System.out.println("Attribut : " + currentAttributeName
						+ " / Valeur : "
						+ current.getAttribute(currentAttributeName));
			}
		}

		return (IProxy) configurationElements[0]
				.createExecutableExtension("IProxy");

	}

	@Override
	public void init() throws ServletException {
		super.init();
		handlers = new HashMap<String, DavMethodHandler>();
		handlers.put("get", new GetHandler());
		handlers.put("propfind", new PropFindHandler());
		handlers.put("proppatch", new PropPatchHandler());
		handlers.put("options", new OptionsHandler());
		handlers.put("report", new ReportHandler());
		handlers.put("put", new PutHandler());
		handlers.put("delete", new DeleteHandler());

		authHandler = new AuthHandler();
	}

}
