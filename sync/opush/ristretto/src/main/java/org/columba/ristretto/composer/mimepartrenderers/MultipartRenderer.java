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
package org.columba.ristretto.composer.mimepartrenderers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;

import org.columba.ristretto.composer.MimePartRenderer;
import org.columba.ristretto.composer.MimeTreeRenderer;
import org.columba.ristretto.io.SequenceInputStream;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.StreamableMimePart;

/**
 * MIME Multipart renderer.
 * 
 * <br>
 * <b>RFC(s):</b> 2046
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class MultipartRenderer extends MimePartRenderer {

	/**
	 * @see org.columba.ristretto.composer.MimePartRenderer#getRegisterString()
	 */
	public String getRegisterString() {
		return "multipart";
	}

	/**
	 * @see org.columba.ristretto.composer.MimePartRenderer#render(org.columba.ristretto.message.MimePart)
	 */
	public InputStream render(MimePart part) throws Exception {
		Vector<InputStream> streams = new Vector<InputStream>(part.countChilds() * 2 + 3);

		MimeHeader header = part.getHeader();
		
		// Create boundary to separate the mime-parts
		String boundary = createUniqueBoundary().toString();
		header.putContentParameter("boundary", boundary );		
		byte[] startBoundary = ( "\r\n--" + boundary + "\r\n" ).getBytes();
		byte[] endBoundary = ( "\r\n--" + boundary + "--\r\n" ).getBytes();
		
		// Create the header and body (if it has one) of the multipart
		streams.add( header.getHeader().getInputStream() );
		if( part instanceof StreamableMimePart) 
			streams.add( ((StreamableMimePart)part).getInputStream() );
		
		// Add the mime-parts 		
		for( int i=0; i<part.countChilds(); i++ ) {		
			streams.add(new ByteArrayInputStream( startBoundary ));	
			streams.add(MimeTreeRenderer.getInstance().renderMimePart(part.getChild(i)));
		}
		
		// Create the closing boundary
		streams.add( new ByteArrayInputStream( endBoundary ) );
		
		return new SequenceInputStream( streams );
	}

	

}
