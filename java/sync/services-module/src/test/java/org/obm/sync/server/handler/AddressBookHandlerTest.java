/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.server.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import javax.inject.Inject;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ContactConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.sync.book.BookType;
import org.obm.sync.server.Request;

import com.google.inject.AbstractModule;

import fr.aliacom.obm.common.contact.AddressBookBindingImpl;
import fr.aliacom.obm.common.session.SessionManagement;

@GuiceModule(AddressBookHandlerTest.Env.class)
@RunWith(GuiceRunner.class)
public class AddressBookHandlerTest {

	@Inject
	private AddressBookHandler handler;

	@Inject
	private IMocksControl control;

	@Before
	public void setup() {
		control.reset();
	}

	@Test
	public void testType() {
		Request req = control.createMock(Request.class);
		expect(req.getParameter("book")).andReturn("contacts");
		control.replay();

		assertThat(handler.type(req)).isEqualTo(BookType.contacts);

		control.verify();
	}

	@Test
	public void testTypeWrongBookParameter() {
		Request req = control.createMock(Request.class);
		expect(req.getParameter("book")).andReturn("non_existent_type");
		control.replay();

		assertThat(handler.type(req)).isNull();

		control.verify();
	}

	@Test
	public void testTypeNoBookParameter() {
		Request req = control.createMock(Request.class);
		expect(req.getParameter("book")).andReturn(null);
		control.replay();

		assertThat(handler.type(req)).isNull();

		control.verify();
	}

	public static class Env extends AbstractModule {
		private final IMocksControl control = createControl();

		private <T> T bindMock(Class<T> cls) {
			T mock = control.createMock(cls);

			bind(cls).toInstance(mock);

			return mock;
		}

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(control);
			bindMock(SessionManagement.class);
			bindMock(AddressBookBindingImpl.class);
			bindMock(ContactConfiguration.class);
		}
	}
}
