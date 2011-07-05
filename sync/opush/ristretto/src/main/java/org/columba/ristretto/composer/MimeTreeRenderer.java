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
import java.util.Hashtable;

import org.columba.ristretto.composer.mimepartrenderers.DefaultMimePartRenderer;
import org.columba.ristretto.composer.mimepartrenderers.MultipartRenderer;
import org.columba.ristretto.composer.mimepartrenderers.TextpartRenderer;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;

/**
 * Provides methods to render a MimeTree.
 * The MimeTree is invokes the registered MimePartRenderer.
 * To register a MimePartRenderer call {@link #addMimePartRenderer(MimePartRenderer)}.
 * 
 * <br>
 * <b>RFC(s):</b> 2046
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */

public class MimeTreeRenderer {
	
	private Hashtable<String, MimePartRenderer> rendererMap;
	
	private static MimeTreeRenderer myInstance;
	
	private MimePartRenderer defaultRenderer;
	
	private MimeTreeRenderer() {
		defaultRenderer = new DefaultMimePartRenderer();
		rendererMap = new Hashtable<String, MimePartRenderer>();
		addMimePartRenderer( new MultipartRenderer() );
		addMimePartRenderer( new TextpartRenderer() );
		
	}

	/**
	 * Adds a MimePartRenderer. The instance of the MimePartRenderer
	 * is used for all rendering of the registered MimeParts.
	 * 
	 * @param renderer the new mimepartrenderer
	 */
	public void addMimePartRenderer( MimePartRenderer renderer ) {
		rendererMap.put(renderer.getRegisterString(), renderer);
	}
	
	/**
	 * Gets the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static MimeTreeRenderer getInstance() {
		
		if( myInstance == null )
			myInstance = new MimeTreeRenderer();
		
		return myInstance;	
	}
	
	/**
	 * Renders a MimeTree.
	 * 
	 * @param tree the mimetree to render
	 * @return the inputstream of the rendered mimetree
	 * 
	 * @throws Exception
	 */
	public InputStream renderMimeTree( MimeTree tree ) throws Exception {
		return renderMimePart( tree.getRootMimeNode() );
	}
	
	/**
	 * Renders a MimePart.
	 * 
	 * @param part the mimepart to render
	 * @return the inputstream of the rendered mimepart
	 * 
	 * @throws Exception
	 */
	public InputStream renderMimePart( MimePart part ) throws Exception {		
		MimePartRenderer renderer = getRenderer( part.getHeader().getMimeType() );
		
		return renderer.render(part);	
	}
	
	private MimePartRenderer getRenderer( MimeType type ) {
		
		// If no ContentType specified return StandardParser
		if (type == null)
			return defaultRenderer;

		MimePartRenderer renderer;

		// First try to find renderer for "type/subtype"

		renderer =
			(MimePartRenderer) rendererMap.get(
				type.toString());
		if (renderer != null) {
			return renderer;
		}

		// Next try to find renderer for "type"

		renderer = (MimePartRenderer) rendererMap.get(type.getType());
		if (renderer != null) {
			return renderer;
		}

		// Nothing found -> return Standardrenderer
		return defaultRenderer;
		
	}

}
