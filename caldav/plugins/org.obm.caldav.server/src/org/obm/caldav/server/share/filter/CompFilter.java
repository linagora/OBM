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

import java.util.ArrayList;
import java.util.List;

import org.obm.caldav.server.share.DavComponentName;



/**
 * http://www.webdav.org/specs/rfc4791.html#rfc.section.9.7.1
 * 
 * @author adrienp
 *
 */
public class CompFilter {
	
//	public final static String VCALENDAR = "vcalendar";
//	public final static String VEVENT = "vevent";
//	public final static String VTODO = "vtodo";
	public final static String NAMESPACE = "urn:ietf:params:xml:ns:caldav";
	
	private DavComponentName name;
	private boolean isNotDefined;
	private TimeRange timeRange;
	private List<PropFilter> propFilters;
	private List<CompFilter> compFilters;
	
	public CompFilter() {
		this.isNotDefined = false;
		this.timeRange = null;
		this.propFilters = new ArrayList<PropFilter>();
		this.compFilters = new ArrayList<CompFilter>();
	}

	public DavComponentName getName() {
		return name;
	}


	public boolean isNotDefined() {
		return isNotDefined;
	}


	public TimeRange getTimeRange() {
		return timeRange;
	}


	public List<PropFilter> getPropFilters() {
		return propFilters;
	}


	public List<CompFilter> getCompFilters() {
		return compFilters;
	}



	public void setName(DavComponentName name) {
		this.name = name;
	}



	public void setIsNotDefined(boolean isNotDefined) {
		this.isNotDefined = isNotDefined;
	}



	public void setTimeRange(TimeRange timeRange) {
		this.timeRange = timeRange;
	}

	public void addPropFilter(PropFilter propFilters) {
		this.propFilters.add(propFilters);
	}

	public void addCompFilter(CompFilter compFilter) {
		this.compFilters.add(compFilter);
	}
	
	public boolean isEmpty(){
		boolean empty = false;
		if(name == null || "".equals(name)){
			empty = true;
		}
		if(isNotDefined){
			empty = true;
		}
		if(timeRange == null || timeRange.isEmpty()){
			empty = true;
		}
		
		if(propFilters.size() == 0){
			empty = true;
		}

		if(compFilters.size() == 0){
			empty = true;
		}
		return empty;
	}
	

}
