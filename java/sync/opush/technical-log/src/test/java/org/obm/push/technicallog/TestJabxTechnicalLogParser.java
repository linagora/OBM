package org.obm.push.technicallog;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.obm.push.technicallog.jaxb.schema.TechnicalLogs;

public class TestJabxTechnicalLogParser {

	@Test
	public void basicTechnicalLog() throws TechnicalLogParserException {
		InputStream xml = loadXML("data/adrien@test.tlse.lng-2011.07.08_03:25:50.log");
		JaxbTechnicalLogParser parser = new JaxbTechnicalLogParser();
		TechnicalLogs logs = parser.parse(xml);
		Assert.assertEquals(5, logs.getTechnicalLog().size());
	}


	private InputStream loadXML(String filepath) {
		InputStream in = ClassLoader.getSystemClassLoader()
				.getResourceAsStream(filepath);
		if (in == null) {
			Assert.fail("Cannot load " + filepath);
		}
		return in;
	}
}
