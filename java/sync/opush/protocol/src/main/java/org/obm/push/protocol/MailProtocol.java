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
package org.obm.push.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.obm.configuration.EmailConfiguration;
import org.obm.push.bean.MailRequestStatus;
import org.obm.push.exception.QuotaExceededException;
import org.obm.push.protocol.bean.MailRequest;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.stream.SizeLimitExceededException;
import org.obm.push.utils.stream.SizeLimitingInputStream;
import org.w3c.dom.Document;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MailProtocol {
	
	private EmailConfiguration emailConfiguration;
	
	@Inject
	/* package */ MailProtocol(EmailConfiguration emailConfiguration) {
		this.emailConfiguration = emailConfiguration;
	}

	public MailRequest getRequest(ActiveSyncRequest request) throws IOException, QuotaExceededException {
		String collectionId = request.getParameter("CollectionId");
		String serverId = request.getParameter("ItemId");
		byte[] mailContent = streamBytes(request.getInputStream());
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
	
	private byte[] streamBytes(InputStream in)
			throws IOException, QuotaExceededException {
		final int maxSize = emailConfiguration.getMessageMaxSize();
		SizeLimitingInputStream sizeLimitingInputStream = new SizeLimitingInputStream(in, maxSize);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			ByteStreams.copy(sizeLimitingInputStream, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		} catch (SizeLimitExceededException e) {
			throw new QuotaExceededException("The message must be smaller than " + maxSize, maxSize, 
					byteArrayOutputStream.toByteArray());
		}
	}

	public Document encodeErrorResponse(String namespace, MailRequestStatus requestStatus) {
		Document ret = DOMUtils.createDoc(null, namespace);
		DOMUtils.createElementAndText(ret.getDocumentElement(), "Status", requestStatus.asXmlValue());
		return ret;
	}

}
