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
package org.obm.push.mail;

import org.minig.imap.EmailView;
import org.minig.imap.Flag;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmail.MSEmailBuilder;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.utils.SerializableInputStream;

import com.google.inject.Singleton;

@Singleton
public class MailViewToMSEmailConverterImpl implements MailViewToMSEmailConverter {

	
	
	@Override
	public MSEmail convert(EmailView emailView) {
		MSEmailBuilder msEmailBuilder = new MSEmail.MSEmailBuilder();
		msEmailBuilder.uid(emailView.getUid());
		
		fillFlags(msEmailBuilder, emailView);
		msEmailBuilder.header(convertHeader(emailView));
		msEmailBuilder.body(convertBody(emailView));
		
		return msEmailBuilder.build();
	}

	private void fillFlags(MSEmailBuilder msEmailBuilder, EmailView emailView) {
		msEmailBuilder.answered(hasFlag(emailView, Flag.ANSWERED));
		msEmailBuilder.read(hasFlag(emailView, Flag.SEEN));
		msEmailBuilder.starred(hasFlag(emailView, Flag.FLAGGED));
	}
	
	private MSEmailHeader convertHeader(EmailView emailView) {
		return new MSEmailHeaderConverter().convertToMSEmailHeader(emailView.getEnvelope());
	}

	private MSEmailBody convertBody(EmailView emailView) {
		SerializableInputStream mimeData = new SerializableInputStream(emailView.getBodyMimePartData());
		MSEmailBodyType bodyType = MSEmailBodyType.fromMimeType(emailView.getBodyContentType().getFullMimeType());
		Integer bodyTruncation = emailView.getBodyTruncation();
		
		return new MSEmailBody(mimeData, bodyType, null, bodyTruncation);
	}

	private boolean hasFlag(EmailView emailView, Flag flag) {
		return emailView.getFlags().contains(flag);
	}
	
}
