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

package org.minig.imap.sieve;

import java.util.List;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SieveCommand<T> {

	protected static final Logger logger = LoggerFactory
			.getLogger(SieveCommand.class);
	
	protected T retVal;
	private static final byte[] CRLF = "\r\n".getBytes();

	public void execute(IoSession session) {

		List<SieveArg> cmd = buildCommand();

		for (int i = 0; i < cmd.size(); i++) {
			SieveArg arg = cmd.get(i);
			if (!arg.isLiteral()) {
				StringBuilder sb = new StringBuilder(new String(arg.getRaw()));
				if (i < cmd.size() - 1 && cmd.get(i + 1).isLiteral()) {
					SieveArg next = cmd.get(i + 1);
					sb.append(" {");
					sb.append(next.getRaw().length);
					sb.append("+}");
				}

				session.write(sb.toString().getBytes());
			} else {
				session.write(arg.getRaw());
			}
			session.write(CRLF);
		}
	}

	public abstract void responseReceived(List<SieveResponse> rs);

	protected abstract List<SieveArg> buildCommand();

	protected boolean commandSucceeded(List<SieveResponse> rs) {
		return rs.size() > 0 && rs.get(0).getData().endsWith("OK");
	}

	protected void reportErrors(List<SieveResponse> rs) {
		for (SieveResponse sr : rs) {
			logger.error(sr.getData());
		}
	}

	public T getReceivedData() {
		return retVal;
	}

}
