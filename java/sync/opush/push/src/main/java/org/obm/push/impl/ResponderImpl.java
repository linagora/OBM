package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.obm.push.protocol.data.IntEncoder;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.push.wbxml.WBXmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.inject.Inject;

public class ResponderImpl implements Responder {

	public static class Factory {
		
		private final IntEncoder intEncoder;

		@Inject
		private Factory(IntEncoder intEncoder) {
			this.intEncoder = intEncoder;
		}
		
		public Responder createResponder(HttpServletResponse resp) {
			return new ResponderImpl(resp, intEncoder);
		}
		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ResponderImpl.class);

	private HttpServletResponse resp;

	private final IntEncoder intEncoder;
	
	private ResponderImpl(HttpServletResponse resp, IntEncoder intEncoder) {
		this.resp = resp;
		this.intEncoder = intEncoder;
	}

	@Override
	public void sendResponse(String defaultNamespace, Document doc) {
		logger.debug("response: send response");
		if (logger.isDebugEnabled()) {
			DOMDumper.dumpXml(logger, doc);
		}
		
		try {
			byte[] wbxml = WBXMLTools.toWbxml(defaultNamespace, doc);
			resp.setContentType("application/vnd.ms-sync.wbxml");
			resp.setContentLength(wbxml.length);
			
			ServletOutputStream out = resp.getOutputStream();
			out.write(wbxml);
			out.flush();
			out.close();	
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		} catch (WBXmlException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void sendResponseFile(String contentType, InputStream file) {
		logger.debug("response: send file");
		try {
			byte[] b = FileUtils.streamBytes(file, false);
			resp.setContentType(contentType);
			resp.setContentLength(b.length);
			ServletOutputStream out = resp.getOutputStream();
			out.write(b);
			out.flush();
			out.close();
			resp.setStatus(HttpServletResponse.SC_OK);	
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendError(int statusCode) {
		logger.debug("response: send error");
		try {
			resp.sendError(statusCode);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendNoChangeResponse() {
		logger.debug("response: send no changes");
	}
	
}