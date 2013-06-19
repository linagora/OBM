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
package org.obm.sync.client.impl;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.util.List;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

@RunWith(SlowFilterRunner.class)
public class AbstractClientImplTest {

	private SyncClientException execptionFactory;
	private HttpClient httpClient;
	private IMocksControl mocks;
	private AbstractClientImpl testee;

	@Before
	public void setUp() {
		mocks = createControl();
		
		Logger logger = LoggerFactory.getLogger(getClass());
		execptionFactory = mocks.createMock(SyncClientException.class);
		httpClient = mocks.createMock(HttpClient.class);
		testee = new AbstractClientImpl(execptionFactory, logger, httpClient) {
			
			@Override
			protected Locator getLocator() {
				throw new IllegalAccessError();
			}
		};
	}

	@Test
	public void testSetPostParameterUseBodyFormWithUTF8Charset() {
		Multimap<String, String> parameters = ImmutableMultimap.of("name", "value");
		Request request = mocks.createMock(Request.class);
		expect(request.bodyForm(formParams(parameters), Charsets.UTF_8)).andReturn(request);
		
		mocks.replay();
		testee.setPostRequestParameters(request, parameters);
		mocks.verify();
	}

	private List<NameValuePair> formParams(Multimap<String, String> parameters) {
		return FluentIterable
				.from(parameters.entries())
				.transform(new Function<Entry<String, String>, NameValuePair>() {

					@Override
					public NameValuePair apply(Entry<String, String> input) {
						return new BasicNameValuePair(input.getKey(), input.getValue());
					}
				}).toList();
	}
}
