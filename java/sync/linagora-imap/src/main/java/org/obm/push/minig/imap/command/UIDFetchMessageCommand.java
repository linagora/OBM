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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.obm.push.exception.ImapMessageNotFoundException;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.utils.FileUtils;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

public class UIDFetchMessageCommand extends Command<InputStream> {

	private final static String IMAP_COMMAND = "UID FETCH";
	private final static String IMAP_SUB_COMMAND = "UID BODY.PEEK[]";
	private final long uid;
	private final Long truncation;

	public UIDFetchMessageCommand(long uid) {
		this(uid, null);
	}
	
	public UIDFetchMessageCommand(long uid, Long truncation) {
		this.uid = uid;
		this.truncation = truncation;
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = IMAP_COMMAND + " " + uid + " (" + IMAP_SUB_COMMAND + truncation() + ")";
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	private String truncation() {
		if (truncation != null) {
			return "<0."+truncation+">";
		} else {
			return "";
		}
	}

	@Override
	public String getImapCommand() {
		return IMAP_COMMAND + " " + IMAP_SUB_COMMAND;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		if (!response.getPayload().contains("BODY[]")) {
			return false;
		}
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		InputStream in = response.getStreamData();
		if (in != null) {
			// -1 pattern of the day to remove "\0" at end of stream
			try {
				byte[] byteData = FileUtils.streamBytes(in, true);
				byte[] dest = new byte[byteData.length - 1];
				System.arraycopy(byteData, 0, dest, 0, dest.length);
				
				if (data == null) {
					data = new ByteArrayInputStream(dest);
				} else {
					byte[] byteArray = ByteStreams.toByteArray(data);
					data = new ByteArrayInputStream(Bytes.concat(byteArray, dest));
				}
			} catch (IOException e) {
			}
		} else {
			logger.warn("fetch is ok with no stream in response. Printing received responses :");
			throw new ImapMessageNotFoundException("UIDFetchMessage failed for uid " + uid);
		}
	}
	
	@Override
	public void setDataInitialValue() {
		data = new ByteArrayInputStream(new byte[]{});
	}
}
