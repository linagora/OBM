package org.obm.push.technicallog;

import java.io.InputStream;

import org.obm.push.technicallog.jaxb.schema.TechnicalLogs;


public interface ITechnicalLogParser {
	
	TechnicalLogs parse(InputStream xml) throws TechnicalLogParserException;
	
}
