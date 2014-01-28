/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.push.arquillian.extension.deployment;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.junit.Test;
import org.obm.push.arquillian.extension.deployment.DeployForEachTestsExtension.Handler;


public class DeployForEachTestsExtensionTest {

	private IMocksControl control;
	private Handler testee;
	private Deployer deployer;
	private Before beforeEvent;
	private After afterEvent;

	@org.junit.Before
	public void setUp() {
		testee = new DeployForEachTestsExtension.Handler();
		control = createControl();
		beforeEvent = control.createMock(Before.class);
		afterEvent = control.createMock(After.class);
		deployer = control.createMock(Deployer.class);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testBeforeWhenNoDeployment() throws Exception {
		control.replay();
		try {
			testee.executeOnJUnitBefore(beforeEvent, deployer, new TestClass(ClassWithoutAnnotation.class));
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test(expected=IllegalStateException.class)
	public void testAfterWhenNoDeployment() throws Exception {
		control.replay();
		try {
			testee.executeOnJUnitAfter(afterEvent, deployer, new TestClass(ClassWithoutAnnotation.class));
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}
	
	@Test
	public void testBeforeWhenArqManagedDeployment() {
		control.replay();
		testee.executeOnJUnitBefore(beforeEvent, deployer, new TestClass(ClassWithManagedDeployment.class));
		control.verify();
	}

	@Test
	public void testAfterWhenArqManagedDeployment() {
		control.replay();
		testee.executeOnJUnitAfter(afterEvent, deployer, new TestClass(ClassWithManagedDeployment.class));
		control.verify();
	}
	
	@Test
	public void testBeforeWhenUnmanagedDeployment() {
		control.replay();
		testee.executeOnJUnitBefore(beforeEvent, deployer, new TestClass(ClassWithUnmanagedDeployment.class));
		control.verify();
	}
	
	@Test
	public void testAfterWhenUnmanagedDeployment() {
		control.replay();
		testee.executeOnJUnitAfter(afterEvent, deployer, new TestClass(ClassWithUnmanagedDeployment.class));
		control.verify();
	}
	
	@Test
	public void testBeforeWhenExtensionManagedDeployment() {
		deployer.deploy("dep_name");
		expectLastCall();
		
		control.replay();
		testee.executeOnJUnitBefore(beforeEvent, deployer, new TestClass(ClassWithExtensionManagedDeployment.class));
		control.verify();
	}
	
	@Test
	public void testAfterWhenExtensionManagedDeployment() {
		deployer.undeploy("dep_name");
		expectLastCall();
		
		control.replay();
		testee.executeOnJUnitAfter(afterEvent, deployer, new TestClass(ClassWithExtensionManagedDeployment.class));
		control.verify();
	}
	
	@Test
	public void testBeforeWhenExtensionManagedDeploymentButNoName() {
		deployer.deploy("_DEFAULT_");
		expectLastCall();
		
		control.replay();
		testee.executeOnJUnitBefore(beforeEvent, deployer, new TestClass(ClassWithExtensionManagedDeploymentUnamed.class));
		control.verify();
	}
	
	@Test
	public void testAfterWhenExtensionManagedDeploymentButNoName() {
		deployer.undeploy("_DEFAULT_");
		expectLastCall();
		
		control.replay();
		testee.executeOnJUnitAfter(afterEvent, deployer, new TestClass(ClassWithExtensionManagedDeploymentUnamed.class));
		control.verify();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testBeforeWhenBothManagedDeployment() throws Exception {
		control.replay();
		try {
			testee.executeOnJUnitBefore(beforeEvent, deployer, new TestClass(ClassWithBothManagedDeployment.class));
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void testAfterWhenBothManagedDeployment() throws Exception {
		control.replay();
		try {
			testee.executeOnJUnitAfter(afterEvent, deployer, new TestClass(ClassWithBothManagedDeployment.class));
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}
	
	@Test
	public void testBeforeWhenClassExtendingManagedDeployment() {
		deployer.deploy("dep_name");
		expectLastCall();
		
		control.replay();
		testee.executeOnJUnitBefore(beforeEvent, deployer, new TestClass(ClassExtendingManagedDeployment.class));
		control.verify();
	}
	
	@Test
	public void testAfterWhenClassExtendingManagedDeployment() {
		deployer.undeploy("dep_name");
		expectLastCall();
		
		control.replay();
		testee.executeOnJUnitAfter(afterEvent, deployer, new TestClass(ClassExtendingManagedDeployment.class));
		control.verify();
	}

	public static class ClassWithoutAnnotation {
		public static void method() {}
	}
	
	public static class ClassWithManagedDeployment {
		@Deployment
		public static void method() {}
	}

	public static class ClassWithUnmanagedDeployment {
		@Deployment(managed=false)
		public static void method() {}
	}

	public static class ClassWithExtensionManagedDeployment {
		@Deployment(managed=false, name="dep_name") 
		@DeployForEachTests
		public static void method() {}
	}
	
	public static class ClassWithExtensionManagedDeploymentUnamed {
		@Deployment(managed=false) 
		@DeployForEachTests
		public static void method() {}
	}

	public static class ClassWithBothManagedDeployment {
		@Deployment
		@DeployForEachTests
		public static void method() {}
	}

	public static class ClassExtendingManagedDeployment extends ClassWithExtensionManagedDeployment {
		public static void anotherMethod() {}
	}
}
