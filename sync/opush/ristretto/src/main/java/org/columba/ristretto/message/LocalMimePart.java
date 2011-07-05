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

import java.io.InputStream;

import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.io.Source;
import org.columba.ristretto.io.SourceInputStream;

/**
 * LocalMimeParts refer to parsed messages that come from a {@link Source}
 * 
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class LocalMimePart extends StreamableMimePart {
	private Source source;
	private Source body;

	/**
	 * Constructs the LocalMimePart.
	 * 
	 * @param header
	 */
	public LocalMimePart( MimeHeader header) {
		super(header);
		body = new CharSequenceSource( "" );
		alreadyEncoded = false;
	}

	/**
	 * Constructs the LocalMimePart.
	 * 
	 * @param header
	 * @param body
	 */
	public LocalMimePart( MimeHeader header, Source body) {
		super(header);
		this.body = body;
	}
	
	/**
	 * Constructs the LocalMimePart.
	 * 
	 * @param header
	 * @param body
	 */
	public LocalMimePart( MimeHeader header, Source body, boolean alreadyEncoded) {
		super(header);
		this.body = body;
		this.alreadyEncoded = alreadyEncoded;
	}

	/**
	 * @return the Source of the MimePart.
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * Set the source. This completes also the header source.
	 * 
	 * @param source
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * Gets an InputStream of the mimeparts body.
	 * 
	 * @see org.columba.ristretto.io.Streamable#getInputStream()
	 */
	public InputStream getInputStream() {
		return new SourceInputStream( body.fromActualPosition() );
	}

	/**
	 * @return Returns the body.
	 */
	public Source getBody() {
		return body;
	}

	/**
	 * @param body The body to set.
	 */
	public void setBody(Source body) {
		this.body = body;
	}

	/**
	 * @param body The body to set.
	 */
	public void setBody(Source body, boolean alreadyEncoded) {
		this.body = body;
		this.alreadyEncoded = alreadyEncoded;
	}

}
