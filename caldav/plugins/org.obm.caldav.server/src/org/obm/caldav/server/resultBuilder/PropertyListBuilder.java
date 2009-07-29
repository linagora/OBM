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

package org.obm.caldav.server.resultBuilder;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropertyListBuilder {

	private Log logger = LogFactory.getLog(PropertyListBuilder.class);

	public Document build(Token t, DavRequest req,
			Set<PropfindPropertyHandler> toLoad, Set<Element> notUsed,
			IProxy proxy) {
		try {
			Document ret = DOMUtils.createDoc(NameSpaceConstant.DAV_NAMESPACE,
					"D:multistatus");
			Element r = ret.getDocumentElement();
			r.setAttribute("xmlns:D", NameSpaceConstant.DAV_NAMESPACE);
			r.setAttribute("xmlns:CS",
					NameSpaceConstant.CALENDARSERVER_NAMESPACE);
			r.setAttribute("xmlns:C", NameSpaceConstant.CALDAV_NAMESPACE);
			r.setAttribute("xmlns", NameSpaceConstant.CALDAV_NAMESPACE);
			Element response = DOMUtils.createElement(r, "D:response");
			DOMUtils.createElementAndText(response, "D:href", req.getHref());

			if (toLoad.size() > 0) {
				Element pStat = DOMUtils.createElement(response, "D:propstat");
				Element p = DOMUtils.createElement(pStat, "D:prop");
				for (PropfindPropertyHandler dph : toLoad) {
					dph.appendPropertyValue(p, t, req, proxy);
				}
				DOMUtils.createElementAndText(pStat, "D:status",
						"HTTP/1.1 200 OK");
			}

			if (notUsed.size() > 0) {
				Element pStatNotUsed = DOMUtils.createElement(response,
						"D:propstat");
				Element pNotUsed = DOMUtils.createElement(pStatNotUsed,
						"D:prop");
				for (Element enu : notUsed) {
					DOMUtils.createElement(pNotUsed, enu.getNamespaceURI(), enu
							.getTagName());
				}
				DOMUtils.createElementAndText(pStatNotUsed, "D:status",
						"HTTP/1.1 404 Not Found");
			}

			return ret;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}

	}

}
