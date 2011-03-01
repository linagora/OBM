package org.obm.sync.mailingList;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Document;

import junit.framework.TestCase;

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

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMUtils.serialise(xml, out);
			assertEquals(getTestMailingString(), out.toString());
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
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			DOMUtils.serialise(xml, out);
			assertEquals(sb.toString(), out.toString());
		} catch (TransformerException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
	}
}
