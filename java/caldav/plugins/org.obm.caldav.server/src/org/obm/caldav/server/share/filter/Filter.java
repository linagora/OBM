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

package org.obm.caldav.server.share.filter;


/**
 * http://www.webdav.org/specs/rfc4791.html#rfc.section.9.7
 * 
 * @author adrienp
 *
 */
public class Filter {

	public final static String NAMESPACE = "urn:ietf:params:xml:ns:caldav";
	private CompFilter vCalendar;
	
	public Filter(){
	}
	
	public CompFilter getCompFilter(){
		return vCalendar;
	}
	
	public void setCompFilter(CompFilter compFilter){
		this.vCalendar = compFilter;
	}
	
}
