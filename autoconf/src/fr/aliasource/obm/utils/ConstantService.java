package fr.aliasource.obm.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
			props.load(new FileInputStream("/etc/obm/obm_conf.ini"));
		} catch (IOException e) {
			logger.error("Problem while trying to read obm_conf.ini", e);
		}
	}

	public Set<Object> getKeySet() {
		return props.keySet();
	}

	public String getStringValue(String prop) {
		String val = props.getProperty(prop);
		if (val.startsWith("\"") && val.endsWith("\"")) {
			val = val.replace("\"", "");
		}
		return val;
	}

	public boolean getBooleanValue(String prop) {
		return Boolean.valueOf(getStringValue(prop)).booleanValue();
	}

	public int getIntValue(String prop) {
		return Integer.parseInt(getStringValue(prop));
	}

}
