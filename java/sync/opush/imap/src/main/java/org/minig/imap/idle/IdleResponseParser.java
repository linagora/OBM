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

package org.minig.imap.idle;

import org.minig.imap.impl.MinaIMAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdleResponseParser {

	private static final Logger logger = LoggerFactory
			.getLogger(IdleResponseParser.class);

	public IdleResponseParser() {
	}

	// * 1 RECENT
	// * 27 EXPUNGE
	// * 35 FETCH (FLAGS (\Deleted \Seen))
	public IdleLine parse(MinaIMAPMessage msg) {
		String response = msg.getMessageLine();
		if(response.startsWith("* BYE")){
			IdleLine r = new IdleLine();
			r.setTag(IdleTag.BYE);
			r.setPayload(response);
			return r;
		}
		String[] tab = response.split(" ");
		if (tab.length > 2) {
			try {
				IdleLine r = new IdleLine();
				r.setTag(IdleTag.getIdleTag(tab[2]));
				r.setPayload(response);
				return r;
			} catch (Exception e) {
				logger.warn("Can't parse idle message[" + response + "]", e);
			}
		} else {
			logger.warn("Can't parse idle message[" + response + "]");
		}
		return null;
	}
}
