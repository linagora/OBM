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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.obm.push.technicallog.LogConfiguration;
import org.obm.push.technicallog.bean.jaxb.LogFile;
import org.obm.push.utils.DateUtils;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.sun.jersey.api.view.Viewable;

public class TechnicalLogFileUtils {
	
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd";
	public final static String LOG_APPENDER_NAME = "technical_log";
	
	private final LogConfiguration logConfiguration;
	
	@Inject
	@VisibleForTesting public TechnicalLogFileUtils(LogConfiguration logConfiguration) {
		this.logConfiguration = logConfiguration;
	}
	
	public InputStream getTechnicalLogFile(String logFileName) throws FileNotFoundException {
		return new FileInputStream(logFileName);
	}
	
	public List<LogFile> retrieveLogsListOrEmpty() {
		try {
			return getLogsList();
		} catch (FolderNotFoundException e) {
			return Lists.newArrayList();
		} catch (DateFormatException e) {
			return Lists.newArrayList();
		}
	}
	
	@VisibleForTesting List<LogFile> getLogsList() throws FolderNotFoundException, DateFormatException {
		List<LogFile> logsList = Lists.newArrayList();
		File directory = new File(logConfiguration.getLogDirectory());
		if (!directory.isDirectory()) {
			throw new FolderNotFoundException();
		}
		
		for (String fileName : directory.list(new TechnicalLogFilenameFilter(logConfiguration))) {
			DateTime date = dateFromFileName(fileName);
			logsList.add(LogFile.builder().date(date).build());
		}
		
		Collections.sort(logsList, new LogFileComparator());
		return logsList;
	}
	
	private static class LogFileComparator implements Comparator<LogFile>, Serializable {

		@Override
		public int compare(LogFile o1, LogFile o2) {
			DateTime date1 = o1.getDate();
			if (date1 == null) {
				return 1;
			}
			
			DateTime date2 = o2.getDate();
			if (date2 == null) {
				return -1;
			}
			
			return date1.compareTo(date2);
		}
	}
	
	public String getFullPathLogFileName(String dateAsString) {
		return fileNameFromDate(dateTimeFormatter().parseDateTime(dateAsString));
	}
	
	
	public boolean isTraceEnabled() {
		return LoggerFactory.getLogger(LOG_APPENDER_NAME).isTraceEnabled();
	}
	
	// fileName : test.2012-11-24.log
	@VisibleForTesting DateTime dateFromFileName(String fileName) throws DateFormatException {
		Iterator<String> it = Splitter.on('.').split(fileName).iterator();
		if (it.hasNext()) {
			it.next();
		}
		
		if (it.hasNext()) {
			String stringAsDate = it.next();
			if (isCurrentDateFileName(stringAsDate)) {
				return new DateTime(DateUtils.getCurrentDate());
			} else {
				checkStringAsDate(stringAsDate);
				return dateTimeFormatter().parseDateTime(stringAsDate);
			}
		}
		return null;
	}

	private boolean isCurrentDateFileName(String stringAsDate) {
		return stringAsDate.equals(logConfiguration.getLogFileExtension());
	}
	
	// date : 2012-11-24
	@VisibleForTesting void checkStringAsDate(String date) throws DateFormatException {
		int numberOfSplits = 0;
		for (String value : Splitter.on('-').split(date)) {
			numberOfSplits++;
			if (Ints.tryParse(value) == null) {
				throw new DateFormatException();
			}
		}
		if (numberOfSplits != 3) {
			throw new DateFormatException();
		}
	}
	
	@VisibleForTesting String fileNameFromDate(DateTime date) {
		if (isCurrentDateComparedWithDayLowerLimit(date)) {
			return logConfiguration.getLogDirectory() 
					+ File.separator
					+ logConfiguration.getTechnicalLogPrefix()
					+ logConfiguration.fileExtensionSeparator()
					+ logConfiguration.getLogFileExtension();
		}
		
		return logConfiguration.getLogDirectory() 
				+ File.separator
				+ logConfiguration.getTechnicalLogPrefix()
				+ logConfiguration.fileExtensionSeparator()
				+ dateTimeFormatter().print(date)
				+ logConfiguration.fileExtensionSeparator()
				+ logConfiguration.getLogFileExtension();
	}

	@VisibleForTesting boolean isCurrentDateComparedWithDayLowerLimit(DateTime date) {
		return DateTimeComparator.getInstance(DateTimeFieldType.dayOfYear())
				.compare(date, DateUtils.getCurrentDate()) == 0;
	}
	
	public DateTimeFormatter dateTimeFormatter() {
		return DateTimeFormat.forPattern(DATE_TIME_PATTERN);
	}
	
	public Viewable technicalLogPageIndex() {
		return new Viewable("/TechnicalLogPage", 
				ImmutableMap.of("logFiles", retrieveLogsListOrEmpty(),
						"appenderActive", isTraceEnabled(),	
						"dateTimeFormatter", dateTimeFormatter()));	
	}
}
