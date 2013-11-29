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
package org.obm.sync.push.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;


public class WBXMLOPClient extends OPClient {

	private final WBXMLTools wbxmlTools;
	
	public WBXMLOPClient(HttpClient httpClient, String loginAtDomain, String password,
			DeviceId devId, String devType, String userAgent, String serverAddress, int port, String webApp, WBXMLTools wbxmlTools, ProtocolVersion protocolVersion) {

		this(httpClient, loginAtDomain, password, devId, devType, userAgent, buildServiceUrl(serverAddress, port, webApp), wbxmlTools, protocolVersion);
	}

	public WBXMLOPClient(HttpClient httpClient, String loginAtDomain, String password,
			DeviceId devId, String devType, String userAgent, String serviceUrl, WBXMLTools wbxmlTools, ProtocolVersion protocolVersion) {

		super(httpClient, loginAtDomain, password, devId, devType, userAgent, serviceUrl, protocolVersion);
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

		Request request = buildRequest(namespace, doc, cmd, policyKey, multipart);
		
		HttpResponse response = Executor.newInstance(hc).execute(request).returnResponse();
		return parseResponse(response);
	}

	@Override
	public <T> Future<T> postASyncXml(Async async, String namespace, Document doc, String cmd, String policyKey, boolean multipart, 
				final ResponseTransformer<T> responseTransformer)
			throws TransformerException, WBXmlException, IOException, HttpRequestException {

		DOMUtils.logDom(doc);

		Request request = buildRequest(namespace, doc, cmd, policyKey, multipart);

		return async.execute(request, new ResponseHandler<T>() {
			@Override
			public T handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				Document document = new DocumentResponseHandler().handleResponse(response);
				return responseTransformer.parse(document);
			}
		});
	}
	
	private class DocumentResponseHandler implements ResponseHandler<Document> {

		@Override
		public Document handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			try {
				return parseResponse(response);
			} catch (HttpRequestException e) {
				Throwables.propagate(e);
			} catch (TransformerException e) {
				Throwables.propagate(e);
			}
			return null;
		}
	}

	private Document parseResponse(HttpResponse response) throws HttpRequestException, IOException, TransformerException {
		Document xml = null;
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
						String file = new String(value, Charsets.UTF_8);
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
		return xml;
	}

	private Request buildRequest(String namespace, Document doc, String cmd, String policyKey, boolean multipart) throws WBXmlException, IOException {
		ByteArrayEntity requestEntity = getRequestEntity(namespace, doc);
		
		Request request = Request.Post(buildUrl(ai.getUrl(), ai.getLogin(),
				ai.getDevId(), ai.getDevType(), cmd))
			.addHeader(new BasicHeader("Content-Type", requestEntity.getContentType().getValue()))
			.addHeader(new BasicHeader("Authorization", ai.authValue()))
			.addHeader(new BasicHeader("User-Agent", ai.getUserAgent()))
			.addHeader(new BasicHeader("Ms-Asprotocolversion", protocolVersion.asSpecificationValue()))
			.addHeader(new BasicHeader("Accept", "*/*"))
			.addHeader(new BasicHeader("Accept-Language", "fr-fr"))
			.addHeader(new BasicHeader("Connection", "keep-alive"))
			.body(requestEntity);
		
		if (multipart) {
			request.addHeader(new BasicHeader("MS-ASAcceptMultiPart", "T"));
			request.addHeader(new BasicHeader("Accept-Encoding", "gzip"));
		}

		if (policyKey != null) {
			request.addHeader(new BasicHeader("X-MS-PolicyKey", policyKey));
		}
		return request;
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
