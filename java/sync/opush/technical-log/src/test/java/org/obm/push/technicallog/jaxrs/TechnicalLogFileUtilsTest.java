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
package org.obm.push.technicallog.jaxrs;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.push.technicallog.LogConfiguration;
import org.obm.push.technicallog.bean.jaxb.LogFile;
import org.obm.push.technicallog.jaxrs.DateFormatException;
import org.obm.push.technicallog.jaxrs.TechnicalLogFileUtils;
import org.obm.push.technicallog.jaxrs.TechnicalLogFilenameFilter;
import org.obm.push.utils.DateUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TechnicalLogFileUtils.class, LoggerFactory.class, DateUtils.class })
public class TechnicalLogFileUtilsTest {

	@Test
	public void testGetTechnicalLogFile() throws Exception {
		FileInputStream expectedFileInputStream = createStrictMock(FileInputStream.class);
		String fileName = "FileName";
		expectNew(FileInputStream.class, fileName)
			.andReturn(expectedFileInputStream).once();
				
		replay(expectedFileInputStream, FileInputStream.class);
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		InputStream inputStream = technicalLogFileUtility.getTechnicalLogFile(fileName);
		
		verify(expectedFileInputStream, FileInputStream.class);
		assertThat(inputStream).isEqualTo(expectedFileInputStream);
	}
	
	@Test (expected=FileNotFoundException.class)
	public void testGetTechnicalLogFileException() throws FileNotFoundException {
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		technicalLogFileUtility.getTechnicalLogFile("notfound");
	}
	
	@Test
	public void testGetLogsList() throws Exception {
		LogConfiguration logConfiguration = createMock(LogConfiguration.class);
		
		String logFileExtension = "log";
		String fileName1 = "1.2012-01-01." + logFileExtension;
		String fileName2 = "2.2012-05-01." + logFileExtension;
		DateTime date1 = new DateTime(2012, 1, 1, 0, 0);
		DateTime date2 = new DateTime(2012, 5, 1, 0, 0);
		
		String logDirectory = "/var/log/opush";
		expect(logConfiguration.getLogDirectory())
			.andReturn(logDirectory).once();
		expect(logConfiguration.getLogFileExtension())
			.andReturn(logFileExtension).times(2);
		
		File directory = createStrictMock(File.class);
		expectNew(File.class, logDirectory)
			.andReturn(directory).once();
		expect(directory.isDirectory())
			.andReturn(true).once();

		TechnicalLogFilenameFilter technicalLogFilenameFilter = createStrictMock(TechnicalLogFilenameFilter.class);
		expectNew(TechnicalLogFilenameFilter.class, logConfiguration)
			.andReturn(technicalLogFilenameFilter);
		
		String[] files = { fileName1, fileName2 };
		expect(directory.list(technicalLogFilenameFilter))
			.andReturn(files).once();
		
		List<LogFile> expectedFiles = ImmutableList.of(
				LogFile.builder().date(date1).build(),
				LogFile.builder().date(date2).build());
		
		replay(logConfiguration, directory, File.class, technicalLogFilenameFilter, TechnicalLogFilenameFilter.class);
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(logConfiguration);
		List<LogFile> logsList = technicalLogFileUtility.getLogsList();
		
		verify(logConfiguration, directory, File.class, technicalLogFilenameFilter, TechnicalLogFilenameFilter.class);
		assertThat(logsList).isEqualTo(expectedFiles);
	}
	
	@Test
 	public void testGetFullPathLogFileName() {
		LogConfiguration logConfiguration = createStrictMock(LogConfiguration.class);
		String logDirectory = "/var/log/opush";
		String technicalLogPrefix = "technicalLog";
		String fileExtensionSeparator = ".";
		String logFileExtension = "log";
		expect(logConfiguration.getLogDirectory())
			.andReturn(logDirectory).once();
		expect(logConfiguration.getTechnicalLogPrefix())
			.andReturn(technicalLogPrefix).once();
		expect(logConfiguration.fileExtensionSeparator())
			.andReturn(fileExtensionSeparator).times(2);
		expect(logConfiguration.getLogFileExtension())
			.andReturn(logFileExtension).once();
		
		DateTime date = new DateTime(2012, 1, 5, 0, 0);
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd"); 
		String expectedFullPathLogFileName = logDirectory 
				+ File.separator 
				+ technicalLogPrefix 
				+ fileExtensionSeparator 
				+ dateTimeFormatter.print(date) 
				+ fileExtensionSeparator 
				+ logFileExtension;
		
		replay(logConfiguration);
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(logConfiguration);
		String fullPathLogFileName = technicalLogFileUtility.getFullPathLogFileName("2012-01-05");
		
		verify(logConfiguration);
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
		
		replay(logger, LoggerFactory.class);
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		boolean traceEnabled = technicalLogFileUtility.isTraceEnabled();
		
		verify(logger, LoggerFactory.class);
		assertThat(traceEnabled).isTrue();
	}
	
	@Test
	public void testDateFromFileNameCurrentLog() throws DateFormatException {
		DateTime expectedDate = new DateTime(2012, 10, 24, 0, 0);
		mockStatic(DateUtils.class);
		expect(DateUtils.getCurrentDate())
			.andReturn(expectedDate.toDate()).once();
		
		LogConfiguration logConfiguration = createStrictMock(LogConfiguration.class);
		
		String logFileExtension = "log";
		expect(logConfiguration.getLogFileExtension())
			.andReturn(logFileExtension).once();
		
		replay(DateUtils.class, logConfiguration);
		
		String fileName = "test.log";
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(logConfiguration);
		DateTime date = technicalLogFileUtility.dateFromFileName(fileName);
		
		verify(DateUtils.class, logConfiguration);
		assertThat(date).isEqualTo(expectedDate);
	}

	@Test
	public void testDateFromFileNameHistorizedLog() throws DateFormatException {
		LogConfiguration logConfiguration = createStrictMock(LogConfiguration.class);
		
		String logFileExtension = "log";
		expect(logConfiguration.getLogFileExtension())
			.andReturn(logFileExtension).once();
		
		replay(logConfiguration);
		
		DateTime expectedDate = new DateTime(2012, 5, 1, 0, 0);
		String fileName = "test.2012-05-01.log";
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(logConfiguration);
		DateTime date = technicalLogFileUtility.dateFromFileName(fileName);
		
		verify(logConfiguration);
		assertThat(date).isEqualTo(expectedDate);
	}

	@Test(expected=DateFormatException.class)
	public void testDateFromFileNameException() throws DateFormatException {
		LogConfiguration logConfiguration = createStrictMock(LogConfiguration.class);
		
		String logFileExtension = "log";
		expect(logConfiguration.getLogFileExtension())
			.andReturn(logFileExtension).once();
		
		replay(logConfiguration);
		
		String fileName = "test.1-2-3-4.log";
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(logConfiguration);
		technicalLogFileUtility.dateFromFileName(fileName);
	}

	@Test(expected=DateFormatException.class)
	public void testCheckStringAsDate() throws DateFormatException {
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		technicalLogFileUtility.checkStringAsDate("test");
	}

	@Test(expected=DateFormatException.class)
	public void testCheckStringAsDateOnlyYear() throws DateFormatException {
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		technicalLogFileUtility.checkStringAsDate("2012");
	}

	@Test(expected=DateFormatException.class)
	public void testCheckStringAsDateOnlyYearAndMonth() throws DateFormatException {
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		technicalLogFileUtility.checkStringAsDate("2012-01");
	}

	@Test
	public void testCheckStringAsDateOnlyFull() throws DateFormatException {
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		technicalLogFileUtility.checkStringAsDate("2012-01-02");
	}

	@Test(expected=DateFormatException.class)
	public void testCheckStringAsDateMoreThanExpected() throws DateFormatException {
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		technicalLogFileUtility.checkStringAsDate("2012-01-02-5");
	}

	@Test
	public void testFileNameFromDate() {
		LogConfiguration logConfiguration = createStrictMock(LogConfiguration.class);
		
		String logDirectory = "/var/log/opush";
		String technicalLogPrefix = "technical";
		String fileExtensionSeparator = ".";
		String logFileExtension = "log";
		expect(logConfiguration.getLogDirectory())
			.andReturn(logDirectory).once();
		expect(logConfiguration.getTechnicalLogPrefix())
			.andReturn(technicalLogPrefix).once();
		expect(logConfiguration.fileExtensionSeparator())
			.andReturn(fileExtensionSeparator).times(2);
		expect(logConfiguration.getLogFileExtension())
			.andReturn(logFileExtension).once();
		
		String separator = "/";
		
		replay(logConfiguration);
		
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd"); 
		DateTime date = new DateTime(2012, 1, 2, 0, 0);
		String expectedFileName = logDirectory 
						+ separator 
						+ technicalLogPrefix
						+ fileExtensionSeparator
						+ dateTimeFormatter.print(date)
						+ fileExtensionSeparator
						+ logFileExtension;
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(logConfiguration);
		String fileName = technicalLogFileUtility.fileNameFromDate(date);
		
		verify(logConfiguration);
		assertThat(fileName).isEqualTo(expectedFileName);
	}
	
	@Test
	public void testFileNameFromDateForCurrentDate() {
		LogConfiguration logConfiguration = createStrictMock(LogConfiguration.class);
		
		String logDirectory = "/var/log/opush";
		String technicalLogPrefix = "technical";
		String fileExtensionSeparator = ".";
		String logFileExtension = "log";
		expect(logConfiguration.getLogDirectory())
			.andReturn(logDirectory).once();
		expect(logConfiguration.getTechnicalLogPrefix())
			.andReturn(technicalLogPrefix).once();
		expect(logConfiguration.fileExtensionSeparator())
			.andReturn(fileExtensionSeparator).once();
		expect(logConfiguration.getLogFileExtension())
			.andReturn(logFileExtension).once();
		
		String separator = "/";
		
		DateTime date = new DateTime(2012, 2, 1, 0, 0);
		mockStatic(DateUtils.class);
		expect(DateUtils.getCurrentDate())
			.andReturn(date.toDate()).once();
		
		replay(logConfiguration, DateUtils.class);
		
		String expectedFileName = logDirectory 
						+ separator 
						+ technicalLogPrefix
						+ fileExtensionSeparator
						+ logFileExtension;
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(logConfiguration);
		String fileName = technicalLogFileUtility.fileNameFromDate(date);
		
		verify(logConfiguration, DateUtils.class);
		assertThat(fileName).isEqualTo(expectedFileName);
	}
	
	@Test
	public void testIsCurrentDateComparedWithDayLowerLimit() {
		DateTime currentDate = new DateTime(DateUtils.getCurrentDate());
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		boolean value = technicalLogFileUtility.isCurrentDateComparedWithDayLowerLimit(currentDate);
		
		assertThat(value).isTrue();
	}
	
	@Test
	public void testIsNotCurrentDateComparedWithDayLowerLimit() {
		DateTime currentDate = new DateTime(2001, 1, 1, 0, 0);
		
		TechnicalLogFileUtils technicalLogFileUtility = new TechnicalLogFileUtils(null);
		boolean value = technicalLogFileUtility.isCurrentDateComparedWithDayLowerLimit(currentDate);
		
		assertThat(value).isFalse();
	}
}
