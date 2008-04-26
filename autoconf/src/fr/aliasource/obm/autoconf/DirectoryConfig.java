package fr.aliasource.obm.autoconf;

import fr.aliasource.obm.utils.ConstantService;

/**
 * Loads configuration informations from the {@link ConstantService}
 * 
 * @author tom
 * 
 */
public class DirectoryConfig {

	private int ldapPort;
	private String ldapSearchBase;
	private String[] ldapAtts;
	private String ldapFilter;
	private String ldapHost;
	private String configXml;

	public DirectoryConfig(String login, ConstantService cs) {
		ldapHost = cs.getStringValue("ldapHost");
		ldapPort = cs.getIntValue("ldapPort");
		ldapSearchBase = cs.getStringValue("ldapSearchBase");
		ldapAtts = cs.getStringValue("ldapAtts").split(",");
		ldapFilter = "(" + cs.getStringValue("ldapFilter") + "=" + login + ")";
		configXml = cs.getStringValue("configXml");
	}

	public int getLdapPort() {
		return ldapPort;
	}

	public String getLdapSearchBase() {
		return ldapSearchBase;
	}

	public String[] getLdapAtts() {
		return ldapAtts;
	}

	public String getLdapFilter() {
		return ldapFilter;
	}

	public String getLdapHost() {
		return ldapHost;
	}

	public String getConfigXml() {
		return configXml;
	}

	public void setConfigXml(String configXml) {
		this.configXml = configXml;
	}

}
