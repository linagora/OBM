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
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.wbxml.WBXmlException;
import org.obm.sync.push.client.beans.AccountInfos;
import org.obm.sync.push.client.beans.Folder;
import org.obm.sync.push.client.beans.GetItemEstimateSingleFolderResponse;
import org.obm.sync.push.client.beans.ProtocolVersion;
import org.obm.sync.push.client.commands.DocumentProvider;
import org.obm.sync.push.client.commands.EmailDeleteSyncRequest;
import org.obm.sync.push.client.commands.EmailSyncCommand;
import org.obm.sync.push.client.commands.EmailSyncCommandWithWait;
import org.obm.sync.push.client.commands.EmailSyncNoOptionsCommand;
import org.obm.sync.push.client.commands.FolderSync;
import org.obm.sync.push.client.commands.GetItemEstimateEmailFolderCommand;
import org.obm.sync.push.client.commands.ItemOperationFetchCommand;
import org.obm.sync.push.client.commands.MeetingResponseCommand;
import org.obm.sync.push.client.commands.MoveItemsCommand;
import org.obm.sync.push.client.commands.MoveItemsCommand.Move;
import org.obm.sync.push.client.commands.Options;
import org.obm.sync.push.client.commands.PartialSyncCommand;
import org.obm.sync.push.client.commands.PingCommand;
import org.obm.sync.push.client.commands.ProvisionStepOne;
import org.obm.sync.push.client.commands.ProvisionStepTwo;
import org.obm.sync.push.client.commands.SimpleSyncCommand;
import org.obm.sync.push.client.commands.Sync;
import org.obm.sync.push.client.commands.SyncWithCommand;
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
	
	protected OPClient(String loginAtDomain, String password, DeviceId devId,
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

	public FolderSyncResponse folderSync(SyncKey key) throws Exception {
		return run(new FolderSync(key));
	}

	public SyncResponse initialSync(Folder... folders) throws Exception {
		return run(new Sync(folders));
	}
	
	public SyncResponse partialSync() throws Exception {
		return run(new PartialSyncCommand());
	}

	public SyncResponse syncEmail(SyncKey key, String collectionId, FilterType filterType, int windowSize) throws Exception {
		return run(new EmailSyncCommand(key, collectionId, filterType, windowSize));
	}

	public SyncResponse syncEmailWithWait(SyncKey key, String collectionId, FilterType filterType, int windowSize) throws Exception {
		return run(new EmailSyncCommandWithWait(key, collectionId, filterType, windowSize));
	}
	
	public SyncResponse syncWithCommand(SyncKey key, String collectionId, SyncCommand command, String serverId) throws Exception {
		return run(new SyncWithCommand(key, collectionId, command, serverId));
	}

	public SyncResponse syncWithoutOptions(SyncKey key, String collectionId) throws Exception {
		return run(new EmailSyncNoOptionsCommand(key, collectionId));
	}
	
	public SyncResponse syncEmail(SyncKey key, int collectionId, FilterType filterType, int windowSize) throws Exception {
		return run(new EmailSyncCommand(key, String.valueOf(collectionId), filterType, windowSize));
	}
	
	public SyncResponse sync(DocumentProvider template) throws Exception {
		return run(new Sync(template));
	}
	
	public SyncResponse sync(SyncKey syncKey, int collectionId, PIMDataType type) throws Exception {
		return run(new SimpleSyncCommand(syncKey, collectionId, type));
	}

	public SyncResponse deleteEmail(SyncKey key, int collectionId, String uid) throws Exception {
		return run(new EmailDeleteSyncRequest(key, collectionId, uid));
	}
	
	public ProvisionResponse provisionStepOne() throws Exception {
		return run(new ProvisionStepOne());
	}

	public ProvisionResponse provisionStepTwo(long acknowledgingPolicyKey) throws Exception {
		return run(new ProvisionStepTwo(acknowledgingPolicyKey));
	}
	
	public GetItemEstimateSingleFolderResponse getItemEstimateOnMailFolder(SyncKey key, int collectionId) throws Exception {
		return run(new GetItemEstimateEmailFolderCommand(key, collectionId));
	}
	
	public GetItemEstimateSingleFolderResponse getItemEstimateOnMailFolder(SyncKey key, FilterType filterType, int collectionId) throws Exception {
		return run(new GetItemEstimateEmailFolderCommand(key, filterType, collectionId));
	}

	public ItemOperationResponse itemOperationFetch(int collectionId, String...serverId) throws Exception {
		return run(new ItemOperationFetchCommand(collectionId, serverId));
	}

	public ItemOperationResponse itemOperationFetch(int collectionId, MSEmailBodyType bodyType, String...serverId) throws Exception {
		return run(new ItemOperationFetchCommand(collectionId, bodyType, serverId));
	}

	public MoveItemsResponse moveItems(Move...moves) throws Exception {
		return run(new MoveItemsCommand(moves));
	}
	
	public MeetingHandlerResponse meetingResponse(String collectionId, String serverId) throws Exception {
		return run(new MeetingResponseCommand(collectionId, serverId));
	}

	public PingResponse ping(String inboxCollectionIdAsString, long hearbeat) throws Exception {
		return run(new PingCommand(inboxCollectionIdAsString, hearbeat));
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
