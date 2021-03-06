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

package org.obm.caldav.server.methodHandler;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.server.StatusCodeConstant;
import org.obm.caldav.server.exception.ResultBuilderException;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.CalDavToken;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class DavMethodHandler {

	protected Log logger = LogFactory.getLog(getClass());
	
	
	public DavMethodHandler() {
	}
	
	public Set<Element> getPropList(Document doc){
		Element root = doc.getDocumentElement();
		Element prop = DOMUtils.getUniqueElement(root, "prop", NameSpaceConstant.DAV_NAMESPACE);
		NodeList propsToLoad = prop.getChildNodes();
		Set<Element> toLoad = new HashSet<Element>();

		for (int i = 0; i < propsToLoad.getLength(); i++) {
			Node n = propsToLoad.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				toLoad.add((Element)n);
			}
		}
		return toLoad;
	}

	public abstract void process(CalDavToken token,IBackend proxy, DavRequest req, HttpServletResponse resp) throws Exception;
	
	public void sendDom(Document doc, HttpServletResponse resp) throws ResultBuilderException{
		resp.setContentType("text/xml; charset=utf-8");
		try {
			DOMUtils.serialise(doc, resp.getOutputStream());
		} catch (Exception e1) {
			throw new ResultBuilderException(StatusCodeConstant.SC_INTERNAL_SERVER_ERROR);
		} 
	}
	
	
	
}
