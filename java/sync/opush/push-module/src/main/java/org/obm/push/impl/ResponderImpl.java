/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.utils.IntEncoder;
import org.obm.push.utils.IntEncoder.Capacity;
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
		private final DOMDumper domDumper;

		@Inject
		private Factory(IntEncoder intEncoder, WBXMLTools wbxmlTools, DOMDumper domDumper) {
			this.intEncoder = intEncoder;
			this.wbxmlTools = wbxmlTools;
			this.domDumper = domDumper;
		}
		
		public Responder createResponder(HttpServletRequest req, HttpServletResponse resp) {
			return new ResponderImpl(req.getContentType(), resp, intEncoder, wbxmlTools, domDumper);
		}
		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ResponderImpl.class);
	
	private final String contentType;
	private final HttpServletResponse resp;
	private final IntEncoder intEncoder;
	private final WBXMLTools wbxmlTools;
	private final DOMDumper domDumper;

	
	/* package */ ResponderImpl(String contentType, HttpServletResponse resp, IntEncoder intEncoder, WBXMLTools wbxmlTools, 
			DOMDumper domDumper) {
		
		this.contentType = contentType;
		this.resp = resp;
		this.intEncoder = intEncoder;
		this.wbxmlTools = wbxmlTools;
		this.domDumper = domDumper;
	}

	@Override
	public void sendWBXMLResponse(String defaultNamespace, Document doc) {
		logger.debug("response: send response");
		domDumper.dumpXml(doc, WBXMLTools.XML_SERIALIZING_VERSION);
		
		try {
			byte[] wbxml = wbxmlTools.toWbxml(defaultNamespace, doc);
			writeData(wbxml, contentType);	
		} catch (WBXmlException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	@Override
	public void sendXMLResponse(String defaultNamespace, Document doc) {
		domDumper.dumpXml(doc);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMUtils.serialize(doc, out);
			byte[] ret = out.toByteArray();
			writeData(ret, "text/xml");	
		} catch (TransformerException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	private void writeData(byte[] data, String type) throws IOException {
		resp.setContentType(type);
		resp.setContentLength(data.length);
		ServletOutputStream out = resp.getOutputStream();
		out.write(data);
		out.flush();
		out.close();
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
		
		byte[] nbDocByte = intEncoder.capacity(Capacity.FOUR).toByteArray(nbDoc);
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
			out.write(intEncoder.capacity(Capacity.FOUR).toByteArray(fileStart));
			// length of the document
			out.write(intEncoder.capacity(Capacity.FOUR).toByteArray(file.length));
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