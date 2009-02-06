package fr.aliasource.obm.autoconf;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novell.ldap.LDAPAttributeSet;

import fr.aliasource.obm.utils.ConstantService;

/**
 * @author nicolasl
 * 
 */
public class AutoconfService extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3346961272290678959L;
	private Log logger;

	public AutoconfService() {
		logger = LogFactory.getLog(getClass());
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String reqString = req.getRequestURI();
		logger.info("AutoconfService : reqString: '" + reqString);

		// get user from reqString : format ".../autoconfiguration/login"
		String login = reqString.substring(reqString.lastIndexOf("/") + 1);
		logger.info("login : '" + login);

		DirectoryConfig dc = new DirectoryConfig(login, ConstantService
				.getInstance());
		LDAPQueryTool lqt = new LDAPQueryTool(dc);
		LDAPAttributeSet attributeSet = lqt.getLDAPInformations();

		if (attributeSet == null) {
			logger.warn("NULL informations obtained from LDAPQueryTool for "
					+ login);
			return;
		}
		
		DBConfig dbc = new DBConfig(ConstantService.getInstance(), attributeSet
				.getAttribute("uid").getStringValue(), attributeSet
				.getAttribute("obmDomain").getStringValue());
		DBQueryTool dbqt = new DBQueryTool(dbc);
		
		HashMap<String, String> hostIps = dbqt.getDBInformation();
		
		if (hostIps == null) {
			logger.warn("NULL information obtained from DBQueryTool for "
					+ login);
			return;
		}
		
		if (hostIps.get("imap") == null || hostIps.get("smtp") == null) {
			logger.warn("No hosts found for  " + login);
			return;
		}

		// map host ip with imap host and smtp host
		
		String imapMailHost = ConstantService.getInstance().getStringValue(
				"imap." + hostIps.get("imap"));
		String smtpMailHost = ConstantService.getInstance().getStringValue(
				"smtp." + hostIps.get("smtp"));
		String ldapHost = ConstantService.getInstance().getStringValue(
				"ldapHostname");

		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z");
		resp.setHeader("Expires", formatter.format(new Date()));
		resp.setContentType("application/xml");

		TemplateLoader tl = new TemplateLoader(dc);
		tl.applyTemplate(attributeSet, imapMailHost, smtpMailHost, ldapHost, resp.getOutputStream());
	}

}
