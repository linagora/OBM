/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.sync.push.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.DeviceId;
import org.obm.push.utils.DOMUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.push.wbxml.WBXmlException;
import org.w3c.dom.Document;

import com.google.common.io.ByteStreams;


public class WBXMLOPClient extends OPClient {

	private final WBXMLTools wbxmlTools;
	
	public WBXMLOPClient(String loginAtDomain, String password,
			DeviceId devId, String devType, String userAgent, String serverAddress, int port, String webApp, WBXMLTools wbxmlTools, ProtocolVersion protocolVersion) {

		this(new PoolingHttpClientBuilder(), loginAtDomain, password, devId, devType, userAgent, buildServiceUrl(serverAddress, port, webApp), wbxmlTools, protocolVersion);
	}

	public WBXMLOPClient(HttpClientBuilder httpClientBuilder, String loginAtDomain, String password,
			DeviceId devId, String devType, String userAgent, String serviceUrl, WBXMLTools wbxmlTools, ProtocolVersion protocolVersion) {

		super(httpClientBuilder, loginAtDomain, password, devId, devType, userAgent, serviceUrl, protocolVersion);
		this.wbxmlTools = wbxmlTools;
	}
	

	private ByteArrayEntity getRequestEntity(String namespace, Document doc) throws WBXmlException, IOException {
		byte[] wbxml = wbxmlTools.toWbxml(namespace, doc);
		return new ByteArrayEntity(wbxml, ContentType.create("application/vnd.ms-sync.wbxml"));
	}

	@Override
	public Document postXml(String namespace, Document doc, String cmd, String policyKey, boolean multipart)
			throws TransformerException, WBXmlException, IOException, HttpRequestException {

		DOMUtils.logDom(doc);

		ByteArrayEntity requestEntity = getRequestEntity(namespace, doc);
		
		HttpPost request = new HttpPost(buildUrl(ai.getUrl(), ai.getLogin(),
				ai.getDevId(), ai.getDevType(), cmd));
		request.setHeaders(new Header[] { new BasicHeader("Content-Type", requestEntity.getContentType().getValue()),
				new BasicHeader("Authorization", ai.authValue()),
				new BasicHeader("User-Agent", ai.getUserAgent()),
				new BasicHeader("Ms-Asprotocolversion", protocolVersion.asSpecificationValue()),
				new BasicHeader("Accept", "*/*"),
				new BasicHeader("Accept-Language", "fr-fr"),
				new BasicHeader("Connection", "keep-alive")
				});
		request.setEntity(requestEntity);
		
		if (multipart) {
			request.addHeader(new BasicHeader("MS-ASAcceptMultiPart", "T"));
			request.addHeader(new BasicHeader("Accept-Encoding", "gzip"));
		}

		if (policyKey != null) {
			request.addHeader(new BasicHeader("X-MS-PolicyKey", policyKey));
		}

		Document xml = null;
		try {
			HttpResponse response = hc.execute(request);
			StatusLine statusLine = response.getStatusLine();
			Header[] hs = response.getAllHeaders();
			for (Header h : hs) {
				logger.error("head[" + h.getName() + "] => "
						+ h.getValue());
			}
			int statusCode = statusLine.getStatusCode();
			HttpEntity entity = response.getEntity();
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("method failed:{}\n{}\n",  statusLine, entity);
				throw new HttpRequestException(statusCode);
			} else {
				byte[] responseBytes = getResponse(response);
				if (response.getFirstHeader("Content-Type") != null
						&& response.getFirstHeader("Content-Type").getValue()
								.contains("application/vnd.ms-sync.multipart")) {
					byte[] all = responseBytes;
					int idx = 0;
					byte[] buffer = new byte[4];
					for (int i = 0; i < buffer.length; i++) {
						buffer[i] = all[idx++];
					}
					int nbPart = byteArrayToInt(buffer);

					for (int p = 0; p < nbPart; p++) {
						for (int i = 0; i < buffer.length; i++) {
							buffer[i] = all[idx++];
						}
						int start = byteArrayToInt(buffer);

						for (int i = 0; i < buffer.length; i++) {
							buffer[i] = all[idx++];
						}
						int length = byteArrayToInt(buffer);

						byte[] value = new byte[length];
						for (int j = 0; j < length; j++) {
							value[j] = all[start++];
						}
						if (p == 0) {
							xml = wbxmlTools.toXml(value);
							DOMUtils.logDom(xml);
						} else {
							String file = new String(value);
							logger.info("File: " + file);
						}

					}
				} else if (entity.getContentLength() > 0) {
					try {
						xml = wbxmlTools.toXml(responseBytes);
						DOMUtils.logDom(xml);
					} finally {
						EntityUtils.consume(entity);
					}
				}
			}
		} finally {
			request.releaseConnection();
		}
		return xml;
	}

	private byte[] getResponse(HttpResponse response) throws IOException {
		InputStream responseStream = null;
		try {
			responseStream = getResponseStream(response);
			return ByteStreams.toByteArray(responseStream);
		} finally {
			IOUtils.closeQuietly(responseStream);
		}
	}

	private InputStream getResponseStream(HttpResponse response) throws IOException {
		InputStream is = response.getEntity().getContent();
		if (response.getFirstHeader("Content-Encoding") != null
				&& response.getFirstHeader("Content-Encoding").getValue().contains("gzip")) {
			return new GZIPInputStream(is);
		} else {
			return is;
		}
	}
	
	private final int byteArrayToInt(byte[] b) {
		byte[] inverse = new byte[b.length];
		int in = b.length - 1;
		for (int i = 0; i < b.length; i++) {
			inverse[in--] = b[i];
		}
		return (inverse[0] << 24) + ((inverse[1] & 0xFF) << 16)
				+ ((inverse[2] & 0xFF) << 8) + (inverse[3] & 0xFF);
	}
	
	private static String buildServiceUrl(String serverAddress, int port, String webApp) {
		return "http://" + serverAddress + ":" + port + webApp;
	}
	
}
