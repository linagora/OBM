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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.obm.push.bean.DeviceId;
import org.obm.push.utils.DOMUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.push.wbxml.WBXmlException;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;


public class WBXMLOPClient extends OPClient {

	private final WBXMLTools wbxmlTools;

	public WBXMLOPClient(String loginAtDomain, String password, DeviceId devId,
			String devType, String userAgent, int port, WBXMLTools wbxmlTools) {

		super(loginAtDomain, password, devId, devType, userAgent, buildServiceUrl(port));
		this.wbxmlTools = wbxmlTools;
	}

	private ByteArrayRequestEntity getRequestEntity(String namespace, Document doc) throws WBXmlException, IOException {
		byte[] wbxml = wbxmlTools.toWbxml(namespace, doc);
		return new ByteArrayRequestEntity(wbxml, "application/vnd.ms-sync.wbxml");
	}

	@Override
	public Document postXml(String namespace, Document doc, String cmd, String policyKey, boolean multipart)
			throws TransformerException, WBXmlException, IOException, HttpRequestException {

		DOMUtils.logDom(doc);

		RequestEntity requestEntity = getRequestEntity(namespace, doc);
		
		PostMethod pm = null;
		pm = new PostMethod(buildUrl(ai.getUrl(), ai.getLogin(),
				ai.getDevId(), ai.getDevType(), cmd));
		pm.setRequestHeader("Content-Length", String.valueOf(requestEntity.getContentLength()));
		pm.setRequestEntity(requestEntity);
		pm.setRequestHeader("Content-Type", requestEntity.getContentType());
		pm.setRequestHeader("Authorization", ai.authValue());
		pm.setRequestHeader("User-Agent", ai.getUserAgent());
		pm.setRequestHeader("Ms-Asprotocolversion", protocolVersion.toString());
		pm.setRequestHeader("Accept", "*/*");
		pm.setRequestHeader("Accept-Language", "fr-fr");
		pm.setRequestHeader("Connection", "keep-alive");
		if (multipart) {
			pm.setRequestHeader("MS-ASAcceptMultiPart", "T");
			pm.setRequestHeader("Accept-Encoding", "gzip");
		}

		if (policyKey != null) {
			pm.setRequestHeader("X-MS-PolicyKey", policyKey);
		}

		Document xml = null;
		try {
			int ret = 0;
			ret = hc.executeMethod(pm);
			Header[] hs = pm.getResponseHeaders();
			for (Header h : hs) {
				logger.error("head[" + h.getName() + "] => "
						+ h.getValue());
			}
			if (ret != HttpStatus.SC_OK) {
				logger.error("method failed:\n" + pm.getStatusLine()
						+ "\n" + pm.getResponseBodyAsString());
				throw new HttpRequestException(ret);
			} else {
				byte[] response = getResponse(pm);
				if (pm.getResponseHeader("Content-Type") != null
						&& pm.getResponseHeader("Content-Type").getValue()
								.contains("application/vnd.ms-sync.multipart")) {
					byte[] all = response;
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
				} else if (response.length > 0) {
					xml = wbxmlTools.toXml(response);
					DOMUtils.logDom(xml);
				}
			}
		} finally {
			pm.releaseConnection();
		}
		return xml;
	}

	@VisibleForTesting
	static String buildUrl(String url, String login, DeviceId deviceId, String devType, String cmd) {
		return url + "?User=" + login
				+ "&DeviceId=" + deviceId.getDeviceId()
				+ "&DeviceType=" + devType
				+ "&Cmd=" + cmd;
	}

	private byte[] getResponse(PostMethod pm) throws IOException {
		InputStream responseStream = null;
		try {
			responseStream = getResponseStream(pm);
			return ByteStreams.toByteArray(responseStream);
		} finally {
			Closeables.closeQuietly(responseStream);
		}
	}

	private InputStream getResponseStream(PostMethod pm) throws IOException {
		InputStream is = pm.getResponseBodyAsStream();
		if (pm.getResponseHeader("Content-Encoding") != null
				&& pm.getResponseHeader("Content-Encoding").getValue().contains("gzip")) {
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
	
	private static String buildServiceUrl(int port) {
		return "http://localhost:" + port + "/ActiveSyncServlet/";
	}
	
}
