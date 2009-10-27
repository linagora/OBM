/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.server.reports;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PrincipalPropertySearch extends ReportProvider {

	@Override
	public void process(Token token, IBackend proxy, DavRequest req,
			HttpServletResponse resp, Set<String> propList) {
		logger.info("process(" + token.getLoginAtDomain() + ", req, resp)");
		Document doc = req.getDocument();
		Element r = doc.getDocumentElement();

		// search criteria
		// NodeList pSearch = r.getElementsByTagName("D:property-search");

		NodeList children = r.getChildNodes();
		// last element is a text node, take the one before
		Element dProp = (Element) children.item(children.getLength() - 2);
		HashSet<String> toLoad = new HashSet<String>();
		children = dProp.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				toLoad.add(n.getNodeName());
			}
		}

		//FIXME
		/*Document ret = new PropertyListBuilder().build(token, req,
				propertiesHandler, toLoad);

		try {
			DOMUtils.logDom(ret);

			resp.setStatus(207); // multi status webdav
			resp.setContentType("text/xml; charset=utf-8");
			DOMUtils.serialise(ret, resp.getOutputStream());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}*/
	}
}
