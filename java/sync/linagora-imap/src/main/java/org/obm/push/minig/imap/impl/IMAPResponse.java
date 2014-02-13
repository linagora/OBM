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

package org.obm.push.minig.imap.impl;

import java.io.InputStream;

import org.obm.push.minig.imap.mime.impl.AtomHelper;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class IMAPResponse {

	private String status;
	private boolean clientDataExpected;
	private String payload;
	private String tag;
	private InputStream streamData;
	private final Supplier<String> fullResponseSupplier;

	public IMAPResponse() {
		this(null, null);
	}

	public IMAPResponse(String status, String payload) {
		setStatus(status);
		setPayload(payload);
		fullResponseSupplier = buildFullResponseSupplier();
	}

	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isOk() {
		return "OK".equals(status);
	}

	public boolean isNo() {
		return "NO".equals(status);
	}

	public boolean isBad() {
		return "BAD".equals(status);
	}

	public boolean isClientDataExpected() {
		return clientDataExpected;
	}
	
	public boolean isContinuation() {
		return "+".equals(tag);
	}

	public void setClientDataExpected(boolean clientDataExpected) {
		this.clientDataExpected = clientDataExpected;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public InputStream getStreamData() {
		return streamData;
	}

	public void setStreamData(InputStream streamData) {
		this.streamData = streamData;
	}
	
	public String getFullResponse() {
		return fullResponseSupplier.get();
	}

	private Supplier<String> buildFullResponseSupplier() {
		return Suppliers.memoize(new Supplier<String>() {

			@Override
			public String get() {
				if (payload == null) {
					return null;
				}
				return AtomHelper.getFullResponse(payload, streamData);
			}
		});
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (tag != null) {
			builder.append("[").append(tag).append("] ");
		}
		builder.append(payload);
		return builder.toString();
	}
}
