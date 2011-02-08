package org.obm.sync.mailingList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.items.AbstractItemsParser;
import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MailingListItemsParser extends AbstractItemsParser {

	@SuppressWarnings("unused")
	private static final Log logger = LogFactory
			.getLog(MailingListItemsParser.class);

	public MailingList parseMailingList(String parameter) throws SAXException,
			IOException, FactoryConfigurationError {
		Document doc = DOMUtils.parse(new ByteArrayInputStream(parameter
				.getBytes()));

		Element root = doc.getDocumentElement();
		return parseMailingList(root);
	}

	public List<MailingList> parseListMailingList(Document doc) {
		Element documentElement = doc.getDocumentElement();
		List<MailingList> ret = new LinkedList<MailingList>();
		NodeList mlsNodeList = documentElement
				.getElementsByTagName("mailingList");
		for (int i = 0; i < mlsNodeList.getLength(); i++) {
			Element e = (Element) mlsNodeList.item(i);
			MailingList ml = parseMailingList(e);
			ret.add(ml);
		}
		return ret;
	}

	public MailingList parseMailingList(Element root) {
		Element mlEle = root;
		if (!mlEle.getTagName().equalsIgnoreCase("mailingList")) {
			mlEle = DOMUtils.getUniqueElement(root, "mailingList");
		}
		MailingList ml = new MailingList();
		String id = mlEle.getAttribute("id");
		if (id != null && id.length() > 0) {
			try {
				ml.setId(Integer.valueOf(id));
			} catch (NumberFormatException e) {
				//DO NOTHING
			}
		}
		ml.setName(mlEle.getAttribute("name"));
		ml.addEmails(parseEmails(mlEle));
		return ml;
	}

	private List<MLEmail> parseEmails(Element uniqueElement) {
		String[] attrs = { "id", "label", "address" };
		String[][] values = DOMUtils.getAttributes(uniqueElement, "email",
				attrs);
		List<MLEmail> ret = new ArrayList<MLEmail>(values.length);
		for (String[] p : values) {
			MLEmail e = new MLEmail(p[1], p[2]);
			String id = p[0];
			if (id != null && id.length() > 0) {
				try {
					e.setId(Integer.valueOf(id));
				} catch (NumberFormatException x) {
					//DO NOTHING
				}
			}
			ret.add(e);
		}
		return ret;
	}

	public List<MLEmail> parseMailingListEmails(String mailingListEmails) {
		try {
			Document doc = DOMUtils.parse(new ByteArrayInputStream(
					mailingListEmails.getBytes()));
			return parseEmails(doc.getDocumentElement());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<MLEmail>(0);
	}

	public List<MLEmail> parseMailingListEmails(Document doc) {
		return parseEmails(doc.getDocumentElement());
	}
}
