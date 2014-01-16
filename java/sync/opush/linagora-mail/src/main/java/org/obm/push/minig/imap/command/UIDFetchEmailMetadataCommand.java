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

import org.obm.push.mail.MailException;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.mime.MimeMessageImpl;
import org.obm.push.minig.imap.command.parser.BodyStructureParser;
import org.obm.push.minig.imap.impl.IMAPParsingTools;
import org.obm.push.minig.imap.impl.IMAPResponse;

/**
 * @author tom
 * 
 */
public class UIDFetchEmailMetadataCommand extends Command<EmailMetadata> {

	private final static String IMAP_COMMAND = "UID FETCH ";
	private final static String IMAP_FETCH_COMMANDS = " (UID FLAGS RFC822.SIZE BODYSTRUCTURE ENVELOPE)";
	
	private final BodyStructureParser bodyStructureParser;
	private final long uid;

	public UIDFetchEmailMetadataCommand(BodyStructureParser bodyStructureParser, long uid) {
		this.bodyStructureParser = bodyStructureParser;
		this.uid = uid;
	}

	@Override
	protected CommandArgument buildCommand() {
		StringBuilder sb = new StringBuilder();
		sb.append(IMAP_COMMAND);
		sb.append(uid);
		sb.append(IMAP_FETCH_COMMANDS);
		return new CommandArgument(sb.toString(), null);
	}

	@Override
	public String getImapCommand() {
		return IMAP_FETCH_COMMANDS;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		String fullPayload = response.getFullResponse();
		if (!fullPayload.contains(" FETCH ") ||
			!fullPayload.contains("UID " + uid) ||
			!fullPayload.contains("FLAGS (") ||
			!fullPayload.contains("RFC822.SIZE ") ||
			!fullPayload.contains("ENVELOPE ") ||
			!fullPayload.contains("BODYSTRUCTURE ")
			) {
			logger.warn("not a UIDFetchEmailMetadataCommand: {}", fullPayload);
			return false;
		}
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		String fullPayload = response.getFullResponse();
		long responseUid = parseUid(fullPayload);
		if (responseUid == uid) {
			int size = UIDFetchMessageSizeCommand.parseSize(fullPayload);
			data = EmailMetadata.builder()
					.uid(uid)
					.size(size)
					.flags(UIDFetchFlagsCommand.parseResponse(fullPayload))
					.envelope(UIDFetchEnvelopeCommand.parseEnvelope(fullPayload))
					.mimeMessage(buildBodyStructure(fullPayload, uid, size))
					.build();
		} else {
			throw new MailException("Try to handle a response with another uid than the command");
		}
	}

	private MimeMessageImpl buildBodyStructure(String fullPayload, long uid, int size) {
		return bodyStructureParser
	                .parseBodyStructure(UIDFetchBodyStructureCommand.getBodyStructurePayload(fullPayload))
	                .uid(uid)
	                .size(size)
	                .build();
	}

	private long parseUid(String payload) {
		try {
			String longAsString = IMAPParsingTools.getStringHasNumberForField(payload, "UID ");
			return Long.valueOf(longAsString);
		} catch(NumberFormatException e) {
			throw new MailException("Cannot find UID in response : " + payload);
		}
	}
	
}
