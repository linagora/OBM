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

import org.obm.push.mail.bean.NameSpaceInfo;
import org.obm.push.minig.imap.command.parser.NamespaceParser;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.RecoveringParseRunner;

public class NamespaceCommand extends SimpleCommand<NameSpaceInfo> {

	private static final NamespaceParser parser = Parboiled.createParser(NamespaceParser.class);
	
	public NamespaceCommand() {
		super("NAMESPACE");
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		if (response.getPayload().startsWith(NamespaceParser.expectedResponseStart)) {
			return true;
		}
		return false;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		NamespaceParser parserInstance = parser.newInstance();
		RecoveringParseRunner<NameSpaceInfo> runner = new RecoveringParseRunner<NameSpaceInfo>(parserInstance.rule());
		NameSpaceInfo nameSpaceInfo = runner.run(response.getPayload()).resultValue;
		
		if (data == null) {
			data = nameSpaceInfo;
		} else {
			data.addAll(nameSpaceInfo);
		}
	}

}
