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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaIMAPMessage {

	private final static Logger logger = LoggerFactory
			.getLogger(MessageSet.class);

	private List<byte[]> frags;
	private String messageLine;
	public MinaIMAPMessage(String line) {
		this.messageLine = line;
		this.frags = new LinkedList<byte[]>();
	}

	public void addLine(byte[] cur) {
		byte[] prev = new byte[0];
		if (frags.size() > 0) {
			prev = frags.get(frags.size() - 1);
		}

		int i = 0;
		for (; i < cur.length && i < prev.length; i++) {
			if (cur[i] != prev[i]) {
				break;
			}
		}
		byte[] newCur = new byte[cur.length - i];
		System.arraycopy(cur, i, newCur, 0, newCur.length);

		if (logger.isDebugEnabled()) {
			logger.debug("addline cur.len" + cur.length + " prev.len "
					+ prev.length + " cur: " + new String(cur) + " prev: "
					+ new String(prev));
		}

		frags.add(newCur);
	}

	public void addBuffer(ByteBuffer buffer) {
		frags.add(buffer.array());
	}

	public boolean hasFragments() {
		return !frags.isEmpty();
	}

	public String toString() {
		StringBuilder b = new StringBuilder("\nimap command:");
		b.append(messageLine);
		if (frags != null) {
			for (byte[] bu : frags) {
				b.append("[buf:").append(bu.length).append(']');
			}
		}
		return b.toString();
	}

	public List<byte[]> getFragments() {
		return frags;
	}

	public String getMessageLine() {
		return messageLine;
	}

}
