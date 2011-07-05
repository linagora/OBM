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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.minig.imap.QuotaInfo;
import org.minig.imap.impl.IMAPResponse;

public class QuotaRootCommand extends SimpleCommand<QuotaInfo> {

	public QuotaRootCommand(String mailbox) {
		super("GETQUOTAROOT " + toUtf7(mailbox));
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		data = new QuotaInfo();
		if (isOk(rs)) {
			Pattern p = Pattern.compile("\\* QUOTA .* \\(STORAGE ");
			for (IMAPResponse imapr : rs) {
				if (logger.isDebugEnabled()) {
					logger.debug("Payload " + imapr.getPayload());
				}
				Matcher m = p.matcher(imapr.getPayload());
				if (m.find()) {
					String rep = m.replaceAll("").replaceAll("\\)", "");
					String[] tab = rep.split(" ");
					if (tab.length == 2) {
						data = new QuotaInfo(Integer.parseInt(tab[0]), Integer
								.parseInt(tab[1]));
					}
				}
			}
		}
	}

}
