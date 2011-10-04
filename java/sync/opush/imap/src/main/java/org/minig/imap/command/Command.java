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
import org.minig.imap.impl.MailboxNameUTF7Converter;
import org.minig.imap.impl.TagProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Command<T> implements ICommand<T> {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected T data;

	@Override
	public void execute(IoSession session, TagProducer tp, Semaphore lock,
			List<IMAPResponse> lastResponses) {
		CommandArgument args = buildCommand();

		String cmd = args.getCommandString();
		StringBuilder sb = new StringBuilder(10 + cmd.length());
		sb.append(tp.nextTag());
		sb.append(' ');
		sb.append(cmd);
		String sent = sb.toString();
		if (logger.isDebugEnabled()) {
			logger.debug("=> '" + sent + "'");
		}
		session.write(sent);
		if (args.hasLiteralData()) {
			lock(lock);
			session.write(args.getLiteralData());
		}

	}

	private void lock(Semaphore lock) {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public T getReceivedData() {
		return data;
	}

	protected abstract CommandArgument buildCommand();

	protected static String toUtf7(String mailbox) {
		String ret = MailboxNameUTF7Converter.encode(mailbox);
		StringBuilder b = new StringBuilder(ret.length() + 2);
		b.append("\"");
		b.append(ret);
		b.append("\"");
		return b.toString();
	}

	protected boolean isOk(List<IMAPResponse> rs) {
		return rs.get(rs.size() - 1).isOk();
	}

	protected static String fromUtf7(String mailbox) {
		return MailboxNameUTF7Converter.decode(mailbox);
	}
}
