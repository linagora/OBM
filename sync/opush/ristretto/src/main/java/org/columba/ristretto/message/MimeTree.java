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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The BodyStrcuture of a MIME message.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class MimeTree implements Serializable, Iterable {
	MimePart rootMimeNode;
	
	private int uid;

	/**
	 * Constructs the MimeTree.
	 * 
	 * @param root
	 */
	public MimeTree(MimePart root) {
		rootMimeNode = root;
	}

	/**
	 * @param number
	 * @return the MimePart
	 */
	public MimePart get(int number) {
		List leafs = getAllLeafs();

		return (MimePart) leafs.get(number);
	}

	/**
	 * 
	 * 
	 * @return the number of MimeParts.
	 */
	public int count() {
		if (rootMimeNode == null)
			return 0;
		return rootMimeNode.count();
	}

	/**
	 * Clear the MimeTree.
	 */
	public void clear() {
		rootMimeNode = null;
	}

	/**
	 * @return all leaf MimeParts in one list
	 */
	public List getAllLeafs() {
		return getLeafs(rootMimeNode);
	}

	/**
	 * Gets the MimePart with the specified address.
	 * The address is compatible with the address defined
	 * in IMAP (RFC3501). 
	 * 
	 * @param address
	 * @return the MimePart
	 */
	public MimePart getFromAddress(Integer[] address) {		
		// If Root-Address and Root has no nodes return rootNode
		if(( Array.getLength( address ) == 1 ) && ( address[0].intValue() == 0 ) && (rootMimeNode.countChilds() == 0) )
			return rootMimeNode;
		
		MimePart actPart = rootMimeNode;

		for (int i = 0; i < Array.getLength(address); i++) {
			actPart = actPart.getChild(address[i].intValue());
		}

		return actPart;
	}

	/**
	 * Gets the first MimePart that is of content type Text.
	 * If a prefered Subtype is specified first this is searched
	 * for. If none is found the first Text part is returned.
	 * 
	 * @param preferedSubtype a preferred Subtype or <code>null</code>
	 * @return the first found Text MIME part.
	 */
	public MimePart getFirstTextPart(String preferedSubtype) {
		MimePart textPart = getFirstLeafWithContentType(rootMimeNode, "text");

		// Have we found anything ?
		if (textPart == null)
			return null;

		// If nothing prefered return found
		if( preferedSubtype == null )
			return textPart;

		MimeType type = textPart.getHeader().getMimeType();
		// Is it of prefered Subtype?	
		if (type.getSubtype().equals(preferedSubtype))
			return textPart;

		// Try to find better TextPart!

		// Check if part of Multipart/Alternative
		MimePart parent = (MimePart) textPart.getParent();

		if (parent != null) {
			if (parent.getHeader().getMimeType().getSubtype().equals("alternative")) {
				
				
				MimePart nextTextPart;
				List alternatives =
					getLeafsWithContentType(parent, "text");

				
				
				// We can leave the first one out because we checked earlier
				for (int i = 1; i < alternatives.size(); i++) {
					
					nextTextPart = (MimePart) alternatives.get(i);
					
					
					if (nextTextPart
						.getHeader()
						.getMimeType().getSubtype()
						.equals(preferedSubtype))
						return nextTextPart;
				}

				// Nothing better found -> return first found!
			}
		}

		return textPart;
	}

	/**
	 * Gets the first Leaf MimePart with the specified
	 * ContentType.
	 * 
	 * @param root
	 * @param contentType
	 * @return the MimePart
	 */
	public MimePart getFirstLeafWithContentType(
		MimePart root,
		String contentType) {
		MimePart result = null;

		if (root.countChilds() > 0) {

			for (int i = 0; i < root.countChilds(); i++) {
				result =
					getFirstLeafWithContentType(
						(MimePart) root.getChild(i),
						contentType);
				if (result != null)
					return result;
			}

		} else {
			if (root.getHeader().getMimeType().getType().equals(contentType))
				return root;
		}

		return null;
	}

	/**
	 * Collects all leaf MIME parts with the specified ContentType.
	 * 
	 * @param root
	 * @param contentType
	 * @return the List of MIME parts.
	 */
	public List getLeafsWithContentType(
		MimePart root,
		String contentType) {


		LinkedList result = new LinkedList();

		if (root.countChilds() > 0) {			
			for (int i = 0; i < root.countChilds(); i++) {
				result.addAll(
					getLeafsWithContentType(
						(MimePart) root.getChild(i),
						contentType));
			}

		} else {
			if (root.getHeader().getMimeType().getType().equals(contentType))
				result.add(root);
		}

		return result;
	}

	/**
	 * Get all Leafs.
	 * 
	 * @param root
	 * @return all Leafs in one List
	 */
	public List getLeafs(MimePart root) {
		LinkedList result = new LinkedList();

		if (root.countChilds() > 0) {

			for (int i = 0; i < root.countChilds(); i++) {
				result.addAll(getLeafs((MimePart) root.getChild(i)));
			}

		} else {
			result.add(root);
		}

		return result;
	}

	/**
	 * Returns the rootMimeNode.
	 * @return MimePart
	 */
	public MimePart getRootMimeNode() {
		return rootMimeNode;
	}

	/**
	 * Sets the rootMimeNode.
	 * @param rootMimeNode The rootMimeNode to set
	 */
	public void setRootMimeNode(MimePart rootMimeNode) {
		this.rootMimeNode = rootMimeNode;
	}

	public Iterator<MimePart> iterator() {
		return getAllLeafs().iterator();
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

}
