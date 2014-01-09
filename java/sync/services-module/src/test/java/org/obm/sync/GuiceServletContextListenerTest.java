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
package org.obm.sync;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import javax.servlet.ServletContext;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.annotations.transactional.TransactionalModule;

import com.google.inject.Binder;
import com.google.inject.Module;


public class GuiceServletContextListenerTest {

	private IMocksControl mocksControl;
	private ServletContext servletContext;

	@Before
	public void setUp() {
		mocksControl = createControl();
		servletContext = mocksControl.createMock(ServletContext.class);
	}

	@Test
	public void testNewWebXmlModuleInstanceWhenValidValue() throws Exception {
		expect(servletContext.getInitParameter("guiceModule")).andReturn(ObmSyncModule.class.getName());

		mocksControl.replay();
		Module moduleInstance = new GuiceServletContextListener().newWebXmlModuleInstance(servletContext);
		mocksControl.verify();
		
		assertThat(moduleInstance).isNotNull().isInstanceOf(ObmSyncModule.class);
	}

	@Test
	public void testNewWebXmlModuleInstanceWhenNull() throws Exception {
		expect(servletContext.getInitParameter("guiceModule")).andReturn(null);

		mocksControl.replay();
		Module moduleInstance = new GuiceServletContextListener().newWebXmlModuleInstance(servletContext);
		mocksControl.verify();
		
		assertThat(moduleInstance).isNull();
	}

	@Test
	public void testNewWebXmlModuleInstanceWhenEmpty() throws Exception {
		expect(servletContext.getInitParameter("guiceModule")).andReturn("");

		mocksControl.replay();
		Module moduleInstance = new GuiceServletContextListener().newWebXmlModuleInstance(servletContext);
		mocksControl.verify();
		
		assertThat(moduleInstance).isNull();
	}

	@Test(expected=ClassNotFoundException.class)
	public void testNewWebXmlModuleInstanceWhenUnexistingClass() throws Exception {
		expect(servletContext.getInitParameter("guiceModule")).andReturn("bla.bla.bla.Clazz");
		mocksControl.replay();

		try {
			new GuiceServletContextListener().newWebXmlModuleInstance(servletContext);
		} catch (Exception e) {
			mocksControl.verify();
			throw e;
		}
	}

	@Ignore
	@Test(expected=ClassCastException.class)
	public void testNewWebXmlModuleInstanceWhenNotModuleType() throws Exception {
		expect(servletContext.getInitParameter("guiceModule")).andReturn(this.getClass().getName());
		mocksControl.replay();

		try {
			new GuiceServletContextListener().newWebXmlModuleInstance(servletContext);
		} catch (Exception e) {
			mocksControl.verify();
			throw e;
		}
	}

	@Ignore
	@Test(expected=InstantiationException.class)
	public void testNewWebXmlModuleInstanceWhenNoEmptyConstructorModule() throws Exception {
		expect(servletContext.getInitParameter("guiceModule")).andReturn(NoDefaultConstructorModule.class.getName());
		mocksControl.replay();

		try {
			new GuiceServletContextListener().newWebXmlModuleInstance(servletContext);
		} catch (Exception e) {
			mocksControl.verify();
			throw e;
		}
	}
	
	public static class NoDefaultConstructorModule implements Module {

		public NoDefaultConstructorModule(@SuppressWarnings("unused") String testing) {
		}
		
		@Override
		public void configure(Binder binder) {
			// do nothing
		}
	}

	@Test
	public void testSelectModuleWhenNotDefinedInWebXml() throws Exception {
		expect(servletContext.getInitParameter("guiceModule")).andReturn(null);
		
		mocksControl.replay();
		Module moduleInstance = new GuiceServletContextListener().selectGuiceModule(servletContext);
		mocksControl.verify();

		assertThat(moduleInstance).isNotNull().isInstanceOf(ObmSyncModule.class);
	}

	@Ignore
	@Test
	public void testSelectModuleWhenDefinedInWebXml() throws Exception {
		expect(servletContext.getInitParameter("guiceModule")).andReturn(TransactionalModule.class.getName());
		
		mocksControl.replay();
		Module moduleInstance = new GuiceServletContextListener().selectGuiceModule(servletContext);
		mocksControl.verify();

		assertThat(moduleInstance).isNotNull().isInstanceOf(TransactionalModule.class);
	}
}
