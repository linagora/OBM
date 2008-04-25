package fr.aliasource.obm.utils;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Service pour des constantes basé sur un fichier
 * <code>constants.properties</code> placé dans le classpath
 * 
 * @author mehdi
 *
 */
public class ConstantService {	
	
	Log logger = LogFactory.getLog(ConstantService.class);
	
	
	private static ConstantService cs = new ConstantService();
	
	public static ConstantService getInstance() {
		return cs;
	}
	
	private Properties props;
	
	private ConstantService() {
		props = new Properties();
		try {
			props.load(getClass().getClassLoader().getResourceAsStream("constants.properties"));
		} catch (IOException e) {
			logger.error("No constants.properties found in classpath", e);
		}
	}
	
	public String getStringValue(String prop) {
		return props.getProperty(prop);
	}
	
	public boolean getBooleanValue(String prop) {
		return Boolean.valueOf(getStringValue(prop)).booleanValue();
	}
	
	public int getIntValue(String prop) {
		return Integer.parseInt(getStringValue(prop));
	}

}
