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
package org.columba.ristretto.io;

import java.io.IOException;

/**
 * Interface definition of Sources.
 * These are the basic data units that the
 * parsers of Ristretto work on.
 * 
 * @author tstich
 *
 */
public interface Source extends CharSequence {
	
	/**
	 * Creates a sub source of this source.
	 * 
	 * @param start the start position
	 * @param end the end position
	 * @return the subsource
	 */
	public Source subSource( int start, int end);
	
	/**
	 * Creates a sub source starting from the actual position.
	 * 
	 * @return the subsource
	 */
	public Source fromActualPosition();
	
	/**
	 * Get the actual read position of the source. 
	 * 
	 * @return the actual read position
	 */
	public int getPosition();
	
	/**
	 * Seek to the given position
	 * 
	 * @param position
	 * @throws IOException
	 */
	public void seek(int position) throws IOException;
	
	/**
	 * Returns the character at the given position
	 * and increases the position.
	 * 
	 * @return the character at the actual position
	 * @throws IOException
	 */
	public char next() throws IOException;
	
	/**
	 * Checks if the end of the Source is reached.
	 * 
	 * @return <code>true</code> if the end of the source is reached
	 */
	public boolean isEOF();
	
	/**
	 * Closes the source.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * Does a deep close of all sources and subsources
	 * associated with this source.
	 * 
	 * @throws IOException
	 */
	public void deepClose() throws IOException;
	
}
