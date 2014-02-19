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
package org.obm.breakdownduration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class BreakdownDurationLoggerServiceTest {
	
	private BreakdownDurationLoggerService breakdownDurationLoggerImpl;
	
	@Before
	public void setup() {
		breakdownDurationLoggerImpl = new BreakdownDurationLoggerService();
	}
	
	@After
	public void tearDown() {
		breakdownDurationLoggerImpl.cleanSession();
	}
	
	@Test
	public void testRecordingNotEnabled() {
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST");
		breakdownDurationLoggerImpl.endRecordingNode(1000);
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isNullOrEmpty();
	}
	
	@Test
	public void testRecordingEnabledButDisabled() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.disableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST");
		breakdownDurationLoggerImpl.endRecordingNode(1000);
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isNullOrEmpty();
	}
	
	@Test
	public void testBuildLogNothing() {
		breakdownDurationLoggerImpl.enableRecording();
		assertThat(breakdownDurationLoggerImpl.buildLog()).isNullOrEmpty();
	}
	
	@Test
	public void testBuildLogOneNode() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST");
		breakdownDurationLoggerImpl.endRecordingNode(1000);
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isEqualTo("REQUEST:1000");
	}
	
	@Test
	public void testBuildLogOneLevel() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST");
		breakdownDurationLoggerImpl.startRecordingNode("SQL");
		breakdownDurationLoggerImpl.endRecordingNode(52);
		breakdownDurationLoggerImpl.endRecordingNode(100);
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isEqualTo(
				"REQUEST:100 ("
						+ "SQL:52, "
						+ "OTHER:48"
				+ ")");
	}

	/*
	 * -REQUEST START
	 *  |
	 *  |-SQL 1 START
	 *  |-SQL 1 END
	 *  |
	 *  |-EMAIL START
	 *    |
	 *    |-SQL 2 START
	 *    |-SQL 2 END
	 *    |
	 *    |-SQL 3 START
	 *    |-SQL 3 END
	 *    |
	 *    |-EXTERNAL_SERVICE START
	 *    |-EXTERNAL_SERVICE END
	 *    |
	 *    |-SQL 4 START
	 *    |-SQL 4 END
	 *    |
	 *  |-EMAIL END
	 *  |
	 *  |-SQL 5 START
	 *  |-SQL 5 END
	 * -REQUEST END 
	 * 
	 */
	@Test
	public void testBuildLogComplexTree() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST"); // REQUEST
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 1
		breakdownDurationLoggerImpl.endRecordingNode(51); // SQL 1
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 2
		breakdownDurationLoggerImpl.endRecordingNode(52); // SQL 2
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 3
		breakdownDurationLoggerImpl.endRecordingNode(53); // SQL 3
		breakdownDurationLoggerImpl.startRecordingNode("EXTERNAL_SERVICE"); // EXTERNAL_SERVICE
		breakdownDurationLoggerImpl.endRecordingNode(20);
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 4
		breakdownDurationLoggerImpl.endRecordingNode(54);  // SQL 4
		breakdownDurationLoggerImpl.endRecordingNode(300); // EMAIL
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 5
		breakdownDurationLoggerImpl.endRecordingNode(55); // SQL 5
		breakdownDurationLoggerImpl.endRecordingNode(1000);
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isEqualTo(
				"REQUEST:1000 (" +
					"EMAIL:300 (" +
						"SQL:159, " +
						"EXTERNAL_SERVICE:20, " +
						"OTHER:121" +
					"), " +
					"SQL:106, " +
					"OTHER:594" +
				")");
	}
	
	@Test
	public void testBuildLogMeldChildGroupEqualsToParentGroup() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST"); // REQUEST
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 1
		breakdownDurationLoggerImpl.endRecordingNode(51); // SQL 1
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL 2
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL 3
		breakdownDurationLoggerImpl.endRecordingNode(3); // EMAIL 3
		breakdownDurationLoggerImpl.endRecordingNode(5); // EMAIL 2
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 2
		breakdownDurationLoggerImpl.endRecordingNode(52); // SQL 2
		breakdownDurationLoggerImpl.endRecordingNode(300); // EMAIL
		breakdownDurationLoggerImpl.endRecordingNode(1000);
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isEqualTo(
				"REQUEST:1000 (" +
					"EMAIL:300 (" +
						"SQL:103, " +
						"OTHER:197" +
					"), " +
					"OTHER:700" +
				")");
	}
	
	@Test
	public void testBuildLogIgnoreTimelessNodes() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST"); // REQUEST
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 1
		breakdownDurationLoggerImpl.endRecordingNode(0); // SQL 1
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 2
		breakdownDurationLoggerImpl.endRecordingNode(0); // SQL 2
		breakdownDurationLoggerImpl.endRecordingNode(300); // EMAIL
		breakdownDurationLoggerImpl.endRecordingNode(1000);
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isEqualTo(
				"REQUEST:1000 (" +
					"EMAIL:300, " +
					"OTHER:700" +
				")");
	}
	
	@Test
	public void testMergeSubTree() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST"); // REQUEST
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL 2
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL
		breakdownDurationLoggerImpl.endRecordingNode(50); // SQL
		breakdownDurationLoggerImpl.endRecordingNode(150); // EMAIL 2
		breakdownDurationLoggerImpl.endRecordingNode(300); // EMAIL
		breakdownDurationLoggerImpl.endRecordingNode(1000);
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isEqualTo(
				"REQUEST:1000 (" +
					"EMAIL:300 (" +
						"SQL:50, " +
						"OTHER:250), " +
					"OTHER:700" +
				")");
	}
	
	@Test
	public void testDuplicateOnRoot() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST"); // REQUEST 1
		breakdownDurationLoggerImpl.endRecordingNode(300); // REQUEST 1
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST"); // REQUEST 2
		breakdownDurationLoggerImpl.endRecordingNode(1000); // REQUEST 2
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isEqualTo(
				"REQUEST:1300");
	}
	
	@Test
	public void testZeroChildNotShown() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST"); // REQUEST
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL
		breakdownDurationLoggerImpl.endRecordingNode(0); // EMAIL
		breakdownDurationLoggerImpl.endRecordingNode(1000); // REQUEST
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isEqualTo(
				"REQUEST:1000");
	}
	
	@Test
	public void testMergeCousins() {
		breakdownDurationLoggerImpl.enableRecording();
		breakdownDurationLoggerImpl.startRecordingNode("REQUEST"); // REQUEST
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL 1
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 1
		breakdownDurationLoggerImpl.endRecordingNode(10); // SQL 1
		breakdownDurationLoggerImpl.endRecordingNode(20); // EMAIL 1
		breakdownDurationLoggerImpl.startRecordingNode("EMAIL"); // EMAIL 2
		breakdownDurationLoggerImpl.startRecordingNode("SQL"); // SQL 2
		breakdownDurationLoggerImpl.endRecordingNode(5); // SQL 2
		breakdownDurationLoggerImpl.endRecordingNode(30); // EMAIL 2
		breakdownDurationLoggerImpl.endRecordingNode(1000); // REQUEST
		
		assertThat(breakdownDurationLoggerImpl.buildLog()).isEqualTo(
				"REQUEST:1000 (" +
					"EMAIL:50 (" +
						"SQL:15, " +
						"OTHER:35), " +
					"OTHER:950" +
				")");
	}
}
