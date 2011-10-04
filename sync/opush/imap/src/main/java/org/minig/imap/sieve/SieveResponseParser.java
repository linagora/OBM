/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.minig.imap.sieve;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SieveResponseParser {

	private static final Logger logger = LoggerFactory
			.getLogger(SieveResponseParser.class);

	public void parse(List<SieveResponse> toFill, SieveMessage sm) {
		for (String l : sm.getLines()) {
			int idx = l.lastIndexOf("\r\n");
			String data = l.substring(0, idx);
			if (logger.isDebugEnabled()) {
				logger.debug("parsed: '" + data + "' len: " + data.length());
			}
			toFill.add(new SieveResponse(data));
		}
	}

}
