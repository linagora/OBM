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
package org.obm.push.mail.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.FetchInstruction;
import org.obm.push.mail.MailTransformation;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;

public class HtmlToText implements Transformer {

	public static class Factory implements Transformer.Factory {
		
		@Override
		public Transformer create(FetchInstruction fetchInstruction) {
			return new HtmlToText();
		}

		@Override
		public MailTransformation describe() {
			return MailTransformation.TEXT_PLAIN_TO_TEXT_HTML;
		}
	}

	@Override
	public InputStream transform(InputStream input, Charset charset) throws IOException {
		Preconditions.checkNotNull(input);
		Preconditions.checkNotNull(charset);
		List<String> lines = CharStreams.readLines(new InputStreamReader(input, charset));
		String html = "<html><body>" + Joiner.on("<br/>").join(lines) + "</body></html>";
		return new ByteArrayInputStream(html.getBytes(charset));
	}

	@Override
	public MSEmailBodyType targetType() {
		return MSEmailBodyType.HTML;
	}

}
