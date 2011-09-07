package org.obm.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfigurationService {
	
	protected Properties props;
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected AbstractConfigurationService(String filename) {
		props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(filename);
			props.load(in);
		} catch (IOException e) {
			logger.error(filename + " not found", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("error closing ini file inputstream", e);
				}
			}
		}
	}
	
	public String getStringValue(String prop) {
		return props.getProperty(prop);
	}

	public boolean getBooleanValue(String prop) {
		return Boolean.valueOf(getStringValue(prop)).booleanValue();
	}

	public int getIntValue(String prop, int defaultValue) {
		try {
			return Integer.parseInt(getStringValue(prop));
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
}
