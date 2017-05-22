/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2017  Linagora
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

import static org.assertj.guava.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.obm.push.mail.mime.BodyParams.GroupedBodyParamBuilder;

import com.google.common.base.Optional;

public class GroupedBodyParamBuilderTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void assignGroupCharsetShouldNotAssignCharsetWhenAbsent() {
		Optional<String> charset = Optional.absent();
		Optional<Integer> groupIndex = Optional.absent();
		BodyParam bodyParam = new BodyParam("key", "value", charset, groupIndex);

		GroupedBodyParamBuilder testee = new GroupedBodyParamBuilder();
		testee.assignGroupCharset(bodyParam);
		
		assertThat(testee.groupCharset).isAbsent();
	}

	@Test
	public void assignGroupCharsetShouldNotAssignCharsetWhenEmpty() {
		Optional<String> charset = Optional.of("");
		Optional<Integer> groupIndex = Optional.absent();
		BodyParam bodyParam = new BodyParam("key", "value", charset, groupIndex);

		GroupedBodyParamBuilder testee = new GroupedBodyParamBuilder();
		testee.assignGroupCharset(bodyParam);
		
		assertThat(testee.groupCharset).isAbsent();
	}

	@Test
	public void assignGroupCharsetShouldAssignCharsetWhenGiven() {
		Optional<String> charset = Optional.of("UTF-8");
		Optional<Integer> groupIndex = Optional.absent();
		BodyParam bodyParam = new BodyParam("key", "value", charset, groupIndex);

		GroupedBodyParamBuilder testee = new GroupedBodyParamBuilder();
		testee.assignGroupCharset(bodyParam);
		
		assertThat(testee.groupCharset).contains("UTF-8");
	}

	@Test
	public void assignGroupCharsetShouldNotThrowWhenAlreadySetAndSame() {
		Optional<String> charset = Optional.of("UTF-8");
		Optional<Integer> groupIndex = Optional.absent();
		BodyParam bodyParam = new BodyParam("key", "value", charset, groupIndex);

		GroupedBodyParamBuilder testee = new GroupedBodyParamBuilder();
		testee.groupCharset = charset;
		
		testee.assignGroupCharset(bodyParam);
		
		assertThat(testee.groupCharset).contains("UTF-8");
	}

	@Test
	public void assignGroupCharsetShouldThrowWhenAlreadySetAndDifferent() {
		expectedException.expect(IllegalStateException.class);
		
		Optional<String> charset = Optional.of("UTF-8");
		Optional<Integer> groupIndex = Optional.absent();
		BodyParam bodyParam = new BodyParam("key", "value", charset, groupIndex);

		GroupedBodyParamBuilder testee = new GroupedBodyParamBuilder();
		testee.groupCharset = Optional.of("UTF-16");
		
		testee.assignGroupCharset(bodyParam);
	}
}
