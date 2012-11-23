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
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.obm.push.bean.DeviceId;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class XMLOPClient extends OPClient {

	public XMLOPClient(String loginAtDomain, String password, DeviceId devId,
			String devType, String userAgent, int port) {
		
		super(loginAtDomain, password, devId, devType, userAgent, buildServiceUrl(port));
	}

	private RequestEntity getRequestEntity(Document doc) throws UnsupportedEncodingException, TransformerException {
		try {
			String xmlData = DOMUtils.serialize(doc);
			return new ByteArrayRequestEntity(xmlData.getBytes("UTF8"), "text/xml");
		} catch (TransformerException e) {
			throw new TransformerException("Cannot serialize data to xml", e);
		}
	}

	private static String buildServiceUrl(int port) {
		return "http://localhost:" + port + "/Autodiscover/";
	}

	@Override
	public Document postXml(String namespace, Document doc, String cmd, String policyKey, boolean multipart)
			throws TransformerException, HttpException, IOException, HttpRequestException {
		
		DOMUtils.logDom(doc);
		
		RequestEntity requestEntity = getRequestEntity(doc);

		PostMethod pm = null;
		pm = new PostMethod(ai.getUrl() + "?User=" + ai.getLogin());
		pm.setRequestHeader("Content-Length", String.valueOf(requestEntity.getContentLength()));
		pm.setRequestEntity(requestEntity);
		pm.setRequestHeader("Content-Type", requestEntity.getContentType());
		pm.setRequestHeader("Authorization", ai.authValue());
		pm.setRequestHeader("Accept", "*/*");
		pm.setRequestHeader("Accept-Language", "fr-fr");
		pm.setRequestHeader("Connection", "keep-alive");
		
		try {
			int ret = 0;
			ret = hc.executeMethod(pm);
			Header[] hs = pm.getResponseHeaders();
			for (Header h: hs) {
				logger.error("head[" + h.getName() + "] => " + h.getValue());
			}
			if (ret != HttpStatus.SC_OK) {
				throw new HttpRequestException(ret, "method failed:\n" + pm.getStatusLine() + "\n" + pm.getResponseBodyAsString());
			} else {
				InputStream in = pm.getResponseBodyAsStream();
				Document docResponse = DOMUtils.parse(in);
				DOMUtils.logDom(docResponse);
				return docResponse;
			}
		} catch (SAXException e) {
			throw new TransformerException(e);
		} catch (FactoryConfigurationError e) {
			throw new TransformerException(e);
		} finally {
			pm.releaseConnection();
		}
	}
	
}
