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
import java.util.Arrays;

import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

public class UIDFetchPartCommand extends Command<InputStream> {

	private final static String IMAP_COMMAND = "UID FETCH";
	private final static String IMAP_SUB_COMMAND = "UID BODY.PEEK";
	private long uid;
	private String section;
	private final Long truncation;

	public UIDFetchPartCommand(long uid, String section) {
		this(uid, section, null);
	}

	public UIDFetchPartCommand(long uid, String section, Long truncation) {
		this.uid = uid;
		this.section = section;
		this.truncation = truncation;
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = IMAP_COMMAND + " " + uid + " (" + IMAP_SUB_COMMAND + "[" + section + "]" + truncation() + ")";
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
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		if (response.getStreamData() != null) {
			try {
				byte[] rawData = ByteStreams.toByteArray(response.getStreamData());
				if (rawData[rawData.length - 1] == ')') {
					rawData = Arrays.copyOf(rawData, rawData.length - 1);
				}
				
				if (data == null) {
					data = new ByteArrayInputStream(rawData);
				} else {
					byte[] byteArray = ByteStreams.toByteArray(data);
					data = new ByteArrayInputStream(Bytes.concat(byteArray, rawData));
				}
			}
			catch (IOException e) {
				logger.error("Error reading response stream", e);
			}
		} else {
			if (data == null) {
				data = new ByteArrayInputStream("[empty]".getBytes());
			}
		}
	}
	
	@Override
	public void setDataInitialValue() {
		data = new ByteArrayInputStream(new byte[]{});
	}
}
