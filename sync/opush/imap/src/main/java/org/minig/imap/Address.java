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

import org.obm.push.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple mail address representation
 * 
 * @author tom
 * 
 */
public final class Address {

	private static final Logger logger = LoggerFactory.getLogger(Address.class);
	
	private String mail;
	private String displayName;

	public Address(String mail) {
		this(null, mail);
	}

	public Address(String displayName, String mail) {
		if (displayName != null) {
			this.displayName = StringUtils
					.stripAddressForbiddenChars(displayName);
		}
		if (mail != null && mail.contains("@")) {
			this.mail = StringUtils.stripAddressForbiddenChars(mail);
		} else {
			// FIXME ...
			if (logger.isDebugEnabled()) {
				logger
						.debug("mail: "
								+ mail
								+ " is not a valid email, building a john.doe@minig.org");
			}
			this.displayName = StringUtils.stripAddressForbiddenChars(mail);
			this.mail = "john.doe@minig.org";
		}
	}

	public String getMail() {
		return mail;
	}

	public String getDisplayName() {
		return displayName != null ? displayName : mail;
	}

	@Override
	public String toString() {
		return "" + displayName + " <" + mail + ">";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mail == null) ? 0 : mail.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		if (mail == null) {
			if (other.mail != null)
				return false;
		} else if (!mail.equals(other.mail))
			return false;
		return true;
	}

}
