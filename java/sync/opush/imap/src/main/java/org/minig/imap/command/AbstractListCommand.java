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

import java.util.List;

import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.impl.IMAPResponse;

public class AbstractListCommand extends SimpleCommand<ListResult> {

	protected boolean subscribedOnly;

	protected AbstractListCommand(boolean subscribedOnly) {
		super((subscribedOnly ? "LSUB " : "LIST ") + "\"\" \"*\"");
		this.subscribedOnly = subscribedOnly;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		ListResult lr = new ListResult(rs.size() - 1);
		for (int i = 0; i < rs.size() - 1; i++) {
			String p = rs.get(i).getPayload();
			if (!p.contains( subscribedOnly? "LSUB " : " LIST ")) {
				continue;
			}
			int oParen = p.indexOf('(', 5);
			int cPren = p.indexOf(')', oParen);
			String flags = p.substring(oParen + 1, cPren);
			if (i == 0) {
				char imapSep = p.charAt(cPren + 3);
				lr.setImapSeparator(imapSep);
			}
			String mbox = fromUtf7(p.substring(cPren + 7, p.length()-1));
			lr.add(new ListInfo(mbox, !flags.contains("\\Noselect"), !flags.contains("\\Noinferiors")));
		}
		data = lr;
	}

}
