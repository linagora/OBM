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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.reports.CalendarMultiGet;
import org.obm.caldav.server.reports.CalendarQuery;
import org.obm.caldav.server.reports.PrincipalPropertySearch;
import org.obm.caldav.server.reports.ReportProvider;
import org.obm.caldav.server.share.Token;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ReportHandler extends DavMethodHandler {

	Map<String, ReportProvider> providers;
	
	public ReportHandler() {
		providers = new HashMap<String, ReportProvider>();
		providers.put("principal-property-search",
				new PrincipalPropertySearch());
		providers.put("calendar-query", new CalendarQuery());
		providers.put("calendar-multiget", new CalendarMultiGet());
	}

	@Override
	public void process(Token token, IBackend proxy, DavRequest req, HttpServletResponse resp) {
		logger.info("process(req, resp)");
		Document d = req.getXml();
		Element r = d.getDocumentElement();
		
		String reportKind = r.getLocalName();
		
		ReportProvider rp = providers.get(reportKind);
		if (rp != null) {
			Set<String> propToLoad = new HashSet<String>();
			for(Element e : getPropList(d)){
				propToLoad.add(e.getLocalName());
			}
			rp.process(token, proxy, req, resp, propToLoad);
		} else {
			logger.error("No report provider for report kind '" + reportKind
					+ "'");
		}
	}
}