/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
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

public class CramMD5Mechanism implements AuthenticationMechanism, CallbackHandler {

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
            sasl.createClient(new String[]{"CRAM-MD5"},
                    user, server.getService(), server.getHostName(),
                    new Hashtable(), this);
            byte[] serverChallenge = server.authReceive();
            LOG.fine(new String(serverChallenge));
            
            byte[] response = sasl.evaluateChallenge(serverChallenge);
            LOG.fine(new String(response));
            server.authSend(response);            
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
