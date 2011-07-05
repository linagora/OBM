/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Ristretto Mail API.
 *
 * The Initial Developers of the Original Code are
 * Timo Stich and Frederik Dietz.
 * Portions created by the Initial Developers are Copyright (C) 2004
 * All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.columba.ristretto.auth.mechanism;

import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.columba.ristretto.auth.AuthenticationException;
import org.columba.ristretto.auth.AuthenticationMechanism;
import org.columba.ristretto.auth.AuthenticationServer;

/**
 * Implementation of the Digest-MD5 SASL AuthenticationMechanism.
 * The implementation is based on the jsse.jar delivered
 * starting with JRE 1.4.2. 
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class DigestMD5Mechanism implements
            AuthenticationMechanism,
            CallbackHandler {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.ristretto.auth.mechanism");

    private String username;
    private char[] password;
    
    SaslWrapper sasl;
    
    
    /**
     * @see org.columba.ristretto.auth.AuthenticationMechanism#authenticate(org.columba.ristretto.auth.AuthenticationServer, java.lang.String, char[])
     */
    public void authenticate(AuthenticationServer server, String user,
            char[] password) throws IOException, AuthenticationException {
    	
    	this.username = user;
        this.password = password;
        try {
        	sasl = new SaslWrapper();
            sasl.createClient(new String[]{"DIGEST-MD5"},
                    user, server.getService(), server.getHostName(),
                    new Hashtable(), this);
            byte[] serverChallenge = server.authReceive();
            LOG.fine(new String(serverChallenge));
            
            byte[] response = sasl.evaluateChallenge(serverChallenge);
            LOG.fine(new String(response));
            server.authSend(response);
            
            serverChallenge = server.authReceive();
            LOG.fine(new String(serverChallenge));
            response = sasl.evaluateChallenge(serverChallenge);
            if (!sasl.isComplete()) {
                throw new AuthenticationException("Authentication failed");
            }
            server.authSend(new byte[0]);
        } catch (Exception e) {
        	if( e.getCause() instanceof IOException ) throw (IOException)e.getCause();
        	else throw new AuthenticationException(e.getCause());
        }
    }
    
    /**
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(Callback[] callbacks) throws IOException,  UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof TextOutputCallback) {
                // display the message according to the specified type
                TextOutputCallback toc = (TextOutputCallback) callbacks[i];
                switch (toc.getMessageType()) {
                    case TextOutputCallback.INFORMATION :
                        LOG.info(toc.getMessage());
                        break;
                    case TextOutputCallback.ERROR :
                        LOG.severe(toc.getMessage());
                        break;
                    case TextOutputCallback.WARNING :
                        LOG.warning(toc.getMessage());
                        break;
                    default :
                        LOG.fine(toc.getMessage());
                }
            } else if (callbacks[i] instanceof NameCallback) {
                NameCallback nc = (NameCallback) callbacks[i];
                nc.setName(username);
            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) callbacks[i];
                pc.setPassword(password);
            } else if (sasl.handle(callbacks[i]) ) {
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
        }
    }
}
