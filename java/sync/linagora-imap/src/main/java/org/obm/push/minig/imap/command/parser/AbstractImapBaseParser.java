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

import java.nio.charset.Charset;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.SkipNode;
import org.parboiled.common.Reference;

public class AbstractImapBaseParser extends BaseParser<Object> {

	/**
	 * parse a printable character but don't push anything on the stack
	 */
	@SkipNode
	Rule quotedCharacter() {
		//exclude 0x0A (CR), 0x0D (LF), 0x22 (dquote)
		return FirstOf(CharRange((char)0x01, (char)0x09),
				CharRange((char)0x0B, (char)0x0C),
				CharRange((char)0x0E, (char)0x21),
				CharRange((char)0x23, (char)0x7F));
	}
	
	/**
	 * parse a string of printable character and push it on the stack
	 */
	Rule stringContent() {
		return Sequence(ZeroOrMore(quotedCharacter()), push(match()));
	}
	
	/**
	 * parse NIL and push a null object on the stack
	 */
	Rule nil() {
		return Sequence(String("NIL"), push(null));
	}

	/**
	 * parse NIL
	 */
	Rule nilNoStack() {
		return String("NIL");
	}
	
	/**
	 * parse a space character, don't touch the stack
	 */
	Rule whitespaces() {
		return OneOrMore(' ');
	}

	/**
	 * parse a double-quoted string and push its content without quotes on the stack
	 */
	Rule string() {
		return FirstOf(Sequence('"', stringContent(), '"'),
				literal());
	}
	
	Rule literal() {
		Reference<Integer> count = new Reference<Integer>();
		return Sequence('{', number(), assignCount(count), '}', literalStringPart(count));
	}

	boolean assignCount(Reference<Integer> count) {
		return count.set((Integer) pop());
	}
	
	Rule literalStringPart(Reference<Integer> count) {
		return Sequence(
				ZeroOrMore(count.get() > 0, ANY, count.set(count.get() - match().getBytes(Charset.forName("ASCII")).length)),
				count.get() == 0,
				push(match())
		); 
	}

	/**
	 * parse a double-quoted string
	 */
	Rule stringNoStack() {
		return Sequence('"', stringContent(), '"', drop());
	}
	
	/**
	 * parse a double-quoted string and push it on the stack or NIL and push null on the stack
	 */
	Rule nstring() {
		return FirstOf(string(), nil());
	}
	
	/**
	 * parse a double-quoted string
	 */
	Rule nstringNoStack() {
		return Sequence(FirstOf(string(), nil()), drop());
	}
	
	/**
	 * parse an {@link Integer} and push it on the stack
	 */
	Rule number() {
		return Sequence(OneOrMore(CharRange('0', '9')), push(match()), convertTopFromStringToInt());
	}
	
	Rule numberNoStack() {
		return Sequence(number(), drop());
	}

	boolean convertTopFromStringToInt() {
		String s = (java.lang.String) pop();
		int value = Integer.valueOf(s);
		push(value);
		return true;
	}
	
	Rule SequenceWithWhitespaces(Object... rules) {
		Object[] array = new Object[rules.length * 2 - 1];
		int i = 0;
		for (Object rule: rules) {
			if (i != 0) {
				array[i++] = whitespaces();
			}
			array[i++] = rule;
		}
		return Sequence(array);
	}
	
	
}
