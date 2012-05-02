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

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.obm.push.wbxml.WBXmlException;
import org.obm.sync.push.client.commands.EmailDeleteSyncRequest;
import org.obm.sync.push.client.commands.EmailSyncCommand;
import org.obm.sync.push.client.commands.FolderSync;
import org.obm.sync.push.client.commands.GetItemEstimateEmailFolderCommand;
import org.obm.sync.push.client.commands.Options;
import org.obm.sync.push.client.commands.ProvisionStepOne;
import org.obm.sync.push.client.commands.ProvisionStepTwo;
import org.obm.sync.push.client.commands.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public abstract class OPClient {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected HttpClient hc;
	private MultiThreadedHttpConnectionManager mtManager;
	protected ProtocolVersion protocolVersion;
	protected AccountInfos ai;

	public abstract Document postXml(String namespace, Document doc, String cmd, String policyKey, boolean multipart)
			throws TransformerException, WBXmlException, IOException, HttpRequestException;
	
	protected OPClient(String loginAtDomain, String password, String devId,
			String devType, String userAgent, String url) {

		setProtocolVersion(ProtocolVersion.V121);
		this.ai = new AccountInfos(loginAtDomain, password, devId, devType,
				url, userAgent);

		this.hc = createHttpClient();
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
		mp.setDefaultMaxConnectionsPerHost(100);
		mp.setMaxTotalConnections(100);

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

	public SyncResponse syncEmail(String key, String collectionId) throws Exception {
		return run(new EmailSyncCommand(key, collectionId));
	}
	
	public SyncResponse syncEmail(String key, int collectionId) throws Exception {
		return run(new EmailSyncCommand(key, String.valueOf(collectionId)));
	}
	
	public SyncResponse sync(Document doc) throws Exception {
		return run(new Sync(doc));
	}

	public SyncResponse deleteEmail(String key, int collectionId, String uid) throws Exception {
		return run(new EmailDeleteSyncRequest(key, collectionId, uid));
	}
	
	public ProvisionResponse provisionStepOne() throws Exception {
		return run(new ProvisionStepOne());
	}

	public ProvisionResponse provisionStepTwo(long acknowledgingPolicyKey) throws Exception {
		return run(new ProvisionStepTwo(acknowledgingPolicyKey));
	}
	
	public GetItemEstimateSingleFolderResponse getItemEstimateOnMailFolder(String key, int collectionId) throws Exception {
		return run(new GetItemEstimateEmailFolderCommand(key, collectionId));
	}
	
	public Document postXml(String namespace, Document doc, String cmd)
			throws TransformerException, WBXmlException, IOException, HttpRequestException {
		return postXml(namespace, doc, cmd, null, false);
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
					return pm.getResponseBody();
				}
			} finally {
				pm.releaseConnection();
			}
		}
		return null;

	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

}
