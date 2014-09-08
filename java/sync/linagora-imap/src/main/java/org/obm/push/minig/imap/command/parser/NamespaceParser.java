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
package org.obm.push.minig.imap.command.parser;

import java.util.Collections;
import java.util.List;

import org.obm.push.mail.bean.NameSpaceInfo;
import org.obm.push.minig.imap.impl.MailboxNameUTF7Converter;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

import com.google.common.collect.Lists;

@BuildParseTree
public class NamespaceParser extends AbstractImapBaseParser {
	
	public static final String expectedResponseStart = "* NAMESPACE";
	
	public Rule rule() {
		return Sequence(namespaceCommand(), 
				namespace(), ACTION(setPersonalNamespaces()), 
				whitespaces(),
				namespace(), ACTION(setOtherUserNamespaces()),
				whitespaces(),
				namespace(), ACTION(setSharedFolderNamespaces()));
	}
	
	@SuppressWarnings("unchecked")
	boolean setPersonalNamespaces() {
		swap();
		NameSpaceInfo nsi = (NameSpaceInfo) pop();
		nsi.setPersonal((List<String>) pop());
		push(nsi);
		return true;
	}

	@SuppressWarnings("unchecked")
	boolean setOtherUserNamespaces() {
		swap();
		NameSpaceInfo nsi = (NameSpaceInfo) pop();
		nsi.setOtherUsers((List<String>) pop());
		push(nsi);
		return true;
	}

	@SuppressWarnings("unchecked")
	boolean setSharedFolderNamespaces() {
		swap();
		NameSpaceInfo nsi = (NameSpaceInfo) pop();
		nsi.setMailShares((List<String>) pop());
		push(nsi);
		return true;
	}

	
	Rule namespace() {
		return FirstOf(
				Sequence(nilNoStack(), push(Collections.emptyList())),
				group());
	}
	
	Rule group() {
		return Sequence('(', 
				Sequence(push(Lists.newArrayList()),
						OneOrMore(entry())),
				')');
	}

	boolean addEntryToList() {
		String expression = (java.lang.String)pop();
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>)peek();
		list.add(MailboxNameUTF7Converter.decode(expression));
		return true;
	}
	
	Rule entry() {
		return Sequence('(', 
				string(), ACTION(addEntryToList()),
				whitespaces(), 
				stringNoStack(),
				ZeroOrMore(extension()),
				')',
				whitespaces());
	}
	
	Rule extension() {
		return Sequence(whitespaces(), 
				stringNoStack(),
				whitespaces(),
				'(', stringNoStack(),
				ZeroOrMore(whitespaces(), stringNoStack()),
				')'
			);
	}
	
	Rule namespaceCommand() {
		return Sequence(String(expectedResponseStart), whitespaces(), push(new NameSpaceInfo())); 
	}
}