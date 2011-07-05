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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.minig.imap.SearchQuery;
import org.minig.imap.impl.IMAPResponse;

public class UIDSearchCommand extends Command<Collection<Long>> {

	// Mon, 7 Feb 1994 21:52:25 -0800

	private SearchQuery sq;

	public UIDSearchCommand(SearchQuery sq) {
		this.sq = sq;
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID SEARCH NOT DELETED";
		if (sq.getAfter() != null) {
			DateFormat df = new SimpleDateFormat("d-MMM-yyyy", Locale.ENGLISH);
			cmd += " NOT BEFORE " + df.format(sq.getAfter());
		}
		if (sq.getBefore() != null) {
			DateFormat df = new SimpleDateFormat("d-MMM-yyyy", Locale.ENGLISH);
			cmd += " BEFORE " + df.format(sq.getBefore());
		}
		
		// logger.info("cmd "+cmd);
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		data = Collections.emptyList();

		IMAPResponse ok = rs.get(rs.size() - 1);
		if (ok.isOk()) {
			String uidString = null;
			Iterator<IMAPResponse> it = rs.iterator();
			for (int j = 0; j < rs.size() - 1; j++) {
				String resp = it.next().getPayload();
				if (resp.startsWith("* SEARCH ")) {
					uidString = resp;
					break;
				}
			}

			if (uidString != null) {
				// 9 => '* SEARCH '.length
				String[] splitted = uidString.substring(9).split(" ");
				final List<Long> result = new ArrayList<Long>(splitted.length);
				for (int i = 0; i < splitted.length; i++) {
					result.add(Long.parseLong(splitted[i]));
				}
				data = result;
			}
		}
	}

}
