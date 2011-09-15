package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class ResponderImpl implements Responder {

	private static final Logger logger = LoggerFactory.getLogger(ResponderImpl.class);

	private HttpServletResponse resp;

	public ResponderImpl(HttpServletResponse resp) {
		this.resp = resp;
	}

	@Override
	public void sendResponse(String defaultNamespace, Document doc) {
		try {
			if (logger.isDebugEnabled()) {
				DOMDumper.dumpXml(logger, doc);
			}
			byte[] wbxml = WBXMLTools.toWbxml(defaultNamespace, doc);
			resp.setContentType("application/vnd.ms-sync.wbxml");
			resp.setContentLength(wbxml.length);
			ServletOutputStream out = resp.getOutputStream();
			out.write(wbxml);
			out.flush();
			out.close();	
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void sendResponseFile(String contentType, InputStream file) {
		try {
			byte[] b = FileUtils.streamBytes(file, false);
			resp.setContentType(contentType);
			resp.setContentLength(b.length);
			ServletOutputStream out = resp.getOutputStream();
			out.write(b);
			out.flush();
			out.close();
			resp.setStatus(200);	
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendError(int statusCode) {
		try {
			resp.sendError(statusCode);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendNoChangeResponse() {
		logger.warn("must inform the device that nothing changed");
	}
	
}