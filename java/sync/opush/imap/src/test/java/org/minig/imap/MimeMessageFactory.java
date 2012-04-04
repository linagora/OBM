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
package org.minig.imap;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.MimeMessage;
import org.minig.imap.mime.MimePart;
import org.minig.imap.mime.ContentType;

import com.google.common.collect.Sets;

public class MimeMessageFactory {

	private static <T extends MimePart> T fillSimpleMimePart(T mimePart, String mimeType, String mimeSubtype, String contentId, String encoding, Map<String, String> bodyParams, MimePart... parts) {
		mimePart.setContentType( buildMimeType(mimeType, mimeSubtype) );
		HashSet<BodyParam> params = Sets.newHashSet();
		for (Entry<String, String> entry: bodyParams.entrySet()) {
			params.add(new BodyParam(entry.getKey(), entry.getValue()));
		}
		mimePart.setBodyParams(params);
		for (MimePart part: parts) {
			mimePart.addPart(part);
		}
		mimePart.setContentId(contentId);
		mimePart.setContentTransfertEncoding(encoding);
		return mimePart;
	}
	
	private static ContentType buildMimeType(String mimeType, String mimeSubtype) {
		ContentType.Builder builder = new ContentType.Builder();
		return builder.primaryType(mimeType).subType(mimeSubtype).build();
	}
	
	public static MimePart createSimpleMimePart(String mimeType, String mimeSubtype, String contentId, String encoding, Map<String, String> bodyParams, MimePart... parts) {
		MimePart tree = new MimePart();
		fillSimpleMimePart(tree, mimeType, mimeSubtype, contentId, encoding, bodyParams, parts);
		return tree;
	}
	
	public static MimeMessage createSimpleMimeTree(String mimeType, String mimeSubtype, String contentId, String encoding, Map<String, String> bodyParams, MimePart... parts) {
		MimeMessage tree = new MimeMessage(createSimpleMimePart(mimeType, mimeSubtype, contentId, encoding, bodyParams, parts));
		return tree;
	}

	
}
