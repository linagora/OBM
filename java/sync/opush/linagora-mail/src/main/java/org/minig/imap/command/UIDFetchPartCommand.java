/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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

package org.minig.imap.command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.minig.imap.impl.IMAPResponse;

import com.google.common.io.ByteStreams;

public class UIDFetchPartCommand extends Command<InputStream> {

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
		String cmd = "UID FETCH " + uid + " (UID BODY.PEEK[" + section + "]" + truncation() + ")";
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
	public void responseReceived(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);
		
		for (IMAPResponse r : rs) {
			logger.debug("r: " + r.getPayload() + " [stream:"
					+ (r.getStreamData() != null) + "]");
		}
		
		IMAPResponse stream = rs.get(0);
		if (isOK && stream.getStreamData() != null) {
			try {
				byte[] rawData = ByteStreams.toByteArray(stream.getStreamData());
				if (rawData[rawData.length - 1] == ')') {
					rawData = Arrays.copyOf(rawData, rawData.length - 1);
				}
				
				data = new ByteArrayInputStream(rawData);
			}
			catch (IOException e) {
				logger.error("Error reading response stream", e);
			}
		} else {
			if (isOK) {
				data = new ByteArrayInputStream("[empty]".getBytes());
			} else {
				IMAPResponse ok = rs.get(rs.size() - 1);
				logger.warn("Fetch of part " + section + " in uid " + uid
						+ " failed: " + ok.getPayload());
			}
		}
	}

}
