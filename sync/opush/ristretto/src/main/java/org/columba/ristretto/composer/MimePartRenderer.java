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
package org.columba.ristretto.composer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import org.columba.ristretto.coder.Base64;
import org.columba.ristretto.message.MimePart;


/**
 * Abstract class that is used to render a mimepart
 * 
 * <br>
 * <b>RFC(s):</b> 2045
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public abstract class MimePartRenderer {
	private static final int BOUNDARY_LENGTH = 32;
	
	private static Random random = new Random();


	/**
	 * The renderer is registered for the returned mimetype(s)
	 * when calling {@link org.columba.ristretto.composer.MimeTreeRenderer#addMimePartRenderer(MimePartRenderer)}.
	 * <br>
	 * A renderer may register for all subtypes of a given type or to a
	 * specific type/subtype.
	 * <br>
	 * <b>Example:</b> "text", "multipart/signed", ...
	 * 
	 * @return the registration string
	 */
	public abstract String getRegisterString();


	/**
	 * Renders the MimePart
	 * 
	 * @param part the mimepart to render
	 * @return the inputstream of the rendered mimepart
	 * @throws Exception
	 */
	public abstract InputStream render(MimePart part) throws Exception;


	/**
	 * Creates a unique boundary that may be used to separate multiparts
	 * 
	 * @return a unique boundary
	 */
	protected CharSequence createUniqueBoundary() {		
		byte[] bytes = new byte[BOUNDARY_LENGTH];
		random.nextBytes(bytes);
		
		return Base64.encode( ByteBuffer.wrap(bytes) );
	}
	
}
