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
package org.obm.push.mail.mime;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class LeafPartsFinderTest {

	@Test
	public void testEmlAttachmentIsALeaf() {
		MimePart textPlain = MimePartImpl.builder().contentType("text/plain").build();
		MimePart rfc822EmbeddedAttachment = MimePartImpl.builder()
				.contentType("message/rfc822")
				.contentDisposition("attachment")
				.addChild(MimePartImpl.builder()
						.contentType("multipart/alternative")
						.addChild(MimePartImpl.builder().contentType("text/plain").build())
						.addChild(MimePartImpl.builder().contentType("text/html").build())
						.build())
				.build();
		MimeMessage message = MimeMessageImpl.builder().addChild(
				MimePartImpl.builder()
					.contentType("multipart/mixed")
					.addChild(textPlain)
					.addChild(rfc822EmbeddedAttachment)
					.build())
				.build();

		boolean filterNested = true;
		boolean depthFirst = true;
		Collection<MimePart> leaves = new LeafPartsFinder(message, depthFirst, filterNested).getLeaves();
		
		assertThat(leaves).hasSize(2);
		assertThat(leaves).containsOnly(textPlain, rfc822EmbeddedAttachment);
	}

	@Test
	public void testEmlAttachmentIsALeafWhenFirstIsAlternative() {
		MimePart textPlain = MimePartImpl.builder().contentType("text/plain").build();
		MimePart textHtml = MimePartImpl.builder().contentType("text/html").build();
		MimePart alternative = MimePartImpl.builder()
				.contentType("multipart/alternative")
				.addChildren(textPlain, textHtml)
				.build();
		
		MimePart rfc822EmbeddedAttachment = MimePartImpl.builder()
				.contentType("message/rfc822")
				.contentDisposition("attachment")
				.addChild(MimePartImpl.builder()
						.contentType("multipart/alternative")
						.addChild(MimePartImpl.builder().contentType("text/plain").build())
						.addChild(MimePartImpl.builder().contentType("text/html").build())
						.build())
				.build();
		
		MimeMessage message = MimeMessageImpl.builder().addChild(
				MimePartImpl.builder()
					.contentType("multipart/mixed")
					.addChild(alternative)
					.addChild(rfc822EmbeddedAttachment)
					.build())
				.build();

		boolean filterNested = true;
		boolean depthFirst = true;
		Collection<MimePart> leaves = new LeafPartsFinder(message, depthFirst, filterNested).getLeaves();
		
		assertThat(leaves).hasSize(3);
		assertThat(leaves).containsOnly(textPlain, textHtml, rfc822EmbeddedAttachment);
	}
}
