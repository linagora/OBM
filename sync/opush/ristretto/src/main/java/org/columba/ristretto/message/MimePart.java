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
import java.util.List;
import java.util.Vector;


/**
 * A mimepart including a MimeHeader.
 * 
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class MimePart implements Serializable {
	
	private List childs;
	private MimePart parent;
	protected MimeHeader header;
	protected int size;
	boolean alreadyEncoded;
	
	/**
	 * Constructs the MimePart.
	 */
	public MimePart() {
		this( new MimeHeader() );
	}
	
	/**
	 * Constructs the MimePart.
	 * 
	 * @param header
	 */
	public MimePart(MimeHeader header ) {
		this.header = header; 
		childs = new Vector();	
	}
	
	/**
	 * Gets the address.
	 * @return Returns a Integer[]
	 */
	public Integer[] getAddress() {
		List result = new Vector();

		if (parent == null)
			result.add(new Integer(0));
		else {
			MimePart nextParent = parent;
			MimePart nextChild = this;

			while (nextParent != null) {
				result.add(0, new Integer( nextParent.getNumber(nextChild) ));

				nextChild = nextParent;
				nextParent = nextParent.getParent();
			}
		}

		Integer[] returnValue = new Integer[result.size()];
			
		for( int i=0; i<result.size(); i++ )
			returnValue[i] = (Integer) result.get(i);

		return returnValue;
	}

	/**
	 * Returns the parent.
	 * @return MimeTreeNode
	 */
	public MimePart getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 * @param parent The parent to set
	 */
	public void setParent(MimePart parent) {
		this.parent = parent;
	}

	/**
	 * @return the number of child MimeParts.
	 */
	public int countChilds() {
		return childs.size();
	}

	/**
	 * @param nr
	 * @return the Child
	 */
	public MimePart getChild(int nr) {
		return (MimePart) childs.get(nr);
	}

	/**
	 * Add a Child MimePart to this MimePart.
	 * 
	 * @param child
	 */
	public void addChild(MimePart child) {
		if( child == null ) return;
		childs.add(child);
		child.setParent(this);
	}

	/**
	 * Counts the number of unique MimeParts. Multipart/Alternative
	 * MimeParts count as one.
	 * 
	 * @return count the number of unique MimeParts.
	 */
	public int count() {
		// If this is a Multipart/Alternative then return also only 1
		if (header.getMimeType().getSubtype().equals("alternative"))
			return 1;

		if (countChilds() == 0)
			return 1;

		int result = 0;

		for (int i = 0; i < countChilds(); i++) {
			result += getChild(i).count();
		}

		return result;
	}

	/**
	 * @param child
	 * @return the number of the Child MimePart
	 */
	public int getNumber(MimePart child) {
		return childs.indexOf(child);
	}

	/**
	 * @return all Child MimeParts in one List
	 */
	public List getChilds() {
		return childs;
	}

	/**
	 * @return the Header
	 */
	public MimeHeader getHeader() {
		return header;
	}

	/**
	 * Method setHeader.
	 * @param h
	 */
	public void setHeader(MimeHeader h) {
		header = h;
	}
	
	/**
	 * Set the Size in bytes.
	 * 
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the size in bytes 
	 */
	public int getSize() {
		return size;
	}

	public boolean isAlreadyEncoded() {
		return alreadyEncoded;
	}
}
