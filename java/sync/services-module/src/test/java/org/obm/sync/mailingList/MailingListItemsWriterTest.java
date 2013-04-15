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
package org.obm.sync.mailingList;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

public class MailingListItemsWriterTest extends TestCase {

	private MailingListItemsWriter writer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		writer = new MailingListItemsWriter();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected MailingList getTestMailingList() {
		MailingList ml = new MailingList();
		ml.setName("Liste diffusion");
		ml.setId(1);
		MLEmail e = new MLEmail("John Do", "john@test.tlse.lng");
		e.setId(1);
		ml.addEmail(e);
		return ml;
	}

	protected String getTestMailingString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<mailingLists xmlns=\"http://www.obm.org/xsd/sync/mailingLists.xsd\">");
		sb.append("<mailingList id=\"1\" name=\"Liste diffusion\">");
		sb.append("<mailingListEmails>");
		sb.append("<email address=\"john@test.tlse.lng\" id=\"1\" label=\"John Do\"/>");
		sb.append("</mailingListEmails>");
		sb.append("</mailingList>");
		sb.append("</mailingLists>");
		return sb.toString();
	}

	public void testGetMailingListsAsXML() {
		try {
			Document xml = writer.getMailingListsAsXML(getTestMailingList());

			String out = DOMUtils.serialize(xml);
			assertEquals(getTestMailingString(), out);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testGetMailingListsAsString() {
		String xml = writer.getMailingListsAsString(getTestMailingList());
		assertEquals(getTestMailingString(), xml);
	}

	public void testDetMailingListEmailsAsString() {
		List<MLEmail> emails = new ArrayList<MLEmail>(3);
		emails.add(new MLEmail("John Do1", "john1@test.tlse.lng"));
		emails.add(new MLEmail("John Do2", "john2@test.tlse.lng"));
		emails.add(new MLEmail("John Do3", "john3@test.tlse.lng"));

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<mailingListEmails xmlns=\"http://www.obm.org/xsd/sync/mailingListEmails.xsd\">");
		sb.append("<email address=\"john1@test.tlse.lng\" label=\"John Do1\"/>");
		sb.append("<email address=\"john2@test.tlse.lng\" label=\"John Do2\"/>");
		sb.append("<email address=\"john3@test.tlse.lng\" label=\"John Do3\"/>");
		sb.append("</mailingListEmails>");

		assertEquals(sb.toString(), writer.getMailingListEmailsAsString(emails));
	}

	public void testGetMailingListEmailsAsXML() {
		try {
			List<MLEmail> emails = new ArrayList<MLEmail>(3);
			MLEmail e = new MLEmail("John Do1", "john1@test.tlse.lng");
			e.setId(1);
			emails.add(e);
			MLEmail e2 = new MLEmail("John Do2", "john2@test.tlse.lng");
			e2.setId(2);
			emails.add(e2);
			MLEmail e3 = new MLEmail("John Do3", "john3@test.tlse.lng");
			e3.setId(3);
			emails.add(e3);

			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			sb.append("<mailingListEmails xmlns=\"http://www.obm.org/xsd/sync/mailingListEmails.xsd\">");
			sb.append("<email address=\"john1@test.tlse.lng\" id=\"1\" label=\"John Do1\"/>");
			sb.append("<email address=\"john2@test.tlse.lng\" id=\"2\" label=\"John Do2\"/>");
			sb.append("<email address=\"john3@test.tlse.lng\" id=\"3\" label=\"John Do3\"/>");
			sb.append("</mailingListEmails>");

			Document xml = writer.getMailingListEmailsAsXML(emails);

			String out = DOMUtils.serialize(xml);
			assertEquals(sb.toString(), out);
		} catch (TransformerException e1) {
			fail(e1.getMessage());
		}
	}
}
