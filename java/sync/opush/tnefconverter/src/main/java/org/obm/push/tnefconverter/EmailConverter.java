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
package org.obm.push.tnefconverter;

import java.io.InputStream;
import java.nio.charset.Charset;

import net.freeutils.tnef.Message;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.columba.ristretto.composer.MimeTreeRenderer;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeType;
import org.obm.push.tnefconverter.ScheduleMeeting.ScheduleMeeting;
import org.obm.push.tnefconverter.ScheduleMeeting.ScheduleMeetingEncoder;
import org.obm.push.tnefconverter.ScheduleMeeting.TNEFExtractorUtils;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailConverter {

	private static Logger logger = LoggerFactory
			.getLogger(EmailConverter.class);

	public InputStream convert(InputStream email) throws TNEFConverterException {
		try {
			MimeStreamParser parser = new MimeStreamParser();
			EmailTnefHandler handler = new EmailTnefHandler();
			parser.setContentHandler(handler);
			parser.parse(email);

			Message message = handler.getTNEFMsg();
			if (message != null) {
				if (logger.isDebugEnabled()) {
					logger.debug(message.toString());
				}
				if (TNEFExtractorUtils.isScheduleMeetingRequest(message)) {
					ScheduleMeeting meeting = new ScheduleMeeting(message);
					ScheduleMeetingEncoder encoder = new ScheduleMeetingEncoder(
							meeting, handler.getSubject(), handler.getFrom(),
							handler.getTo(), handler.getCc());
					String ics = encoder.encodeToIcs();
					if (logger.isDebugEnabled()) {
						logger.debug("ICS from tnef: " + ics);
					}

					Header rootHeader = new Header();

					BasicHeader basicHeader = new BasicHeader(rootHeader);
					basicHeader.setTo(handler.getTo().toArray(new Address[0]));
					basicHeader.setSubject(handler.getSubject(), Charset
							.defaultCharset());
					basicHeader.set("Thread-Topic", handler.getSubject());
					basicHeader.setFrom(handler.getFrom());
					if (handler.getCc().size() > 0) {
						basicHeader.setCc(handler.getCc().toArray(
								new Address[0]));
					}

					MimeHeader mh = new MimeHeader(rootHeader);
					mh.set("MIME-Version", "1.0");
					mh.setMimeType(new MimeType("multipart", "mixed"));
					LocalMimePart root = new LocalMimePart(mh);

					MimeHeader textMimeHeader = new MimeHeader(new Header());
					textMimeHeader.set("Content-Type",
							"text/plain; charset=\"UTF-8\"");
					textMimeHeader.setContentTransferEncoding("8bit");
					LocalMimePart textPart = new LocalMimePart(textMimeHeader);
					textPart.setBody(new CharSequenceSource(meeting
							.getDescription()));
					root.addChild(textPart);

					MimeHeader requestMimeHeader = new MimeHeader(new Header());
					String method = "REQUEST";
					switch (meeting.getMethod()) {
					case ScheduleMeetingCanceled:
						method = "CANCEL";
						break;
					case ScheduleMeetingRequest:
						method = "REQUEST";
						break;
					case ScheduleMeetingRespPos:
					case ScheduleMeetingRespNeg:
					case ScheduleMeetingRespTent:
						method = "REPLY";
						break;
					}
					requestMimeHeader.set("Content-Type",
							"text/calendar; charset=\"UTF-8\"; method="
									+ method + "; charset=\"UTF-8\"");
					requestMimeHeader.setContentTransferEncoding("8bit");
					LocalMimePart requestPart = new LocalMimePart(
							requestMimeHeader);
					requestPart.setBody(new CharSequenceSource(ics.replace(
							"\0", "")
							+ "\n"));
					root.addChild(requestPart);

					MimeHeader tnefMimeHeader = new MimeHeader(new Header());
					tnefMimeHeader.set("Content-Type", "application/ms-tnef");
					tnefMimeHeader.set("X-MS-Has-Attach", "");
					tnefMimeHeader.setContentTransferEncoding("base64");
					tnefMimeHeader.setContentDisposition("attachment");
					LocalMimePart tnefPart = new LocalMimePart(tnefMimeHeader);
					byte[] b = FileUtils
							.streamBytes(handler.getTnefDoc(), true);
					tnefPart.setBody(new CharSequenceSource(new String(Base64
							.encodeBase64(b))));
					root.addChild(tnefPart);
					InputStream in = MimeTreeRenderer.getInstance()
							.renderMimePart(root);
					return in;
				}
			}
			return null;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new TNEFConverterException(e);
		}
	}
	
	
}
