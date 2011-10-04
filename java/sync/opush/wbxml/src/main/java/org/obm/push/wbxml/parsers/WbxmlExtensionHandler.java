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
