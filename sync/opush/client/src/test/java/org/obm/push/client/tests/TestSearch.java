package org.obm.push.client.tests;

import java.io.InputStream;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
@Ignore
public class TestSearch extends AbstractPushTest {
	
	public void testSearchWM() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("SearchRequestWM.xml");
		Document doc = DOMUtils.parse(in);
		Element query = DOMUtils.getUniqueElement(doc.getDocumentElement(), "Query");
		query.setTextContent("Meddd");
		Document ret = postXml120("Search", doc, "Search");
		assertNotNull(ret);
	}
	
	public void testSearchError() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("SearchRequestError.xml");
		Document doc = DOMUtils.parse(in);
		Element query = DOMUtils.getUniqueElement(doc.getDocumentElement(), "Query");
		query.setTextContent("Meddd");
		Document ret = postXml120("Search", doc, "Search");
		assertNotNull(ret);
	}


}
