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
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.methodHandler.CopyHandler;
import org.obm.caldav.server.methodHandler.DavMethodHandler;
import org.obm.caldav.server.methodHandler.LockHandler;
import org.obm.caldav.server.methodHandler.MkColHandler;
import org.obm.caldav.server.methodHandler.MoveHandler;
import org.obm.caldav.server.methodHandler.OptionsHandler;
import org.obm.caldav.server.methodHandler.PropFindHandler;
import org.obm.caldav.server.methodHandler.PropPatchHandler;
import org.obm.caldav.server.methodHandler.PutHandler;
import org.obm.caldav.server.methodHandler.ReportHandler;
import org.obm.caldav.server.methodHandler.UnlockHandler;
import org.obm.caldav.server.share.Token;

/**
 * WebDAV for CalDAV implementation
 * 
 * @author tom
 * 
 */
public class WebdavServlet extends HttpServlet {

	private static final Log logger = LogFactory.getLog(WebdavServlet.class);

	private static final long serialVersionUID = 1410911584964336424L;

	private Map<String, DavMethodHandler> handlers;
	private AuthHandler authHandler;
	private IProxy proxy;
	
	/**
	 * Handles the special WebDAV methods.
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Token token = authHandler.doAuth(request);
		if(token != null){
			this.proxy = new ProxyImpl(token);
		}
		
		if (proxy == null || !proxy.isConnected()) {
			String uri = request.getMethod() + " " + request.getRequestURI()
            + " " + request.getQueryString();
			logger.warn("invalid auth, sending http 401 (uri: " + uri + ")");
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
			handler.process(token, proxy,new DavRequest(request), response);
		} else {
			super.service(request, response);
		}
		

	}

	@Override
	public void init() throws ServletException {
		super.init();
		handlers = new HashMap<String, DavMethodHandler>();
		handlers.put("propfind", new PropFindHandler());
		handlers.put("proppatch", new PropPatchHandler());
		handlers.put("mkcol", new MkColHandler());
		handlers.put("copy", new CopyHandler());
		handlers.put("move", new MoveHandler());
		handlers.put("lock", new LockHandler());
		handlers.put("unlock", new UnlockHandler());
		handlers.put("options", new OptionsHandler());
		handlers.put("report", new ReportHandler());
		handlers.put("put", new PutHandler());
		
		authHandler = new AuthHandler();
	}

}
