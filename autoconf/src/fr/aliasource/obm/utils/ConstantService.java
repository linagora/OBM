package fr.aliasource.obm.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.Pattern;

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
			// first load properties by using Properties.load method
			//   we know that always work
			props.load(new FileInputStream("/etc/obm/obm_conf.ini"));
			
			
			// second load properties by considering section
			//    the algorithm may not work in some case
			//    but the line above is here to prevent this ;)
			BufferedReader input = new BufferedReader(new InputStreamReader(
					new FileInputStream("/etc/obm/obm_conf.ini")));
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/tmp/monfichier")));
			
			String section_name = "";
			String line;
			Pattern section_pattern = Pattern.compile("^ *\\[(.*)\\] *$");
			Pattern assert_pattern = Pattern.compile("(.+?)=(.+)");
			Pattern comment_pattern = Pattern.compile("^[#;].*");
			Pattern quoted_pattern = Pattern.compile("^\"(.*)\"$");
			Matcher matches;
			
			while ((line = input.readLine()) != null) {
				if (!comment_pattern.matcher(line).matches()) {
//					logger.info("line="+line);
					
					matches = section_pattern.matcher(line);
					if (matches.matches()) {
						section_name = matches.group(1);
//						logger.info("section="+section_name);
					}

//					logger.info("section_name==autoconf : "+section_name.equals("autoconf"));
					
					if (section_name.equals("autoconf")) {
						matches = assert_pattern.matcher(line);
						if (matches.matches()) {
							String key = matches.group(1).trim();
							String value = matches.group(2).trim();

							matches = quoted_pattern.matcher(value);
							if (matches.matches()) {
								value = matches.group(1).trim();
							}
							
							props.setProperty(key, value);
//							logger.info("["+key +"]=["+value+"]");
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("Problem while trying to read obm_conf.ini", e);
		}
	}
	
	public Properties getProperties() {
		return props;
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
