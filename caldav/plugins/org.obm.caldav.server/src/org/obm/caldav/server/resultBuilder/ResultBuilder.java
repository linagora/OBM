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

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * 
 * 
 * @author adrienp
 *
 */
public abstract class ResultBuilder {
	
	protected Log logger = LogFactory.getLog(ResultBuilder.class);
	
	protected Document createMultiStatusDocument() throws ParserConfigurationException, FactoryConfigurationError{
		Document ret = DOMUtils.createDoc(NameSpaceConstant.DAV_NAMESPACE, "D:multistatus");
		Element r = ret.getDocumentElement();
		r.setAttribute("xmlns:D", "DAV:");
		r.setAttribute("xmlns:CS", "http://calendarserver.org/ns/");
		r.setAttribute("xmlns:C", "urn:ietf:params:xml:ns:caldav");
		r.setAttribute("xmlns", "urn:ietf:params:xml:ns:caldav");
		return ret;
	}
	
	protected Document createScheduleResponseDocument() throws ParserConfigurationException, FactoryConfigurationError{
		Document ret = DOMUtils.createDoc(NameSpaceConstant.CALDAV_NAMESPACE, "C:schedule-response");
		Element r = ret.getDocumentElement();
		r.setAttribute("xmlns:D", "DAV:");
		r.setAttribute("xmlns:CS", "http://calendarserver.org/ns/");
		r.setAttribute("xmlns:C", "urn:ietf:params:xml:ns:caldav");
		r.setAttribute("xmlns", "urn:ietf:params:xml:ns:caldav");
		return ret;
	}
}
