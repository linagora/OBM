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
package org.obm.push.jaxrs;

import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.powermock.api.easymock.PowerMock.createStrictMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.LogConfiguration;
import org.obm.push.bean.jaxb.LogFile;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { TechnicalLogFileUtility.class, LoggerFactory.class })
public class TechnicalLogFileUtilityTest {

	@Test
	public void testGetTechnicalLogFile() throws Exception {
		FileInputStream expectedFileInputStream = createStrictMock(FileInputStream.class);
		String fileName = "FileName";
		expectNew(FileInputStream.class, fileName)
			.andReturn(expectedFileInputStream).once();
				
		replayAll();
		
		InputStream inputStream = TechnicalLogFileUtility.getTechnicalLogFile(fileName);
		
		verifyAll();
		assertThat(inputStream).isEqualTo(expectedFileInputStream);
	}
	
	@Test (expected=FileNotFoundException.class)
	public void testGetTechnicalLogFileException() throws FileNotFoundException {
		TechnicalLogFileUtility.getTechnicalLogFile("notfound");
	}
	
	@Test
	public void testGetLogsList() throws Exception {
		LogConfiguration logConfiguration = createStrictMock(LogConfiguration.class);
		
		String logDirectory = "/var/log/opush";
		expect(logConfiguration.getLogDirectory())
			.andReturn(logDirectory).once();
		
		File directory = createStrictMock(File.class);
		expectNew(File.class, logDirectory)
			.andReturn(directory).once();
		expect(directory.isDirectory())
			.andReturn(true).once();

		TechnicalLogFilenameFilter technicalLogFilenameFilter = createStrictMock(TechnicalLogFilenameFilter.class);
		expectNew(TechnicalLogFilenameFilter.class, logConfiguration)
			.andReturn(technicalLogFilenameFilter);
		
		String[] files = { "1.txt", "2.log" };
		expect(directory.list(technicalLogFilenameFilter))
			.andReturn(files).once();
		
		List<LogFile> expectedFiles = ImmutableList.of(
				LogFile.builder().fileName("1.txt").build(),
				LogFile.builder().fileName("2.log").build());
		
		replayAll();
		
		List<LogFile> logsList = TechnicalLogFileUtility.getLogsList(logConfiguration);
		
		verifyAll();
		assertThat(logsList).isEqualTo(expectedFiles);
	}
	
	@Test
 	public void testGetFullPathLogFileName() {
		LogConfiguration logConfiguration = createStrictMock(LogConfiguration.class);
		
		String logDirectory = "/var/log/opush";
		expect(logConfiguration.getLogDirectory())
			.andReturn(logDirectory).once();
		
		String fileName = "FileName";
		String expectedFullPathLogFileName = logDirectory + File.separator + fileName;
		
		replayAll();
		
		String fullPathLogFileName = TechnicalLogFileUtility.getFullPathLogFileName(logConfiguration, fileName);
		
		verifyAll();
		assertThat(fullPathLogFileName).isEqualTo(expectedFullPathLogFileName);
	}
	
	@Test
	public void testIsTraceEnabled() {
		Logger logger = createStrictMock(Logger.class);
		mockStatic(LoggerFactory.class);
		expect(LoggerFactory.getLogger("technical_log"))
			.andReturn(logger).once();
		
		expect(logger.isTraceEnabled())
			.andReturn(true).once();
		
		replayAll();
		
		boolean traceEnabled = TechnicalLogFileUtility.isTraceEnabled();
		
		verifyAll();
		assertThat(traceEnabled).isTrue();
	}
}
