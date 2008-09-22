/* ***** BEGIN LICENSE BLOCK *****
 *   Version: MPL 1.1/GPL 2.0/LGPL 2.1
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
 * The Original Code is OBM MAJA.
 *
 * The Initial Developer of the Original Code is
 * Nicolas Lascombes.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
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

var scriptLoader = Components.classes["@mozilla.org/moz/jssubscript-loader;1"]
                             .createInstance(Components.interfaces.mozIJSSubScriptLoader);

scriptLoader.loadSubScript("chrome://obmmaja/content/utils.js");

doMaj();

function doMaj() {
  var oldVersion = utils._getPreference("extensions.obmmaja.versionOnLastMaj", "0");
  var currentVersion = utils._getExtensionVersion("{912dfb00-4684-11dd-ae16-0800200c9a66}");
  
/* Section où l'on force les préférences au démarrage */
  
//on force le paramètre "network.protocol-handler.expose.http" à chaque démarrage, afin que les liens s'ouvrent dans le navigateur par défaut
//	utils._setPreference("network.protocol-handler.expose.http", false, "user" );
//	utils._setPreference("network.protocol-handler.expose.https", false, "user" );
	
/* fin de la section de forçage des préférences */
  
  if ( utils._isExtensionUpdated(oldVersion, currentVersion) ) {
    
    setPreferences(oldVersion);
    
    utils._setPreference("extensions.obmmaja.versionOnLastMaj", currentVersion, "user" );
  }
}

function setPreferences(oldVersion) {

	//mailHost = utils._getPreference("mail.server.serveur-courrier-3mi.hostname", "0");

	/* preferences ajoutees avec la version 1.0.7 */
	//if ( utils._isExtensionUpdated(oldVersion, "1.0.7") ) {
		// correction pour que l'auto-completion ne se fasse plus sur maia
	//        utils._setPreference("ldap_2.autoComplete.ldapServers","ldap_2.servers.serveur-ldap-contacts-publics,ldap_2.servers.serveur-ldap-groupes-publics,ldap_2.servers.serveur-ldap-3mi", "user");
	//}
	
	
	/* preferences ajoutees avec la version 1.0.6 */
	//if ( utils._isExtensionUpdated(oldVersion, "1.0.6") ) {
		// correction pour la maj auto des extensions sur le serveur d'entite
	  //      utils._setPreference("extensions.update.url", "http://"+mailHost+"/xpi/extensionsupdate.rdf", "user");
	  //      utils._setPreference("extensions.getMoreExtensionsURL", "http://"+mailHost+"/xpi/", "user");
	  //      utils._setPreference("extensions.getMoreThemesURL", "http://"+mailHost+"/xpi/", "user");
		//app.update.url : valeur qui ne fonctionne pas mais comme ça pas de mise à jour sur internet
		//	utils._setPreference("app.update.url", "https://"+mailHost+"/bling-bling/", "user");
		// correction pour la synchro automatique des contacts
		//	utils._setPreference("extensions.obm.auto.refresh.timeout", 3600000, "user");
	//}

	/* preferences ajoutees avec la version 1.0.5 */
	//if ( utils._isExtensionUpdated(oldVersion, "1.0.5") ) {
	  //      utils._setPreference("mailnews.reply_header_type", 2, "user" );
	  //      utils._setPreference("mail.quota.mainwindow_threshold.show", 100, "user" );
	  //      utils._setPreference("extension.displayquota.warnpercent", 80, "user" );
	  //      utils._setPreference("extension.displayquota.whenusenotif", 2, "user" );
	//}

}
