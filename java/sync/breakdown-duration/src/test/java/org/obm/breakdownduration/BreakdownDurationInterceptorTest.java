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
 * (notably email and meeting requests), (ii) retain all hypertext links between 
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
package org.obm.breakdownduration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.breakdownduration.BreakdownDurationLoggerService;
import org.obm.breakdownduration.BreakdownDurationModule;
import org.obm.breakdownduration.BreakdownDurationLoggerService.Node;
import org.obm.breakdownduration.BreakdownDurationLoggerService.Root;
import org.obm.breakdownduration.bean.Group;
import org.obm.breakdownduration.bean.Watch;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModule(BreakdownDurationModule.class)
public class BreakdownDurationInterceptorTest {

	@Inject NotAnnotatedClass notAnnotatedClass;
	@Inject AnnotatedSQLClass annotatedSQLClass;
	@Inject AnnotatedEmailClass annotatedEmailClass;
	@Inject AnnotatedExternalClass annotatedExternalClass;
	@Inject BreakdownDurationLoggerService breakdownDurationLoggerService;
	
	@Before
	public void setUp() {
		breakdownDurationLoggerService.enableRecording();
	}
	
	@After
	public void tearDown() {
		breakdownDurationLoggerService.disableRecording();
		breakdownDurationLoggerService.cleanSession();
	}
	
	@Test
	public void interceptNothingWhenNotAnnotatedClass() throws Exception {
		notAnnotatedClass.aMethod();
		
		assertThat(getTreeRoot().children).isEmpty();
	}
	
	@Test
	public void interceptedWhenAnnotatedClass() throws Exception {
		annotatedSQLClass.aMethod();

		Node head = getTreeHead();
		assertThat(head.children).isEmpty();
		assertThat(head.group).isEqualTo(Group.SQL);
		assertThat(head.timeElapsedInMs).isGreaterThan(9);
	}

	@Test
	public void interceptedWhenPublicMethodCallsPrivateMethod() throws Exception {
		annotatedSQLClass.aMethodCallingOthers();

		Node head = getTreeHead();
		assertThat(head.children).isEmpty();
		assertThat(head.group).isEqualTo(Group.SQL);
		assertThat(head.timeElapsedInMs).isGreaterThan(190);
	}

	@Test(expected=CustomException.class)
	public void interceptedWhenAnnotatedButThrowsException() throws Exception {
		try {
			annotatedSQLClass.aMethodThrowingCustomException();
		} catch (Exception e) {
			Node head = getTreeHead();
			assertThat(head.children).isEmpty();
			assertThat(head.group).isEqualTo(Group.SQL);
			assertThat(head.timeElapsedInMs).isGreaterThan(90);
			throw e;
		}
	}

	@Test
	public void interceptedWhenTwoLevels() throws Exception {
		annotatedEmailClass.aMethodCallingSQLClass();

		Node head = getTreeHead();
		assertThat(head.children).hasSize(1);
		assertThat(head.group).isEqualTo(Group.EMAIL);
		assertThat(head.timeElapsedInMs).isGreaterThan(55);
		assertThat(head.children).hasSize(1);
		Node childSQL = head.children.get(0);
		assertThat(childSQL.children).isEmpty();
		assertThat(childSQL.group).isEqualTo(Group.SQL);
		assertThat(childSQL.timeElapsedInMs).isGreaterThan(9);
	}

	@Test
	public void interceptedWhenThreeLevels() throws Exception {
		annotatedEmailClass.aMethodCallingSQLThenExternalClass();

		Node head = getTreeHead();
		assertThat(head.group).isEqualTo(Group.EMAIL);
		assertThat(head.timeElapsedInMs).isGreaterThan(340);
		assertThat(head.children).hasSize(1);
		
		Node childSQL = head.children.get(0);
		assertThat(childSQL.group).isEqualTo(Group.SQL);
		assertThat(childSQL.timeElapsedInMs).isGreaterThan(90);
		assertThat(childSQL.children).hasSize(1);
		Node childExternal = childSQL.children.get(0);
		assertThat(childExternal.children).isEmpty();
		assertThat(childExternal.group).isEqualTo(Group.EXTERNAL_SERVICE);
		assertThat(childExternal.timeElapsedInMs).isGreaterThan(190);
	}
	
	@Test
	public void interceptedWhenManyCallToSameChild() throws Exception {
		annotatedEmailClass.aMethodCallingFiveTimesSQLClass();
		
		Node head = getTreeHead();
		assertThat(head.group).isEqualTo(Group.EMAIL);
		assertThat(head.timeElapsedInMs).isGreaterThan(45);
		assertThat(head.children).hasSize(5);
		Node firstSQL = head.children.get(0);
		assertThat(firstSQL.children).isEmpty();
		assertThat(firstSQL.group).isEqualTo(Group.SQL);
		assertThat(firstSQL.timeElapsedInMs).isGreaterThan(9);
		Node secondSQL = head.children.get(0);
		assertThat(secondSQL.children).isEmpty();
		assertThat(secondSQL.group).isEqualTo(Group.SQL);
		assertThat(secondSQL.timeElapsedInMs).isGreaterThan(9);
		Node thirdSQL = head.children.get(0);
		assertThat(thirdSQL.children).isEmpty();
		assertThat(thirdSQL.group).isEqualTo(Group.SQL);
		assertThat(thirdSQL.timeElapsedInMs).isGreaterThan(9);
		Node fourthSQL = head.children.get(0);
		assertThat(fourthSQL.children).isEmpty();
		assertThat(fourthSQL.group).isEqualTo(Group.SQL);
		assertThat(fourthSQL.timeElapsedInMs).isGreaterThan(9);
		Node fifthSQL = head.children.get(0);
		assertThat(fifthSQL.children).isEmpty();
		assertThat(fifthSQL.group).isEqualTo(Group.SQL);
		assertThat(fifthSQL.timeElapsedInMs).isGreaterThan(9);
	}
	
	@Test
	public void interceptedWhenManyChild() throws Exception {
		annotatedEmailClass.aMethodCallingExternalSQLThenContactClass();
		
		Node head = getTreeHead();
		assertThat(head.children).hasSize(3);
		assertThat(head.group).isEqualTo(Group.EMAIL);
		assertThat(head.timeElapsedInMs).isGreaterThan(220);
		Node childExternal = head.children.get(0);
		assertThat(childExternal.children).isEmpty();
		assertThat(childExternal.group).isEqualTo(Group.EXTERNAL_SERVICE);
		assertThat(childExternal.timeElapsedInMs).isGreaterThan(190);
		Node childSQL = head.children.get(1);
		assertThat(childSQL.children).isEmpty();
		assertThat(childSQL.group).isEqualTo(Group.SQL);
		assertThat(childSQL.timeElapsedInMs).isGreaterThan(9);
		Node childContact = head.children.get(2);
		assertThat(childContact.children).isEmpty();
		assertThat(childContact.group).isEqualTo(Group.CONTACTS);
		assertThat(childContact.timeElapsedInMs).isGreaterThan(19);
	}

	public static class CustomException extends Exception {}

	@Watch(Group.SQL)
	public static class AnnotatedSQLClass {

		private final AnnotatedExternalClass annotatedExternalClass;

		@Inject
		@VisibleForTesting AnnotatedSQLClass(AnnotatedExternalClass annotatedExternalClass) {
			this.annotatedExternalClass = annotatedExternalClass;
		}

		public void aMethod() throws Exception {
			Thread.sleep(10);
		}

		public void aMethodCallingOthers() throws Exception {
			aPrivateMethod();
			Thread.sleep(100);
		}

		public void aMethodCallingExternalMethod() throws Exception {
			annotatedExternalClass.aMethod();
			Thread.sleep(100);
		}

		private void aPrivateMethod() throws Exception {
			Thread.sleep(100);
		}

		public void aMethodThrowingCustomException() throws Exception {
			Thread.sleep(100);
			throw new CustomException();
		}
	}

	@Watch(Group.EMAIL)
	public static class AnnotatedEmailClass {

		private final AnnotatedSQLClass annotatedSQLClass;
		private final AnnotatedExternalClass annotatedExternalClass;
		private final AnnotatedContactClass annotatedContactClass;

		@Inject
		@VisibleForTesting AnnotatedEmailClass(
				AnnotatedSQLClass annotatedSQLClass,
				AnnotatedExternalClass annotatedExternalClass, 
				AnnotatedContactClass annotatedContactClass) {
			this.annotatedSQLClass = annotatedSQLClass;
			this.annotatedExternalClass = annotatedExternalClass;
			this.annotatedContactClass = annotatedContactClass;
		}
		
		public void aMethodCallingExternalSQLThenContactClass() throws Exception {
			annotatedExternalClass.aMethod();
			annotatedSQLClass.aMethod();
			annotatedContactClass.aMethod();
			Thread.sleep(10);
		}

		public void aMethodCallingFiveTimesSQLClass() throws Exception {
			annotatedSQLClass.aMethod();
			annotatedSQLClass.aMethod();
			annotatedSQLClass.aMethod();
			annotatedSQLClass.aMethod();
			annotatedSQLClass.aMethod();
			Thread.sleep(10);
		}

		public void aMethodCallingSQLClass() throws Exception {
			annotatedSQLClass.aMethod();
			Thread.sleep(50);
		}
		
		public void aMethodCallingSQLThenExternalClass() throws Exception {
			annotatedSQLClass.aMethodCallingExternalMethod();
			Thread.sleep(50);
		}
	}

	@Watch(Group.EXTERNAL_SERVICE)
	public static class AnnotatedExternalClass {

		@Inject
		@VisibleForTesting AnnotatedExternalClass() {}

		public void aMethod() throws Exception {
			Thread.sleep(200);
		}
	}
	
	@Watch(Group.CONTACTS)
	public static class AnnotatedContactClass {
		
		@Inject
		@VisibleForTesting AnnotatedContactClass() {}
		
		public void aMethod() throws Exception {
			Thread.sleep(20);
		}
	}

	public static class NotAnnotatedClass {
		
		@Inject
		@VisibleForTesting NotAnnotatedClass() {}
		
		public void aMethod() throws Exception {
			Thread.sleep(20);
		}
	}

	private Node getTreeHead() {
		return getTreeRoot().children.get(0);
	}

	private Root getTreeRoot() {
		return BreakdownDurationLoggerService.treeBuilder.get().build();
	}
}
