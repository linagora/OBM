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
