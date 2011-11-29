package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.obm.push.protocol.data.IntEncoder;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.push.wbxml.WBXmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ResponderImpl implements Responder {

	public static class Factory {
		
		private final IntEncoder intEncoder;
		private final WBXMLTools wbxmlTools;

		@Inject
		private Factory(IntEncoder intEncoder, WBXMLTools wbxmlTools) {
			this.intEncoder = intEncoder;
			this.wbxmlTools = wbxmlTools;
		}
		
		public Responder createResponder(HttpServletResponse resp) {
			return new ResponderImpl(resp, intEncoder, wbxmlTools);
		}
		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ResponderImpl.class);

	private HttpServletResponse resp;

	private final IntEncoder intEncoder;

	private final WBXMLTools wbxmlTools;
	
	/* package */ ResponderImpl(HttpServletResponse resp, IntEncoder intEncoder, WBXMLTools wbxmlTools) {
		this.resp = resp;
		this.intEncoder = intEncoder;
		this.wbxmlTools = wbxmlTools;
	}

	@Override
	public void sendResponse(String defaultNamespace, Document doc) {
		logger.debug("response: send response");
		if (logger.isDebugEnabled()) {
			DOMDumper.dumpXml(logger, doc);
		}
		
		try {
			byte[] wbxml = wbxmlTools.toWbxml(defaultNamespace, doc);
			Preconditions.checkNotNull(wbxml);
			
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
		Preconditions.checkNotNull(contentType);
		Preconditions.checkNotNull(file);
		
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

	public void sendMSSyncMultipartResponse(String defaultNamespace,
			Document doc, Collection<byte[]> files, boolean gzip) {
		Preconditions.checkNotNull(defaultNamespace);
		Preconditions.checkNotNull(doc);
		Preconditions.checkNotNull(files);
		
		resp.setContentType("application/vnd.ms-sync.multipart");
		OutputStream out = null;
		try {
			out = getOutputStream(gzip);
			byte[] wbxml = wbxmlTools.toWbxml(defaultNamespace, doc);

			List<byte[]> fileByte = constructByteArraysList(files, wbxml);
			int nbDoc = fileByte.size();

			int fileStart = 0;
			
			// Number of part
			fileStart += writeNbParts(out, nbDoc);
			fileStart = writePartsOffsets(out, fileByte, nbDoc, fileStart);
			writeByteArrays(out, fileByte);
			
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		} catch (WBXmlException e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	private OutputStream getOutputStream(boolean gzip) throws IOException {
		OutputStream out = null;
		try {
			out = resp.getOutputStream();
			if (gzip) {
				resp.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
				resp.addHeader(HttpHeaders.CONTENT_ENCODING, HttpHeaderValues.GZIP);
				return new GZIPOutputStream(out);
			} else {
				return out;
			}
		} catch (IOException t) {
			IOUtils.closeQuietly(out);
			throw t;
		} catch (RuntimeException e) {
			IOUtils.closeQuietly(out);
			throw e;
		}
	}

	private List<byte[]> constructByteArraysList(Collection<byte[]> files,
			byte[] wbxml) {

		List<byte[]> fileByte = new ArrayList<byte[]>(files.size());
		fileByte.add(0, wbxml);
		for (byte[] file : files) {
			fileByte.add(file);
		}
		return fileByte;
	}
	
	private int writeNbParts(OutputStream out, int nbDoc)
			throws IOException {
		
		byte[] nbDocByte = intEncoder.toByteArray(nbDoc);
		out.write(nbDocByte);
		return nbDocByte.length;
	}
	
	private int writePartsOffsets(OutputStream out, List<byte[]> fileByte,
			int nbDoc, int fileStart) throws IOException {
		
		// 8 bytes per document: 4 bytes for the start position and 4 bytes
		// for the length
		fileStart += nbDoc * 8;
		for (byte[] file : fileByte) {
			// Start of document
			out.write(intEncoder.toByteArray(fileStart));
			// length of the document
			out.write(intEncoder.toByteArray(file.length));
			fileStart += file.length;
		}
		return fileStart;
	}
	
	private void writeByteArrays(OutputStream out, List<byte[]> fileByte)
			throws IOException {

		for (byte[] file : fileByte) {
			logger.info("Adding file with length"+file.length);
			out.write(file);
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