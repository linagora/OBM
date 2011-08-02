package org.obm.push.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.obm.push.protocol.logging.TechnicalLogType;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.w3c.dom.Document;

public class Responder {

	private static final Logger logger = LoggerFactory
			.getLogger(Responder.class);

	private HttpServletResponse resp;

	public Responder(HttpServletResponse resp) {
		this.resp = resp;
	}

	public void sendResponse(String defaultNamespace, Document doc) throws IOException {
		if (logger.isInfoEnabled()) {
			Marker asXmlResponseMarker = TechnicalLogType.ACTIVE_SYNC_RESPONSE.getMarker();
			DOMDumper.dumpXml(logger, asXmlResponseMarker, doc);
		}
		byte[] wbxml = WBXMLTools.toWbxml(defaultNamespace, doc);
		resp.setContentType("application/vnd.ms-sync.wbxml");
		resp.setContentLength(wbxml.length);
		ServletOutputStream out = resp.getOutputStream();
		out.write(wbxml);
		out.flush();
		out.close();
	}
	
	public void sendResponseFile(String contentType, InputStream file)
			throws IOException {
		byte[] b = FileUtils.streamBytes(file, false);
		resp.setContentType(contentType);
		resp.setContentLength(b.length);
		ServletOutputStream out = resp.getOutputStream();
		out.write(b);
		out.flush();
		out.close();
		resp.setStatus(200);
	}

	public void sendError(int statusCode) throws IOException {
		resp.sendError(statusCode);
	}

	public void sendNoChangeResponse() {
		logger.warn("must inform the device that nothing changed");
	}

	public void sendMSSyncMultipartResponse(String defaultNamespace,
			Document doc, List<InputStream> files, boolean gzip)
			throws IOException {
		if (logger.isInfoEnabled()) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				DOMUtils.serialise(doc, out, true);
				Marker asXmlResponseMarker = MarkerFactory.getMarker("ActiveSyncXmlResponse");
				logger.info(asXmlResponseMarker, out.toString());
			} catch (TransformerException e) {
			}
		}
		resp.setContentType("application/vnd.ms-sync.multipart");
		OutputStream out = resp.getOutputStream();
		if (gzip) {
			resp.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
			resp.addHeader(HttpHeaders.CONTENT_ENCODING, HttpHeaderValues.GZIP);
			out = new GZIPOutputStream(out);
		}
		try {
			int fileStart = 0;
			List<byte[]> fileByte = new ArrayList<byte[]>(files.size());
			byte[] wbxml = WBXMLTools.toWbxml(defaultNamespace, doc);
			fileByte.add(0, wbxml);
			for (InputStream is : files) {
				fileByte.add(FileUtils.streamBytes(is, true));
			}
			int nbDoc = fileByte.size();

			// Number of part
			byte[] nbDocByte = intToInverseByteArray(nbDoc);
			out.write(nbDocByte);
			fileStart += nbDocByte.length;

			// 8 bytes per document: 4 bytes for the start position and 4 bytes
			// for the length
			fileStart += nbDoc * 8;
			for (byte[] file : fileByte) {
				// Start of document
				out.write(intToInverseByteArray(fileStart));
				// length of the document
				out.write(intToInverseByteArray(file.length));
				fileStart += file.length;
			}
			for (byte[] file : fileByte) {
				out.write(file);
				logger.info("Adding file with length" + file.length);
			}

		} finally {
			out.flush();
			out.close();
		}
	}

	private byte[] intToInverseByteArray(final int integer) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeInt(integer);
		dos.flush();
		byte[] intByte = bos.toByteArray();
		byte[] inverse = new byte[4];
		int in = intByte.length - 1;
		for (int i = 0; i < inverse.length; i++) {
			inverse[i] = intByte[in--];
		}
		return inverse;
	}

}
