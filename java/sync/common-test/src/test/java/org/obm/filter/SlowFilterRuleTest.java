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
package org.obm.filter;

import static org.easymock.EasyMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

public class SlowFilterRuleTest {

	private SlowFilterRule testee;
	private Slow slowAnnotation;
	private IMocksControl control;

	@Before
	public void setup() {
		control = createNiceControl();
		testee = new SlowFilterRule();
		testee.slowFilterConfiguration = control.createMock(SlowFilterConfiguration.class);
		slowAnnotation = new Slow() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}
		};
	}
	
	@Test
	public void testHasToRunTestWhenNoAnnotation() {
		Slow methodAnnotation = null;
		control.replay();
		boolean hasToRunTest = testee.hasToRunTest(methodAnnotation);
		
		control.verify();		
		assertThat(hasToRunTest).isTrue();
	}
	
	@Test
	public void testHasToRunTestWhenSlowAnnotation() {
		Slow methodAnnotation = slowAnnotation;
		control.replay();
		boolean hasToRunTest = testee.hasToRunTest(methodAnnotation);
		
		control.verify();
		assertThat(hasToRunTest).isTrue();
	}

	@Test
	public void testHasToRunTestWhenNoAnnotationAndFastOnlyConfiguration() {
		Slow methodAnnotation = null;
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.FAST_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("true");
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.SLOW_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("false");
		control.replay();
		boolean hasToRunTest = testee.hasToRunTest(methodAnnotation);
		
		assertThat(hasToRunTest).isTrue();
	}
	
	@Test
	public void testSkipTestWhenNoAnnotationAndSlowOnlyConfiguration() {
		Slow methodAnnotation = null;
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.FAST_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("false");
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.SLOW_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("true");
		control.replay();
		boolean hasToRunTest = testee.hasToRunTest(methodAnnotation);
		
		assertThat(hasToRunTest).isFalse();
	}

	@Test
	public void testSkipTestWhenAnnotationAndFastOnlyConfiguration() {
		Slow methodAnnotation = slowAnnotation;
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.FAST_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("true");
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.SLOW_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("false");
		control.replay();
		boolean hasToRunTest = testee.hasToRunTest(methodAnnotation);
		
		assertThat(hasToRunTest).isFalse();
	}
	
	@Test
	public void testSkipTestWhenAnnotationAndSlowOnlyConfiguration() {
		Slow methodAnnotation = slowAnnotation;
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.FAST_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("false");
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.SLOW_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("true");
		control.replay();
		boolean hasToRunTest = testee.hasToRunTest(methodAnnotation);
		
		assertThat(hasToRunTest).isTrue();
	}

	@Test
	public void testHasToRunTestWhenAnnotationAndBothConfigurations() {
		Slow methodAnnotation = slowAnnotation;		
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.FAST_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("true");
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.SLOW_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("true");
		control.replay();

		boolean hasToRunTest = testee.hasToRunTest(methodAnnotation);
		
		assertThat(hasToRunTest).isTrue();
	}
	
	@Test
	public void testHasToRunTestWhenNoAnnotationAndBothConfigurations() {
		Slow methodAnnotation = null;
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.FAST_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("true");
		expect(testee.slowFilterConfiguration.getConfigurationValue(Slow.SLOW_CONFIGURATION_ENVIRONMENT_KEY)).andReturn("true");
		control.replay();
		boolean hasToRunTest = testee.hasToRunTest(methodAnnotation);
		
		assertThat(hasToRunTest).isTrue();
	}


	@Test @Slow
	public void testSlowTestIsntRunIfNotAllowed() {
		boolean allowedByConfiguration = testee.hasToRunTest(newSlowAnnotation());
		
		assertThat(allowedByConfiguration).isTrue();
	}

	private Slow newSlowAnnotation() {
		return new Slow() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return Slow.class;
			}
		};
	}
}
