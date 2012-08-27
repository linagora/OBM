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

package org.minig.imap.command;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.minig.imap.command.parser.BodyStructureParser;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;
import org.minig.imap.mime.MimeMessage;
import org.minig.imap.mime.impl.AtomHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class UIDFetchBodyStructureCommand extends Command<Collection<MimeMessage>> {

	private TreeSet<Long> uids;
	private final BodyStructureParser bodyStructureParser;

	public UIDFetchBodyStructureCommand(BodyStructureParser bodyStructureParser, Collection<Long> uid) {
		this.bodyStructureParser = bodyStructureParser;
		this.uids = new TreeSet<Long>(uid);
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID FETCH " + MessageSet.asString(uids) + " (UID RFC822.SIZE BODYSTRUCTURE)";
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);
		if (imaplogger.isInfoEnabled()) {
			for (IMAPResponse r: rs) {
				imaplogger.info(r.getPayload());
			}
		}
		
		if (isOK) {
			List<MimeMessage> mts = new LinkedList<MimeMessage>();
			Iterator<IMAPResponse> it = rs.iterator();
			int len = rs.size() - 1;
			for (int i = 0; i < len; i++) {
				IMAPResponse ir = it.next();
				String s = AtomHelper.getFullResponse(ir.getPayload(), ir.getStreamData());
				
				String bs = getBodyStructurePayload(s);
				if (bs == null) {
					continue;
				}
				
				if (bs.length() < 2) {
					logger.warn("strange bs response: " + s);
					continue;
				}

				long uid = getUid(s);
				int size = getSize(s);
				
				try {
					//remove closing brace
					MimeMessage message = bodyStructureParser.parseBodyStructure(bs.substring(0, bs.length() - 1));
					message.setUid(uid);
					message.setSize(size);
					mts.add(message);
				} catch (RuntimeException re) {
					logger.error("error parsing:\n" + new String(s));
					logger.error("payload was:\n" + s);
					throw re;
				}
			}
			data = mts;
		} else {
			IMAPResponse ok = rs.get(rs.size() - 1);
			logger.warn("bodystructure failed : " + ok.getPayload());
			data = Collections.emptyList();
		}
	}

	private int getSize(String fullPayload) {
		String intAsString = getNumberForField(fullPayload, "RFC822.SIZE ");
		return Integer.valueOf(intAsString);
	}

	private Long getUid(String fullPayload) {
		String longAsString = getNumberForField(fullPayload, "UID ");
		return Long.valueOf(longAsString);		
	}

	private String getNumberForField(String fullPayload, String field) {
		String uidStartToken = field;
		int uidIdx = fullPayload.indexOf(uidStartToken);
		String content = fullPayload.substring(uidIdx + uidStartToken.length());
		ImmutableList<Character> chars = Lists.charactersOf(content);
		StringBuilder longAsString = new StringBuilder();
		for (Character c: chars) {
			if (Character.isDigit(c)) {
				longAsString.append(c);
			} else {
				break;
			}
		}
		return longAsString.toString();
	}
	
	private String getBodyStructurePayload(String fullPayload) {
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
		ImmutableList<Character> chars = Lists.charactersOf(content);
		int scope = 1;
		int position = 0;
		for (Character c: chars) {
			position++;
			if (c == '(') scope++;
			if (c == ')') scope--;
			if (scope == 0) {
				return content.substring(0, position);
			}
		}
		return null;
	}
	
}
