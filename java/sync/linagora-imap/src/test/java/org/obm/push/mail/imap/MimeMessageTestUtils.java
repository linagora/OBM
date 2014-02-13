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
package org.obm.push.mail.imap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Iterator;

import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.mime.MimePart;

public class MimeMessageTestUtils {

	private static String prefixMessage(MimePart expected) {
		return "part with address " + expected.getAddress();
	}
	
	public static void checkMimeTree(MimePart expected, MimePart actual) {
		assertThat(actual.getChildren().size()).isEqualTo(expected.getChildren().size()).describedAs(prefixMessage(expected) + "has wrong number of children"); 
		assertThat(actual.getPrimaryType()).isEqualTo(expected.getPrimaryType()).describedAs(prefixMessage(expected));
		assertThat(actual.getSubtype()).isEqualTo(expected.getSubtype()).describedAs(prefixMessage(expected));
		assertThat(actual.getContentTransfertEncoding()).isEqualTo(expected.getContentTransfertEncoding()).describedAs(prefixMessage(expected));
		assertThat(actual.getContentId()).isEqualTo(expected.getContentId()).describedAs(prefixMessage(expected));
		assertThat(actual.getContentLocation()).isEqualTo(expected.getContentLocation()).describedAs(prefixMessage(expected));
		assertThat(actual.getBodyParams()).isEqualTo(expected.getBodyParams()).as(prefixMessage(expected));
		assertThat(actual.getSize()).isEqualTo(expected.getSize()).describedAs(prefixMessage(expected));
		Iterator<MimePart> expectedParts = expected.getChildren().iterator();
		Iterator<MimePart> actualParts = actual.getChildren().iterator();
		while (actualParts.hasNext()) {
			checkMimeTree(expectedParts.next(), actualParts.next());
		}
	}

	public static MimePart getPartByAddress(MimePart message, MimeAddress addr) {
		Collection<MimePart> children = message.getChildren();
		for (MimePart part: children) {
			if (addr.equals(part.getAddress())) {
				return part;
			}
			MimePart result = getPartByAddress(part, addr);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
}
