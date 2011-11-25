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
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.minig.imap;

import com.google.common.base.Objects;

public final class Address {

	private final String mail;
	private final String displayName;

	public Address(String mail) {
		this(null, mail);
	}
	
	public Address(String displayName, String mail) {
		this.displayName = formatString(displayName);
		this.mail = formatMail(mail);
	}

	private String formatString(String str) {
		if (str != null) {
			return str.replace("\"", "").replace("<", "").replace(">", "");
		}
		return str;
	}
	
	private String formatMail(String mail) {
		if (mail != null && mail.contains("@")) {
			return formatString(mail);
		}
		return mail;
	}
	
	public String getMail() {
		return mail;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(mail, displayName);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Address) {
			Address that = (Address) object;
			return Objects.equal(this.mail, that.mail)
				&& Objects.equal(this.displayName, that.displayName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("mail", mail)
			.add("displayName", displayName)
			.toString();
	}

}