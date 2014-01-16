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
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Future;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.TransformerException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.DeviceId;
import org.obm.push.utils.DOMUtils;
import org.obm.push.wbxml.WBXmlException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class XMLOPClient extends OPClient {

	public XMLOPClient(HttpClient httpClient, String loginAtDomain, String password, DeviceId devId,
			String devType, String userAgent, int port) {
		
		super(httpClient, loginAtDomain, password, devId, devType, userAgent, buildServiceUrl(port), ProtocolVersion.V121);
	}

	private ByteArrayEntity getRequestEntity(Document doc) throws UnsupportedEncodingException, TransformerException {
		try {
			String xmlData = DOMUtils.serialize(doc);
			return new ByteArrayEntity(xmlData.getBytes("UTF8"), ContentType.TEXT_XML);
		} catch (TransformerException e) {
			throw new TransformerException("Cannot serialize data to xml", e);
		}
	}

	private static String buildServiceUrl(int port) {
		return "http://localhost:" + port + "/Autodiscover/";
	}

	@Override
	public Document postXml(String namespace, Document doc, String cmd, String policyKey, boolean multipart)
			throws TransformerException, WBXmlException, IOException, HttpRequestException {
		
		DOMUtils.logDom(doc);
		
		ByteArrayEntity requestEntity = getRequestEntity(doc);

		HttpPost request = new HttpPost(ai.getUrl() + "?User=" + ai.getLogin());
		request.setHeaders(new Header[] {
				new BasicHeader("Content-Type", requestEntity.getContentType().getValue()),
				new BasicHeader("Authorization", ai.authValue()),
				new BasicHeader("Accept", "*/*"),
				new BasicHeader("Accept-Language", "fr-fr"),
				new BasicHeader("Connection", "keep-alive")
			});
		request.setEntity(requestEntity);
		
		try {
			HttpResponse response = hc.execute(request);
			StatusLine statusLine = response.getStatusLine();
			Header[] hs = response.getAllHeaders();
			for (Header h: hs) {
				logger.error("head[" + h.getName() + "] => " + h.getValue());
			}
			int statusCode = statusLine.getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("method failed:{}\n{}\n",  statusLine, response.getEntity());
				throw new HttpRequestException(statusCode);
			} else {
				InputStream in = response.getEntity().getContent();
				Document docResponse = DOMUtils.parse(in);
				DOMUtils.logDom(docResponse);
				return docResponse;
			}
		} catch (SAXException e) {
			throw new TransformerException(e);
		} catch (FactoryConfigurationError e) {
			throw new TransformerException(e);
		} finally {
			request.releaseConnection();
		}
	}

	@Override
	public <T> Future<T> postASyncXml(Async async, String namespace, Document doc, String cmd, String policyKey, boolean multipart, ResponseTransformer<T> documentHandler)
			throws TransformerException, WBXmlException, IOException, HttpRequestException {
		throw new RuntimeException("Not implements for XMLOPClient");
	}
}
