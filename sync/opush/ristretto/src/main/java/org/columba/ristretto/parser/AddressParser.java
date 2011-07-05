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
package org.columba.ristretto.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.columba.ristretto.message.Address;

/**
 * Parser for mail addresses as defined in RFC 2822.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class AddressParser {
	
    private static final Pattern addressTokenizerPattern =
        Pattern.compile("([^\\s]+?)(\\s|<|$)+");
    
    private static final Pattern trimPattern =
        Pattern.compile("[\"<]?([^\"<>]*)[\">]?");        
    
    //TODO implement Group parsing
    
    
	/**
	 * Parses a mailbox-list like specified in RFC 2822. The result is a List
	 * of { @link Address } objects. 
	 * 
	 * @param mailboxList CharSequence to parse 
	 * @return	Array of parsed addresses
	 * @throws ParserException
	 */
	public static Address[] parseMailboxList(CharSequence mailboxList) throws ParserException {
		List<Address> result = new LinkedList<Address>();
		CharSequence[] tokens = tokenizeList(mailboxList);

		for( int i=0; i< tokens.length; i++) {
			try {
				result.add(parseAddress(tokens[i]));
			} catch (ParserException e) {
			}
		}
		
		Address[] a = new Address[result.size()];
		result.toArray(a);
		return a;
	}
	
	/**
	 * Parses a address like specified in RFC 2822. The result is a 
	 * of { @link Address } objects. 
	 * 
	 * @param address CharSequence to parse 
	 * @return	parsed addresses
	 * @throws ParserException
	 */
	public static Address parseAddress(CharSequence address) throws ParserException {
		Matcher addressTokenizer = addressTokenizerPattern.matcher(address);
	    
		ArrayList<String> tokens = new ArrayList<String>();
		
		while (addressTokenizer.find()) {
		    tokens.add(addressTokenizer.group(1));
		}
		
		if (tokens.size() < 1) {
                    throw new ParserException("No valid EMail address", address);
                } else if (tokens.size() == 1) {
		    return new Address(trim((String)tokens.get(0)));
		} else {
		    StringBuilder name = new StringBuilder((String)tokens.get(0));
		    
		    for (int i = 1; i < tokens.size() - 1; i++) {
		        name.append(' ');
		        name.append((String)tokens.get(i));		        
		    }
		    
		    return new Address(trim(name),
                            trim((String)tokens.get(tokens.size() - 1)));
		}
	}

	private static String trim(CharSequence input) {
	    Matcher trimMatcher = trimPattern.matcher(input);
	    
	    if( trimMatcher.matches() ) {	   
	        return trimMatcher.group(1);
	    } else {
	        return input.toString();
	    }
	}
	
	private static CharSequence[] tokenizeList(CharSequence input) {
		List<CharSequence> result = new ArrayList<CharSequence>();
		boolean quoted= false;
		
		int start = 0;
		int i;
		
		for( i=0; i<input.length(); i++) {
			char c = input.charAt(i);
			
			switch( c ) {			
				case '\"' : {
					quoted ^= true;
					break;
				}
				
				case ',' : {
					if( ! quoted ) {
						result.add(input.subSequence(start, i));
						start = i+1;
					}
					break;
				}
			}
		}
		if( start < i ) {
			result.add(input.subSequence(start, i));			
		}		
		
		return (CharSequence[])result.toArray(new CharSequence[0]);
	}
}
