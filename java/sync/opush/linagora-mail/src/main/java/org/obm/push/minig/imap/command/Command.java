/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */

package org.obm.push.minig.imap.command;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.MailboxNameUTF7Converter;
import org.obm.push.minig.imap.impl.TagProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public abstract class Command<T> implements ICommand<T> {

	protected final Logger logger = LoggerFactory.getLogger("IMAP.DEBUG");
	protected final Logger imaplogger = LoggerFactory.getLogger("IMAP.COMMAND");
	
	protected T data;
	
	@Override
	public WriteFuture execute(IoSession session, TagProducer tp, Semaphore lock) {
		
		CommandArgument args = buildCommand();
		String cmd = args.getCommandString();
		StringBuilder sb = new StringBuilder(10 + cmd.length());
		sb.append(tp.nextTag());
		sb.append(' ');
		sb.append(cmd);
		String sent = sb.toString();
		imaplogger.info("C: {}", sent);
		WriteFuture writeFuture = session.write(sent);
		if (args.hasLiteralData()) {
			writeFuture.awaitUninterruptibly();
			if (writeFuture.isWritten()) {
				lock(lock);
				writeFuture = session.write(args.getLiteralData());
			}
		}
		return writeFuture;

	}

	@Override
	public final void responseReceived(List<IMAPResponse> lastResponses) {
		if (imaplogger.isInfoEnabled()) {
			imaplogger.info("Command status {}", isOk(lastResponses) ? "SUCCESS" : "FAILURE");
			imaplogger.info("S: {}", Joiner.on('\n').join(lastResponses));
		}
		handleResponses(lastResponses);
	}
	
	@Override
	public void handleResponses(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);
		
		setDataInitialValue();
		if (isOK) {
			for (IMAPResponse response : rs) {
				if (isMatching(response)) {
					handleResponse(response);
				}
			}
		} else {
			IMAPResponse ok = rs.get(rs.size() - 1);
			logger.warn("error on {}: {}", getImapCommand(), ok.getPayload());
		}
	}

	@Override
	public void setDataInitialValue() {
		data = null;
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
	
	protected static String toUtf7WithoutQuotes(String partition) {
		return MailboxNameUTF7Converter.encode(partition);
	}

	protected boolean isOk(List<IMAPResponse> rs) {
		return Iterables.getLast(rs).isOk();
	}

	protected static String fromUtf7(String mailbox) {
		return MailboxNameUTF7Converter.decode(mailbox);
	}
}
