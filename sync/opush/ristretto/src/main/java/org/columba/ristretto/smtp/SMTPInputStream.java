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
package org.columba.ristretto.smtp;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.columba.ristretto.io.ConnectionDroppedException;
import org.columba.ristretto.parser.ParserException;
import org.columba.ristretto.smtp.parser.SMTPResponseParser;

/**
 * This stream is used to read responses from a SMTP server. Every response is
 * finalized by a CR LF. The stream reads a line from the stream and pases it to
 * the {@link SMTPResponseParser}.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class SMTPInputStream extends FilterInputStream {

	private StringBuilder lineBuffer;

	/**
	 * Constructs a SMTPInputStream.
	 * 
	 * @param arg0
	 *            the inputstream of the socket.
	 */
	public SMTPInputStream(InputStream arg0) {
		super(arg0);

		lineBuffer = new StringBuilder();
	}

	/**
	 * Reads a response from the socket inputstream. Every response is finalized
	 * by a CR LF. The stream reads a line from the stream and pases it to the
	 * {@link SMTPResponseParser}.
	 * 
	 * @return the received response.
	 * @throws IOException
	 * @throws SMTPException
	 */
	public SMTPResponse readSingleLineResponse() throws IOException,
			SMTPException {
		readLineInBuffer();

		try {
			return SMTPResponseParser.parse(lineBuffer);
		} catch (ParserException e) {
			throw new SMTPException("Malformed answer from server", e);
		}
	}

	/**
	 * Reads until a CR LF terminates the reponse.
	 * 
	 * @throws IOException
	 */
	private void readLineInBuffer() throws IOException {
		// Clear the buffer
		lineBuffer.delete(0, lineBuffer.length());

		int read = in.read();
		// read until CRLF
		while (read != '\r' && read != -1) {
			lineBuffer.append((char) read);
			read = in.read();
		}
		lineBuffer.append((char) read);

		// read the LF
		read = in.read();
		if (read != '\n')
			throw new ConnectionDroppedException();
		lineBuffer.append((char) read);
	}
}
