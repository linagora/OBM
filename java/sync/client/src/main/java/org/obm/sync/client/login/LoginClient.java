package org.obm.sync.client.login;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.client.exception.SIDNotFoundException;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.locators.Locator;
import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import fr.aliacom.obm.common.domain.ObmDomain;

public class LoginClient extends AbstractClientImpl implements LoginService {

	private final Locator locator;
	private final String origin;

	@Inject
	private LoginClient(@Named("origin")String origin,
			SyncClientException syncClientException, Locator locator) {
		super(syncClientException);
		this.origin = origin;
		this.locator = locator;
	}
	
	@Override
	public AccessToken login(String loginAtDomain, String password) {
		Multimap<String, String> params = ArrayListMultimap.create();
		params.put("login", loginAtDomain);
		params.put("password", password);
		params.put("origin", origin);

		AccessToken token = newAccessToken(loginAtDomain, origin);
		
		Document doc = execute(token, "/login/doLogin", params);
		Element root = doc.getDocumentElement();
		String email = DOMUtils.getElementText(root, "email");
		String sid = DOMUtils.getElementText(root, "sid");
		Element v = DOMUtils.getUniqueElement(root, "version");
		MavenVersion version = new MavenVersion();
		if (v != null) {
			version.setMajor(v.getAttribute("major"));
			version.setMinor(v.getAttribute("minor"));
			version.setRelease(v.getAttribute("release"));
		}
		token.setDomain( getDomain(root) );
		token.setSessionId(sid);
		token.setVersion(version);
		token.setEmail(email);
		return token;
	}

	@Override
	public AccessToken authenticate(String loginAtDomain, String password) throws AuthFault {
		AccessToken token = login(loginAtDomain, password);
		try {
			if (token == null || token.getSessionId() == null) {
				throw new AuthFault(loginAtDomain + " can't log on obm-sync. The username or password isn't valid");
			}
		} finally {
			logout(token);
		}
		return token;
	}

	@Override
	public void logout(AccessToken at) {
		try {
			Multimap<String, String> params = ArrayListMultimap.create();
			setToken(params, at);
			executeVoid(at, "/login/doLogout", params);
		} catch (SIDNotFoundException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	private ObmDomain getDomain(Element root) {
		ObmDomain obmDomain = new ObmDomain();
		Element domain = DOMUtils.getUniqueElement(root, "domain");
		obmDomain.setName(DOMUtils.getElementText(domain));
		obmDomain.setUuid(domain.getAttribute("uuid"));
		return obmDomain;
	}
	
	private AccessToken newAccessToken(String loginAtDomain, String origin) {
		ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName(loginAtDomain.split("@", 2)[1]);

		AccessToken token = new AccessToken(0, origin);
		token.setUser(loginAtDomain.split("@", 2)[0]);
		token.setDomain(obmDomain);
		return token;
	}

	@Override
	protected Locator getLocator() {
		return locator;
	}
	
}
