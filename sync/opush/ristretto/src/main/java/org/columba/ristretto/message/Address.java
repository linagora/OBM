/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Ristretto Mail API.
 *
 * The Initial Developers of the Original Code are
 * Timo Stich and Frederik Dietz.
 * Portions created by the Initial Developers are Copyright (C) 2004
 * All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.columba.ristretto.message;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.columba.ristretto.coder.EncodedWord;
import org.columba.ristretto.parser.AddressParser;
import org.columba.ristretto.parser.ParserException;

/**
 * Address is the representation of a mail address as specified by RFC 2822. It
 * consistis of a human readable display name and the technical mail address.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class Address implements Serializable, Comparable<Address> {

	private String displayName;
	private String mailAddress;

	static final long serialVersionUID = -7663098020871087578l;

	/**
	 * Construct a Address with a valid mail address that must not be parsed.
	 * <p>
	 * <b>Note:</b> If you want to parse an Address from a String use the
	 * <code>static</code> {@link #parse(CharSequence)} instead.
	 * 
	 * @param mailAddress
	 *            a valid mail address (e.g. my@mail.org).
	 */
	public Address(String mailAddress) {
		this("", mailAddress);
	}

	/**
	 * Contruct a Address with a valid mail address and a displayname.
	 * <p>
	 * <b>Note:</b> If you want to parse an Address from a String use the
	 * <code>static</code> {@link #parse(CharSequence)} instead.
	 * 
	 * @param displayName
	 *            the name to display for this address, i.e. the real name (e.g.
	 *            Timo Stich).
	 * @param mailAddress
	 *            A valid mail address (e.g. my@mail.org).
	 */

	public Address(String displayName, String mailAddress) {
		this.displayName = displayName;
		this.mailAddress = mailAddress;
	}

	/**
	 * Gets the displayname. The returned String can be used to give a human
	 * readable representation of the mail address.
	 * <p>
	 * Example: "Timo Stich"
	 * 
	 * @return the human readable String representation or <code>null</code> if
	 *         not set.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Gets the mail address. This is the technical part of the mail address as
	 * defined in RFC 2822.
	 * <p>
	 * Example: my@mail.org
	 * 
	 * @return the mail address
	 */
	public String getMailAddress() {
		return mailAddress;
	}

	/**
	 * Get the canonical form of the mailaddress. This is basically the
	 * technical mail address in brackets and is defined in RFC 2822.
	 * <p>
	 * Example: &lt;my@mail.org&gt;
	 * 
	 * @return the canonical form of the mailaddress
	 */
	public String getCanonicalMailAddress() {
		return '<' + mailAddress + '>';
	}

	/**
	 * Gets the short form of the address. This is either the human readable
	 * display name if available or the technical mail address.
	 * <p>
	 * Examples: "Timo Stich" or "my@mail.org"
	 * 
	 * @return the short form of the address
	 */
	public String getShortAddress() {
		if ((displayName != null) && (displayName.length() > 0)) {
			return displayName;
		} else {
			return mailAddress;
		}
	}

	/**
	 * Sets the displayname. This is the human readable part of the address.
	 * <p>
	 * Example: "Timo Stich"
	 * 
	 * @param displayName
	 *            the human readable representation of the address.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		boolean result = false;
		if ((arg0 != null) && (arg0 instanceof Address)) {
			Address a = (Address) arg0;
			result = true;
			if (mailAddress != null) {
				result = result
						&& mailAddress.equalsIgnoreCase(a.getMailAddress());
			}
		}

		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (displayName == null || displayName.length() == 0) {
			return mailAddress;
		} else {
			StringBuilder sb = EncodedWord.encode(displayName, Charset
					.forName("UTF-8"), EncodedWord.QUOTED_PRINTABLE);
			sb.append(' ');
			sb.append(getCanonicalMailAddress());
			return sb.toString();
		}
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		if (displayName != null) {
			// write a 1 in front so we know we have a displayname
			out.writeByte(1);
			out.writeUTF(displayName);
			out.writeUTF(mailAddress);
		} else {
			// write a 0 in front so we know we have no displayname
			out.writeByte(0);
			out.writeUTF(mailAddress);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		byte mode = in.readByte();

		if (mode == 1) {
			displayName = in.readUTF();
		}

		mailAddress = in.readUTF();
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Address arg0) {
		return mailAddress.compareToIgnoreCase(arg0.getMailAddress());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return mailAddress.hashCode();
	}

	/**
	 * Uses the {@link AddressParser} to parse the CharSequence.
	 * 
	 * @param source
	 *            the address that is parsed, e.g.
	 *            "Timo Stich &lt;my@mail.org&gt;"
	 * @return the Address object that represents the input CharSequence
	 * @throws ParserException
	 */
	public static Address parse(CharSequence source) throws ParserException {
		return AddressParser.parseAddress(source);
	}
}
