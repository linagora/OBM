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
			handler.process(token, req, resp);
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
		
		authHandler = new AuthHandler();
	}

}
