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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.minig.imap.impl.IMAPResponse;
import org.obm.push.utils.FileUtils;

public class UIDFetchMessageCommand extends Command<InputStream> {

	private long uid;

	public UIDFetchMessageCommand(long uid) {
		this.uid = uid;
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID FETCH " + uid + " (UID BODY.PEEK[])";
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		IMAPResponse stream = rs.get(0);
		IMAPResponse ok = rs.get(rs.size() - 1);
		if (ok.isOk() && stream.getStreamData() != null) {
			InputStream in = stream.getStreamData();
			
			// -1 pattern of the day to remove "\0" at end of stream
			byte[] dest = new byte[0];
			try {
				byte[] byteData = FileUtils.streamBytes(in, true);
				dest = new byte[byteData.length - 1];
				System.arraycopy(byteData, 0, dest, 0, dest.length);
			} catch (IOException e) {
			}
			data = new ByteArrayInputStream(dest);
		} else {
			if (ok.isOk()) {
				logger
						.warn("fetch is ok with no stream in response. Printing received responses :");
				for (IMAPResponse ir : rs) {
					logger.warn("    <= " + ir.getPayload());
				}
				data = new ByteArrayInputStream("".getBytes());
			} else {
				logger.error("UIDFetchMessage failed for uid " + uid + ": "
						+ ok.getPayload());
			}
		}
	}

}
