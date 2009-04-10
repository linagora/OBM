package org.obm.caldav.server.impl;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.aliasource.utils.DOMUtils;

public class PropFindHandler extends DavMethodHandler {

	@Override
	public void process(Token t, DavRequest req, HttpServletResponse resp) {
		logger.info("process(req, resp)");

		String depth = req.getHeader("Depth");
		logger.info("depth: " + depth);

		Document doc = req.getDocument();
		Element root = doc.getDocumentElement();
		Element prop = DOMUtils.getUniqueElement(root, "D:prop");
		NodeList propsToLoad = prop.getChildNodes();
		Set<String> toLoad = new HashSet<String>();

		for (int i = 0; i < propsToLoad.getLength(); i++) {
			Node n = propsToLoad.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				String name = n.getNodeName();
				toLoad.add(name);
				logger.info(name);
			}
		}

		Document ret = new PropertyListBuilder().build(t, req, toLoad);

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
