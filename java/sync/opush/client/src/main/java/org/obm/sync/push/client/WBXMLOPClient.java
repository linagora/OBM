package org.obm.sync.push.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;


public class WBXMLOPClient extends OPClient {

	private final WBXMLTools wbxmlTools;

	public WBXMLOPClient(String loginAtDomain, String password, String devId,
			String devType, String userAgent, int port, WBXMLTools wbxmlTools) {

		super(loginAtDomain, password, devId, devType, userAgent, buildServiceUrl(port));
		this.wbxmlTools = wbxmlTools;
	}

	private ByteArrayRequestEntity getRequestEntity(String namespace, Document doc) throws Exception {
		byte[] wbxml = wbxmlTools.toWbxml(namespace, doc);
		return new ByteArrayRequestEntity(wbxml, "application/vnd.ms-sync.wbxml");
	}

	@Override
	public Document postXml(String namespace, Document doc, String cmd,
			String policyKey, boolean multipart) throws Exception {

		DOMUtils.logDom(doc);

		RequestEntity requestEntity = getRequestEntity(namespace, doc);
		
		PostMethod pm = null;
		pm = new PostMethod(ai.getUrl() + "?User=" + ai.getLogin()
				+ "&DeviceId=" + ai.getDevId() + "&DeviceType="
				+ ai.getDevType() + "&Cmd=" + cmd);
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
			} else {
				InputStream is = pm.getResponseBodyAsStream();
				File localCopy = File.createTempFile("pushresp_", ".bin");
				FileUtils.transfer(is, new FileOutputStream(localCopy), true);
				logger.info("binary response stored in "
						+ localCopy.getAbsolutePath());

				InputStream in = new FileInputStream(localCopy);
				if (pm.getResponseHeader("Content-Encoding") != null
						&& pm.getResponseHeader("Content-Encoding").getValue()
								.contains("gzip")) {
					in = new GZIPInputStream(in);
				}
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				FileUtils.transfer(in, out, true);
				if (pm.getResponseHeader("Content-Type") != null
						&& pm.getResponseHeader("Content-Type").getValue()
								.contains("application/vnd.ms-sync.multipart")) {
					byte[] all = out.toByteArray();
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
				} else if (out.toByteArray().length > 0) {
					xml = wbxmlTools.toXml(out.toByteArray());
					DOMUtils.logDom(xml);
				}
			}
		} finally {
			pm.releaseConnection();
		}
		return xml;
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
