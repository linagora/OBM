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

package org.minig.imap.impl;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMAPResponseParser {

	private final static Logger logger = LoggerFactory
			.getLogger(IMAPResponseParser.class);

	private boolean serverHelloReceived;

	public IMAPResponseParser() {
		serverHelloReceived = false;
	}

	public IMAPResponse parse(MinaIMAPMessage msg) {
		String response = msg.getMessageLine();
		IMAPResponse r = new IMAPResponse();
		int idx = response.indexOf(' ');
		if (idx < 0) {
			logger.warn("response without space (forcing bad status): "+response);
			r.setStatus("BAD");
			r.setPayload(response);
			return r;
		}
		
		String tag = response.substring(0, idx);
		r.setTag(tag);
		int statusIdx = response.indexOf(' ', idx + 1);
		if (statusIdx < 0) {
			statusIdx = response.length();
		}
		String status = response.substring(idx + 1, statusIdx);
		if (logger.isDebugEnabled()) {
			logger.debug("TAG: " + tag + " STATUS: " + status);
		}
		r.setStatus(status);

		boolean clientDataExpected = false;
		if ("+".equals(tag) || !"*".equals(tag)) {
			clientDataExpected = true;
		}

		if (!serverHelloReceived) {
			clientDataExpected = true;
			serverHelloReceived = true;
		}
		r.setClientDataExpected(clientDataExpected);

		r.setPayload(response);

		if (msg.hasFragments()) {
			List<byte[]> all = msg.getFragments();
			int len = 0;
			for (byte[] b : all) {
				len += b.length;
			}
			byte[] data = new byte[len];
			int copyIdx = 0;
			for (int i = 0; i < all.size(); i++) {
				// remove closing paren on last response...
				byte[] b = all.get(i);
				int copySize = b.length;
				if (b.length > 0 && i == (all.size() - 1) && b[b.length - 1] == ')') {
					copySize = copySize - 1;
				}
				System.arraycopy(b, 0, data, copyIdx, copySize);
				copyIdx += b.length;
			}
			r.setStreamData(new ByteArrayInputStream(data));
		}

		return r;
	}

	public void setServerHelloReceived(boolean serverHelloReceived) {
		this.serverHelloReceived = serverHelloReceived;
	}

}
