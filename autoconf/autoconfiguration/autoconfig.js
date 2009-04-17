//
//  Nom du fichier : autoconfig.js
//  Fonction : script d'autoconfiguration
//    téléchargé par thunderbird il récupère
//    un fichier xml de prefs/comptes/extensions 
//      puis l'applique.
//             
//
// Fonctionnement :
//  Au premier démarrage de Thunderbird, l'autoconfiguration est jouée 2 fois :
//      1. positionne la préférence 'config.obm.autoconfigStatus=0'
//      2. si 'config.obm.autoconfigStatus=0', on traite les préférences, les
//         extensions et les comptes et on positionne 'config.obm.autoconfigStatus=1'
//  Aux démarrages suivants si la pref est à 1 on ne traite que les extensions
//
//  Pour réappliquer toutes les modifs il faut repasser la pref à 0.

// Définition des niveaux de log
const LOG_ALL = 6;
const LOG_DEBUG = 5;
const LOG_INFO = 4;
const LOG_WARN = 3;
const LOG_ERROR = 2;
const LOG_FATAL = 1;
const LOG_OFF = 0;
const LOG_MESSAGES = ["NONE", "FATAL", "ERROR", "WARN", "INFO", "DEBUG"];

// Configuration du niveau de log
const LOG_LEVEL = LOG_ALL;

const PREF_LOGIN = "config.obm.login";
const PREF_AUTOCONF = "config.obm.autoconfigStatus";

const promptService = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
                                .getService(Components.interfaces.nsIPromptService);
                                
const appStartup = Components.interfaces.nsIAppStartup;
const appStartupService = Components.classes["@mozilla.org/toolkit/app-startup;1"]
                                .getService(appStartup);

var global_config_url = _getPreference("autoadmin.global_config_url");
global_config_url = global_config_url.replace(/autoconfig.js$/, "");
const CONFIG_XML_URL = global_config_url + "autoconfiguration/%s";

try {
  _logToFile(LOG_INFO, "Debut de l'autoconfiguration.\n");
} catch (e) {
  _displayError("Autoconfiguration", "Erreur lors de l'autoconfiguration !" + "\n\n" + e);
}

runAutoconfiguration();

try {
  _logToFile(LOG_INFO, "Fin de l'autoconfiguration.\n");
} catch (e) {
  _displayError("Autoconfiguration", "Erreur lors de l'autoconfiguration !" + "\n\n" + e);
}

function runAutoconfiguration() {
  try {
    var login = _getPreference(PREF_LOGIN,"");
    var autoconfStatus = _getPreference(PREF_AUTOCONF, null);
    
    while ( login == "" ) {
      login = _displayPrompt("Demande d'identifiant",
                               "Autoconfiguration de Thunderbird." + "\n\n"
                             + "Veuillez entrer votre identifiant d'autoconfiguration." );
      if (login == null) {
        //user canceled
        _logToFile(LOG_WARN, "Autoconfiguration annulée par l'utilisateur.\n");
        appStartupService.quit(appStartup.eForceQuit);
        return;
      }
      _displayMessage("Autoconfiguration", "Quand Thunderbird s'ouvrira patientez un peu, fermez et relancez.");
    }

    if ( !login ) {
      // utilisateur inconnu ou login non renseigné
      // -> autoconfiguration avortée
      _logToFile(LOG_WARN, "utilisateur inconnu ou login non renseigné.\n");
      _setPreference(PREF_AUTOCONF, autoconfStatus == null ? 0 : 1);
      return;
    }

    if (  autoconfStatus != null  ) {

      // récupère les données de configuration stockées
      // sur le serveur de référence en XML
      
      var configurationXML = _getDataHTTP(CONFIG_XML_URL.replace("%s", login));
      
      if ( configurationXML == "failed") {
        _logToFile(LOG_ERROR, "Impossible de contacter le service d'autoconfiguration.\n");
        _displayMessage("Autoconfiguration","Impossible de contacter le service d'autoconfiguration.");
        return;
      }
      
      if ( configurationXML == "error" ) {
        _logToFile(LOG_ERROR, "Erreur lors du chargement des paramètres d'autoconfiguration.\n");
        _displayMessage("Autoconfiguration","Erreur lors de l'autoconfiguration.");
        return;
      }
      
      if ( configurationXML == "" ) {
        _logToFile(LOG_ERROR, "Erreur lors de l'autoconfiguration : pas d'adresse correspondante dans l'annuaire.\n");
        _displayError("Autoconfiguration", "Erreur lors de l'autoconfiguration :" + "\n\n"
                                         + "pas d'adresse correspondante dans l'annuaire.");
        _setPreference(PREF_LOGIN, "");
        appStartupService.quit(appStartup.eForceQuit);
        return;
      }

      var configurationData = configurationXML;
      try {
        configurationData = new XML(configurationData.replace(/<\?xml .*\?>/, ""));
      } catch (e) {
        _logToFile(LOG_ERROR, "Fichier XML de configuration non valide.\n");
        _logToFile("Ficher reçu :\n");
        _logToFile(configurationData + "\n");
        _displayError("Autoconfiguration", "Fichier XML de configuration non valide.");
        _setPreference(PREF_LOGIN, "");
        appStartupService.quit(appStartup.eForceQuit);
        return;
      }
      
      if (autoconfStatus == 0) {
        // configurer les proxys
        _setupProxies(configurationData);
        // importer les certificats X.509
        _importCertificates(configurationData);
        // configurer les annuaires LDAP (autocomplétion)
        _setupDirectories(configurationData);
        // configurer les comptes de messageries
        _setupAccounts(configurationData);
        // positionner les préférences
        _setupPreferences(configurationData);
      }
      
      // installer les extensions XPI
      _installExtensions(configurationData);
    }
    
    _setPreference(PREF_LOGIN, login);
    
    _setPreference(PREF_AUTOCONF, autoconfStatus == null ? 0 : 1);

  } catch (e) {
    _logToFile(LOG_ERROR, "Erreur lors de l'autoconfiguration : " + e + "\n");
    _displayError("Autoconfiguration", "Erreur lors de l'autoconfiguration !" + "\n\n" + e);
  }
}

function _displayError(title, text) {
  promptService.alert(null, title, text);
}

function _displayMessage(title, text) {
  promptService.alert(null, title, text); // XXX -> rather info icon
}

function _displayPrompt(title, text) {
  var input = { value: "" };
  if (promptService.prompt(null, title, text, input, null, {}) ) {
    return input.value;
  } else {
    //cancel pressed
    return null;
  }
}

// effectue une requête HTTP (générique)
function _getDataHTTP(aURL) {
  var ioService = Components.classes["@mozilla.org/network/io-service;1"]
                            .getService(Components.interfaces.nsIIOService);

  var channel = ioService.newChannel(aURL, null, null);
  
  var inputStream = channel.open();
  httpChannel = channel.QueryInterface(Components.interfaces.nsIHttpChannel);

  try {
      
      if (! httpChannel.requestSucceeded ) {
        _logToFile(LOG_ERROR, "Probleme lors de la requête HTTP vers '" + aURL + "'.\n");
        return "failed";
      }
      
      var charset = "iso8859-1";
      const replacementChar = Components.interfaces.nsIConverterInputStream.DEFAULT_REPLACEMENT_CHARACTER;
      var converterInputStream = Components.classes["@mozilla.org/intl/converter-input-stream;1"]
                                           .createInstance(Components.interfaces.nsIConverterInputStream);
      converterInputStream.init(inputStream, charset, 32768, replacementChar);
      var str = "";
      var buffer = {};
      while ( converterInputStream.readString(32768, buffer) != 0 ) {
        str += buffer.value;
      }

      return str;
  } catch (e) {
    _logToFile(LOG_ERROR, "Erreur lors de la lecture de '" + aURL + "' : "+ e + "\n");
    return "error";
  }
}

// positionne les préférences utilisateur, qui seront stockées dans le
// fichier "prefs.js" du profil Mozilla de l'utilisateur
function _setupPreferences(aConfigurationData) {
  var preferences = aConfigurationData.*::preferences;

  _logToFile(LOG_INFO, "Configuration des préférences.\n");

  for each ( var preference in preferences.*::preference ) {
    if ("@remove" in preference) {
      if (preference.@remove == "true") {
        _deletePrefBranch(preference.@name);
      }
    } else {
      var typedValue;
  
      // créé une variable JavaScript typée
      switch ( preference.@type.toString() ) {
        case "string":
          typedValue = preference.@value.toString();
          break;
        case "integer":
          typedValue = Number(preference.@value).valueOf(); // NaN ??
          break;
        case "boolean":
          typedValue = (preference.@value == "true");
          break;
        default:
          break;
      }
  
      _setPreference(preference.@name, typedValue, preference.@set);
    }
  }
}

// positionne une préférence utilisateur
function _setPreference(aName, aTypedValue, aSet) {
  var prefService = Components.classes["@mozilla.org/preferences-service;1"]
                              .getService(Components.interfaces.nsIPrefService);    
  var prefBranch_User = prefService.getBranch(null);
  var prefBranch_Default = prefService.getDefaultBranch(null);

  var prefBranch = prefBranch_User;

  var prefMethod;

  _logToFile(LOG_DEBUG, "Configuration de la préférence '" + aName + "' à '" + aTypedValue + "' (de type " + (typeof aTypedValue) + ") statut='" + aSet + "'.\n");

  if ( prefBranch_Default.prefIsLocked(aName) ) {
    prefBranch_Default.unlockPref(aName);
  }

  if ( aSet == "lock" || aSet == "default" ) {
    prefBranch = prefBranch_Default;
  }

  if ( typeof aTypedValue == "string" ) {
    prefMethod = prefBranch.setCharPref;
  } else if ( typeof aTypedValue == "number" ) {
    prefMethod = prefBranch.setIntPref;
  } else if ( typeof aTypedValue == "boolean" ) {
    prefMethod = prefBranch.setBoolPref;
  }

  prefMethod.call(this, aName, aTypedValue);

  if ( aSet == "lock" ) {
    prefBranch_Default.lockPref(aName);
  }
}

function _getPreference(aName, aDefaultValue) {
  var prefBranch = Components.classes["@mozilla.org/preferences-service;1"]
                             .getService(Components.interfaces.nsIPrefBranch);
  var value;
  switch (prefBranch.getPrefType(aName)) {
    case prefBranch.PREF_BOOL:
      value = prefBranch.getBoolPref(aName);
      break;
    case prefBranch.PREF_INT:
      value = prefBranch.getIntPref(aName);
      break;
    case prefBranch.PREF_STRING:
      value = prefBranch.getCharPref(aName);
      break;
    default:
      value = aDefaultValue;
      break;
  }
  _logToFile(LOG_DEBUG, "Récupération de la préférence '" + aName + "' de valeur '" + value + "'.\n");
  return value;
}

function _deletePrefBranch(aName) {
  try {
    _logToFile(LOG_DEBUG, "Suppression de la préférence '" + aName + "'.\n");
    var prefBranch = Components.classes["@mozilla.org/preferences-service;1"]
                               .getService(Components.interfaces.nsIPrefBranch);
    prefBranch.deleteBranch(aName);
  } catch (e) {
    _logToFile(LOG_ERROR, "Lors de la suppression de la préférence '" + aName + "' : " + e + "\n");
    //nothing
  }
}

function _prefBranchExists(aBranchName) {
  var prefBranch = Components.classes["@mozilla.org/preferences-service;1"]
                             .getService(Components.interfaces.nsIPrefBranch);
  var childList = prefBranch.getChildList(aBranchName, {});
  return childList.length > 0;
}

function _setupProxies(aConfigurationData) {
  var proxies = aConfigurationData.*::proxies;

  _logToFile(LOG_INFO, "Configuration des proxies.\n");

  for each ( var proxy in proxies.*::proxy ) {
    if ( proxy.@type == "auto" ) {
      _setPreference("network.proxy.type", 2);
      _setPreference("network.proxy.autoconfig_url",
                     proxy.@value.toString());
    } else if ( proxy.@type == "manual" ) {
      _setPreference("network.proxy.type", 1);
      _setPreference("network.proxy.share_proxy_settings", true);
      for each ( var protocol in ["http", "ssl", "ftp",
                                  "gopher", "socks"] ) {
        _setPreference("network.proxy." + protocol,
                       proxy.@host.toString());
        _setPreference("network.proxy." + protocol + "_port",
                       Number(proxy.@port.toString()).valueOf());
      }
      if ( "@exclude" in proxy ) {
        _setPreference("network.proxy.no_proxies_on",
                       proxy.@exclude.toString());
      }
    }
  }
}

// installe les extensions listées dans les données de configuration,
// dans le cas où elles ne sont pas déjà installées
function _installExtensions(aConfigurationData) {
  var requiresRestart = false;

  var extensionsToInstall = [];

  var extensions = aConfigurationData.*::extensions;

  var extensionManager = Components.classes["@mozilla.org/extensions/manager;1"]
                                   .getService(Components.interfaces.nsIExtensionManager);

  _logToFile(LOG_INFO, "Installation des extensions.\n");

  for each ( var extension in extensions.*::extension ) {
    if ( _extensionMustBeUnInstalled(extension) ) {
      if (_extensionIsAlreadyInstalled(extension.@id)) {
        _logToFile(LOG_DEBUG, "Suppression de l'extension '" + extension.@id + "'.\n");
        extensionManager.uninstallItem(extension.@id);
      }
    } else {
      if ( _extensionMustBeInstalled(extension) ) {
        _logToFile(LOG_DEBUG, "Installation de l'extension '" + extension.@src.toString() + "'.\n");
        extensionsToInstall.push(extension.@src.toString());
      }
    }
  }
  var installManager = Components.classes["@mozilla.org/xpinstall/install-manager;1"]
                                 .getService(Components.interfaces.nsIXPInstallManager);

  //PICASSO-75 : impossible d'installer des extensions
  //bug thunderbird : si on apelle aucune méthode sur installManager
  //l'install d'une extension crashe après l'autoconf
  // -> problème au niveau du destructeur certainement
  installManager.initManagerFromChrome(extensionsToInstall,
                                         extensionsToInstall.length,
                                         null /* listener */);
  if ( extensionsToInstall.length > 0 ) {
     requiresRestart = true;
  }

  return requiresRestart;
}

// vérifie qu'une extension doit être installée si :
// * elle ne l'est pas déjà
//  ou
// * elle peut être mise à jour
//  et
// * elle est compatible avec "l'environnment Mozilla d'exécution"
//  et
// * elle n'est pas spécifiée comme interdite
function _extensionMustBeInstalled(aExtension) {
  return ( (!_extensionIsAlreadyInstalled(aExtension.@id)
         || _extensionCanBeUpdated(aExtension.@id, aExtension.@version))
         && _extensionIsCompatible(aExtension)
         && ( !aExtension.@allowed || aExtension.@allowed == "true"));
}

// vérifie si l'extension est spécifié dans le xml comme :
// * à désinstaller
// ou
// * interdite
// et
// * qu'elle est installé
function _extensionMustBeUnInstalled(aExtension) {
  return ( ((aExtension.@uninstall && aExtension.@uninstall == "true")
  				 || (aExtension.@allowed && aExtension.@allowed == "false"))
           && _extensionIsAlreadyInstalled(aExtension.@id) );
}


// vérifie qu'une extension n'est pas déjà installée,
// d'après son identifiant
function _extensionIsAlreadyInstalled(aExtensionID) {

  var installed = false;
  var extensionManager = Components.classes["@mozilla.org/extensions/manager;1"]
                                   .getService(Components.interfaces.nsIExtensionManager);
  return extensionManager.getInstallLocation(aExtensionID) != null;
}

function _extensionCanBeUpdated(aExtensionID, aVersion) {
  const rdfService = Components.classes["@mozilla.org/rdf/rdf-service;1"]
                               .getService(Components.interfaces.nsIRDFService);
  const comparator = Components.classes["@mozilla.org/xpcom/version-comparator;1"]
                               .getService(Components.interfaces.nsIVersionComparator);

  var canBeUpdated = false;

  var extensionsDS = rdfService.GetDataSourceBlocking(_getFileURLFromProfile("extensions.rdf"));

  var itemResource = rdfService.GetResource("urn:mozilla:item:" + aExtensionID);
  var versionResource = rdfService.GetResource("http://www.mozilla.org/2004/em-rdf#version");

  var target = extensionsDS.GetTarget(itemResource, versionResource, true);
  if ( target ) {
    var version = target.QueryInterface(Components.interfaces.nsIRDFLiteral).Value;
    canBeUpdated = comparator.compare(aVersion, version) > 0;
  }

  rdfService.UnregisterDataSource(extensionsDS);

  _logToFile(LOG_DEBUG, "L'extension '" + aExtensionID + (canBeUpdated?" peut":" ne peut pas") + " être mise à jour (" + version + "->" + aVersion + ").\n");

  return canBeUpdated;
}

// vérifie qu'une extension est compatible avec "l'environnment Mozilla d'exécution"
function _extensionIsCompatible(aExtension) {
  const runtime = Components.classes["@mozilla.org/xre/app-info;1"]
                            .getService(Components.interfaces.nsIXULRuntime);
  const appInfo = Components.classes["@mozilla.org/xre/app-info;1"]
                            .getService(Components.interfaces.nsIXULAppInfo);
  const comparator = Components.classes["@mozilla.org/xpcom/version-comparator;1"]
                               .getService(Components.interfaces.nsIVersionComparator);
  const platform = runtime.OS + "_" + runtime.XPCOMABI;

  var applicationCompatible = false;
  var platformCompatible = false;

  // vérifie que l'extension est compatible avec l'application Mozilla
  for each ( var targetApplication in aExtension.*::targetApplication ) {
    if ( targetApplication.@id == appInfo.ID
      && comparator.compare(targetApplication.@minVersion, appInfo.version) <= 0
      && comparator.compare(targetApplication.@maxVersion, appInfo.version) >= 0 ) {
      applicationCompatible = true;
      _logToFile(LOG_DEBUG, "L'extension '" + aExtension.@id + "' est compatible avec l'application.\n");
      break;
    }
  }

  // vérifie que l'extension est compatible avec la plate-forme
  if ( aExtension.*::targetPlatform.length() == 0 ) {
    // l'extension est compatible avec toutes les plates-formes
    platformCompatible = true;
    _logToFile(LOG_DEBUG, "L'extension '" + aExtension.@id + "' est compatible avec la plateforme.\n");
  } else {
    for each ( var targetPlatform in aExtension.*::targetPlatform ) {
      if ( targetPlatform.@name == platform ) {
        platformCompatible = true;
        _logToFile(LOG_DEBUG, "L'extension '" + aExtension.@id + "' est compatible avec la plateforme.\n");
        break;
      }
    }
  }

  return applicationCompatible && platformCompatible;
}

function _getFileURLFromProfile(aFilename) {
  const ioService = Components.classes["@mozilla.org/network/io-service;1"]
                              .getService(Components.interfaces.nsIIOService);

  var file = Components.classes["@mozilla.org/file/directory_service;1"]
                       .getService(Components.interfaces.nsIProperties)
                       .get("ProfD", Components.interfaces.nsIFile);
  var fileHandler = ioService.getProtocolHandler("file")
                             .QueryInterface(Components.interfaces.nsIFileProtocolHandler);

  file.append(aFilename);
  return fileHandler.getURLSpecFromFile(file);
}

// importe les certificats listés dans les données de configuration,
// dans le cas où ils ne sont pas déjà présents dans le keystore
function _importCertificates(aConfigurationData) {
  const BEGIN_CERT_TAG = "-----BEGIN CERTIFICATE-----";
  const END_CERT_TAG = "-----END CERTIFICATE-----";

  // http://www.mozilla.org/projects/security/pki/nss/tools/certutil.html
  const trustFlags = "C,C,C";

  var certDB = Components.classes["@mozilla.org/security/x509certdb;1"]
                         .getService(Components.interfaces.nsIX509CertDB2);

  var ioService = Components.classes["@mozilla.org/network/io-service;1"]
                            .getService(Components.interfaces.nsIIOService);

  var scriptableStream = Components.classes["@mozilla.org/scriptableinputstream;1"]
                                   .getService(Components.interfaces.nsIScriptableInputStream);

  var certificates = aConfigurationData.*::certificates;

  _logToFile(LOG_INFO, "Import des certificats.\n");

  for each ( var certificate in certificates.*::certificate ) {
    var fingerprint;

    if ( "@md5Fingerprint" in certificate ) {
      fingerprint = certificate.@md5Fingerprint.toString();
    } else if ( "@sha1Fingerprint" in certificate ) {
      fingerprint = certificate.@sha1Fingerprint.toString();
    }

    if ( _certificateIsAlreadyInstalled(fingerprint) ) {
      _logToFile(LOG_DEBUG, "Certificat " + certificate.@src + " déjà installé.\n");
      continue;
    }

    var certificateContent = _getDataHTTP(certificate.@src);
    if ( !certificateContent ) {
      continue;
    }
    if ( configurationXML == "failed" ) {
      _logToFile(LOG_ERROR, "Impossible de télécharger le certificat '" + certificate.@src + "'.\n");
      continue;
    }
    
    if ( configurationXML == "error" ) {
      _logToFile(LOG_ERROR, "Erreur lors du téléchargement du certificat '" + certificate.@src + "'.\n");
      continue;
    }
    
    if ( configurationXML == "" ) {
      _logToFile(LOG_DEBUG, "Le certificat " + certificate.@src + " est vide.\n");
      continue;
    }
         
    certificateContent = certificateContent.replace(/[\r\n]/g, "");
    var begin = certificateContent.indexOf(BEGIN_CERT_TAG);
    var end = certificateContent.indexOf(END_CERT_TAG);
    certificateContent = certificateContent.substring(begin + BEGIN_CERT_TAG.length, end);

    try {
      certDB.addCertFromBase64(certificateContent,
                               trustFlags, ""); // name: not taken into account, so ""!
    } catch (e) {
      _logToFile(LOG_ERROR, "Installation du certificat " + certificate.@src + " impossible.\n");
    }
  }
}

function _certificateIsAlreadyInstalled(aCertificateFingerprint) {
  var certDB = Components.classes["@mozilla.org/security/x509certdb;1"]
                         .getService(Components.interfaces.nsIX509CertDB);

  var installedCertificates = new Array();

  var certificates = {};
  certDB.findCertNicknames(null, // default token
                           Components.interfaces.nsIX509CertDB.TRUSTED_SSL,
                           {}, certificates);

  for each ( var certificateDescription in certificates.value ) {
    // format de chaque entrée :
    // (\u0001 Token de stockage)? \u0001 Nom du certificat \u0001 Clé du certificat
    var pieces = certificateDescription.split("\u0001");
    var dbKey = pieces.pop();
    var certificate = certDB.findCertByDBKey(dbKey, null);
    installedCertificates.push(certificate);
  }

  for each ( var certificate in installedCertificates ) {
    if ( certificate.md5Fingerprint == aCertificateFingerprint
      || certificate.sha1Fingerprint == aCertificateFingerprint ) {
      return true;
    }
  }

  return false;
}

// configure les comptes de messagerie listés dans les données de configuration :
// comptes IMAP et POP, serveurs SMTP, identités utilisateur
function _setupAccounts(aConfigurationData) {
  // COMPTES
  var accounts = aConfigurationData.*::accounts;

  var currentAccounts = _getPreference("mail.accountmanager.accounts", "");
  var allAccounts = (currentAccounts != "" ? currentAccounts.split(",") : []);

  _logToFile(LOG_INFO, "Configuration des comptes de messagerie.\n");

  for each ( var account in accounts.*::account ) {
    if ( currentAccounts.indexOf(account.@id.toString()) != -1 ) {
      // compte existe déjà
      _logToFile(LOG_DEBUG, "Le compte " + account.@id + " existe déjà.\n");
      continue;
    }

    _setPreference("mail.account." + account.@id + ".identities",
                   account.@identities.toString().replace(" ", ","));
    _setPreference("mail.account." + account.@id + ".server",
                   account.@server.toString());
    allAccounts.push(account.@id.toString());
  }

  _setPreference("mail.accountmanager.accounts",
                 allAccounts.join(","));

  if ( "@defaultAccount" in accounts ) {
    _setPreference("mail.accountmanager.defaultaccount",
                   accounts.@defaultAccount.toString());
  }

  // IDENTITÉS
  var identities = aConfigurationData.*::identities;

  for each ( var identity in identities.*::identity ) {
    if ( _prefBranchExists("mail.identity." + identity.@id) ) {
      // identité existe déjà
      _logToFile(LOG_DEBUG, "L'identité " + identity.@id + " existe déjà.\n");
      continue;
    }

    _setPreference("mail.identity." + identity.@id + ".fullName",
                   identity.@fullName.toString());
    _setPreference("mail.identity." + identity.@id + ".useremail",
                   identity.@fromAddress.toString());
    _setPreference("mail.identity." + identity.@id + ".smtpServer",
                   identity.@smtpServer.toString());
    if ( "@autocompleteDirectory" in identity ) {
      _setPreference("mail.identity." + identity.@id + ".directoryServer",
                     "ldap_2.servers." + identity.@autocompleteDirectory.toString());
      _setPreference("mail.identity." + identity.@id + ".overrideGlobal_Pref", true);
    }
    
    
    if ( "@draftFolder_pickerMode" in identity ) {
      _setPreference("mail.identity." + identity.@id + ".drafts_folder_picker_mode",
                     identity.@draftFolder_pickerMode.toString());
                     
    }
    if ( "@fccFolder_pickerMode" in identity ) {
      _setPreference("mail.identity." + identity.@id + ".fcc_folder_picker_mode",
                     identity.@fccFolder_pickerMode.toString());
    }
    if ( "@stationeryFolder_pickerMode" in identity ) {
      _setPreference("mail.identity." + identity.@id + ".tmpl_folder_picker_mode",
                     identity.@stationeryFolder_pickerMode.toString());
    }
    
    
    if ( "@draftFolder" in identity ) {
       var value = identity.@draftFolder.toString();
      if ( value.match(/.*@.*@/) ) {
        //multi-domain : replace 'login@domain@server' by 'login%40domain@server'
        value = value.replace("@","%40");
      }
      _setPreference("mail.identity." + identity.@id + ".draft_folder", value);
                     
    }
    if ( "@fccFolder" in identity ) {
      var value = identity.@fccFolder.toString();
      if ( value.match(/.*@.*@/) ) {
        //multi-domain : replace 'login@domain@server' by 'login%40domain@server'
        value = value.replace("@","%40");
      }
      _setPreference("mail.identity." + identity.@id + ".fcc_folder", value);
    }
    if ( "@stationeryFolder" in identity ) {
      var value = identity.@stationeryFolder.toString();
      if ( value.match(/.*@.*@/) ) {
        //multi-domain : replace 'login@domain@server' by 'login%40domain@server'
        value = value.replace("@","%40");
      }
      _setPreference("mail.identity." + identity.@id + ".stationery_folder", value);
    }
    
    if ( "@composeHtml" in identity) {
      _setPreference("mail.identity." + identity.@id + ".compose_html",
                     identity.@composeHtml == "true" ? true : false);
    }
    
    if ( "@replyOnTop" in identity) {
      var value;
      switch ( identity.@replyOnTop.toString() ) {
        case "1":
          value = 1;
          break;
        case "2":
          value = 2;
          break;
        case "3":
          value = 3;
          break;
      }
      _setPreference("mail.identity." + identity.@id + ".reply_on_top",
                     value);
    }
    
    if ( "@sigBottom" in identity) {
      _setPreference("mail.identity." + identity.@id + ".sig_bottom",
                     identity.@sigBottom == "true" ? true : false);
    }
  }

  // SERVEURS
  var servers = aConfigurationData.*::servers;

  for each ( var server in servers.*::server.(@type == "imap" || @type == "pop3" || @type == "nntp") ) {
    if ( _prefBranchExists("mail.server." + server.@id) ) {
      // serveur existe déjà
      _logToFile(LOG_DEBUG, "Le serveur " + server.@id + " existe déjà.\n");
      continue;
    }

      _setPreference("mail.server." + server.@id + ".hostname",
                     server.@hostname.toString());
      _setPreference("mail.server." + server.@id + ".type",
                     server.@type.toString());
      _setPreference("mail.server." + server.@id + ".name",
                     server.@label.toString());
      if ( "@username" in server ) {
        _setPreference("mail.server." + server.@id + ".userName",
                       server.@username.toString());
      }
      if ( "@port" in server ) {
        _setPreference("mail.server." + server.@id + ".port",
                       server.@port.toString());
      }
      if ( "@secureConnection" in server ) {
        var value;
        switch ( server.@secureConnection.toString() ) {
          case "never":
            value = 0;
            break;
          case "tlsIfAvailable":
            value = 1;
            break;
          case "tls":
            value = 2;
            break;
          case "ssl":
            value = 3;
            break;
        }

        _setPreference("mail.server." + server.@id + ".socketType",
                       value);
      }
      if ( "@secureAuthentication" in server ) {
        _setPreference("mail.server." + server.@id + ".useSecAuth",
                       server.@secureAuthentication == "true" ? true : false);
      }

      if ( "@trashFolder" in server ) {
        var value = server.@trashFolder.toString();
        if ( value.match(/.*@.*@/) ) {
          //multi-domain : replace 'login@domain@server' by 'login%40domain@server'
          value = value.replace("@","%40");
        }
        _setPreference("mail.server." + server.@id + ".trash_folder_name", value);
      }
      
      if ( "@downloadBodies" in server ) {
        _setPreference("mail.server." + server.@id + ".download_bodies_on_get_new_mail",
                      server.@downloadBodies == "true" ? true : false);
      }
      
      if ( "@offlineDownload" in server ) {
        _setPreference("mail.server." + server.@id + ".offline_download",
                      server.@offlineDownload == "true" ? true : false);
      }
      
      if ( "@useSubscription" in server ) {
        _setPreference("mail.server." + server.@id + ".using_subscription",
                      server.@useSubscription == "true" ? true : false);
      }
      
      if ( "@useIdle" in server ) {
        _setPreference("mail.server." + server.@id + ".use_idle",
                      server.@useIdle == "true" ? true : false);
      }
  }

  var addedServers = [];
  var currentServers = _getPreference("mail.smtpservers");
  currentServers = (currentServers ? currentServers.split(",") : []);

  for each ( var server in servers.*::server.(@type == "smtp") ) {
    if ( currentServers.indexOf(server.@id.toString()) != -1 ) {
      _logToFile(LOG_DEBUG, "Le serveur SMTP " + server.@id + " existe déjà.\n");
      // serveur existe déjà
      continue;
    }

      _setPreference("mail.smtpserver." + server.@id + ".hostname",
                     server.@hostname.toString());
      _setPreference("mail.smtpserver." + server.@id + ".description",
                     server.@label.toString());
      if ( "@username" in server ) {
        _setPreference("mail.smtpserver." + server.@id + ".username",
                       server.@username.toString());
        _setPreference("mail.smtpserver." + server.@id + ".auth_method", 1);
      } else {
        _setPreference("mail.smtpserver." + server.@id + ".auth_method", 0);
      }

    if ( "@port" in server ) {
        _setPreference("mail.smtpserver." + server.@id + ".port",
                       server.@port.toString());
      }

      if ( "@secureConnection" in server ) {
        var value;
        switch ( server.@secureConnection.toString() ) {
          case "never":
            value = 0;
            break;
          case "tlsIfAvailable":
            value = 1;
            break;
          case "tls":
            value = 2;
            break;
          case "ssl":
            value = 3;
            break;
        }

        _setPreference("mail.smtpserver." + server.@id + ".try_ssl", value);
        addedServers.push(server.@id.toString());
      }
  }

  _setPreference("mail.smtpservers", currentServers.concat(addedServers).join(","));

  if ( "@defaultSMTP" in servers ) {
    _setPreference("mail.smtp.defaultserver",
                   servers.@defaultSMTP.toString());
  }
}

// configure les annuaires LDAP listés dans les données de configuration
function _setupDirectories(aConfigurationData) {
  var directories = aConfigurationData.*::directories;

  var autoCompleteDirectories = _getPreference("ldap_2.autoComplete.ldapServers", "");

  _logToFile(LOG_INFO, "Configuration des annuaires LDAP.\n");

  for each ( var directory in directories.*::directory ) {
    
    if ( _getPreference("ldap_2.servers." + directory.@id + ".description") != undefined ) {
      _logToFile(LOG_DEBUG, "L'annuaire LDAP " + directory.@id + " existe déjà.\n");
      // annuaire existe déjà
      continue;
    }

    // important, sert à supprimer notamment la préférence ".position"
    _deletePrefBranch("ldap_2.servers." + directory.@id);

    _setPreference("ldap_2.servers." + directory.@id + ".description",
                   directory.@label.toString());
    _setPreference("ldap_2.servers." + directory.@id + ".uri",
                   directory.@uri.toString());
    if ( "@bindDN" in directory ) {
      _setPreference("ldap_2.servers." + directory.@id + ".auth.dn",
                     directory.@bindDN.toString());
      _setPreference("ldap_2.servers." + directory.@id + ".auth.savePassword",
                     true);
    }
    if ( "@maxHits" in directory ) {
      _setPreference("ldap_2.servers." + directory.@id + ".maxHits",
                     Number(directory.@maxHits.toString()).valueOf());
    }
    
    // si les prefs suivantes ne sont pas renseignées la complétion d'addresse crashe Thunderbird
    _setPreference("ldap_2.servers." + directory.@id + ".filename",
                    directory.@id + ".mab");
    _setPreference("ldap_2.servers." + directory.@id + ".replication.lastChangeNumber"
                    , 0);                
    
  if ( "@autocomplete" in directory ) {
    //extension multi-ldap : activer l'annuaire pour l'auto-complétion
    if ( !autoCompleteDirectories.match("ldap_2.servers." + directory.@id) ) {
      if (autoCompleteDirectories != "") {
        autoCompleteDirectories += ",";
      }
      autoCompleteDirectories += "ldap_2.servers." + directory.@id;
    }
  }
    
  }
  //extension multi-ldap : activer tous les annuaires pour l'auto-complétion
  _setPreference("ldap_2.autoComplete.ldapServers", autoCompleteDirectories);
  _setPreference("ldap_2.autoComplete.useDirectory", true); 
}

//Fonctions d'enregistrement des logs
var gLogStream = null;
function _logToFile(aLevel, aString) {
  try {
    if ( !gLogStream ) {
      var file = Components.classes["@mozilla.org/file/directory_service;1"]
                           .getService(Components.interfaces.nsIProperties)
                           .get("TmpD", Components.interfaces.nsIFile);
      file.append("autoconf.log");
      //file.createUnique(Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 0600);
      gLogStream = Components.classes["@mozilla.org/network/file-output-stream;1"]
                             .createInstance(Components.interfaces.nsIFileOutputStream);
      gLogStream.init(file, 0x02 | 0x08 | 0x20, 0600, 0); // XXX
    }
    if (LOG_LEVEL >= aLevel) {
      var message = LOG_MESSAGES[aLevel] + " : " + aString;
      gLogStream.write(message, message.length);
    }
  } catch (e) {}
}

function _logToConsole(msg) {
  var aConsoleService = Components.classes["@mozilla.org/consoleservice;1"].
    getService(Components.interfaces.nsIConsoleService);
  if (msg && msg != "")
    aConsoleService.logStringMessage(msg);
}

