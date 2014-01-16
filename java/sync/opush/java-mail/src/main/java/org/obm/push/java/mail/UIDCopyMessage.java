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
package org.obm.push.java.mail;

import java.util.Iterator;
import java.util.NoSuchElementException;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.protocol.IMAPProtocol;

public class UIDCopyMessage extends IMAPCommand<Long> {

	private static final String COMMAND_UIDCOPY_NAME = "UID COPY";
	private static final String RESPONSE_UIDCOPY_BEGIN = "[COPYUID ";
	private static final String RESPONSE_UIDCOPY_END = "]";
	private static final String RESPONSE_UIDCOPY_SEPARATOR = " ";
	
	private final String folderDst;
	private final Long messageUid;

	public UIDCopyMessage(final String folderDst, final long messageUid) {
		Preconditions.checkNotNull(Strings.emptyToNull(folderDst));
		
		this.folderDst = folderDst;
		this.messageUid = messageUid;
	}
	
	@Override
	public Long doCommand(IMAPProtocol protocol) throws ProtocolException {
		Argument args = buildUIDCOPYCommandArgs(folderDst, messageUid);
		Response lastResponse = doUIDCOPYCommand(protocol, args);
		return parseNewMessageUID(lastResponse);
	}

	private Argument buildUIDCOPYCommandArgs(final String folderDst, final Long messageUid) {
		
		String folderDstEncoded = encodeMailboxForIMAP(folderDst);

		Argument args = new Argument();	
		args.writeAtom(String.valueOf(messageUid));
		args.writeString(folderDstEncoded);
		return args;
	}

	private Response doUIDCOPYCommand(IMAPProtocol protocol, Argument args) throws ProtocolException {
		return doCommandThenGetLastResponse(protocol, COMMAND_UIDCOPY_NAME, args);
	}

	@VisibleForTesting long parseNewMessageUID(Response lastResponse) throws ProtocolException {
		String responseValue = lastResponse.getRest();
		String uidsToParse = parseUIDsInResponse(responseValue);
		return findNewMessageUID(uidsToParse);
	}

	@VisibleForTesting String parseUIDsInResponse(String responseValue) throws ProtocolException {
		int startParsingIndex = findStartParsingIndex(responseValue);
		int endParsingIndex = responseValue.indexOf(RESPONSE_UIDCOPY_END, startParsingIndex);
		try {
			String uidsInResponse = responseValue.substring(startParsingIndex, endParsingIndex);
			return uidsInResponse;
		} catch (IndexOutOfBoundsException e) {
			String msg = String.format("The response isn't formated as expected, received:%s startIndex:%d endIndex:%d",
					responseValue, startParsingIndex, endParsingIndex);
			throw new ProtocolException(msg, e);
		}
	}

	private int findStartParsingIndex(String responseValue) throws ProtocolException {
		int indexWithResponseTags = responseValue.indexOf(RESPONSE_UIDCOPY_BEGIN);
		if (indexWithResponseTags >= 0) {
			return indexWithResponseTags + RESPONSE_UIDCOPY_BEGIN.length();
		}
		String msg = String.format(
				"The response doesn't contains the expected tag:%s , received:%s", RESPONSE_UIDCOPY_BEGIN, responseValue);
		throw new ProtocolException(msg); 
	}

	@VisibleForTesting long findNewMessageUID(String uidsToParse) throws ProtocolException {
		Iterator<String> splittedResponse = Splitter.on(RESPONSE_UIDCOPY_SEPARATOR)
													.omitEmptyStrings()
													.split(uidsToParse).iterator();
													
		try {
			splittedResponse.next(); // UIDValidity
			String uidCopied = splittedResponse.next();
			String uidOfNewMessage = splittedResponse.next();
			
			assertEndOfTheResponse(splittedResponse);
			assertUidCopiedIsTheExpectedOne(uidCopied);
			return uidStringToLong(uidOfNewMessage);
		} catch (NoSuchElementException e) {
			String msg = String.format("The response doesn't contains values as expected," +
					" expected:['UIDVALIDITY' 'COPIED_UID' 'NEW_UID'] received:[%s]", uidsToParse);
			throw new ProtocolException(msg, e);
		}
	}
	
	private void assertEndOfTheResponse(Iterator<String> splittedResponse) throws ProtocolException {
		if (splittedResponse.hasNext()) {
			String nextArg = splittedResponse.next();
			if (!Strings.isNullOrEmpty(nextArg)){
				throw new ProtocolException("Too many args are received, unexpected value:" + nextArg);
			} else {
				assertEndOfTheResponse(splittedResponse);
			}
		}
	}

	private void assertUidCopiedIsTheExpectedOne(String uidCopied) throws ProtocolException {
		long uidCopiedAsLong = uidStringToLong(uidCopied);
		if (!messageUid.equals(uidCopiedAsLong)) {
			String msg = String.format(
					"Server sent uid copied isn't the expected one. Expected:%d Received:%d", messageUid, uidCopiedAsLong);
			throw new ProtocolException(msg);
		}
	}

	private long uidStringToLong(String uidOfNewMessage) throws ProtocolException {
		try {
			return Long.parseLong(uidOfNewMessage);
		} catch (NumberFormatException e) {
			throw new ProtocolException("Failing parsing uid, it should be a long value", e);
		}
	}
}
