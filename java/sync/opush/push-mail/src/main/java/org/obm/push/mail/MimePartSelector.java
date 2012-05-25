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
package org.obm.push.mail;

import java.util.List;

import org.minig.imap.mime.ContentType;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeMessage;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.MSEmailBodyType;

import com.google.common.collect.ImmutableList;

public class MimePartSelector {
	
	private static final int DEFAULT_TRUNCATION_SIZE = 32*1024;
	private static final ImmutableList<BodyPreference> DEFAULT_BODY_PREFERENCES = 
			ImmutableList.<BodyPreference> builder()
					.add(new BodyPreference.Builder().bodyType(MSEmailBodyType.PlainText)
							.truncationSize(DEFAULT_TRUNCATION_SIZE).build())
					.add(new BodyPreference.Builder().bodyType(MSEmailBodyType.HTML)
							.truncationSize(DEFAULT_TRUNCATION_SIZE).build()).build(); 
	
	public FetchInstructions select(List<BodyPreference> bodyPreferences, MimeMessage mimeMessage) {
		if (bodyPreferences == null || bodyPreferences.isEmpty()) {
			return fetchIntructions(DEFAULT_BODY_PREFERENCES, mimeMessage);
		} else {
			return fetchIntructions(bodyPreferences, mimeMessage);
		}
	}

	private FetchInstructions fetchIntructions(List<BodyPreference> bodyPreferences, MimeMessage mimeMessage) {
		FetchInstructions fetchInstructions = selectMimePart(bodyPreferences, mimeMessage);
		if (fetchInstructions != null) {
			return fetchInstructions;
		}
		
		if (!bodyPreferences.equals(DEFAULT_BODY_PREFERENCES)) {
			fetchInstructions = selectMimePart(DEFAULT_BODY_PREFERENCES, mimeMessage);
			if (fetchInstructions != null) {
				return fetchInstructions;
			}
		}
		return defaultFetchInstructions(mimeMessage);
	}
	
	private FetchInstructions selectMimePart(List<BodyPreference> bodyPreferences, MimeMessage mimeMessage) {
		for (BodyPreference bodyPreference: bodyPreferences) {
			if (isContentType(bodyPreference)) {
				IMimePart mimePart = findMimePartMatching(mimeMessage, bodyPreference);
				if (isMatching(mimePart, bodyPreference)) {
					return buildFetchInstructions(mimePart, bodyPreference);
				}
			} else {
				return buildFetchInstructions(mimeMessage, bodyPreference);
			}
		}
		return null;
	}

	private FetchInstructions defaultFetchInstructions(MimeMessage mimeMessage) {
		return new FetchInstructions.Builder()
			.mimePart(mimeMessage.getMimePart())
			.truncation(DEFAULT_TRUNCATION_SIZE).build();
	}

	private boolean isMatching(IMimePart mimePart, BodyPreference bodyPreference) {
		if (mimePart != null) {
			if (bodyPreference.isAllOrNone() && bodyPreference.getTruncationSize() != null) {
				return mimePart.getSize() < bodyPreference.getTruncationSize();
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	private FetchInstructions buildFetchInstructions(IMimePart mimePart, BodyPreference bodyPreference) {
		return new FetchInstructions.Builder()
			.mimePart(mimePart)
			.truncation(bodyPreference.getTruncationSize())
			.build();
	}

	private IMimePart findMimePartMatching(MimeMessage mimeMessage, BodyPreference bodyPreference) {
		ContentType contentType = toContentType(bodyPreference.getType());
		return mimeMessage.findMainMessage(contentType);
	}

	private boolean isContentType(BodyPreference bodyPreference) {
		return bodyPreference.getType() != MSEmailBodyType.MIME;
	}
	
	private ContentType toContentType(MSEmailBodyType bodyType) {
		String contentType = toMimeType(bodyType);
		return new ContentType.Builder().contentType(contentType).build();
	}
	
	private String toMimeType(MSEmailBodyType bodyType) {
		switch (bodyType) {
		case PlainText:
			return "text/plain";
		case HTML:
			return "text/html";
		case RTF:
			return "text/rtf";
		default:
			throw new IllegalArgumentException("Unexpected MSEmailBodyType");
		}
	}
}