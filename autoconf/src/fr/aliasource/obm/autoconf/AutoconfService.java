/**
 * 
 */
package fr.aliasource.obm.autoconf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPUrl;

import fr.aliasource.obm.utils.ConstantService;

/**
 * @author fritos
 *
 */
public class AutoconfService extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3346961272290678959L;
	private Log logger;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		logger = LogFactory.getLog(AutoconfService.class);

		String reqString = req.getRequestURI();
		logger.info("AutoconfService : reqString: '" + reqString);

		// get user from reqString : format ".../autoconfiguration/login"
		String login = reqString.substring(reqString.lastIndexOf("/") + 1);
		logger.info("login : '" + login);

		// Find user data in ldap
		
		String ldapHost =ConstantService.getInstance().getStringValue("ldapHost");
		int ldapPort = ConstantService.getInstance().getIntValue("ldapPort");
		String ldapSearchBase = ConstantService.getInstance().getStringValue("ldapSearchBase");
		String[] ldapAtts = ConstantService.getInstance().getStringValue("ldapAtts").split(",");
		String ldapFilter ="(" + ConstantService.getInstance().getStringValue("ldapFilter") + "=" + login +")";
		
		LDAPAttributeSet attributeSet = getLdapInfos(ldapHost, ldapPort,
				ldapSearchBase, ldapAtts, ldapFilter);
		
		if ( attributeSet == null ) {
			return;
		}

		File tmp = null;
		try {
			tmp = File.createTempFile("temp", ".xml");
			
			String configXml = ConstantService.getInstance().getStringValue("configXml");
			BufferedReader reader = new BufferedReader(new FileReader(configXml));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));

			// Merge ldap data with xml file
			generateXMLConfig(reader, writer, attributeSet);

			// Send xml result file
			SimpleDateFormat formatter = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss z");
			resp.setHeader("Expires", formatter.format(new Date()));

			resp.setContentType("plain/text");
			resp.setContentLength((int) tmp.length());

			transfer(new FileInputStream(tmp), resp.getOutputStream());
		} finally {
			tmp.delete();
		}
		
	}

	/**
	 * @param ldapHost
	 * @param ldapPort
	 * @param ldapSearchBase
	 * @param ldapAtts
	 * @param ldapFilter
	 * @return
	 */
	private LDAPAttributeSet getLdapInfos(String ldapHost, int ldapPort,
			String ldapSearchBase, String[] ldapAtts, String ldapFilter) {
		LDAPConnection ld = new LDAPConnection();
		LDAPSearchResults searchResults;
		LDAPAttributeSet attributeSet;
		try {
			ld.connect(ldapHost, ldapPort);
			searchResults = ld.search(ldapSearchBase, LDAPConnection.SCOPE_SUB, ldapFilter,  ldapAtts, false);
			LDAPEntry nextEntry = searchResults.next();
            attributeSet = nextEntry.getAttributeSet();
            return attributeSet;
		} catch (LDAPException e) {
			logger.error("Error finding user info", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private void generateXMLConfig(BufferedReader reader,
			BufferedWriter writer, LDAPAttributeSet attributeSet) throws IOException {

		while (true) {
			String line = reader.readLine();
			
	        if (line == null) {
	            break;
	        }
	        
	        Iterator iterator = attributeSet.iterator();
			while ( iterator.hasNext() ) {
				LDAPAttribute att = (LDAPAttribute) iterator.next();
				line = line.replaceAll("\\|" + att.getName() + "\\|", att.getStringValue());
			}
	        
	        writer.write(line);
	        writer.newLine();
	    }
	 
	    writer.close();
	    reader.close();
	}

	private void transfer(FileInputStream in, ServletOutputStream out)
			throws IOException {

		byte[] buffer = new byte[128];
		while (true) {
			int read = in.read(buffer);
			if (read == -1) {
				break;
			}
			out.write(buffer, 0, read);
		}

	}

}
