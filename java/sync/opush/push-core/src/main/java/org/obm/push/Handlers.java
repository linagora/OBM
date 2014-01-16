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
package org.obm.push;
import org.obm.push.handler.FolderSyncHandler;
import org.obm.push.handler.GetAttachmentHandler;
import org.obm.push.handler.GetItemEstimateHandler;
import org.obm.push.handler.IRequestHandler;
import org.obm.push.handler.ItemOperationsHandler;
import org.obm.push.handler.MeetingResponseHandler;
import org.obm.push.handler.MoveItemsHandler;
import org.obm.push.handler.PingHandler;
import org.obm.push.handler.ProvisionHandler;
import org.obm.push.handler.SearchHandler;
import org.obm.push.handler.SendMailHandler;
import org.obm.push.handler.SettingsHandler;
import org.obm.push.handler.SmartForwardHandler;
import org.obm.push.handler.SmartReplyHandler;
import org.obm.push.handler.SyncHandler;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class Handlers {

	private final ImmutableMap<String, IRequestHandler> handlers;

	@Inject
	private Handlers(FolderSyncHandler folderSyncHandler,
			SyncHandler syncHandler,
			GetItemEstimateHandler getItemEstimateHandler,
			ProvisionHandler provisionHandler,
			PingHandler pingHandler,
			SettingsHandler settingsHandler,
			SearchHandler searchHandler,
			SendMailHandler sendMailHandler,
			MoveItemsHandler moveItemsHandler,
			SmartReplyHandler smartReplyHandler,
			SmartForwardHandler smartForwardHandler,
			MeetingResponseHandler meetingResponseHandler,
			GetAttachmentHandler getAttachmentHandler,
			ItemOperationsHandler itemOperationsHandler) {
		
		handlers = ImmutableMap.<String, IRequestHandler>builder()
				.put("FolderSync",		folderSyncHandler)
				.put("Sync", 			syncHandler)
				.put("GetItemEstimate", getItemEstimateHandler)
				.put("Provision", 		provisionHandler)
				.put("Ping", 			pingHandler)
				.put("Settings", 		settingsHandler)
				.put("Search", 			searchHandler)
				.put("SendMail", 		sendMailHandler)
				.put("MoveItems", 		moveItemsHandler)
				.put("SmartReply", 		smartReplyHandler)
				.put("SmartForward", 	smartForwardHandler)
				.put("MeetingResponse",	meetingResponseHandler)
				.put("GetAttachment", 	getAttachmentHandler)
				.put("ItemOperations", 	itemOperationsHandler)
				.build();
	}
	
	public IRequestHandler getHandler(String command) {
		return handlers.get(command);
	}
	
}
