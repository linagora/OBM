package org.obm.push.technicallog;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.obm.push.technicallog.jaxb.schema.TechnicalLogs;


public class JabxTechnicalLogParser implements ITechnicalLogParser{

	@Override
	public TechnicalLogs parse(InputStream xml) throws TechnicalLogParserException {
		try {
			JAXBContext context = JAXBContext.newInstance(TechnicalLogs.class);
			return (TechnicalLogs) context.createUnmarshaller().unmarshal(xml);
			
		} catch (JAXBException e) {
			throw new TechnicalLogParserException("Error while parsing xml", e);
		}
	}

}
