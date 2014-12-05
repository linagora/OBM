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
package org.obm.push.mail.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.mail.bean.AttributeValue.AttributeSuffix;


public class AttributeValueTest {

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenNullAttributeSuffix() {
		AttributeValue.builder().attributeSuffix(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenNullValue() {
		AttributeValue.builder().value(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void attributeSuffixIsMandatory() {
		AttributeValue.builder().build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void valueIsMandatory() {
		AttributeValue.builder().attributeSuffix(AttributeSuffix.PRIVATE).build();
	}
	
	@Test
	public void builderShouldBuild() {
		String value = "value";
		AttributeValue attributeValue = AttributeValue.builder().attributeSuffix(AttributeSuffix.PRIVATE).value(value).build();
		assertThat(attributeValue.getAttributeSuffix()).isEqualTo(AttributeSuffix.PRIVATE);
		assertThat(attributeValue.getValue()).isEqualTo(value);
	}
	
	@Test
	public void privateValue() {
		String value = "value";
		AttributeValue attributeValue = AttributeValue.privateValue(value);
		assertThat(attributeValue.getAttributeSuffix()).isEqualTo(AttributeSuffix.PRIVATE);
		assertThat(attributeValue.getValue()).isEqualTo(value);
	}
	
	@Test
	public void sharedValue() {
		String value = "value";
		AttributeValue attributeValue = AttributeValue.sharedValue(value);
		assertThat(attributeValue.getAttributeSuffix()).isEqualTo(AttributeSuffix.SHARED);
		assertThat(attributeValue.getValue()).isEqualTo(value);
	}
	
	@Test
	public void serializePrivate() {
		String value = "value";
		String expectedSerialize = "\"value.priv\" \"value\"";
		AttributeValue attributeValue = AttributeValue.builder().attributeSuffix(AttributeSuffix.PRIVATE).value(value).build();
		assertThat(attributeValue.serialize()).isEqualTo(expectedSerialize);
	}
	
	@Test
	public void serializeShared() {
		String value = "value";
		String expectedSerialize = "\"value.shared\" \"value\"";
		AttributeValue attributeValue = AttributeValue.builder().attributeSuffix(AttributeSuffix.SHARED).value(value).build();
		assertThat(attributeValue.serialize()).isEqualTo(expectedSerialize);
	}
}
