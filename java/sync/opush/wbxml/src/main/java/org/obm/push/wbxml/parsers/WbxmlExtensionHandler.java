/* ***** BEGIN LICENSE BLOCK *****
 *
 * %%
 * "Copyleft" 1999, Stefan Haustein, Oberhausen, NW, Germany. 
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * ***** END LICENSE BLOCK ***** */

package org.obm.push.wbxml.parsers;

import org.xml.sax.SAXException;

/** A class can handle the WAP XML extensions by implementing 
    this interface. */

public interface WbxmlExtensionHandler {

    /** called when EXT_I_0, EXT_I_1, or EXT_I_2 is detected 
	in the document */

    public void ext_i (int id, String par) throws SAXException;

    /** called when EXT_I_0, EXT_I_1, or EXT_I_2 is detected 
	in the document */

    public void ext_t (int id, int par) throws SAXException;

    /** called when EXT_T_0, EXT_T_1, or EXT_T_2 is detected 
	in the document */

    public void ext (int id) throws SAXException;

    /** called when the OPAQUE token is detected 
	in the document */

    public void opaque (byte [] data) throws SAXException;
}    
