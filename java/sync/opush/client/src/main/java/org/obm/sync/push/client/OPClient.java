package org.obm.sync.push.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.sync.push.client.commands.FolderSync;
import org.obm.sync.push.client.commands.Options;
import org.obm.sync.push.client.commands.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class OPClient {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private HttpClient hc;
	private MultiThreadedHttpConnectionManager mtManager;
	private ProtocolVersion protocolVersion;
	private AccountInfos ai;

	private WBXMLTools wbxmlTools;

	static {
		XTrustProvider.install();
	}

	public OPClient(String loginAtDomain, String password, String devId,
			String devType, String userAgent, String url, WBXMLTools wbxmlTools) {

		setProtocolVersion(ProtocolVersion.V121);
		this.ai = new AccountInfos(loginAtDomain, password, devId, devType,
				url, userAgent);

		this.hc = createHttpClient();
		this.wbxmlTools = wbxmlTools;
	}

	public void destroy() {
		mtManager.shutdown();
	}

	private <T> T run(IEasCommand<T> cmd) throws Exception {
		return cmd.run(ai, this, hc);
	}

	private HttpClient createHttpClient() {
		this.mtManager = new MultiThreadedHttpConnectionManager();
		HttpClient ret = new HttpClient(mtManager);
		HttpConnectionManagerParams mp = ret.getHttpConnectionManager()
				.getParams();
		mp.setDefaultMaxConnectionsPerHost(8);
		mp.setMaxTotalConnections(16);

		return ret;
	}

	public void options() throws Exception {
		run(new Options());
	}

	public FolderSyncResponse folderSync(String key) throws Exception {
		return run(new FolderSync(key));
	}

	public SyncResponse initialSync(Folder[] folders) throws Exception {
		return run(new Sync(folders));
	}

	public SyncResponse sync(Document doc) throws Exception {
		return run(new Sync(doc));
	}

	public Document postXml(String namespace, Document doc, String cmd)
			throws Exception {
		return postXml(namespace, doc, cmd, null, false);
	}

	@SuppressWarnings("deprecation")
	public Document postXml(String namespace, Document doc, String cmd,
			String policyKey, boolean multipart) throws Exception {

		DOMUtils.logDom(doc);

		byte[] data = wbxmlTools.toWbxml(namespace, doc);
		PostMethod pm = null;
		pm = new PostMethod(ai.getUrl() + "?User=" + ai.getLogin()
				+ "&DeviceId=" + ai.getDevId() + "&DeviceType="
				+ ai.getDevType() + "&Cmd=" + cmd);
		pm.setRequestHeader("Content-Length", "" + data.length);
		pm.setRequestBody(new ByteArrayInputStream(data));
		pm.setRequestHeader("Content-Type", "application/vnd.ms-sync.wbxml");
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

	public byte[] postGetAttachment(String attachmentName) throws Exception {
		PostMethod pm = new PostMethod(ai.getUrl() + "?User=" + ai.getLogin()
				+ "&DeviceId=" + ai.getDevId() + "&DeviceType="
				+ ai.getDevType() + "&Cmd=GetAttachment&AttachmentName="
				+ attachmentName);
		pm.setRequestHeader("Authorization", ai.authValue());
		pm.setRequestHeader("User-Agent", ai.getUserAgent());
		pm.setRequestHeader("Ms-Asprotocolversion", protocolVersion.toString());
		pm.setRequestHeader("Accept", "*/*");
		pm.setRequestHeader("Accept-Language", "fr-fr");
		pm.setRequestHeader("Connection", "keep-alive");

		synchronized (hc) {
			try {
				int ret = hc.executeMethod(pm);
				Header[] hs = pm.getResponseHeaders();
				for (Header h : hs) {
					logger.error("head[" + h.getName() + "] => "
							+ h.getValue());
				}
				if (ret != HttpStatus.SC_OK) {
					logger.error("method failed:\n" + pm.getStatusLine()
							+ "\n" + pm.getResponseBodyAsString());
				} else {
					for (Header h : pm.getResponseHeaders()) {
						logger.info(h.getName() + ": " + h.getValue());
					}
					InputStream is = pm.getResponseBodyAsStream();
					File localCopy = File.createTempFile("pushresp_", ".bin");
					FileUtils.transfer(is, new FileOutputStream(localCopy),
							true);
					logger.info("binary response stored in "
							+ localCopy.getAbsolutePath());

					FileInputStream in = new FileInputStream(localCopy);
					return FileUtils.streamBytes(in, true);
				}
			} finally {
				pm.releaseConnection();
			}
		}
		return null;

	}

	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

}
