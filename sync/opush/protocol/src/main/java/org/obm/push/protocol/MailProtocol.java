package org.obm.push.protocol;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.obm.push.protocol.bean.MailRequest;
import org.obm.push.protocol.request.ActiveSyncRequest;

public class MailProtocol {

	public MailRequest getRequest(ActiveSyncRequest request) throws IOException {
		String collectionId = request.getParameter("CollectionId");
		String serverId = request.getParameter("ItemId");
		
		InputStream mailContent = new BufferedInputStream(request.getInputStream());
		mailContent.mark(mailContent.available());
		
		return new MailRequest(collectionId, serverId, getSaveInSentParameter(request) , mailContent);
	}

	private boolean getSaveInSentParameter(ActiveSyncRequest request) {
		boolean saveInSent = false;
		String sis = request.getParameter("SaveInSent");
		if (sis != null) {
			saveInSent = sis.equalsIgnoreCase("T");
		}
		return saveInSent;
	}

}
