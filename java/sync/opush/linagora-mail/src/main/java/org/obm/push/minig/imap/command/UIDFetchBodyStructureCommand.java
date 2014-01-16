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

package org.obm.push.minig.imap.command;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimeMessageImpl;
import org.obm.push.minig.imap.command.parser.BodyStructureParser;
import org.obm.push.minig.imap.impl.IMAPParsingTools;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.ImapMessageSet;

import com.google.common.collect.ImmutableList;

public class UIDFetchBodyStructureCommand extends Command<Collection<MimeMessage>> {

	private final static String IMAP_COMMAND = "UID FETCH";
	private ImapMessageSet imapMessageSet;
	private final BodyStructureParser bodyStructureParser;
	private String bodyStructure;

	public UIDFetchBodyStructureCommand(BodyStructureParser bodyStructureParser, MessageSet messages) {
		this.bodyStructureParser = bodyStructureParser;
		this.imapMessageSet = ImapMessageSet.wrap(messages);
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = IMAP_COMMAND + " " + imapMessageSet.asString() + " (UID RFC822.SIZE BODYSTRUCTURE)";
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public String getImapCommand() {
		return IMAP_COMMAND;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		String fullPayload = response.getFullResponse();
		
		bodyStructure = getBodyStructurePayload(fullPayload);
		if (bodyStructure == null) {
			return false;
		}
		
		if (bodyStructure.length() < 2) {
			logger.warn("strange bs response: {}", fullPayload);
			return false;
		}
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		String fullPayload = response.getFullResponse();
		List<MimeMessage> mts = new LinkedList<MimeMessage>();
		
		long uid = getUid(fullPayload);
		int size = getSize(fullPayload);
		
		try {
			//remove closing brace
			MimeMessageImpl.Builder messageBuilder = bodyStructureParser.parseBodyStructure(bodyStructure);
			messageBuilder.uid(uid).size(size);
			mts.add(messageBuilder.build());
			
			if (data == null || data.isEmpty()) {
				data = mts;
			} else {
				data.addAll(mts);
			}
		} catch (RuntimeException re) {
			logger.error("error parsing:\n{}", fullPayload);
			throw re;
		}
	}

	private int getSize(String fullPayload) {
		String intAsString = IMAPParsingTools.getStringHasNumberForField(fullPayload, "RFC822.SIZE ");
		return Integer.valueOf(intAsString);
	}

	private Long getUid(String fullPayload) {
		String longAsString = IMAPParsingTools.getStringHasNumberForField(fullPayload, "UID ");
		return Long.valueOf(longAsString);		
	}
	
	public static String getBodyStructurePayload(String fullPayload) {
		String bodystructureStartToken = "BODYSTRUCTURE ";
		int bsIdx = fullPayload.indexOf(bodystructureStartToken);
		if (bsIdx == -1) {
			return null;
		}
		int contentStart = bsIdx + bodystructureStartToken.length();
		if (fullPayload.charAt(contentStart) != '(') {
			return null;
		}
		String content = fullPayload.substring(contentStart);
		return IMAPParsingTools.substringFromOpeningToClosingBracket(content);
	}

	@Override
	public void setDataInitialValue() {
		data = ImmutableList.of();
	}
}
