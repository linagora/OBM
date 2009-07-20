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
public class TextMatch {

	private boolean negateCondition;
	private String collation;
	private String text;

	public TextMatch() {
		this.negateCondition = false;
		this.collation = "";
		this.text = "";
	}

	public boolean isNegateCondition() {
		return negateCondition;
	}

	public String getCollation() {
		return collation;
	}

	public String getText() {
		return text;
	}

	public void setNegateCondition(boolean negateCondition) {
		this.negateCondition = negateCondition;
	}

	public void setCollation(String collation) {
		this.collation = collation;
	}

	public void setText(String text) {
		this.text = text;
	}

}
