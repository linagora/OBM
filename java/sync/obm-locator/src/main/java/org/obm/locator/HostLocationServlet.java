package org.obm.locator;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.locator.impl.LocatorDbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Locates OBM host IP addresses with a service, service_property, login@domain.
 * This call url should be /location/host/sync/obm_sync/login@domain
 * 
 */
public class HostLocationServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private LocatorDbHelper locatorDbHelper;
	
	@Override
	public void init() throws ServletException {
		super.init();
		locatorDbHelper = LocatorDbHelper.getInstance();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		logger.info("obm locator doGet servlet");

		String uri = getRequestUri(req);
		String[] split;
		try {
			split = splitUri(resp, uri);
		} catch (Exception e) {
			return;
		}

		String service = split[2];
		String property = split[3];
		String loginAtDomain = split[4];

		Set<String> ips = locatorDbHelper.findDomainHost(
				loginAtDomain, service, property);

		if (ips.size() > 0) {
			writeResponse(req, resp, ips);
		} else {
			sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
					"Could not find " + service + "/" + property + " for "
							+ loginAtDomain);
		}

	}

	private void writeResponse(HttpServletRequest req,
			HttpServletResponse resp, Set<String> ips) throws IOException {

		resp.setCharacterEncoding("utf-8");
		resp.setContentType("text/plain");

		StringBuilder returnedIps = new StringBuilder();
		returnedIps.append("[");

		StringBuilder sb = new StringBuilder();
		for (String ip : ips) {
			sb.append(ip);
			sb.append('\n');
			returnedIps.append(' ');
			returnedIps.append(ip);
		}
		returnedIps.append(" ]");

		byte[] b = sb.toString().getBytes();
		resp.setContentLength(b.length);

		ServletOutputStream out = resp.getOutputStream();
		out.write(b);
		out.close();

		if (logger.isInfoEnabled()) {
			logger.info("uri : " + req.getRequestURI() + " => returned "
					+ ips.size() + " IP(s) address(es) "
					+ returnedIps.toString());
		}
	}

	private String getRequestUri(HttpServletRequest req) {
		String uri = null;
		if (req != null) {
			uri = req.getPathInfo();
			if (uri.startsWith("/")) {
				uri = uri.substring(1);
			}
		}
		return uri;
	}

	private String[] splitUri(HttpServletResponse resp, String uri)
			throws Exception {

		String[] split = uri.split("/");
		if (split.length != 5) {
			sendErrorResponse(
					resp,
					HttpServletResponse.SC_FORBIDDEN,
					"uri should have 5 parts (got "
							+ split.length
							+ " parts "
							+ "(/location/host/sync/obm_sync/login@domain). The query was: "
							+ uri);
			throw new Exception("Format of Uri is incorrect");
		}
		return split;
	}

	private void sendErrorResponse(HttpServletResponse resp, int sc,
			String message) throws IOException {

		logger.error(message);
		resp.sendError(sc, message);
	}

}
