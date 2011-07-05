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

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.columba.ristretto.coder.EncodedWord;
import org.columba.ristretto.parser.AddressParser;
import org.columba.ristretto.parser.DateParser;
import org.columba.ristretto.parser.ParserException;

/**
 * Wrapper around the {@link Header} to provide a convienient
 * way to access all important headerfields defined in RFC822.
 * 
 * 
 */
public class BasicHeader {
	
	/**
	 * Priority value
	 */
	public final static int HIGHEST = 5;
	/**
	 * Priority value
	 */
	public final static int HIGH = 4;
	/**
	 * Priority value
	 */
	public final static int NORMAL = 3;
	/**
	 * Priority value
	 */
	public final static int LOW = 2;
	/**
	 * Priority value
	 */
	public final static int LOWEST = 1;
	
	private static final Pattern whiteSpaceTokenizer = Pattern.compile("\\s*([^\\s]+)");
	
	private Header header;
	
	/**
	 * Constructs a BasicHeader from a {@link Header}
	 * 
	 * @param header parsed 
	 */
	public BasicHeader( Header header ) {
		this.header = header;
	}

	/**
	 * Gets the field "From" from the header and uses the {@link AddressParser}
	 * to return a parsed {@link Address} list
	 * 
	 * @return {[@link Adress} list
	 */
	public Address getFrom() {
		return getAddress(header.get("From"));
	}

	/**
	 * Gets the field "Sender" from the header and uses the {@link AddressParser}
	 * to return a parsed {@link Address} list
	 * 
	 * @return {[@link Adress} list
	 */
	public Address getSender() {
		return getAddress(header.get("Sender"));
	}
	
	/**
	 * Gets the field "X-BeenThere" from the header and uses the {@link AddressParser}
	 * to return a parsed {@link Address} list
	 * 
	 * @return {[@link Adress} list
	 */
	public Address getBeenThere() {
	    Address result = getAddress(header.get("X-BeenThere"));
	    if( result == null ) {
	        result = getAddress(header.get("X-Beenthere"));
	    }
	    
	    return result;
	}

	/**
	 * Gets the field "To" from the header and uses the {@link AddressParser}
	 * to return a parsed {@link Address} list
	 * 
	 * @return {[@link Adress} list
	 */
	public Address[] getTo() {
		return getMailboxlist(header.get("To"));
	}

	/**
	 * Gets the field "Cc" from the header and uses the {@link AddressParser}
	 * to return a parsed {@link Address} list
	 * 
	 * @return {[@link Adress} list
	 */
	public Address[] getCc() {
		return getMailboxlist(header.get("Cc"));
	}
	
	/**
	 * Gets the field "Bcc" from the header and uses the {@link AddressParser}
	 * to return a parsed {@link Address} list
	 * 
	 * @return {[@link Adress} list
	 */
	public Address[] getBcc() {
		return getMailboxlist(header.get("Bcc"));
	}
	
	/**
	 * Gets the field "Reply-To" from the header and uses the {@link AddressParser}
	 * to return a parsed {@link Address} list
	 * 
	 * @return {[@link Adress} list
	 */
	public Address[] getReplyTo() {
		return getMailboxlist(header.get("Reply-To"));
	}

	/**
	 * Gets the field "Subject" from the header and uses the {@link EncodedWord} to
	 * decode an encoded subject.
	 * 
	 * @return human-readable subject
	 */
	public String getSubject() {
		String subject = header.get("Subject");
		if( subject != null) return EncodedWord.decode( subject ).toString();
		else return ""; 
	}
	
	/**
	 * Gets the field "Message-ID" from the header
	 * 
	 * @return the message-id
	 */
	public String getMessageID() {
		return header.get("Message-ID");
	}
	
	/**
	 * Gets the field "References" from the header and splits the list 
	 * in an Array of message-ids
	 * 
	 * @return list of message-ids
	 */
	public String[] getReferences() {
		String references = header.get("References");
		return getWhitespaceSeparatedList(references);
	}

	/**
	 * Gets the field "In-Reply-To" from the header and splits the list 
	 * in an Array of message-ids
	 * 
	 * @return list of message-ids
	 */
	public String[] getInReplyTo() {
		String references = header.get("In-Reply-To");
		return getWhitespaceSeparatedList(references);
	}

	/**
	 * Gets the field "X-Priority" from the header and 
	 * converts the value to an int. Constants for the priorities
	 * are also defined in this class.
	 * If an error occurs while converting or no "X-Priority" field
	 * is defined, {@link #NORMAL} priority is returned.
	 * 
	 * @return priority of the message
	 */
	public int getPriority() {
		String priority = header.get("X-Priority");
		if( priority != null ) {
			try {
				return Integer.parseInt(priority);
			} catch (NumberFormatException e) {
				return NORMAL;
			}
		}
		return NORMAL;
	}
	
	/**
	 * Gets the field "Date" from the header and uses the {@link DateParser}
	 * to parse it into a {@link Date} object.
	 * If an error occurs the current date is returned.
	 * 
	 * @return sent date of the message
	 */
	public Date getDate() {
		String date = header.get("Date");
		if( date != null ) {
			try {
				return DateParser.parse( removeComments(date) );
			} catch (ParserException e) {
				return new Date();
			}
		}
		return new Date();
	}

	/**
	 * Sets the field "From" of the header and uses {@link EncodedWord}
	 * to encode names if needed in UTF-8 using QuotedPrintable encoding. 
	 * We chose UTF-8 to cover any possible characters.
	 * 
	 * @param address	mailaddresses with or without names
	 */
	public void setFrom( Address address ) {
		header.set("From", address.toString() );
	}

	/**
	 * Sets the field "To" of the header and uses {@link EncodedWord}
	 * to encode names if needed in UTF-8 using QuotedPrintable encoding. 
	 * We chose UTF-8 to cover any possible characters.
	 * 
	 * @param addresses	mailaddresses with or without names
	 */
	public void setTo( Address[] addresses ) {
		setAddressList(addresses, "To");
	}

	/**
	 * Sets the field "Cc" of the header and uses {@link EncodedWord}
	 * to encode names if needed in UTF-8 using QuotedPrintable encoding. 
	 * We chose UTF-8 to cover any possible characters.
	 * 
	 * @param addresses	mailaddresses with or without names
	 */
	public void setCc( Address[] addresses ) {
		setAddressList(addresses, "Cc");
	}

	/**
	 * Sets the field "Bcc" of the header and uses {@link EncodedWord}
	 * to encode names if needed in UTF-8 using QuotedPrintable encoding. 
	 * We chose UTF-8 to cover any possible characters.
	 * 
	 * @param addresses	mailaddresses with or without names
	 */
	public void setBcc( Address[] addresses ) {
		setAddressList(addresses, "Bcc");
	}

	/**
	 * Sets the field "Reply-To" of the header and uses {@link EncodedWord}
	 * to encode names if needed in UTF-8 using QuotedPrintable encoding. 
	 * We chose UTF-8 to cover any possible characters.
	 * 
	 * @param addresses	mailaddresses with or without names
	 */
	public void setReplyTo( Address[] addresses ) {
		setAddressList(addresses, "Reply-To");
	}

	/**
	 * Sets the field "Date" of the header by using {@link MessageDate}
	 * to convert the {@link Date} object into a RFC2822 compatible
	 * date string
	 * 
	 * @param date of the message creation
	 */
	public void setDate( Date date ) {
		header.set("Date",MessageDate.toString(date));
	}
	
	/**
	 * Sets the field "Message-ID" of the header. This should be
	 * a unique ID that only this message has. RFC2822 says the
	 * following about this field:
	 * "The "Message-ID:" field provides a unique message identifier that 
	 * refers to a particular version of a particular message.  The
   	 * uniqueness of the message identifier is guaranteed by the host that
     * generates it (see below).  This message identifier is intended to be
     * machine readable and not necessarily meaningful to humans.  A message
     * identifier pertains to exactly one instantiation of a particular
     * message; subsequent revisions to the message each receive new message
     * identifiers." (RFC2822, p.23)
	 * 
	 * @param messageid unique id of this message
	 */
	public void setMessageID(String messageid) {
		header.set( "Message-ID", messageid);
	}
	
	/**
	 * Sets the field "References" of the header. The messageids are
	 * the references this message refers to. The information of this
	 * and the "In-Repy-To" field can be used by mailclients e.g. to create a threaded view of
	 * a discussion
	 * 
	 * @param messageids the messageids of the messages this message refers to
	 * 
	 * @see #setInReplyTo
	 */
	public void setReferences( String[] messageids ) {
		setMessageIDList( "References", messageids );
	}
	
	/**
	 * Sets the field "In-Reply-To" of the header. The messageids are
	 * the references this message refers to. The information of this
	 * and the "In-Repy-To" field can be used by mailclients e.g. to create a threaded view of
	 * a discussion
	 * 
	 * @param messageids the messageids of the messages this message refers to
	 * 
	 * @see #setReferences
	 */
	public void setInReplyTo( String[] messageids ) {
		setMessageIDList( "In-Reply-To", messageids );
	}

	/**
	 * Sets the field "X-Priority" of the header. Use the provided constants
	 * to specify the priority.
	 * 
	 * @param priority of the message
	 */
	public void setPriority( int priority ) {
		header.set( "X-Priority", Integer.toString(priority));
	}
	
	/**
	 * Sets the field "Subject" of the header using {@link EncodedWord} to
	 * encode the subject with the given charset (e.g. ISO-8859-1) and Quoted-
	 * Printable encoding.
	 * 
	 * @param subject of the message
	 * @param charset the subject is encoded in
	 */
	public void setSubject( String subject, Charset charset ) {
		header.set("Subject", EncodedWord.encode(subject, charset,EncodedWord.QUOTED_PRINTABLE));
	}

	private void setMessageIDList( String key, String[] messageids) { 
		StringBuilder field = new StringBuilder( messageids[0] );
		for( int i=1; i<messageids.length; i++) {
			field.append(' ');
			field.append(messageids[i]);
		}
		
		header.set(key, EncodedWord.encode(field, Charset.forName("UTF-8"), EncodedWord.QUOTED_PRINTABLE).toString());
	}

	private void setAddressList(Address[] addresses, String fieldKey) {
		StringBuilder addressfield = new StringBuilder(addresses[0].toString()); 
		
		for( int i=1; i<addresses.length; i++) {
			addressfield.append( ", ");
			addressfield.append( addresses[i].toString() );				
		}
		
		header.set( fieldKey, addressfield.toString());		
	}

	private String[] getWhitespaceSeparatedList(String references) {
		if( references != null ) {
			LinkedList<String> referenceList = new LinkedList<String>();
			Matcher matcher = whiteSpaceTokenizer.matcher(references);
			while( matcher.matches() ) {
				referenceList.add(matcher.group(1));
			}
			return referenceList.toArray(new String[referenceList.size()]);
		}
		return new String[0];
	}

	private Address[] getMailboxlist(String from) {
		if( from != null) {
			try {
				return AddressParser.parseMailboxList( EncodedWord.decode(removeComments(from)) );
			} catch (ParserException e) {
				//TODO use logging here
				return new Address[] {};
			}
		}
		return new Address[] {};
	}

	private Address getAddress(String from) {
		if( from != null && from.length() > 0) {
			try {
				return AddressParser.parseAddress( EncodedWord.decode(removeComments(from)) );
			} catch (ParserException e) {
				//TODO use logging here
				return null;
			}
		}
		
		return null;
	}


	/**
	 * @return the number of headers
	 */
	public int count() {
		return header.count();
	}

	/**
	 * Get the header with the specified key.
	 * 
	 * @param key
	 * @return the header
	 */
	public String get(String key) {
		return header.get(key);
	}

	/**
	 * Set the header with the specified key.
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, Object value) {
		header.set(key, value);
	}

	private static final int PLAIN = 0;
	private static final int QUOTED = 1;
	private static final int COMMENT = 2;
	
	
	/**
	 * Remove any comments as defined in RFC2822 from the
	 * String.
	 * 
	 * @param value
	 * @return the comment-free String
	 */
	public static final String removeComments(String value) {
		StringBuilder result = new StringBuilder( value.length() );
		
		int state = PLAIN;
		int depth = 0;
		char current;
		
		for( int i=0; i < value.length(); i++) {
			current = value.charAt(i);

			switch( current ) {
				case( '\"' ) : {
					if( state == COMMENT ) break;
					
					if( state == QUOTED ) state = PLAIN;
					else state = QUOTED;
					result.append(current);					
					break;
				}
				
				case( '(' ) :  {
					if( state == QUOTED ) {
						result.append(current);
						break;
					}
					
					if( state == COMMENT ) {
						depth++;
					} else {
						state = COMMENT;
						depth = 1;
					}
					break;
				}
				
				case( ')') : {
					if( state == QUOTED ) {
						result.append(current);
						break;
					}
					if( state == COMMENT ) {
						depth--;
						if( depth == 0) state = PLAIN;
						break;
					}
					result.append(current);
					break;
				}
				
				default : {
					if( state != COMMENT ) result.append( current);
				}
				
			}
		}
				
		return result.toString();
	}
	
	/**
	 * Get all keys from the header.
	 * 
	 * @return Enumeration of keys
	 */
	public Enumeration<String> getKeys() {
		return header.getKeys();
	}

}
