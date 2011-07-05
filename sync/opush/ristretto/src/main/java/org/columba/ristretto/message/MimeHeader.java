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

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.columba.ristretto.coder.EncodedWord;
import org.columba.ristretto.io.Streamable;
import org.columba.ristretto.parser.MimeTypeParser;

/**
 * Wrapper around a {@link Header} to convieniently access Mime-Specific header
 * fields.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class MimeHeader implements Streamable, Serializable {

	private static final long serialVersionUID = 3049187467929003211L;

	/**
	 * Transfer encoding.
	 */
	public final static int PLAIN = 0; // default
	/**
	 * Transfer encoding.
	 */
	public final static int QUOTED_PRINTABLE = 1;
	/**
	 * Transfer encoding.
	 */
	public final static int BASE64 = 2;

	private Header header;

	/**
	 * Constructs a MimeHeader with a new instanciated {@link Header}.
	 * <p>
	 * Sets the {@link MimeType} to "text/plain".
	 * 
	 */
	public MimeHeader() {
		this(new Header());
		setMimeType(new MimeType("text", "plain"));
	}

	/**
	 * Constructs a MimeHeader from a {@link Header}
	 * 
	 * @param header
	 */
	public MimeHeader(Header header) {
		this.header = header;
	}

	/**
	 * Constructs a MimeHeader with a new instanciated {@link Header}.
	 * <p>
	 * 
	 * @param type
	 *            type of the mime-part (e.g. text)
	 * @param subtype
	 *            subtype of the mime-part (e.g. plain)
	 */
	public MimeHeader(String type, String subtype) {
		this.header = new Header();
		setMimeType(new MimeType(type, subtype));
	}

	/**
	 * Gets the {@link MimeType} of the mime-part.
	 * <p>
	 * Intern the {@link MimeTypeParser} is used to parse the headerfield
	 * "Content-Type" to retrieve the mimetype.
	 * 
	 * @return {@link MimeType} of the mime-part
	 */
	public MimeType getMimeType() {
		return MimeTypeParser.parse(header.get("Content-Type"));
	}

	/**
	 * Gets the mimetype of the mime-part (e.g. text)
	 * 
	 * @return mimetype of the mime-part
	 * 
	 * @deprecated Use {@link #getMimeType()} instead
	 */
	public String getContentType() {
		return getMimeType().getType();
	}

	/**
	 * Gets the mimesubtype of the mime-part (e.g. plain)
	 * 
	 * @return mimesubtype of the mime-part
	 * 
	 * @deprecated Use {@link #getMimeType()} instead
	 */
	public String getContentSubtype() {
		return getMimeType().getSubtype();
	}

	/**
	 * Gets the content-disposition of the mime-part. This can be either
	 * "attachment" of "inline". MailClients may use this information to either
	 * display this mime-part inside the message-body or as a separate
	 * attachment
	 * <p>
	 * See RFC2183
	 * 
	 * @return content disposition of the mime-part
	 */
	public String getContentDisposition() {
		return this.header.get("Content-Disposition");
	}

	/**
	 * Gets a content parameter of the mime-part. This parameter is specified in
	 * the "Content-Type" headerfield e.g. the boundary of a multipart or the
	 * charset of a text part
	 * <p>
	 * See RFC2045
	 * 
	 * @param key
	 *            the parameter name (e.g. boundary)
	 * @return the value of the parameter or null if not found
	 */
	public String getContentParameter(String key) {
		return getParameter(header.get("Content-Type"), key);
	}

	/**
	 * Gets the content transfer encoding of the mime-part. This may be either
	 * base64 or quoted-printable.
	 * <p>
	 * See RFC2045 for more information about the encodings.
	 * 
	 * @return the content transfer encoding
	 */
	public int getContentTransferEncoding() {
		String value = header.get("Content-Transfer-Encoding");
		if (value != null) {
			if (value.equalsIgnoreCase("quoted-printable"))
				return QUOTED_PRINTABLE;
			if (value.equalsIgnoreCase("base64"))
				return BASE64;
		}

		return PLAIN;
	}

	/**
	 * Gets the content id of the mime-part. Content IDs are used to reference
	 * to e.g. images that are attached to the message inside a html message
	 * body.
	 * <p>
	 * See RFC2393.
	 * 
	 * @return content id of the mime-part
	 */
	public String getContentID() {
		String result = header.get("Content-ID");
		if (result == null) {
			return header.get("Content-Id");
		}
		return result;
	}

	/**
	 * Gets the content description of the mime-part.
	 * <p>
	 * See RFC2045
	 * 
	 * @return the content description
	 */
	public String getContentDescription() {
		return header.get("Content-Description");
	}

	/**
	 * Gets a parameter of the content disposition of the mime-part. This might
	 * be e.g. a filename.
	 * <p>
	 * See RFC2183
	 * 
	 * @param key
	 * @return the disposition parameter
	 */
	public String getDispositionParameter(String key) {
		return getParameter(header.get("Content-Disposition"), key);
	}

	protected String getParameter(String headerLine, String key) {
		if (headerLine == null)
			return null;
		Pattern parameterPattern = Pattern.compile(key
				+ "\\s*=\\s*((\"([^\"]+)\")|([^\r\n\\s;]+))",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = parameterPattern.matcher(headerLine);
		if (matcher.find()) {
			if (matcher.group(3) != null) {
				return matcher.group(3);
			} else {
				return matcher.group(4);
			}
		}
		return null;
	}

	protected String appendParameter(String headerLine, String key, String value) {
		StringBuffer result = new StringBuffer(headerLine);
		result.ensureCapacity(headerLine.length() + 2 + key.length() + 1
				+ value.length());
		result.append("; ");
		result.append(key);
		result.append('=');
		result.append(value);
		return result.toString();
	}

	/**
	 * Gets the filename of the mime-part. Use this method to check both places
	 * a filename may be specified:
	 * <ul>
	 * <li> as the content-type parameter name
	 * <li> as the content-disposition parameter filename
	 * </ul>
	 * 
	 * 
	 * @return filename of the mime-part
	 */
	public String getFileName() {
		String result = null;

		result = getContentParameter("name");
		if (result != null)
			return EncodedWord.decode(result).toString();

		result = getDispositionParameter("filename");
		if (result != null)
			return EncodedWord.decode(result).toString();

		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return header.toString();
	}

	/**
	 * Adds a content parameter to the mime-part. This parameter is specified in
	 * the "Content-Type" headerfield e.g. the boundary of a multipart or the
	 * charset of a text part
	 * <p>
	 * Note: Make sure that you called {@link #setMimeType(MimeType)} <b>before</b>
	 * you call this method, because it will erase all previously added
	 * parameters
	 * <p>
	 * See RFC2045
	 * 
	 * @param key
	 *            the parameter name (e.g. boundary)
	 * @param value
	 *            the value of the parameter
	 */
	public void putContentParameter(String key, String value) {
		header.set("Content-Type", appendParameter(header.get("Content-Type"),
				key, "\"" + value + "\""));
	}

	/**
	 * Adds a parameter to the content disposition of the mime-part. This might
	 * be e.g. a filename.
	 * <p>
	 * Note: Make sure that you called {@link #setContentDisposition(String)}
	 * <b>before</b> you call this method, because it will erase all previously
	 * added parameters
	 * <p>
	 * <p>
	 * See RFC2183
	 * 
	 * @param key
	 * @param value
	 */
	public void putDispositionParameter(String key, String value) {
		header.set("Content-Disposition", appendParameter(header
				.get("Content-Disposition"), key, "\"" + value + "\""));
	}

	/**
	 * Sets the mime-type of the mime-part. For text parts of the message this
	 * is something like text/plain or text/html, while an image might be of
	 * type image/jpeg.
	 * <p>
	 * See RFC2046 for more mime-types
	 * 
	 * @param type
	 *            {@link MimeType} of the mime-part
	 */
	public void setMimeType(MimeType type) {
		header.set("Content-Type", type.toString());
	}

	/**
	 * Sets the content description of the mime-part.
	 * <p>
	 * Note: Make sure that you called this method <b>before</b> you call
	 * {@link #putContentParameter(String,String)}, because it will erase all
	 * previously added parameters
	 * <p>
	 * See RFC2045
	 * 
	 * @param description
	 *            of the mime-part
	 */
	public void setContentDescription(String description) {
		header.set("Content-Description", description);
	}

	/**
	 * Sets the content-disposition of the mime-part. This can be either
	 * "attachment" of "inline". MailClients may use this information to either
	 * display this mime-part inside the message-body or as a separate
	 * attachment
	 * <p>
	 * Note: Make sure that you called this method <b>before</b> you call
	 * {@link #putDispositionParameter(String,String)}, because it will erase
	 * all previously added parameters
	 * <p>
	 * See RFC2183
	 * 
	 * @param disposition
	 *            of the message
	 */
	public void setContentDisposition(String disposition) {
		header.set("Content-Disposition", disposition);
	}

	/**
	 * Sets the content id of the mime-part. Content IDs are used to reference
	 * to e.g. images that are attached to the message inside a html message
	 * body.
	 * <p>
	 * See RFC2393.
	 * 
	 * @param id
	 *            of the mime-part
	 */
	public void setContentID(String id) {
		header.set("Content-ID", id);
	}

	/**
	 * Sets the content transfer encoding of the mime-part. This may be either
	 * base64 or quoted-printable.
	 * <p>
	 * See RFC2045 for more information about the encodings.
	 * 
	 * @param encoding
	 *            of the mime-part
	 */
	public void setContentTransferEncoding(String encoding) {
		header.set("Content-Transfer-Encoding", encoding);
	}

	/**
	 * @return the number of defined Headers.
	 */
	public int count() {
		return header.count();
	}

	/**
	 * Get the Header for the specified key.
	 * 
	 * @param key
	 * @return the Header
	 */
	public String get(String key) {
		return header.get(key);
	}

	/**
	 * Set the header with the specified value.
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, Object value) {
		header.set(key, value);
	}

	/**
	 * Gets the charset this mime-part is encoded in. If no charset parameter is
	 * found or the charset is not supported by the VM the default charset of
	 * the system is returned.
	 * 
	 * @return {@link Charset} of the mime-part
	 */
	public Charset getCharset() {
		String charsetField = getContentParameter("charset");

		if (charsetField != null) {
			try {
				return Charset.forName(charsetField);
			} catch (IllegalCharsetNameException e) {
				return Charset.forName(System.getProperty("file.encoding"));
			} catch (UnsupportedCharsetException e) {
				return Charset.forName(System.getProperty("file.encoding"));
			}
		} else {
			return Charset.forName(System.getProperty("file.encoding"));
		}
	}

	/**
	 * @return the Header
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * Set the Header.
	 * 
	 * @param header
	 */
	public void setHeader(Header header) {
		this.header = header;
	}

	/**
	 * @see org.columba.ristretto.io.Streamable#getInputStream()
	 */
	public InputStream getInputStream() {
		return header.getInputStream();
	}

}
