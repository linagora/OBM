package org.obm.caldav.server.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebdavServlet extends HttpServlet {

	private static final Log logger = LogFactory.getLog(WebdavServlet.class);

	private static final long serialVersionUID = 1410911584964336424L;

	private static final String METHOD_PROPFIND = "PROPFIND";
	private static final String METHOD_PROPPATCH = "PROPPATCH";
	private static final String METHOD_MKCOL = "MKCOL";
	private static final String METHOD_COPY = "COPY";
	private static final String METHOD_MOVE = "MOVE";
	private static final String METHOD_LOCK = "LOCK";
	private static final String METHOD_UNLOCK = "UNLOCK";

	/**
	 * Handles the special WebDAV methods.
	 */
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String method = req.getMethod();

		if (logger.isInfoEnabled()) {
			logger.info("[" + method + "] " + req.getRequestURI());
		}

		if (method.equals(METHOD_PROPFIND)) {
			doPropfind(req, resp);
		} else if (method.equals(METHOD_PROPPATCH)) {
			doProppatch(req, resp);
		} else if (method.equals(METHOD_MKCOL)) {
			doMkcol(req, resp);
		} else if (method.equals(METHOD_COPY)) {
			doCopy(req, resp);
		} else if (method.equals(METHOD_MOVE)) {
			doMove(req, resp);
		} else if (method.equals(METHOD_LOCK)) {
			doLock(req, resp);
		} else if (method.equals(METHOD_UNLOCK)) {
			doUnlock(req, resp);
		} else {
			super.service(req, resp);
		}

	}

	private void doUnlock(HttpServletRequest req, HttpServletResponse resp) {
	}

	private void doLock(HttpServletRequest req, HttpServletResponse resp) {
	}

	private void doMove(HttpServletRequest req, HttpServletResponse resp) {
	}

	private void doCopy(HttpServletRequest req, HttpServletResponse resp) {
	}

	private void doMkcol(HttpServletRequest req, HttpServletResponse resp) {
	}

	private void doProppatch(HttpServletRequest req, HttpServletResponse resp) {
	}

	private void doPropfind(HttpServletRequest req, HttpServletResponse resp) {
	}

}
