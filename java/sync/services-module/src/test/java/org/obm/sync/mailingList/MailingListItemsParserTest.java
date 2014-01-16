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
package org.obm.sync.mailingList;

import java.io.ByteArrayInputStream;
import java.util.List;

import junit.framework.TestCase;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

public class MailingListItemsParserTest extends TestCase {

	private MailingListItemsParser parser;

//	public void testParseMailingList(String parameter) throws
//			FactoryConfigurationError {
//	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		parser = new MailingListItemsParser();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testParseListMailingList() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			sb.append("<mailingLists xmlns=\"http://www.obm.org/xsd/sync/mailingLists.xsd\">");
			sb.append("<mailingList id=\"1\" name=\"Liste diffusion\">");
			sb.append("<mailingListEmails>");
			sb.append("<email address=\"john@test.tlse.lng\" id=\"1\" label=\"John Do\"/>");
			sb.append("</mailingListEmails>");
			sb.append("</mailingList>");
			sb.append("<mailingList name=\"Liste diffusion\">");
			sb.append("<mailingListEmails>");
			sb.append("<email address=\"john2@test.tlse.lng\" label=\"John Do2\"/>");
			sb.append("</mailingListEmails>");
			sb.append("</mailingList>");
			sb.append("</mailingLists>");

			Document doc = DOMUtils.parse(new ByteArrayInputStream(sb
					.toString().getBytes()));
			List<MailingList> ret = parser.parseListMailingList(doc);

			MailingList ml = new MailingList();
			ml.setId(1);
			ml.setName("Liste diffusion");
			MLEmail e = new MLEmail("John Do", "john@test.tlse.lng");
			e.setId(1);
			ml.addEmail(e);
			assertEquals(ml, ret.iterator().next());

			MailingList ml2 = new MailingList();
			ml2.setName("Liste diffusion2");
			ml2.addEmail(new MLEmail("John Do2", "john2@test.tlse.lng"));
			assertEquals(ml, ret.iterator().next());

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testParseMailingList() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			sb.append("<mailingLists xmlns=\"http://www.obm.org/xsd/sync/mailingLists.xsd\">");
			sb.append("<mailingList id=\"1\" name=\"Liste diffusion\">");
			sb.append("<mailingListEmails>");
			sb.append("<email address=\"john@test.tlse.lng\" id=\"1\" label=\"John Do\"/>");
			sb.append("</mailingListEmails>");
			sb.append("</mailingList>");
			sb.append("</mailingLists>");

			MailingList ret = parser.parseMailingList(sb.toString());

			MailingList ml = new MailingList();
			ml.setId(1);
			ml.setName("Liste diffusion");
			ml.addEmail(new MLEmail("John Do", "john@test.tlse.lng"));
			assertEquals(ml, ret);

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testParseMailingListEmails() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			sb.append("<mailingListEmails xmlns=\"http://www.obm.org/xsd/sync/mailingListEmails.xsd\">");
			sb.append("<email address=\"john1@test.tlse.lng\" label=\"John Do1\"/>");
			sb.append("<email address=\"john2@test.tlse.lng\" label=\"John Do2\"/>");
			sb.append("<email address=\"john3@test.tlse.lng\" label=\"John Do3\"/>");
			sb.append("</mailingListEmails>");

			List<MLEmail> ret = parser.parseMailingListEmails(sb.toString());

			MLEmail e1 = new MLEmail("John Do1", "john1@test.tlse.lng");
			MLEmail e2 = new MLEmail("John Do2", "john2@test.tlse.lng");
			MLEmail e3 = new MLEmail("John Do3", "john3@test.tlse.lng");

			ret.contains(e1);
			ret.contains(e2);
			ret.contains(e3);

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testParseMailingListEmailsXML() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			sb.append("<mailingListEmails xmlns=\"http://www.obm.org/xsd/sync/mailingListEmails.xsd\">");
			sb.append("<email address=\"john1@test.tlse.lng\" label=\"John Do1\"/>");
			sb.append("<email address=\"john2@test.tlse.lng\" label=\"John Do2\"/>");
			sb.append("<email address=\"john3@test.tlse.lng\" label=\"John Do3\"/>");
			sb.append("</mailingListEmails>");

			List<MLEmail> ret = parser.parseMailingListEmails(DOMUtils
					.parse(new ByteArrayInputStream(sb.toString().getBytes())));

			MLEmail e1 = new MLEmail("John Do1", "john1@test.tlse.lng");
			MLEmail e2 = new MLEmail("John Do2", "john2@test.tlse.lng");
			MLEmail e3 = new MLEmail("John Do3", "john3@test.tlse.lng");

			ret.contains(e1);
			ret.contains(e2);
			ret.contains(e3);

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
