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
import java.util.concurrent.Future;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.protocol.PingProtocol;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.wbxml.WBXmlException;
import org.obm.sync.push.client.beans.AccountInfos;
import org.obm.sync.push.client.beans.Folder;
import org.obm.sync.push.client.beans.GetItemEstimateSingleFolderResponse;
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
import org.obm.sync.push.client.commands.SmartEmailCommand.SmartForward;
import org.obm.sync.push.client.commands.SmartEmailCommand.SmartReply;
import org.obm.sync.push.client.commands.Sync;
import org.obm.sync.push.client.commands.SyncWithCommand;
import org.obm.sync.push.client.commands.SyncWithDataCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public abstract class OPClient {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected HttpClient hc;
	protected ProtocolVersion protocolVersion;
	protected AccountInfos ai;

	public abstract Document postXml(String namespace, Document doc, String cmd, String policyKey, boolean multipart)
			throws TransformerException, WBXmlException, IOException, HttpRequestException;
	
	public abstract <T> Future<T> postASyncXml(Async async, String namespace, Document doc, String cmd, String policyKey, boolean multipart, ResponseTransformer<T> documentHandler)
			throws TransformerException, WBXmlException, IOException, HttpRequestException;
	
	protected OPClient(HttpClient httpClient, String loginAtDomain, String password,
			DeviceId devId, String devType, String userAgent, String url, ProtocolVersion protocolVersion) {

		setProtocolVersion(protocolVersion);
		this.ai = new AccountInfos(loginAtDomain, password, devId, devType, url, userAgent);
		this.hc = httpClient;
	}

	private <T> T run(IEasCommand<T> cmd) throws Exception {
		return cmd.run(ai, this, hc);
	}

	private <T> Future<T> runASync(Async async, IEasCommand<T> cmd) throws Exception {
		return cmd.runASync(ai, this, async);
	}

	public OptionsResponse options() throws Exception {
		return run(new Options());
	}

	public Boolean emailReply(byte[] emailData, int collectionId, ServerId serverId) throws Exception {
		return run(new SmartReply(emailData, collectionId, serverId));
	}

	public Boolean emailForward(byte[] emailData, int collectionId, ServerId serverId) throws Exception {
		return run(new SmartForward(emailData, collectionId, serverId));
	}

	public FolderSyncResponse folderSync(SyncKey key) throws Exception {
		return run(new FolderSync(key));
	}

	public SyncResponse initialSync(SyncDecoder decoder, Folder... folders) throws Exception {
		return run(new Sync(decoder, folders));
	}
	
	public SyncResponse partialSync(SyncDecoder decoder) throws Exception {
		return run(new PartialSyncCommand(decoder));
	}

	public SyncResponse sync(SyncDecoder decoder, SyncKey syncKey, Folder... folders) throws Exception {
		return run(new Sync(decoder, syncKey, folders));
	}
	
	public SyncResponse syncEmail(SyncDecoder decoder, SyncKey key, String collectionId, FilterType filterType, int windowSize) throws Exception {
		return run(new EmailSyncCommand(decoder, key, collectionId, filterType, windowSize));
	}

	public SyncResponse syncEmailWithWait(SyncDecoder decoder, SyncKey key, String collectionId, FilterType filterType, int windowSize) throws Exception {
		return run(new EmailSyncCommandWithWait(decoder, key, collectionId, filterType, windowSize));
	}
	
	public SyncResponse syncWithCommand(SyncDecoder decoder, SyncKey key, String collectionId, SyncCommand command, String serverId) throws Exception {
		return syncWithCommand(decoder, key, collectionId, command, serverId, null);
	}
	
	public SyncResponse syncWithCommand(SyncDecoder decoder, SyncKey key, String collectionId, SyncCommand command, String serverId, String clientId) throws Exception {
		return run(new SyncWithCommand(decoder, key, collectionId, command, serverId, clientId));
	}
	
	public SyncResponse syncWithCommand(SyncWithDataCommand.Factory factory, Device device, SyncKey key,
			String collectionId, SyncCommand command, String serverId, String clientId, IApplicationData data) throws Exception {
		return run(factory.create(key, collectionId, command, serverId, clientId, data, device));
	}

	public SyncResponse syncWithoutOptions(SyncDecoder decoder, SyncKey key, String collectionId) throws Exception {
		return run(new EmailSyncNoOptionsCommand(decoder, key, collectionId));
	}
	
	public SyncResponse syncEmail(SyncDecoder decoder, SyncKey key, int collectionId, FilterType filterType, int windowSize) throws Exception {
		return run(new EmailSyncCommand(decoder, key, String.valueOf(collectionId), filterType, windowSize));
	}
	
	public SyncResponse sync(SyncDecoder decoder, DocumentProvider template) throws Exception {
		return run(new Sync(decoder, template));
	}
	
	public SyncResponse sync(SyncDecoder decoder, SyncKey syncKey, int collectionId, PIMDataType type) throws Exception {
		return run(new SimpleSyncCommand(decoder, syncKey, collectionId, type));
	}

	public SyncResponse deleteEmail(SyncDecoder decoder, SyncKey key, int collectionId, String uid) throws Exception {
		return run(new EmailDeleteSyncRequest(decoder, key, collectionId, uid));
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

	public PingResponse ping(PingProtocol pingProtocol, String inboxCollectionIdAsString, long heartbeat) throws Exception {
		return run(new PingCommand(pingProtocol, inboxCollectionIdAsString, heartbeat));
	}

	public Future<PingResponse> pingASync(Async async, PingProtocol pingProtocol, String inboxCollectionIdAsString, long heartbeat) throws Exception {
		return runASync(async, new PingCommand(pingProtocol, inboxCollectionIdAsString, heartbeat));
	}
	
	public Document postXml(String namespace, Document doc, String cmd)
			throws TransformerException, WBXmlException, IOException, HttpRequestException {
		return postXml(namespace, doc, cmd, null, false);
	}

	public byte[] postGetAttachment(String attachmentName) throws Exception {
		String url = buildUrl(ai.getUrl(), ai.getLogin(), ai.getDevId(), ai.getDevType(), "GetAttachment", "&AttachmentName=" + attachmentName);
		HttpPost request = new HttpPost(url);
		request.setHeaders(new Header[] { new BasicHeader("Authorization", ai.authValue()),
				new BasicHeader("User-Agent", ai.getUserAgent()),
				new BasicHeader("Ms-Asprotocolversion", protocolVersion.asSpecificationValue()),
				new BasicHeader("Accept", "*/*"),
				new BasicHeader("Accept-Language", "fr-fr"),
				new BasicHeader("Connection", "keep-alive")
				});

		synchronized (hc) {
			try {
				HttpResponse response = hc.execute(request);
				StatusLine statusLine = response.getStatusLine();
				Header[] hs = response.getAllHeaders();
				for (Header h : hs) {
					logger.error("head[" + h.getName() + "] => "
							+ h.getValue());
				}
				if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
					logger.error("method failed:{}\n{}\n",  statusLine, response.getEntity());
				} else {
					for (Header h : hs) {
						logger.info(h.getName() + ": " + h.getValue());
					}
					return IOUtils.toByteArray(response.getEntity().getContent());
				}
			} finally {
				request.releaseConnection();
			}
		}
		return null;

	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public String buildUrl(String url, String login, DeviceId deviceId, String devType, String cmd, String extra) {
		return buildUrl(url, login, deviceId, devType, cmd) + extra;
	}
	
	public String buildUrl(String url, String login, DeviceId deviceId, String devType, String cmd) {
		return buildUrl(url, login, deviceId, devType) + "&Cmd=" + cmd;
	}
	
	public String buildUrl(String url, String login, DeviceId deviceId, String devType) {
		return url 
				+ "?User=" + login
				+ "&DeviceId=" + deviceId.getDeviceId()
				+ "&DeviceType=" + devType;
	}
}
