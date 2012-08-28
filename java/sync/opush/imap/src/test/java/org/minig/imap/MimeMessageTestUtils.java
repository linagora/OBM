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
package org.minig.imap;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.minig.imap.mime.IMimePart;
import org.obm.push.mail.MimeAddress;

public class MimeMessageTestUtils {

	private static String prefixMessage(IMimePart expected) {
		return "part with address " + expected.getAddress();
	}
	
	public static void checkMimeTree(IMimePart expected, IMimePart actual) {
		Assert.assertEquals(prefixMessage(expected) + " has wrong number of children", 
				expected.getChildren().size(), actual.getChildren().size());
		Assert.assertEquals(prefixMessage(expected), expected.getPrimaryType(), actual.getPrimaryType());
		Assert.assertEquals(prefixMessage(expected), expected.getSubtype(), actual.getSubtype());
		Assert.assertEquals(prefixMessage(expected), expected.getContentTransfertEncoding(), actual.getContentTransfertEncoding());
		Assert.assertEquals(prefixMessage(expected), expected.getContentId(), actual.getContentId());
		Assert.assertArrayEquals(prefixMessage(expected), expected.getBodyParams().toArray(), actual.getBodyParams().toArray());
		Assert.assertEquals(prefixMessage(expected), expected.getSize(), actual.getSize());
		Iterator<IMimePart> expectedParts = expected.getChildren().iterator();
		Iterator<IMimePart> actualParts = actual.getChildren().iterator();
		while (actualParts.hasNext()) {
			checkMimeTree(expectedParts.next(), actualParts.next());
		}
	}

	public static IMimePart getPartByAddress(IMimePart message, MimeAddress addr) {
		Collection<IMimePart> children = message.getChildren();
		for (IMimePart part: children) {
			if (addr.equals(part.getAddress())) {
				return part;
			}
			IMimePart result = getPartByAddress(part, addr);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
}
