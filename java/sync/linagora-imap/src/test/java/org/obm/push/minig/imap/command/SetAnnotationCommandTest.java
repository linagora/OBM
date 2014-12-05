/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.mail.bean.AnnotationEntry;
import org.obm.push.mail.bean.AttributeValue;
import org.obm.push.minig.imap.impl.IMAPResponse;


public class SetAnnotationCommandTest {

	@Test(expected=NullPointerException.class)
	public void commandShouldThrowWhenNullMailbox() {
		SetAnnotationCommand setAnnotationCommand = new SetAnnotationCommand(null, AnnotationEntry.SHAREDSEEN, AttributeValue.sharedValue("value"));
		setAnnotationCommand.buildCommand().getCommandString();
	}

	@Test(expected=NullPointerException.class)
	public void commandShouldThrowWhenNullAnnotationEntry() {
		SetAnnotationCommand setAnnotationCommand = new SetAnnotationCommand("user/usera@mydomain.org", null, AttributeValue.sharedValue("value"));
		setAnnotationCommand.buildCommand().getCommandString();
	}

	@Test(expected=NullPointerException.class)
	public void commandShouldThrowWhenNullAttributeValue() {
		SetAnnotationCommand setAnnotationCommand = new SetAnnotationCommand("user/usera@mydomain.org", AnnotationEntry.SHAREDSEEN, null);
		setAnnotationCommand.buildCommand().getCommandString();
	}

	@Test
	public void commandShouldBeWellFormedWhenShared() {
		String expectedCommand = "SETANNOTATION \"user/usera@mydomain.org\" \"/vendor/cmu/cyrus-imapd/sharedseen\" (\"value.shared\" \"value\")";
		SetAnnotationCommand setAnnotationCommand = new SetAnnotationCommand("user/usera@mydomain.org", AnnotationEntry.SHAREDSEEN, AttributeValue.sharedValue("value"));
		String command = setAnnotationCommand.buildCommand().getCommandString();
		assertThat(command).isEqualTo(expectedCommand);
	}

	@Test
	public void commandShouldBeWellFormedWhenPrivate() {
		String expectedCommand = "SETANNOTATION \"user/usera@mydomain.org\" \"/vendor/cmu/cyrus-imapd/sharedseen\" (\"value.priv\" \"value\")";
		SetAnnotationCommand setAnnotationCommand = new SetAnnotationCommand("user/usera@mydomain.org", AnnotationEntry.SHAREDSEEN, AttributeValue.privateValue("value"));
		String command = setAnnotationCommand.buildCommand().getCommandString();
		assertThat(command).isEqualTo(expectedCommand);
	}

	@Test
	public void dataInitialValue() {
		SetAnnotationCommand setAnnotationCommand = new SetAnnotationCommand("user/usera@mydomain.org", AnnotationEntry.SHAREDSEEN, AttributeValue.sharedValue("value"));
		setAnnotationCommand.setDataInitialValue();
		assertThat(setAnnotationCommand.data).isFalse();
	}

	@Test
	public void handleResponse() {
		IMAPResponse response = new IMAPResponse("OK", null);
		SetAnnotationCommand setAnnotationCommand = new SetAnnotationCommand("user/usera@mydomain.org", AnnotationEntry.SHAREDSEEN, AttributeValue.sharedValue("value"));
		setAnnotationCommand.handleResponse(response);
		assertThat(setAnnotationCommand.data).isTrue();
	}
}
