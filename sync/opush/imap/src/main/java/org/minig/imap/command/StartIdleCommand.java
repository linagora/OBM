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

import org.minig.imap.impl.IMAPResponse;

public class StartIdleCommand extends Command<Boolean> {

	public StartIdleCommand() {
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "IDLE";
		if (logger.isDebugEnabled()) {
			logger.debug("cmd: " + cmd);
		}
		return new CommandArgument(cmd, null);
	}

}
