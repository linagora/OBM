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
