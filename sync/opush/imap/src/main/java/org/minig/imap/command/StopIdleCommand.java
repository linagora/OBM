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
import java.util.concurrent.Semaphore;

import org.apache.mina.common.IoSession;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.TagProducer;

public class StopIdleCommand extends Command<Boolean> {

	public StopIdleCommand() {
	}

	@Override
	public void execute(IoSession session, TagProducer tp, Semaphore lock,
			List<IMAPResponse> lastResponses) {
		CommandArgument args = buildCommand();
		session.write(args.getCommandString());
		lock.release();
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "DONE";
		if (logger.isDebugEnabled()) {
			logger.debug("cmd: " + cmd);
		}

		return new CommandArgument(cmd, null);
	}

}
