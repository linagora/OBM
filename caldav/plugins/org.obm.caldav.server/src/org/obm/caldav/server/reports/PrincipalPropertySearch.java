package org.obm.caldav.server.reports;

import java.util.HashSet;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.impl.PropertyListBuilder;
import org.obm.caldav.server.impl.Token;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.aliasource.utils.DOMUtils;

public class PrincipalPropertySearch extends ReportProvider {

	@Override
	public void process(Token token, DavRequest req, HttpServletResponse resp) {
		// TODO Auto-generated method stub
		logger.info("process("+token.getLoginAtDomain()+", req, resp)");
		Document doc = req.getDocument();
		Element r = doc.getDocumentElement();
		
		// search criteria
		// NodeList pSearch = r.getElementsByTagName("D:property-search");
		
		
		NodeList children = r.getChildNodes();
		// last element is a text node, take the one before
		Element dProp = (Element) children.item(children.getLength() - 2);
		HashSet<String> toLoad = new HashSet<String>();
		children = dProp.getChildNodes();
		for (int i=0;i<children.getLength();i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				toLoad.add(n.getNodeName());
			}
		}
		
		Document ret = new PropertyListBuilder().build(token, req, toLoad);

		try {
			DOMUtils.logDom(ret);

			resp.setStatus(207); // multi status webdav
			resp.setContentType("text/xml; charset=utf-8");
			DOMUtils.serialise(ret, resp.getOutputStream());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
