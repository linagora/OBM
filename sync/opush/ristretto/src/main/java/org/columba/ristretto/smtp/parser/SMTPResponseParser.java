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
package org.columba.ristretto.smtp.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.columba.ristretto.parser.ParserException;
import org.columba.ristretto.smtp.SMTPResponse;

/**
 * Parses a response line from a SMTP server.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class SMTPResponseParser {

	private static final Pattern responsePattern = Pattern.compile("(\\d+)" + // group
																				// 1 =
																				// response
																				// code
			"(-| )?" + // group 2 = hyphen
			"([^\r\n]+)?\r\n"); // group 3 = response message

	private static final Pattern greetingPattern = Pattern.compile("^([^\\s]*)"
			+ // group 1 = domain
			" ?(.*)$"); // group 2 = message

	/**
	 * Parses a response line from a SMTP server. This line is finished with a
	 * CR LF and interpreted like defined in RFC2821.
	 * 
	 * @see SMTPResponse
	 * 
	 * @param in
	 *            the reponse line
	 * @return the parse response
	 * @throws ParserException
	 */
	public static SMTPResponse parse(CharSequence in) throws ParserException {
		Matcher matcher = responsePattern.matcher(in);
		if (matcher.matches()) {
			int code = Integer.parseInt(matcher.group(1));
			if (code != 220) {
				return new SMTPResponse(code,
						matcher.group(2) != null ? matcher.group(2).equals("-")
								: false, matcher.group(3));
			} else {
				// find domain
				Matcher greetingMatcher = greetingPattern.matcher(matcher
						.group(3));
				if (greetingMatcher.matches()) {
					return new SMTPResponse(code,
							matcher.group(2) != null ? matcher.group(2).equals(
									"-") : false, greetingMatcher.group(2),
							greetingMatcher.group(1));

				} else {
					throw new ParserException(in.toString());
				}
			}
		}

		throw new ParserException(in.toString());
	}
}
