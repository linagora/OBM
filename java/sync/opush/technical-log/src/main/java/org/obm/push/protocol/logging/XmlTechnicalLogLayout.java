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
package org.obm.push.protocol.logging;

import java.util.Map;

import ch.qos.logback.classic.log4j.XMLLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class XmlTechnicalLogLayout extends XMLLayout{

	@Override
	public String doLayout(ILoggingEvent event) {
		Map<String, String> propertyMap = event.getMDCPropertyMap();

		long timestamp = event.getTimeStamp();
		String level = event.getLevel().toString();
		String logger = event.getLoggerName();

		String logHeader = "<time>"+timestamp+"</time>";
		logHeader += "<level>"+level+"</level>";
		logHeader += "<logger>"+logger+"</logger>";

		if(propertyMap != null){

			logHeader += "<title>"+propertyMap.get("title")+"</title>";
			logHeader += "<user>"+propertyMap.get("user")+"</user>";
		}

		String xmlLayout = logHeader+getMessage(event);
		xmlLayout = "<TechnicalLog>" + xmlLayout + "</TechnicalLog>";

		return xmlLayout;
	}

	public String getMessage(ILoggingEvent event){
		String messageTagName	= "message";
		String markerName 		= event.getMarker().getName();
		TechnicalLogType marker = TechnicalLogType.Index.INSTANCE.searchByName(markerName);

		if(marker != null){
			messageTagName = marker.getMarkerName();
		}

		return "<"+messageTagName+"><![CDATA[" +
				""+event.getMessage()+"]]></"+messageTagName+">";
	}

	@Override
	public String getFileHeader() {
		String fileHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
		fileHeader += "<TechnicalLogs>";

		return fileHeader;
	}

	public String getFileFooter(){
		return "</TechnicalLogs>";
	}

}
