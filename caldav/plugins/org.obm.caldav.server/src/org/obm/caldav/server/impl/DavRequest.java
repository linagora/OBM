package org.obm.caldav.server.impl;

import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import fr.aliasource.utils.DOMUtils;

public class DavRequest {

	private static final Log logger = LogFactory.getLog(DavRequest.class);

	private Document document;

	private HttpServletRequest req;

	@SuppressWarnings("unchecked")
	public DavRequest(HttpServletRequest req) {
		this.req = req;
		Enumeration headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String hn = (String) headerNames.nextElement();
			String val = req.getHeader(hn);
			logger.info(hn + ": " + val);
		}

		if (req.getHeader("Content-Type") != null) {
			try {
				InputStream in = req.getInputStream();
				document = DOMUtils.parse(in);
				DOMUtils.logDom(document);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public String getHeader(String string) {
		return req.getHeader(string);
	}

	public Document getDocument() {
		return document;
	}

	public String getHref() {
		return req.getRequestURL().toString();
	}

}
