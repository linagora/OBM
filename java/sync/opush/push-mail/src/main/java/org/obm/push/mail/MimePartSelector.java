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

import java.util.List;

import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class MimePartSelector {
	
	private static final Logger logger = LoggerFactory.getLogger(MimePartSelector.class);
	
	public FetchInstruction select(BodyPreferencePolicy bodyPreferencePolicy, List<BodyPreference> bodyPreferences, MimeMessage mimeMessage) {
		logger.debug("BodyPreferences {} MimeMessage {}", bodyPreferences, mimeMessage.getMimePart());
		
		List<BodyPreference> safeBodyPreferences = bodyPreferencePolicy.bodyPreferencesMatchingPolicy(bodyPreferences);
		List<FetchInstruction> fetchInstructions = fetchIntructions(bodyPreferencePolicy, safeBodyPreferences, mimeMessage);
		return bodyPreferencePolicy.selectBetterFit(fetchInstructions, safeBodyPreferences);
	}

	private List<FetchInstruction> fetchIntructions(BodyPreferencePolicy bodyPreferencePolicy, List<BodyPreference> bodyPreferences, MimeMessage mimeMessage) {
		List<FetchInstruction> fetchInstructions = findMatchingInstructions(bodyPreferencePolicy, bodyPreferences, mimeMessage);
		if (!fetchInstructions.isEmpty()) {
			return fetchInstructions;
		} else if (bodyPreferencePolicy.mayUsesDefaultBodyPreferences()) {
			return findMatchingInstructions(bodyPreferencePolicy, BodyPreferencePolicy.DEFAULT_BODY_PREFERENCES, mimeMessage);
		}
		return fetchInstructions;
	}
	
	private List<FetchInstruction> findMatchingInstructions(BodyPreferencePolicy bodyPreferencePolicy, List<BodyPreference> bodyPreferences, MimeMessage mimeMessage) {
		List<FetchInstruction> fetchInstructions = Lists.newArrayList();
		for (BodyPreference bodyPreference: bodyPreferences) {
			if (isContentType(bodyPreference)) {
				fetchInstructions.addAll(findMatchingInstruction(bodyPreferencePolicy, mimeMessage, bodyPreference));
			} else {
				fetchInstructions.add(buildFetchInstruction(FetchInstruction.builder(), mimeMessage, bodyPreference));
			}
		}
		return fetchInstructions;
	}

	private List<FetchInstruction> findMatchingInstruction(BodyPreferencePolicy bodyPreferencePolicy, MimeMessage mimeMessage, BodyPreference bodyPreference) {
		List<FetchInstruction> fetchInstructions = Lists.newArrayList();
		for (FetchHints hints: bodyPreferencePolicy.listContentTypes(bodyPreference.getType())) {
			MimePart mimePart =  mimeMessage.findMainMessage(hints.getContentType());
			if (isOptionsMatching(mimePart, bodyPreference)) {
				fetchInstructions.add(buildFetchInstruction(hints.getInstruction(), mimePart, bodyPreference));
			}
		}
		return fetchInstructions;
	}

	private boolean isOptionsMatching(MimePart mimePart, BodyPreference bodyPreference) {
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
	
	private FetchInstruction buildFetchInstruction(FetchInstruction.Builder instruction, MimePart mimePart, BodyPreference bodyPreference) {
		return instruction
			.mimePart(mimePart)
			.truncation(bodyPreference.getTruncationSize())
			.bodyType(bodyPreference.getType())
			.build();
	}
	
	private boolean isContentType(BodyPreference bodyPreference) {
		return bodyPreference.getType() != MSEmailBodyType.MIME;
	}
}