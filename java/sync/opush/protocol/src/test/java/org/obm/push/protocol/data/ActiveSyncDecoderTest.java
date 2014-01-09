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
package org.obm.push.protocol.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.exception.activesync.ASRequestBooleanFieldException;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ActiveSyncDecoderTest {

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testIntegerWhenNotANumber() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName>a10</FieldName></root>").getDocumentElement();
		
		new ActiveSyncDecoder().uniqueIntegerFieldValue(request, field("FieldName"));
	}

	@Test
	public void testIntegerWhenJustTag() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName /></root>").getDocumentElement();

		Integer value = new ActiveSyncDecoder().uniqueIntegerFieldValue(request, field("FieldName"));
		
		assertThat(value).isNull();
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testIntegerWhenEmpty() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName> </FieldName></root>").getDocumentElement();

		new ActiveSyncDecoder().uniqueIntegerFieldValue(request, field("FieldName"));
	}

	@Test
	public void testIntegerWhenNoTag() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName></FieldName></root>").getDocumentElement();

		Integer value = new ActiveSyncDecoder().uniqueIntegerFieldValue(request, field("OtherField"));
		
		assertThat(value).isNull();
	}

	@Test
	public void testIntegerWhen0() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName>0</FieldName></root>").getDocumentElement();

		Integer value = new ActiveSyncDecoder().uniqueIntegerFieldValue(request, field("FieldName"));

		assertThat(value).isEqualTo(0);
	}

	@Test
	public void testIntegerWhen1000() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName>1000</FieldName></root>").getDocumentElement();

		Integer value = new ActiveSyncDecoder().uniqueIntegerFieldValue(request, field("FieldName"));

		assertThat(value).isEqualTo(1000);
	}
	
	@Test(expected=ASRequestBooleanFieldException.class)
	public void testBooleanWhenNotBoolean() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName>not boolean</FieldName></root>").getDocumentElement();

		new ActiveSyncDecoder().uniqueBooleanFieldValue(request, field("FieldName"));
	}

	@Test
	public void testBooleanWhenNotPresent() throws Exception {
		Element request = DOMUtils.parse("<root></root>").getDocumentElement();

		Boolean value = new ActiveSyncDecoder().uniqueBooleanFieldValue(request, field("FieldName"));

		assertThat(value).isNull();
	}

	@Test
	public void testBooleanWhenEmptyIsTrue() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName></FieldName></root>").getDocumentElement();

		Boolean value = new ActiveSyncDecoder().uniqueBooleanFieldValue(request, field("FieldName"));

		assertThat(value).isTrue();
	}

	@Test
	public void testBooleanWhenJustTagIsTrue() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName /></root>").getDocumentElement();

		Boolean value = new ActiveSyncDecoder().uniqueBooleanFieldValue(request, field("FieldName"));

		assertThat(value).isTrue();
	}

	@Test
	public void testBooleanFalse() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName>0</FieldName></root>").getDocumentElement();

		Boolean value = new ActiveSyncDecoder().uniqueBooleanFieldValue(request, field("FieldName"));

		assertThat(value).isFalse();
	}

	@Test
	public void testBooleanTrue() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName>1</FieldName></root>").getDocumentElement();

		Boolean value = new ActiveSyncDecoder().uniqueBooleanFieldValue(request, field("FieldName"));

		assertThat(value).isTrue();
	}

	@Test
	public void testBooleanDefaultFalse() throws Exception {
		Element request = DOMUtils.parse("<root></root>").getDocumentElement();

		boolean value = new ActiveSyncDecoder().uniqueBooleanFieldValue(request, field("FieldName"), false);

		assertThat(value).isFalse();
	}

	@Test
	public void testBooleanDefaultTrue() throws Exception {
		Element request = DOMUtils.parse("<root></root>").getDocumentElement();

		boolean value = new ActiveSyncDecoder().uniqueBooleanFieldValue(request, field("FieldName"), true);

		assertThat(value).isTrue();
	}

	@Test
	public void testStringWhenNotPresent() throws Exception {
		Element request = DOMUtils.parse("<root></root>").getDocumentElement();

		String value = new ActiveSyncDecoder().uniqueStringFieldValue(request, field("FieldName"));

		assertThat(value).isNull();
	}

	@Test
	public void testStringWhenJustTag() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName /></root>").getDocumentElement();

		String value = new ActiveSyncDecoder().uniqueStringFieldValue(request, field("FieldName"));

		assertThat(value).isNull();
	}

	@Test
	public void testStringWhenEmptyTag() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName></FieldName></root>").getDocumentElement();

		String value = new ActiveSyncDecoder().uniqueStringFieldValue(request, field("FieldName"));

		assertThat(value).isNull();
	}

	@Test
	public void testStringWhenOnlySpace() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName> </FieldName></root>").getDocumentElement();

		String value = new ActiveSyncDecoder().uniqueStringFieldValue(request, field("FieldName"));

		assertThat(value).isEqualTo(" ");
	}

	@Test
	public void testStringWhenData() throws Exception {
		Element request = DOMUtils.parse("<root><FieldName>hey data</FieldName></root>").getDocumentElement();

		String value = new ActiveSyncDecoder().uniqueStringFieldValue(request, field("FieldName"));

		assertThat(value).isEqualTo("hey data");
	}

	@Test
	public void testAppendBooleanWhenNull() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendBoolean(doc.getDocumentElement(), field("FieldName"), null);


		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root/>"));
	}

	@Test
	public void testAppendBooleanWhenFalse() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendBoolean(doc.getDocumentElement(), field("FieldName"), false);


		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root><FieldName>0</FieldName></root>"));
	}

	@Test
	public void testAppendBooleanWhenTrue() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendBoolean(doc.getDocumentElement(), field("FieldName"), true);


		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root><FieldName>1</FieldName></root>"));
	}

	@Test
	public void testAppendIntegerWhenNull() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendInteger(doc.getDocumentElement(), field("FieldName"), null);


		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root/>"));
	}

	@Test
	public void testAppendIntegerWhenMin() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendInteger(doc.getDocumentElement(), field("FieldName"), Integer.MIN_VALUE);


		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root><FieldName>-2147483648</FieldName></root>"));
	}

	@Test
	public void testAppendIntegerWhenMax() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendInteger(doc.getDocumentElement(), field("FieldName"), Integer.MAX_VALUE);


		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root><FieldName>2147483647</FieldName></root>"));
	}
	
	@Test
	public void testAppendIntegerWhenZero() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendInteger(doc.getDocumentElement(), field("FieldName"), 0);


		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root><FieldName>0</FieldName></root>"));
	}
	
	@Test
	public void testAppendStringWhenNull() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendString(doc.getDocumentElement(), field("FieldName"), null);

		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root/>"));
	}
	
	@Test
	public void testAppendStringWhenData() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendString(doc.getDocumentElement(), field("FieldName"), "hey data");

		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root><FieldName>hey data</FieldName></root>"));
	}
	
	@Test
	public void testAppendStringWhenBinChar() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendString(doc.getDocumentElement(), field("FieldName"), "hey bin "+ (char)0x92+" data");

		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root><FieldName>hey bin &#146; data</FieldName></root>"));
	}
	
	@Test
	public void testAppendStringWhenSpecialXmlChar() throws Exception {
		Document doc = DOMUtils.createDoc(null, "root");
		
		new ActiveSyncDecoder().appendString(doc.getDocumentElement(), field("FieldName"), "hey special xml <?!> data");

		assertThat(DOMUtils.serialize(doc)).isEqualTo(xml("<root><FieldName>hey special xml &lt;?!&gt; data</FieldName></root>"));
	}

	private String xml(String data) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + data;
	}

	private ActiveSyncFields field(final String fieldName) {
		return new ActiveSyncFields() {
			
			@Override
			public String getName() {
				return fieldName;
			}
		};
	}
}
