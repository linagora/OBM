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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.minig.imap.FlagsList;
import org.minig.imap.impl.IMAPResponse;
import org.obm.push.utils.FileUtils;

public class AppendCommand extends Command<Long> {

	private InputStream in;
	private String mailbox;
	private FlagsList flags;

	public AppendCommand(String mailbox, InputStream message, FlagsList flags) {
		this.mailbox = mailbox;
		this.in = message;
		this.flags = flags;
	}

	@Override
	protected CommandArgument buildCommand() {
		StringBuilder cmd = new StringBuilder(50);
		cmd.append("APPEND ");
		cmd.append(toUtf7(mailbox));
		cmd.append(" ");
		if (!flags.isEmpty()) {
			cmd.append(flags.toString());
			cmd.append(" ");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			FileUtils.transfer(in, out, true);
		} catch (IOException e) {
			logger.error("Cannot create tmp buffer for append command", e);
		}

		cmd.append("{");
		cmd.append(out.toByteArray().length);
		cmd.append("}");
		return new CommandArgument(cmd.toString(), out.toByteArray());
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		IMAPResponse r = rs.get(rs.size() - 1);
		if (r.isOk()) {
			String s = r.getPayload();
			int idx = s.lastIndexOf("]");
			int space = s.lastIndexOf(' ', idx - 1);
			data = Long.parseLong(s.substring(space + 1, idx));
		} else {
			data = -1l;
			for (IMAPResponse resp : rs) {
				logger.warn("S: '" + resp.getPayload() + "'");
			}
		}
	}

}
