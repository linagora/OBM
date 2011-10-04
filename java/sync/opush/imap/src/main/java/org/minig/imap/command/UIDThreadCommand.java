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

package org.minig.imap.command;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MailThread;

public class UIDThreadCommand extends Command<List<MailThread>> {

	public UIDThreadCommand() {
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID THREAD REFERENCES UTF-8 NOT DELETED";
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		data = new LinkedList<MailThread>();

		IMAPResponse ok = rs.get(rs.size() - 1);
		if (ok.isOk()) {
			String threads = null;
			Iterator<IMAPResponse> it = rs.iterator();
			for (int j = 0; j < rs.size() - 1; j++) {
				String resp = it.next().getPayload();
				if (resp.startsWith("* THREAD ")) {
					threads = resp;
					break;
				}
			}

			if (threads != null) {
				parseParenList(data, threads.substring("* THREAD ".length()));
				if (logger.isDebugEnabled()) {
					logger.debug("extracted " + data.size() + " threads");
				}
			}
		}
	}

	private void parseParenList(List<MailThread> data, String substring) {
		int parentCnt = 0;
		MailThread m = new MailThread();
		StringBuilder sb = new StringBuilder();
		for (char c : substring.toCharArray()) {
			if (c == '(') {
				if (parentCnt == 0) {
					m = new MailThread();
				}
				parentCnt++;
			} else if (c == ')') {
				parentCnt--;
				if (sb.length() > 0) {
					sb = addUid(m, sb);
				}
				if (m.size() > 0 && parentCnt == 0) {
					data.add(m);
				}
			} else if (Character.isDigit(c)) {
				sb.append(c);
			} else if (sb.length() > 0) {
				sb = addUid(m, sb);
			}
		}
	}

	private StringBuilder addUid(MailThread m, StringBuilder sb) {
		long l = Long.parseLong(sb.toString());
		m.add(l);
		sb = new StringBuilder();
		return sb;
	}

}
