package fr.aliasource.obm.autoconf;

import fr.aliasource.obm.utils.ConstantService;

/**
 * @author nicolasl
 *
 */
public class DBConfig {

	private String login;
	private String domainName;
	private String query;
	

	public DBConfig(ConstantService cs, String login, String domain) {
		this.login =login;
		this.domainName = domain;
		this.query = cs.getStringValue("dbQuery");
	}
	

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	
	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}
	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}
	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return domainName;
	}
	/**
	 * @param domainName the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
}
