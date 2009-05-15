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

	/**
	 * Handles the special WebDAV methods.
	 */
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Token token = authHandler.doAuth(req);
		if (token == null) {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String method = req.getMethod();

		if (logger.isInfoEnabled()) {
			logger.info("[" + method + "] " + req.getRequestURI());
		}


		DavMethodHandler handler = handlers.get(method.toLowerCase());
		if (handler != null) {
			

			
			handler.process(token, new DavRequest(req), resp);
		} else {
			super.service(req, resp);
		}

	}

	@Override
	public void init() throws ServletException {
		super.init();
		handlers = new HashMap<String, DavMethodHandler>();
		handlers.put("propfind", new PropFindHandler());
		handlers.put("proppatch", new PropFindHandler());
		handlers.put("mkcol", new PropFindHandler());
		handlers.put("copy", new PropFindHandler());
		handlers.put("move", new PropFindHandler());
		handlers.put("lock", new PropFindHandler());
		handlers.put("unlock", new PropFindHandler());
		handlers.put("options", new OptionsHandler());
		handlers.put("report", new ReportHandler());
		handlers.put("put", new PutHandler());

		authHandler = new AuthHandler();
	}

}
