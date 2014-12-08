/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MailboxNameUTF7ConverterTest {

	@Parameters
	public static Collection<String[]> data() {
		return Arrays.asList(new String[][] {
			{ "", "" },
			{ "regular", "regular" },
			{ "Envoyé", "Envoy&AOk-" },
			{ "é è", "&AOk- &AOg-" },
			{"ébkbfibqifbqdhiqfishdqsfqdsfééééébfviugbdqskfvbsklqvlqbgvkiqshbfigisqfdqsdsfsqf",
			 "&AOk-bkbfibqifbqdhiqfishdqsfqdsf&AOkA6QDpAOkA6Q-bfviugbdqskfvbsklqvlqbgvkiqshbfigisqfdqsdsfsqf"},
			{ "A\u2262\u0391.", "A&ImIDkQ-." },
			{ "\u65E5\u672C\u8A9E", "&ZeVnLIqe-" },
			{ "Hi Mom -\u263A-!", "Hi Mom -&Jjo--!" },
			{ "Item 3 is \u00A31.", "Item 3 is &AKM-1." },
			// Custom examples that contain more than one mode shift.
			{ "Jyv\u00E4skyl\u00E4", "Jyv&AOQ-skyl&AOQ-" },
			{ "\'\u4F60\u597D\' heißt \"Hallo\"", "\'&T2BZfQ-\' hei&AN8-t \"Hallo\"" },
			// The ampersand sign is represented by &-.
			{ "Hot & Spicy & Fruity", "Hot &- Spicy &- Fruity" },
			// Slashes are converted to commas.
			{ "\uffff\uedca\u9876\u5432\u1fed", "&,,,typh2VDIf7Q-" },
			//
			{ "&&x&&", "&-&-x&-&-" },
			{ "~peter/mail/台北/日本語", "~peter/mail/&U,BTFw-/&ZeVnLIqe-" },
			{ "tietäjä", "tiet&AOQ-j&AOQ-" },
			{ "Töst-", "T&APY-st-" },
			{ "Foo&Bar-2011", "Foo&-Bar-2011" },
			{ "~/bågø", "~/b&AOU-g&APg-" },
			{ "båx", "b&AOU-x" },
			{ "bøx", "b&APg-x" },
			{ "Skräppost", "Skr&AOQ-ppost" },
			{ "Ting & Såger", "Ting &- S&AOU-ger" },
			{ "~/Følder/mailbåx & stuff + more", "~/F&APg-lder/mailb&AOU-x &- stuff + more" } });
	}

    @Parameter(value = 0) public String decoded;
    @Parameter(value = 1) public String encoded;
	
    @Test
    public void encode() {
    	String value = MailboxNameUTF7Converter.encode(decoded);
		assertThat(value).isEqualTo(encoded);
    }

    @Test
    public void decode() {
    	String value = MailboxNameUTF7Converter.decode(encoded);
		assertThat(value).isEqualTo(decoded);
    }
}
