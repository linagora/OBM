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
 * 
 * @author adrienp
 *
 */
public class ParamFilter {
	
	private String name;
	private boolean isNotDefined;
	private TextMatch textMatch;
	
	public ParamFilter() {
		super();
		this.name = "";
		this.isNotDefined = false;
		this.textMatch = null;
	}
	
	public String getName() {
		return name;
	}
	public boolean isNotDefined() {
		return isNotDefined;
	}
	public TextMatch getTextMatch() {
		return textMatch;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setNotDefined(boolean isNotDefined) {
		this.isNotDefined = isNotDefined;
	}
	public void setTextMatch(TextMatch textMatch) {
		this.textMatch = textMatch;
	}
	
	

}
