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

public class UIDFetchBodyStructureCommand extends Command<Collection<MimeMessage>> {

	private TreeSet<Long> uids;
	private final BodyStructureParser bodyStructureParser;

	public UIDFetchBodyStructureCommand(BodyStructureParser bodyStructureParser, Collection<Long> uid) {
		this.bodyStructureParser = bodyStructureParser;
		this.uids = new TreeSet<Long>(uid);
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID FETCH " + MessageSet.asString(uids)
				+ " (UID BODYSTRUCTURE)";
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);
		
		for (IMAPResponse r : rs) {
			logger.debug("ri: " + r.getPayload() + " [stream:"
					+ (r.getStreamData() != null) + "]");
		}
		
		if (isOK) {
			List<MimeMessage> mts = new LinkedList<MimeMessage>();
			Iterator<IMAPResponse> it = rs.iterator();
			int len = rs.size() - 1;
			for (int i = 0; i < len; i++) {
				IMAPResponse ir = it.next();
				String s = ir.getPayload();

				int bsIdx = s.indexOf(" BODYSTRUCTURE ");
				if (bsIdx == -1) {
					continue;
				}

				String bs = s.substring(bsIdx + " BODYSTRUCTURE ".length());

				if (bs.length() < 2) {
					logger.warn("strange bs response: " + s);
					continue;
				}

				int uidIdx = s.indexOf("(UID ");
				long uid = Long.parseLong(s.substring(
						uidIdx + "(UID ".length(), bsIdx));

				String bsData = AtomHelper.getFullResponse(bs, ir.getStreamData());
				try {
					//remove closing brace
					MimeMessage message = bodyStructureParser.parseBodyStructure(bsData.substring(0, bsData.length() - 1));
					message.setUid(uid);
					mts.add(message);
				} catch (RuntimeException re) {
					logger.error("error parsing:\n" + new String(bsData));
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

}
