package fr.aliasource.funambol.security;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.funambol.framework.core.Authentication;
import com.funambol.framework.core.Cred;
import com.funambol.framework.security.Officer;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.tools.Base64;
import com.funambol.server.config.Configuration;

import fr.aliasource.funambol.OBMException;
import fr.aliasource.obm.items.manager.CalendarManager;

public class ObmOfficer implements Officer, java.io.Serializable {

	private static final long serialVersionUID = 6689829013762588002L;

	protected PersistentStore ps = null;
	private String clientAuth = Cred.AUTH_TYPE_BASIC;

	private String serverAuth = Cred.AUTH_NONE;

	private String obmAddress = null;

	private static final Log logger = LogFactory.getLog(ObmOfficer.class);

	public String getClientAuth() {
		return this.clientAuth;
	}

	public String getServerAuth() {
		return this.serverAuth;
	}

	private String getObmAddress() {
		return this.obmAddress;
	}

	/**
	 * Authenticates a credential.
	 * 
	 * @param credential
	 *            the credential to be authenticated
	 * 
	 * @return true if the credential is autenticated, false otherwise
	 */
	public Sync4jUser authenticateUser(Cred credential) {
		logger.info("authenticateUser");

		if (logger.isTraceEnabled()) {
			logger.trace(" ObmOfficer authenticate() Start");
		}

		Configuration config = Configuration.getConfiguration();
		ps = config.getStore();

		String type = credential.getType();

		if ((Cred.AUTH_TYPE_BASIC).equals(type)) {
			return authenticateBasicCredential(credential);
		}
		return null;
	}

	/**
	 * Authorizes a resource.
	 * 
	 * @param principal
	 *            the requesting entity
	 * @param resource
	 *            the name (or the identifier) of the resource to be authorized
	 * 
	 * @return
	 */
	public AuthStatus authorize(Principal principal, String resource) {
		logger.info("authorize");

		return AuthStatus.AUTHORIZED;
	}

	/**
	 * Un-authenticates a user. Do nothing. In the current implementation, the
	 * authentication is discarde as soon as the LoginContext is garbage
	 * collected.
	 * 
	 * @param
	 */
	public void unAuthenticate(Sync4jUser user) {
		logger.info("unAuthenticate");
	}

	/**
     *
     */
	private Sync4jUser authenticateBasicCredential(Cred credential) {

		String username = null, password = null;
		Sync4jPrincipal principal = null;
		Sync4jUser user = null;

		Authentication auth = credential.getAuthentication();
		String deviceId = auth.getDeviceId();
		String userpwd = new String(Base64.decode(auth.getData()));

		int p = userpwd.indexOf(':');

		if (p == -1) {
			username = userpwd;
			password = "";
		} else {
			username = (p > 0) ? userpwd.substring(0, p) : "";
			password = (p == (userpwd.length() - 1)) ? "" : userpwd
					.substring(p + 1);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Username: " + username);
		}

		user = new Sync4jUser();
		user.setEmail("");
		user.setFirstname("");
		user.setLastname("");
		user.setPassword(password);
		String[] roles = new String[] { "sync_user" };
		user.setRoles(roles);
		user.setUsername(username);

		// Verify that exist the principal for the
		// specify deviceId and username
		boolean principalFound = false;
		try {
			principal = Sync4jPrincipal.createPrincipal(username, deviceId);
			ps.read(principal);
			credential.getAuthentication().setPrincipalId(principal.getId());
			principalFound = true;
		} catch (NotFoundException nfe) {
			if (logger.isTraceEnabled()) {
				logger.trace("Principal for " + username + " devid: "
						+ deviceId + " not found");
			}
		} catch (PersistentStoreException e) {
			logger.error("Error reading principal: " + e);
			return null;
		}

		String server = this.getObmAddress();
		boolean isLoggedIn = checkObmCredentials(server, username, password);

		if (!isLoggedIn) {
			return null;
		}

		if (!principalFound) {
			principal = Sync4jPrincipal.createPrincipal(username, deviceId);
			try {
				ps.store(principal);
			} catch (PersistentStoreException ex) {
				logger.error("Error creating new principal: " + ex);
				return null;
			}

			credential.getAuthentication().setPrincipalId(principal.getId());
		}

		return user;
	}

	private boolean checkObmCredentials(String serverUrl, String login,
			String password) {

		if (serverUrl == null || serverUrl.equals("")) {

			String message = "'ObmAddress' is null or empty. check the file: "
					+ "config/com/funambol/server/security/ObmOfficer.xml";

			if (logger.isFatalEnabled()) {
				logger.fatal(message);
			}

			throw new IllegalStateException(message);
		}

		// using cal manager is somewhat crappy as we cannot share token between
		// book & cal
		// with this architecture
		CalendarManager manager = new CalendarManager(getObmAddress());

		boolean ret = false;
		try {
			manager.logIn(login, password);
			ret = true;
			manager.logout();
		} catch (OBMException e) {
			logger.error("Error on obm login: " + e);
			ret = false;
		}
		return ret;
	}

	public void setObmAddress(String obmAddress) {
		this.obmAddress = obmAddress;
	}

}
