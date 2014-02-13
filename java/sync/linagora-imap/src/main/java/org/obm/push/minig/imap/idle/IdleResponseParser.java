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

package org.obm.push.minig.imap.idle;

import org.obm.push.mail.imap.idle.IdleLine;
import org.obm.push.mail.imap.idle.IdleTag;
import org.obm.push.minig.imap.impl.MinaIMAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdleResponseParser {

	private static final Logger logger = LoggerFactory
			.getLogger(IdleResponseParser.class);

	public IdleResponseParser() {
	}

	// * 1 RECENT
	// * 27 EXPUNGE
	// * 35 FETCH (FLAGS (\Deleted \Seen))
	public IdleLine parse(MinaIMAPMessage msg) {
		String response = msg.getMessageLine();
		if(response.startsWith("* BYE")){
			IdleLine r = new IdleLine();
			r.setTag(IdleTag.BYE);
			r.setPayload(response);
			return r;
		}
		String[] tab = response.split(" ");
		if (tab.length > 2) {
			try {
				IdleLine r = new IdleLine();
				r.setTag(IdleTag.getIdleTag(tab[2]));
				r.setPayload(response);
				return r;
			} catch (Exception e) {
				logger.warn("Can't parse idle message[" + response + "]", e);
			}
		} else {
			logger.warn("Can't parse idle message[" + response + "]");
		}
		return null;
	}
}
