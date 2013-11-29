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
package org.obm.push.mail;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Charsets;

public class AttachmentHelper {

	public final static String COLLECTION_ID = "collectionId";
	public final static String MESSAGE_ID = "messageId";
	public final static String MIME_PART_ADDRESS = "mimePartAddress";
	public final static String CONTENT_TYPE = "contentType";
	public final static String CONTENT_TRANSFERE_ENCODING = "contentTransferEncoding";

	public static String getAttachmentId(String collectionId, String messageId,
			String mimePartAddress, String contentType,
			String contentTransferEncoding) {
		String ct = Base64.encodeBase64String(contentType.getBytes(Charsets.UTF_8));
		String ret = collectionId + "_" + messageId + "_" + mimePartAddress
				+ "_" + ct;
		if (contentTransferEncoding != null
				&& !contentTransferEncoding.isEmpty()) {
			String cte = Base64.encodeBase64String(contentTransferEncoding.getBytes());
			ret += "_" + cte;
		}
		return ret;
	}

	public static Map<String, String> parseAttachmentId(String attachmentId) {
		String[] tab = attachmentId.split("_");
		if (tab.length < 4) {
			return null;
		}
		Map<String, String> data = new HashMap<String, String>();
		data.put(COLLECTION_ID, tab[0]);
		data.put(MESSAGE_ID, tab[1]);
		data.put(MIME_PART_ADDRESS, tab[2]);
		data.put(CONTENT_TYPE, new String(Base64.decodeBase64(tab[3]), Charsets.UTF_8));
		if(tab.length >=5){
			data.put(CONTENT_TRANSFERE_ENCODING, new String(Base64.decodeBase64(tab[4]), Charsets.UTF_8));
		}
		return data;
	}
}
