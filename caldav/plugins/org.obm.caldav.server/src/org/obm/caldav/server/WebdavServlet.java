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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.exception.AuthenticationException;
import org.obm.caldav.server.exception.CalDavException;
import org.obm.caldav.server.impl.AuthHandler;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.methodHandler.CopyHandler;
import org.obm.caldav.server.methodHandler.DavMethodHandler;
import org.obm.caldav.server.methodHandler.DeleteHandler;
import org.obm.caldav.server.methodHandler.GetHandler;
import org.obm.caldav.server.methodHandler.LockHandler;
import org.obm.caldav.server.methodHandler.MkColHandler;
import org.obm.caldav.server.methodHandler.MoveHandler;
import org.obm.caldav.server.methodHandler.OptionsHandler;
import org.obm.caldav.server.methodHandler.PostHandler;
import org.obm.caldav.server.methodHandler.PropFindHandler;
import org.obm.caldav.server.methodHandler.PropPatchHandler;
import org.obm.caldav.server.methodHandler.PutHandler;
import org.obm.caldav.server.methodHandler.ReportHandler;
import org.obm.caldav.server.methodHandler.UnlockHandler;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.RunnableExtensionLoader;

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

	private IBackendFactory backendFactory;

	/**
	 * Handles the special WebDAV methods.
	 */
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		IBackend backend = null;
		try {
			String method = request.getMethod();
			logger.info("\n[" + method + "] " + request.getRequestURI());
			DavRequest dr = new DavRequest(request);

			Token token = authHandler.doAuth(dr);
			backend = performAuthentification(dr, response, token);
			if (backend == null) {
				return;
			}

			DavMethodHandler handler = handlers.get(method.toLowerCase());
			if (handler != null) {
				backend.login(token);
				handler.process(token, backend, dr, response);

			} else {
				super.service(request, response);
			}
		} catch (CalDavException e) {
			logger.error(e.getMessage(), e);
			response.sendError(e.getHttpStatusCode());
		} catch (Exception e) {
			// rfc4791 1.3 Method Preconditions and Postconditions
			response.sendError(StatusCodeConstant.SC_INTERNAL_SERVER_ERROR);
			logger.error(e.getMessage(), e);
		} finally {
			appendHearder(response, backend);
			if (backend != null) {
				backend.logout();
			}
		}
	}

	private IBackend performAuthentification(DavRequest dr,
			HttpServletResponse response, Token token) {
		IBackend backend = null;
		try {
			backend = getBackend(token);
		} catch (AuthenticationException auth) {
			logger.error(auth.getMessage());
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		
		if (backend == null) {
			String uri = dr.getMethod() + " " + dr.getRequestURI() + " "
					+ dr.getQueryString();
			logger.info("invalid auth, sending http 401 (uri: " + uri + ")");
			String s = "Basic realm=\"Obm CalDav for calendar "+ dr.getCalendarComponantName() +"\"";
			response.setHeader("WWW-Authenticate", s);

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
		return backend;
	}

	private void appendHearder(HttpServletResponse response, IBackend proxy) {
		// head[Content-Length] => 147
		// head[Date] => Wed, 29 Jul 2009 11:29:03 GMT
		// head[Expires] => Wed, 29 Jul 2009 11:29:03 GMT
		// head[Cache-Control] => private, max-age=0
		// head[X-Content-Type-Options] => nosniff
		// head[DAV] => 1, calendar-access, calendar-schedule, calendar-proxy
		// head[ETag] => "14F6F6-1000-4A7061DA"
		response
				.addHeader("Allow",
						"OPTIONS, PROPFIND, HEAD, GET, REPORT, PROPPATCH, PUT, DELETE, POST");
		response
				.addHeader("DAV",
						"1, calendar-access, calendar-schedule, calendar-proxy, calendar-auto-schedule");
		response.addHeader("Cache-Control", "private, max-age=0");
		if (proxy != null) {
			try {
				response.setHeader("ETag", proxy.getETag());
			} catch (Exception e) {
			}
		}
	}

	private IBackendFactory getBackendFactory() {

		RunnableExtensionLoader<IBackendFactory> rel = new RunnableExtensionLoader<IBackendFactory>();
		List<IBackendFactory> backs = rel
				.loadExtensions("org.obm.caldav.server", "backend", "backend",
						"implementation");
		if (backs.size() > 0) {
			IBackendFactory bf = backs.get(0);
			return bf;
		} else {
			logger.error("No caldav backend found.");
			return null;
		}
	}

	private IBackend getBackend(Token token) throws Exception {
		return this.backendFactory.loadBackend(token);
	}

	@Override
	public void init() throws ServletException {
		super.init();
		this.backendFactory = getBackendFactory();
		handlers = new HashMap<String, DavMethodHandler>();
		handlers.put("get", new GetHandler());
		handlers.put("post", new PostHandler());
		handlers.put("propfind", new PropFindHandler());
		handlers.put("proppatch", new PropPatchHandler());
		handlers.put("options", new OptionsHandler());
		handlers.put("report", new ReportHandler());
		handlers.put("put", new PutHandler());
		handlers.put("delete", new DeleteHandler());
		handlers.put("mkcol", new MkColHandler());
		handlers.put("copy", new CopyHandler());
		handlers.put("move", new MoveHandler());
		handlers.put("lock", new LockHandler());
		handlers.put("unlock", new UnlockHandler());

		authHandler = new AuthHandler();
	}

}
