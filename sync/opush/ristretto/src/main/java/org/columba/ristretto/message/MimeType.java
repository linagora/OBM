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

/**
 * Represents the RFC2045 defined mimetyp of the Content-Type headerfield.
 * 
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class MimeType {

	private String type;
	private String subtype;

	/**
	 * Constructs the MIME type.
	 * 
	 * @param type
	 * @param subtype
	 */
	public MimeType( String type, String subtype) {
		this.type = type;
		this.subtype = subtype;
	}

	/**
	 * Constructs the MIME type.
	 */
	public MimeType() {
		this( "text", "plain");
	}

	/**
	 * @return the Subtype.
	 */
	public String getSubtype() {
		return subtype;
	}

	/**
	 * @return the ContentType.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param string
	 */
	public void setSubtype(String string) {
		subtype = string;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		 boolean result = true;
		 MimeType arg = (MimeType) arg0;
		 if( type != null) {
		 	result &= this.type.equals(arg.type);
		 }
		 if( subtype != null) {
		 	result &= this.subtype.equals((arg.subtype));
		 }
		 return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return type + '/' + subtype;
	}

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.toString().hashCode();
    }
    
}
