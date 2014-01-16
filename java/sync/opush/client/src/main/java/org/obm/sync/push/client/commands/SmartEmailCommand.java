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
package org.obm.sync.push.client.commands;

import java.util.concurrent.Future;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ByteArrayEntity;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.ServerId;
import org.obm.sync.push.client.IEasCommand;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.beans.AccountInfos;

public abstract class SmartEmailCommand implements IEasCommand<Boolean> {

	private final byte[] emailData;
	private final int collectionId;
	private final ServerId serverId;
	
	public SmartEmailCommand(byte[] emailData, int collectionId, ServerId serverId) {
		this.emailData = emailData;
		this.collectionId = collectionId;
		this.serverId = serverId;
	}

	@Override
	public Boolean run(AccountInfos ai, OPClient opc, HttpClient hc) throws Exception {
		return Request
			.Post(opc.buildUrl(ai.getUrl(), ai.getLogin(), ai.getDevId(), ai.getDevType(), getCommandName(), commandParams()))
				.addHeader("User-Agent", ai.getUserAgent())
				.addHeader("Authorization", ai.authValue())
				.addHeader("Ms-Asprotocolversion", ProtocolVersion.V121.asSpecificationValue())
				.addHeader("Accept", "*/*")
				.addHeader("Accept-Language", "fr-fr")
				.addHeader("Connection", "keep-alive")
				.body(new ByteArrayEntity(emailData))
				.execute()
				.returnResponse()
				.getStatusLine()
				.getStatusCode() == HttpStatus.SC_OK;
	}

	@Override
	public Future<Boolean> runASync(AccountInfos ai, OPClient opc, Async async) throws Exception {
		throw new RuntimeException("Not implements for SmartEmailCommand");
	}

	protected abstract String getCommandName();

	private String commandParams() {
		return new StringBuilder()
			.append("&CollectionId=")
			.append(collectionId)
			.append("&ItemId=")
			.append(serverId.toString())
			.append("&SaveInSent=T")
			.toString();
	}
	
	public static class SmartReply extends SmartEmailCommand {

		public SmartReply(byte[] emailData, int collectionId, ServerId serverId) {
			super(emailData, collectionId, serverId);
		}

		@Override
		protected String getCommandName() {
			return "SmartReply";
		}
	}
	
	public static class SmartForward extends SmartEmailCommand {
		
		public SmartForward(byte[] emailData, int collectionId, ServerId serverId) {
			super(emailData, collectionId, serverId);
		}
		
		@Override
		protected String getCommandName() {
			return "SmartForward";
		}
	}
}
