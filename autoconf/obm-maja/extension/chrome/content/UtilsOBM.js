/* TODO LICENSE  */

function debug(s) {
  const debugging = true;
  if (debugging) {
      dump(s + "\n");
  }
}

var gLogStream;

function debugObject(o) {
  for (var i in o) {
    debug(i + ": " + o[i]);
  }
}

/** @ignore */
function XMLSerializer() {
  return Components.classes["@mozilla.org/xmlextras/xmlserializer;1"]
                   .createInstance(Components.interfaces.nsIDOMSerializer);
}

/** @ignore */
function XMLHttpRequest() {
  return Components.classes["@mozilla.org/xmlextras/xmlhttprequest;1"]
                   .createInstance(Components.interfaces.nsIXMLHttpRequest);
}

/** @ignore */
function DOMParser() {
  return Components.classes["@mozilla.org/xmlextras/domparser;1"]
                   .createInstance(Components.interfaces.nsIDOMParser);
}

/** @ignore */
function PrefBranch() {
  return Components.classes["@mozilla.org/preferences-service;1"]
                   .getService(Components.interfaces.nsIPrefBranch);
}

/** @ignore */
function DateTime() {
  return Components.classes["@mozilla.org/calendar/datetime;1"]
                   .createInstance(Components.interfaces.calIDateTime);
}

function PasswordManager() {
  return Components.classes["@mozilla.org/passwordmanager;1"]
                   .getService(Components.interfaces.nsIPasswordManager);
}

/* UtilsOBM */

/**
  * UtilsOBM est une classe rassemblant toutes
  * les fonctions "utilitaires" de l'extension.
  * <p>
  * Toutes ses fonctions sont statiques.
  * @constructor
 */
function UtilsOBM() {
}

/**
  * Envoie une requête SOAP.
  * @param {String} aTransportURI
  * @param {String} aMethodName
  * @param {String} aTargetObjectURI
  * @param {Array} aParameters Un tableau d'objets de type XML
  * @type Object
  * @return Un object avec pour propriétes : 
  * <ul>
  *   <li><code>status</code> - code du statut HTTP</li>
  *   <li><code>statusText</code> - texte du statut HTTP</li>
  *   <li><code>responseXML</code> - objet de type XML</li>
  * </ul>
  */
UtilsOBM.soapRequest = function UO_soapRequest(aTransportURI, aMethodName, aTargetObjectURI, aParameters) {
    var env = new Namespace("env", "http://schemas.xmlsoap.org/soap/envelope/");
    var ns = new Namespace("ns", aTargetObjectURI);
    var response;

    var soapEnvelope = 
      <env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/"
                    env:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
        <env:Header/>
        <env:Body>
          <ns:{aMethodName} xmlns:ns={aTargetObjectURI}>
          </ns:{aMethodName}>
        </env:Body>
      </env:Envelope>;

    for each ( var parameter in aParameters ) {
      soapEnvelope.env::Body.ns::[aMethodName].parameter += parameter;
    }

    var httpRequest = new XMLHttpRequest();

    httpRequest.open('POST', aTransportURI, false);
    httpRequest.setRequestHeader("SOAPAction", " ");
    httpRequest.send(soapEnvelope.toString());

    if ( UtilsOBM.getBoolPref("extensions.obm.log") ) {
      UtilsOBM.logToFile("===>\n" + soapEnvelope.toString() + "\n\n");
    }

    var httpResponse = {
      status: httpRequest.status,
      statusText: httpRequest.statusText,
      responseXML: new XML(httpRequest.responseText.replace(/<\?xml.*\?>/, ""))
    };

    if ( UtilsOBM.getBoolPref("extensions.obm.log") ) {
      UtilsOBM.logToFile("<====\n" + httpRequest.responseText + "\n\n");
    }
   
    return httpResponse;
}
UtilsOBM.killSoapRequest = function UO_killSoapRequest(httpRequest){
  UtilsOBM.logToFile("killing httpRequest\n");
  httpRequest.abort();
}

UtilsOBM.logToFile = function(aString) {
  try {
    if ( !gLogStream ) {
      var file = Components.classes["@mozilla.org/file/directory_service;1"]
                           .getService(Components.interfaces.nsIProperties)
                           .get("TmpD", Components.interfaces.nsIFile);
      file.append("obm-extension.log");
      //file.createUnique(Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 0600);
      gLogStream = Components.classes["@mozilla.org/network/file-output-stream;1"]
                             .createInstance(Components.interfaces.nsIFileOutputStream);
      gLogStream.init(file, 0x02 | 0x08 | 0x20, 0600, 0); // XXX
    }
    gLogStream.write(aString, aString.length);
  } catch (e) {}
}

/**
 * Transforme une date renvoyée par OBM
 * en un object JavaScript de type Date.
 * @param {String} aXMLDate Une date au format ISO-8601
 * @type Date
 */
UtilsOBM.xmlDateToJSDate = function UO_xmlDateToJSDate(aXMLDate) {
    var jsDate = new Date();
    try {
      jsDate.setISO8601(aXMLDate);
      return jsDate;
    } catch (e) {
      return "";
    }
}

/**
 * Transforme un object JavaScript de type Date
 * en une date compréhensible par OBM.
 * @param {Date} aJSDate Un object JavaScript de type Date
 * @type String
 */
UtilsOBM.jsDateToXMLDate = function UO_jsDateToXMLDate(aJSDate) {
    var xmlDate = aJSDate.toISO8601String();
    return xmlDate;
}

/**
  * Renvoie la variable passée en paramètre si
  * sa valeur est non nulle, sinon chaîne vide <code>""</code>.
  * @private
  * @param {Object} aVar
  * @type Object
  */
UtilsOBM._ifNotNull = function UO_ifNotNull(aVar) {
  return aVar != null ? aVar : "";
}

/**
  * Récupère la valeur de type "char" d'une préférence utilisateur.
  * @param {String} aPrefName Le chemin complet de la préférence
  *                           (ex: <code>"extensions.obm..."</code>)
  * @param {String} aDefaultValue Une valeur par défaut, renvoyé si
  *                               la préférence demandée n'existe pas
  * @type String
  */
UtilsOBM.getCharPref = function UO_getCharPref(aPrefName, aDefaultValue) {
  var prefValue;
  try {
    var prefService = new PrefBranch();

    prefValue = prefService.getCharPref(aPrefName);
  } catch (e) {
    prefValue = aDefaultValue;
  }
  return prefValue;
}

/**
  * Définit la valeur d'une préférence utilisateur.
  * @param {String} aPrefName Le chemin complet de la préférence
  *                           (ex: <code>"extensions.obm..."</code>)
  * @param {String} aValue La valeur de la préférence à écrire
  */
UtilsOBM.setCharPref = function UO_setCharPref(aPrefName, aValue) {
  var prefService = new PrefBranch();
  prefService.setCharPref(aPrefName, aValue);
}
/**
  * Récupère la valeur de type "bool" d'une préférence utilisateur.
  * @param {String} aPrefName Le chemin complet de la préférence
  *                           (ex: <code>"extensions.obm..."</code>)
  * @param {String} aDefaultValue Une valeur par défaut, renvoyé si
  *                               la préférence demandée n'existe pas
  * @type Boolean
  */
UtilsOBM.getBoolPref = function UO_getBoolPref(aPrefName, aDefaultValue) {
  var prefValue;
  try {
    var prefService = new PrefBranch();

    prefValue = prefService.getBoolPref(aPrefName);
  } catch (e) {
    prefValue = aDefaultValue;
  }
  return prefValue;
}
/**
  * Définit la valeur d'une préférence utilisateur.
  * @param {String} aPrefName Le chemin complet de la préférence
  *                           (ex: <code>"extensions.obm..."</code>)
  * @param {Boolean} aValue La valeur de la préférence à écrire
  */
UtilsOBM.setIntPref = function UO_setIntPref(aPrefName, aValue) {
  var prefService = new PrefBranch();
  prefService.setIntPref(aPrefName, aValue);
}

/**
  * Récupère la valeur de type "bool" d'une préférence utilisateur.
  * @param {String} aPrefName Le chemin complet de la préférence
  *                           (ex: <code>"extensions.obm..."</code>)
  * @param {String} aDefaultValue Une valeur par défaut, renvoyé si
  *                               la préférence demandée n'existe pas
  * @type Boolean
  */
UtilsOBM.getIntPref = function UO_getIntPref(aPrefName, aDefaultValue) {
  var prefValue;
  try {
    var prefService = new PrefBranch();

    prefValue = prefService.getIntPref(aPrefName);
  } catch (e) {
    prefValue = aDefaultValue;
  }
  return prefValue;
}
/**
  * Définit la valeur d'une préférence utilisateur.
  * @param {String} aPrefName Le chemin complet de la préférence
  *                           (ex: <code>"extensions.obm..."</code>)
  * @param {Boolean} aValue La valeur de la préférence à écrire
  */
UtilsOBM.setBoolPref = function UO_setBoolPref(aPrefName, aValue) {
  var prefService = new PrefBranch();
  prefService.setBoolPref(aPrefName, aValue);
}

/*
UtilsOBM.getComplexValue = function(aPrefName, aType) {
  var prefValue;
  try {
    var prefService = new PrefBranch();

    prefValue = prefService.getComplexValue(aPrefName, aType);
  } catch (e) {
    prefValue = { data: null };
  }
  return prefValue;
}

UtilsOBM.setComplexValue = function(aPrefName, aType, aValue) {
  var prefService = new PrefBranch();
  var data = { data: aValue };
  prefService.setComplexValue(aPrefName, aType, data);
}
*/

/**
  * Vérifie la définition d'une préférence utilisateur.
  * @param {String} aPrefName Le chemin complet de la préférence
  *                           (ex: <code>"extensions.obm..."</code>)
  * @type bool
  */
UtilsOBM.isPrefExist = function UO_isPrefExist(aName){
    var prefService = new PrefBranch();
    return prefService.prefHasUserValue(aName);
}
/**
  * Supprime la définition d'une préférence utilisateur.
  * @param {String} aPrefName Le chemin complet de la préférence
  *                           (ex: <code>"extensions.obm..."</code>)
  */
UtilsOBM.unsetPref = function UO_unsetPref(prefName){
    var prefService = new PrefBranch();
    if (prefService.prefHasUserValue(prefName)) {
    	prefService.clearUserPref(prefName);
   	}
}

/**
  * Supprime une branche de préférences utilisateur, y compris ses enfants.
  * @param {String} aBranch preference branch name (e.g. "extensions.obm.xyz")
  */
UtilsOBM.deletePreferenceBranch = function UO_deletePreferenceBranch(aBranch) {
  var prefService = new PrefBranch();
  prefService.deleteBranch(aBranch);
}

UtilsOBM.getItemType = function UO_getItemType(aItem) {
    var itemType;

    if ( aItem instanceof Components.interfaces.calIEvent ) {
      itemType = "calIEvent";
    } else if ( aItem instanceof Components.interfaces.calITodo ) {
      itemType = "calITodo";
    }

    return itemType;
}

UtilsOBM._getItemsChanges = function UO_getItemsChanges(aContainerType, aContainerName, aItemType) {
    var changeTypes = [ "added", "updated", "removed" ];

    var itemsChanges = new HashMap();

    for each ( var type in changeTypes ) {
      var pref = UtilsOBM.getCharPref("extensions.obm.changes."
                                    + aContainerType + "."
                                    + aContainerName + "."
                                    + aItemType + "." + type, "");
      var changes = (pref != "" ? pref.split(",") : new Array());
      itemsChanges.put(type, changes);
    }

    return itemsChanges;
}

UtilsOBM._setItemsChanges = function UO_setItemsChanges(aContainerType, aContainerName, aItemType, aChanges) {
    var changeTypes = aChanges.keySet();

    for each ( var changeType in changeTypes ) {
      UtilsOBM.setCharPref("extensions.obm.changes."
                         + aContainerType + "."
                         + aContainerName + "."
                         + aItemType + "."
                         + changeType,
                           aChanges.get(changeType).join(","));
    }
}

UtilsOBM.removeItemFromChangesList = function UO_addItemToChangesList(
                                         aContainerType,
                                         aContainerName,
                                         aItemType,
                                         aChangeType,
                                         aItemID) {
    var allChanges = UtilsOBM._getItemsChanges(aContainerType, aContainerName, aItemType);
    var typeChanges = allChanges.get(aChangeType);
    var typeChanges2 = [];
    for each ( var itemID in typeChanges ) {
      if ( itemID != aItemID ) {
        typeChanges2.push(itemID);
      }
    }
    allChanges.put(aChangeType, typeChanges2);
    UtilsOBM._setItemsChanges(aContainerType, aContainerName, aItemType, allChanges);
}

UtilsOBM.addItemToChangesList = function UO_addItemToChangesList(
                                         aContainerType,
                                         aContainerName,
                                         aItemType,
                                         aChangeType,
                                         aItemID) {

    var itemsChanges = UtilsOBM._getItemsChanges(aContainerType,
                                                 aContainerName,
                                                 aItemType);
    var itemsArray;

    // type == "updated" && already in "added" => don't add to "updated"
    if ( aChangeType == "updated"
      && itemsChanges.get("added").indexOf(aItemID) != -1 ) {
      return;
    }
    else if ( aChangeType == "removed" ) {

      function removeItem(element, index, array) {
        return (element != this.itemID);
      }

      // type == "removed" && already in "added" => delete from "added"
      //                                            don't add to "removed"
      if ( itemsChanges.get("added").indexOf(aItemID) != -1 ) {
        itemsArray = itemsChanges.get("added");
        var o = {}; o.itemID = aItemID;
        itemsArray = itemsArray.filter(removeItem, o);
        itemsChanges.put("added", itemsArray);
      }
      // type == "removed" && already in "updated" => delete from "updated"
      //                                              add to "removed"
      else if ( itemsChanges.get("updated").indexOf(aItemID) != -1 ) {
        itemsArray = itemsChanges.get("updated");
        var o = {}; o.itemID = aItemID;
        itemsArray = itemsArray.filter(removeItem, o);
        itemsChanges.put("updated", itemsArray);

        itemsArray = itemsChanges.get("removed");
        itemsArray.push(aItemID);
        itemsChanges.put("removed", itemsArray);
      } else {
        itemsArray = itemsChanges.get(aChangeType);
        itemsArray.push(aItemID);
        itemsChanges.put(aChangeType, itemsArray);
      }
    } else {
      // already a item with this change type
      itemsArray = itemsChanges.get(aChangeType);
      if ( itemsArray.indexOf(aItemID) != -1 ) {
        return;
      }
      itemsArray.push(aItemID);
      itemsChanges.put(aChangeType, itemsArray);
    }

    UtilsOBM._setItemsChanges(aContainerType,
                              aContainerName,
                              aItemType,
                              itemsChanges);

}

UtilsOBM.resetAllItemsChangesLists = function UO_resetAllItemsChangesLists(aContainerType, aContainerName, aItemType) {
    var changes = new HashMap();
    changes.put("added", []);
    changes.put("updated", []);
    changes.put("removed", []);

    UtilsOBM._setItemsChanges(aContainerType, aContainerName, aItemType, changes);
}

/*
XXX HACK
no synchronous interface to get an item
see bug #296832
or could an observer be used instead ??
*/
/**
  * Récupère l'item de l'agenda, de façon synchrone.
  * @param {calICalendar} aCalendar
  * @param {String} aItemID
  * @type calIItemBase
  */
UtilsOBM.getCalendarItem_Synchronous = function UO_getCalendarItem_Synchronous(aCalendar, aItemID) {
    var listener = UtilsOBM._createListener(
                     function (aCalendar, aStatus, aOperationType,
                               aId, aDetail) {
                       this.done = true;
                     },
                     function (aCalendar, aStatus, aItemType, 
                               aDetail, aCount, aItems) {
                       if ( aItems.length == 1 )
                         this.item = aItems[0];
                     } );
    aCalendar.getItem(aItemID, listener);
    UtilsOBM._waitForDefined(listener.done);
    return listener.item;
}

/**
  * Retourne le nom OBM de l'agenda.
  * @param {calICalendar} aCalendar
  * @type String
  */
UtilsOBM.getCalendarOBMName = function UO_getCalendarOBMName(aCalendar) {
  var calMgr = UtilsOBM.getCalendarManager();
  if (calMgr.getCalendarPref) {
    return calMgr.getCalendarPref(aCalendar, "X-OBM-NAME"); 
  } else {
    return calMgr.getCalendarPref_(aCalendar, "X-OBM-NAME"); 
  }
}
/**
  * Retourne la liste des agendas OBM souscris
  * @param {calICalendar} aCalendar
  * @type Array
  */
UtilsOBM.getSubscribedOBMCalendar = function UO_getSubscribedOBMCalendar(){
    var calendars = UtilsOBM.getCalendarManager().getCalendars({});
    var OBMcalendars = new Array();
    for each ( var calendar in calendars ) {
	    if ( calendar.type == "obm" ) {
		OBMcalendars.push(calendar);	       
	    }
	}
   return OBMcalendars;
}
/**
  * Retourne Vrai si Ligthning est installe et active.
  * @type boolean
  */
UtilsOBM.isLightningInstalledAndEnabled = function UO_isLightningInstalledAndEnabled(){
    return (!(!Components.interfaces.calICalendarManager));
}
/**
  * Repositionne la pref syncOnQuit après un reset. 
  */
UtilsOBM.setSyncOnQuitAfterReset = function UO_setSyncOnQuitAfterReset(){
    var prefService = new PrefBranch();
    var resetPrefName = "extensions.obm.syncOnQuit.beforeReset";
    var normalPrefName = "extensions.obm.syncOnQuit";
    if (UtilsOBM.isPrefExist(resetPrefName)){
	var beforeReset = UtilsOBM.getBoolPref(resetPrefName);
	UtilsOBM.setBoolPref(normalPrefName, beforeReset);
	UtilsOBM.unsetPref(resetPrefName);
    }
}
/**
  * Retourne le gestionnaire d'agendas de Calendar.
  * @type calICalendarManager
  */
UtilsOBM.getCalendarManager = function UO_getCalendarManager() {
  return Components.classes["@mozilla.org/calendar/manager;1"]
                   .getService(Components.interfaces.calICalendarManager);
}
UtilsOBM.createAttendee = function UO_createAttendee(aCalendar, aOwnerName){
    var calAttendee = Components.classes["@mozilla.org/calendar/attendee;1"]
                                  .createInstance(Components.interfaces.calIAttendee);
    
    calAttendee.id = "MAILTO:"+aOwnerName;
	var calMgr = UtilsOBM.getCalendarManager();
	if (calMgr.getCalendarPref) {
        calAttendee.commonName = calMgr.getCalendarPref(aCalendar, "X-OBM-NAME") + " (" + aOwnerName + ")";
	} else {
        calAttendee.commonName = calMgr.getCalendarPref_(aCalendar, "X-OBM-NAME") + " (" + aOwnerName + ")";
	}


    calAttendee.participationStatus = "";
    calAttendee.role = "";
    return calAttendee;
}

UtilsOBM.addOwnerToAttendees = function UO_addOwnerToAttendees(aCalendar, allAttendees){

    var ownerName;

	var calMgr = UtilsOBM.getCalendarManager();
	if (calMgr.getCalendarPref) {
		ownerName = calMgr.getCalendarPref(aCalendar, "X-OBM-EMAIL");
	} else {
		ownerName = calMgr.getCalendarPref_(aCalendar, "X-OBM-EMAIL");
	}


    var addOwner = true;
    if (allAttendees == null || allAttendees.length == 0){
	if (allAttendees == null){
	    allAttendees = [];
	}
	
    } else {
	for each (var attendee in allAttendees){
	    if (attendee.id == "MAILTO:"+ownerName){
		addOwner = false;
		break;
	    }
	    Components.reportError("attendee -> " + attendee.id + " -> " + attendee.commonName);
	}
    }
    if (addOwner){
	allAttendees.unshift(UtilsOBM.createAttendee(aCalendar, ownerName));
    }
    return allAttendees;
}
/**
  * Retourne la liste des agendas OBM souscrits.
  * @type Array[String]
  */
UtilsOBM.getSubscribedCalendarsList = function UO_getSubscribedCalendarsList() {
    var calendarManager = UtilsOBM.getCalendarManager();

    var calendars = calendarManager.getCalendars({});

    var array = new Array();
  
    for each ( var calendar in calendars ) {
      if ( calendar.type == "obm" ) {
        array.push(UtilsOBM.getCalendarOBMName(calendar));
      }
    }

    return array;
}

/**
  * Retourne le prochain identifiant d'agenda
  * du provider <i>storage</i>.
  * @type String
  */
UtilsOBM.getNextLocalCalendarID = function UO_getNextLocalCalendarID() {
    var calendarManager = UtilsOBM.getCalendarManager();
    var calendars = calendarManager.getCalendars({});
    
    var minID = 20;
    
    for each ( var calendar in calendars ) {
      if ( calendar.uri ) {
        var calendarID = calendar.uri.spec.match(/id=(\d+)/);
        if ( calendarID != null ) {
          if ( Number(calendarID[1]) >= minID ) {
            minID = Number(calendarID[1]) + 1;
          }
        }
      }
    }

    return minID;
}

/**
  * Créé un <i>listener</i> d'opération vide, c'est-à-dire dont
  * les <i>handlers</i> ne "font rien".
  * @type calIOperationListener
  */
UtilsOBM.createEmptyListener = function UO_createEmptyListener() {
  return UtilsOBM._createListener(function() {}, function() {});
}

/**
  * Créé un <i>listener</i> d'opération.
  * @private
  * @param {function} aOnOperationComplete
  * @param {function} aOnGetResult
  * @type calIOperationListener
  */
UtilsOBM._createListener = function UO_createListener(aOnOperationComplete, aOnGetResult) {
  return {
    onOperationComplete: aOnOperationComplete,
    onGetResult: aOnGetResult
  };
}

/**
  * Retourne après XX millisecondes.
  * @param {integer} aDuration Nombre de millisecondes à attendre.
  */
UtilsOBM.sleep = function UO_sleep(aDuration) {
  var thread = Components.classes["@mozilla.org/thread;1"]
                         .createInstance(Components.interfaces.nsIThread);
  thread.currentThread.sleep(aDuration);
}

/**
  * Boucle tant que le paramètre est "undefined".
  * @private
  * @param {Object} aProperty
  */
UtilsOBM._waitForDefined = function UO_waitForDefined(aProperty) {
  // YOB241006 var thread = Components.classes["@mozilla.org/thread;1"]
  // YOB241006                        .createInstance(Components.interfaces.nsIThread);
  while ( aProperty === undefined ) {
    UtilsOBM.sleep(5);
  }
}

/**
  * Transforme un DOMElement en un objet de type XML.
  * @param {DOMElement} aDOMElement
  * @type XML
  */
UtilsOBM.DOMElementToE4X = function UO_DOMElementToE4X(aDOMElement) {
    var serializer = new XMLSerializer();
    var xmlString = serializer.serializeToString(aDOMElement);
    xmlString = xmlString.replace(/xmlns="[a-zA-Z0-9-\:\/\.]*"/, ""); // remove namespace
    xmlString = xmlString.replace(/<\?.*\?>/, ""); // remove <?xml ... ?> 
    return new XML(xmlString);
}

/**
  * Ajoute un fils à un noeud DOM.
  * @param {DOMNode} aNode
  * @param {XML} aE4X
  */
UtilsOBM.appendE4XChildToNode = function UO_appendE4XChildToNode(aNode, aE4X) {
    var parser = Components.classes["@mozilla.org/xmlextras/domparser;1"]
                           .createInstance(Components.interfaces.nsIDOMParser);
    var resultDoc = parser.parseFromString(aE4X.toString(), "text/xml");
    aNode.appendChild(resultDoc.documentElement);
}

/**
  * Supprime tous les espaces de nom XML (sauf celui par defaut : xmlns="...").
  * @param {XML} aE4X
  */
UtilsOBM.removeE4XNamespaces = function UO_removeE4XNamespaces(aE4X) {
    for each ( var namespace in aE4X.inScopeNamespaces() ) {
      aE4X.removeNamespace(namespace);
    }
}

/**
  * Vide un noeud DOM, c'est à dire supprime tous ses fils.
  * @param {DOMNode} aNode
  */
UtilsOBM.emptyDOMNode = function UO_emptyDOMNode(aNode) {
    while (aNode.firstChild) {
      aNode.removeChild(aNode.firstChild);
    }
}

/**
  * Génère une couleur aléatoire, au format HTML #xxyyzz.
  * @type String
  */
UtilsOBM.getRandomColor = function UO_getRandomColor() {
    var result = "#";
    for ( var i = 0; i < 3; i++ ) {
      var hexValue = Math.round(Math.random() * 255).toString(16);
      result += (hexValue.length == 1 ? "0" : "") + hexValue;
    }
    return result;
}

/**
  * Affiche un message sur la console JavaScript,
  * avec une icône adaptée à la sévérité de l'erreur.
  * @param {String} aMessage Le message à afficher.
  * @param {String} aSeverity La sévérité de l'erreur à l'origine du message :
  *                 <ul>
  *                   <li>normal<li>
  *                   <li>warning</li>
  *                   <li>error</li>
  *                 </ul>
  */
UtilsOBM.logMessageToConsole = function UO_logMessageToConsole(aMessage, aSeverity) {
    Components.utils.reportError(aMessage); return;
    // see <http://kb.mozillazine.org/JavaScript_Console>
  
    var consoleService = Components.classes["@mozilla.org/consoleservice;1"]
                                 .getService(Components.interfaces.nsIConsoleService);
  
    var scriptErrorInterface = Components.interfaces.nsIScriptError;
    
    var category = "XUL javascript"; // XXX ??
    var message = "OBM connector extension:\n" + aMessage; 

    var flag;
    switch ( aSeverity ) {
      case "error":
        flag = scriptErrorInterface.errorFlag;
        break;
      case "warning":
        flag = scriptErrorInterface.warningFlag;
        break;
      case "normal":
      case undefined:
      case null:
      case "":
      default:
        consoleService.logStringMessage(message);
        return;
    }
                                 
    var scriptError = Components.classes["@mozilla.org/scripterror;1"]
                                .createInstance(Components.interfaces.nsIScriptError);
                                
    scriptError.init(message,
                     null /* aSourceName */,
                     null /* aSourceLine */,
                     null /* aLineNumber */, 
                     null /* aColumnNumber */,
                     flag, category);
                     
    consoleService.logMessage(scriptError);
}

/** @ignore */
/* TODO Work In Progress... */
UtilsOBM.newError = function UO_newError(aType, aLabel, aXMLMessage) {
  var sbs = Components.classes["@mozilla.org/intl/stringbundle;1"]
                      .getService(Components.interfaces.nsIStringBundleService);
  var props = sbs.createBundle("chrome://obm-extension/locale/errors_warnings.properties");
  var error = new Error(); // new OBMError() ??
  error.type = aType;
  error.title = props.GetStringFromName(aLabel + "_title");
  error.content = props.GetStringFromName(aLabel + "_content");
  error.message = error.title + ":\n" + error.content;
  error.xmlMessage = aXMLMessage;
  return error;
}

/** @ignore */
/* TODO Work In Progress... */
UtilsOBM.showErrorPrompt = function UO_showErrorPrompt(aError) {
    var paramBlock = Components.classes["@mozilla.org/embedcomp/dialogparam;1"]
                                 .createInstance(Components.interfaces.nsIDialogParamBlock);
    paramBlock.SetNumberStrings(4);
    paramBlock.SetString(0, aError.type);
    paramBlock.SetString(1, aError.title);
    paramBlock.SetString(2, aError.content);
    paramBlock.SetString(3, aError.xmlMessage);

    var wWatcher = Components.classes["@mozilla.org/embedcomp/window-watcher;1"]
                             .getService(Components.interfaces.nsIWindowWatcher);
    wWatcher.openWindow(null,
                        "chrome://obm-extension/content/xul/obmErrorPrompt.xul",
                        "_blank",
                        "chrome,dialog=yes",
                        paramBlock);

}


UtilsOBM.getLocalizedMessage = function UO_getLocalizedMessage(aString){
  var bundle = Components.classes["@mozilla.org/intl/stringbundle;1"]
     	.getService(Components.interfaces.nsIStringBundleService)
     	.createBundle("chrome://obm-extension/locale/errors_warnings.properties");
  return bundle.GetStringFromName(aString);
}

UtilsOBM.getLocalizedFileMessage = function UO_getLocalizedFileMessage(aFile,aString){
  var bundle = Components.classes["@mozilla.org/intl/stringbundle;1"]
     	.getService(Components.interfaces.nsIStringBundleService)
     	.createBundle("chrome://"+aFile);
  return bundle.GetStringFromName(aString);
}

UtilsOBM.showAlert = function UO_showAlert(aString) {	
  var aMessage = UtilsOBM.getLocalizedMessage(aString);
  var aTitle = UtilsOBM.getLocalizedMessage("obmconnector");
  var alertsService = Components.classes["@mozilla.org/alerts-service;1"]
                                .getService(Components.interfaces.nsIAlertsService);
  alertsService.showAlertNotification("chrome://obm-extension/skin/obm_logo.png", 
                                     aTitle , aMessage, false, "", null);
}

UtilsOBM._setPasswordForHost = function UO_setPasswordForHost(aHost, aUsername, aPassword) {
  var passwordManager = new PasswordManager();
  // remove the current password for this host if it already exists
  var username = {};
  var password = {};
  if ( UtilsOBM._findPasswordForHost(aHost, username, password) ) {
    passwordManager.removeUser(aHost, username.value);
  }

  passwordManager.addUser(aHost, aUsername, aPassword);
}

UtilsOBM._findPasswordForHost = function UO_findPasswordForHost(aHost, aUsername /* object */, aPassword /* object */) {
  var passwordManager = new PasswordManager();
  var passwords = passwordManager.enumerator;
  while ( passwords.hasMoreElements() ) {
    var password = passwords.getNext()
                            .QueryInterface(Components.interfaces.nsIPassword);
    if ( password.host == aHost ) {
      aUsername.value = password.user; 
      aPassword.value = password.password;
      return true;
    }
  }
  return false;
}
UtilsOBM.getOBMCredentials = function UO_getOBMCredentials(aUsername /* object */, aPassword /* object */) {
  const HOST = "obm-obm-obm"; // sort of key
  var retour = UtilsOBM._findPasswordForHost(HOST, aUsername, aPassword);
  if (UtilsOBM.getBoolPref("extensions.obm.rememberpassword", true) == false){
    retour &= UtilsOBM.getTemporaryPassword(aPassword);
  }
//  Components.utils.reportError("user : " + aUsername.value + " & pass : " + aPassword.value + " => " + retour);
  return retour;
}

UtilsOBM.setOBMCredentials = function UO_setOBMCredentials(aUsername, aPassword) {
  const HOST = "obm-obm-obm"; // sort of key
  return UtilsOBM._setPasswordForHost(HOST, aUsername, aPassword);
}

UtilsOBM.setTemporaryPassword = function UO_setTemporaryPassword(aPassword){
  var hiddenWindow = Components.classes["@mozilla.org/appshell/appShellService;1"]
         .getService(Components.interfaces.nsIAppShellService)
         .hiddenDOMWindow;
  var window = hiddenWindow; 
  if (window)
    window.Global.setPasswordSession(aPassword);
}

UtilsOBM.getTemporaryPassword = function UO_getTemporaryPassword(aPassword /*object*/){
  var hiddenWindow = Components.classes["@mozilla.org/appshell/appShellService;1"]
         .getService(Components.interfaces.nsIAppShellService)
         .hiddenDOMWindow;
//  Components.utils.reportError("hidden : "+ hiddenWindow + " global : " + hiddenWindow.Global);
  var window = hiddenWindow;
  if (window == null || window.Global.getPasswordSession() == null || window.Global.getPasswordSession() == ""){
    return false;
  } else {
    aPassword.value = window.Global.getPasswordSession();
    return true;
  }
}


UtilsOBM.checkConnectionInformation = function UO_checkLoginInformation() {
  var pref = UtilsOBM.getCharPref("extension.obm.server");
  return pref && UtilsOBM.getOBMCredentials({}, {});
}

// renvoie la date du jour, au format DateTime pour comparaison...
UtilsOBM.getNowDateTime = function UO_getNowDateTime(){
      return now();
      /*var nowDate = new Date();
      var retour = Components.classes["@mozilla.org/calendar/datetime;1"].createInstance(Components.interfaces.calIDateTime);
      
      retour.year = nowDate.getFullYear();
      retour.month = nowDate.getMonth();
      retour.day = nowDate.getDate();
      retour.hour = nowDate.getHours();
      retour.minute = nowDate.getMinutes();
      retour.second = nowDate.getSeconds();

      return retour;*/
}
/* Paul Sowden -- http://delete.me.uk/2005/03/iso8601.html -- AFL license */
/** @ignore */
Date.prototype.setISO8601 = function setISO8601(string) {
    var regexp = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})" +
        "(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\.([0-9]+))?)?" +
        "(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?";
    var d = string.match(new RegExp(regexp));

    var offset = 0;
    var date = new Date(d[1], 0, 1);

    if (d[3]) { date.setMonth(d[3] - 1); }
    if (d[5]) { date.setDate(d[5]); }
    if (d[7]) { date.setHours(d[7]); }
    if (d[8]) { date.setMinutes(d[8]); }
    if (d[10]) { date.setSeconds(d[10]); }
    if (d[12]) { date.setMilliseconds(Number("0." + d[12]) * 1000); }
    if (d[14]) {
        offset = (Number(d[16]) * 60) + Number(d[17]);
        offset *= ((d[15] == '-') ? 1 : -1);
    }

    offset -= date.getTimezoneOffset();
    time = (Number(date) + (offset * 60 * 1000));
    this.setTime(Number(time));
}

/** @ignore */
Date.prototype.toISO8601String = function toISO8601String(format, offset) {
    /* accepted values for the format [1-6]:
     1 Year:
       YYYY (eg 1997)
     2 Year and month:
       YYYY-MM (eg 1997-07)
     3 Complete date:
       YYYY-MM-DD (eg 1997-07-16)
     4 Complete date plus hours and minutes:
       YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
     5 Complete date plus hours, minutes and seconds:
       YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
     6 Complete date plus hours, minutes, seconds and a decimal
       fraction of a second
       YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
    */
    if (!format) { var format = 6; }
    if (!offset) {
        var offset = 'Z';
        var date = this;
    } else {
        var d = offset.match(/([-+])([0-9]{2}):([0-9]{2})/);
        var offsetnum = (Number(d[2]) * 60) + Number(d[3]);
        offsetnum *= ((d[1] == '-') ? -1 : 1);
        var date = new Date(Number(Number(this) + (offsetnum * 60000)));
    }

    var zeropad = function (num) { return ((num < 10) ? '0' : '') + num; }

    var str = "";
    str += date.getUTCFullYear();
    if (format > 1) { str += "-" + zeropad(date.getUTCMonth() + 1); }
    if (format > 2) { str += "-" + zeropad(date.getUTCDate()); }
    if (format > 3) {
        str += "T" + zeropad(date.getUTCHours()) +
               ":" + zeropad(date.getUTCMinutes());
    }
    if (format > 5) {
        var secs = Number(date.getUTCSeconds() + "." +
                   ((date.getUTCMilliseconds() < 100) ? '0' : '') +
                   zeropad(date.getUTCMilliseconds()));
        str += ":" + zeropad(secs);
    } else if (format > 4) { str += ":" + zeropad(date.getUTCSeconds()); }

    if (format > 3) { str += offset; }
    return str;
}


UtilsOBM.addToolbarButton = function UO_addToolbarButton(aWindowType, aToolbarDomId, aToolbarRdfID, aToolbarButtonNames, aButtonBeforeDomId) {
  var rdfService = Components.classes["@mozilla.org/rdf/rdf-service;1"]
                             .getService(Components.interfaces.nsIRDFService);
  var datasource = Components.classes["@mozilla.org/rdf/datasource;1?name=local-store"]
                             .getService(Components.interfaces.nsIRDFDataSource);
  
  var subject = rdfService.GetResource(aToolbarRdfID);
  
  var targets = datasource.ArcLabelsOut(subject);
  if ( !targets || !targets.hasMoreElements()) {
    UtilsOBM.logToFile("toolbar not in localstore : add it by dom"+"\n");
    var windowManager = Components.classes['@mozilla.org/appshell/window-mediator;1']
                                  .getService(Components.interfaces.nsIWindowMediator);
    var toolBarWindow = windowManager.getMostRecentWindow(aWindowType);
    
    if (!toolBarWindow) {
      //no window
      UtilsOBM.logToFile("no window"+"\n");
      return false;
    }    
    //toolbar not in localstore : add it by dom
    var toolbar = toolBarWindow.document.getElementById(aToolbarDomId);
    if ( toolbar ) {
      var buttonferore = null;
      try {
        buttonBefore = toolBarWindow.document.getElementById(aButtonBeforeDomId);
      } catch(e) {
        //not found : will insert at end of bar
      }
      toolbar.insertItem("separator", buttonBefore, null, false);
      for ( var i = 0; i < aToolbarButtonNames.length; i++ ) {
        toolbar.insertItem(aToolbarButtonNames[i], buttonBefore, null, false);
      }
      toolbar.setAttribute("currentset", toolbar.currentSet);
      toolBarWindow.document.persist(aToolbarDomId, "currentset");
      toolBarWindow.MailToolboxCustomizeDone(true);
      return true;
    } else {
      UtilsOBM.logToFile("no toolbar"+"\n");
      return false;
    }
    
  } else {
    UtilsOBM.logToFile("in localstore"+"\n");
    while ( targets.hasMoreElements() ) {
      var predicate = targets.getNext();
      if (predicate instanceof Components.interfaces.nsIRDFResource) {
        if ( predicate.Value == "currentset" ) {
          var target = datasource.GetTarget(subject, predicate, true);
          if ( target instanceof Components.interfaces.nsIRDFLiteral ) {
            // add "separator,calendar-obm-sync-button" before aButtonBeforeDomId
            var set = target.Value.split(",");
           // if OBM button hasn't been added yet
            if ( set.lastIndexOf(aToolbarButtonNames[0]) == -1 ) {
              var pos = set.lastIndexOf(aButtonBeforeDomId);
              if ( pos == -1 ) {
                pos = set.length;
              }
              for ( var i = aToolbarButtonNames.length - 1; i >= 0; i-- ) {
                set.splice(pos, 0, aToolbarButtonNames[i]);
              }

              // add a separator before
              if ( set[pos - 1] != "separator") {
                set.splice(pos, 0, "separator");
              }

              var oldValue = rdfService.GetLiteral(target.Value);
              var newValue = rdfService.GetLiteral(set.join(","));
              datasource.Change(subject, predicate, oldValue, newValue);
            }
          }
        }
      }
    }//end while
  }
  return true;
}

UtilsOBM.isValideDate = function UO_isValideDate(string){
    var regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}" +
        "T[0-9]{2}:[0-9]{2}:[0-9]{2}\.[0-9]+" +
        "Z|[-+][0-9]{2}:[0-9]{2}";
    var d = string.match(new RegExp(regexp));
    return (d != null);
}

UtilsOBM.detectVersion = function UO_detectVersion(){
    var info = Components.classes["@mozilla.org/xre/app-info;1"]
                     .getService(Components.interfaces.nsIXULAppInfo);
    // Get the name of the application running us
    var retour = {
	namle: info.name, // Returns "Firefox" for Firefox
	version: info.version, // Returns "2.0.0.1" for Firefox version 2.0.0.1
    };
    return retour;
}

// (Exception supprimees -> nouveaux evenements crees)
UtilsOBM.addExceptionItem = function UO_addExceptionItem(aContainer, aContainerName, aItemID){
    var aItem = UtilsOBM.getCalendarItem_Synchronous(aContainer, aItemID);

    // is Occurence Event
    var newAddedItemsIDs = [];
    try {
			if (aItem.recurrenceInfo != null){
				var aExceptionIDs = aItem.recurrenceInfo.getExceptionIds({});
				// has Exception
				if (aExceptionIDs.length != 0){

					for each (var aExceptionID in aExceptionIDs){
						var ocurrence = aItem.recurrenceInfo.getOccurrenceFor(aExceptionID, false);
						UtilsOBM.logToFile("exception : " + aExceptionID + "\n");
						UtilsOBM.logToFile("ocurrence title : " + ocurrence.title +"\n");
						UtilsOBM.logToFile("ocurrence recurrenceStartDate : " + ocurrence.recurrenceStartDate +"\n");
	
						var uuidgen = Components.classes["@mozilla.org/uuid-generator;1"] 
								.getService(Components.interfaces.nsIUUIDGenerator);
						var uuid = uuidgen.generateUUID().toString();
	
						var newItem = ocurrence.clone();
						newItem.recurrenceInfo = null;
						newItem.recurrenceId = null;
						newItem.parentItem = null;
						newItem.title = ocurrence.title;
						newItem.id = uuid.substring(1, uuid.length - 1);
	
						var aListenerAdd = {};
						aListenerAdd.onOperationComplete = function(cal, res, op, id, item) {
								//Components.reportError("adopt -> cal : " + cal + ", res : " + res + ",op : " + op + ", id : " + id + ", item : " + item);
								this.id = id;
								this.done = true;
						}
						aContainer.adoptItem(newItem, aListenerAdd);
						UtilsOBM._waitForDefined(aListenerAdd.done);
						newAddedItemsIDs.push(aListenerAdd.id);
					}
				}
			}
    } catch (e) {
    	UtilsOBM.logToFile(e);
    }
    return newAddedItemsIDs;
}

var current_window = {};
UtilsOBM.setWindow = function UO_setWindow(window){
  current_window = window;
}
UtilsOBM.getWindow = function UO_getWindow(){
  return current_window;
}

//Used in pref windows
UtilsOBM.validateIntegers = function validateIntegers(event) {
    if (isNaN(Number(event.target.value))) {
        var newValue = parseInt(event.target.value);
        event.target.value = isNaN(newValue) ? "" : newValue;
        event.preventDefault();
    }
}

UtilsOBM.validateNaturalNums = function validateNaturalNums(event) {
    UtilsOBM.validateIntegers(event);
    var num = event.target.value;
    if (num < 0) {
        event.target.value = -1 * num;
        event.preventDefault();
    }
}


/*
 * HashMap
 * see Java's HashMap API documentation
 */
 
/** @ignore */
function HashMap() {
  this.length = 0;
  this.data = {};
}

HashMap.prototype.put = function(aKey, aValue) {
  var previous = this.data[aKey];
  this.data[aKey] = aValue;
  if ( previous === undefined ) { this.length++; }
  return previous;
}


HashMap.prototype.get = function(aKey) {
  return typeof this.data[aKey] == "undefined" ? undefined : this.data[aKey];
}

HashMap.prototype.keySet = function() {
  var keySet = new Array();
  for (var key in this.data) {
    keySet.push(key);
  }
  return keySet;
}

HashMap.prototype.values = function() {
  var values = new Array();
  for (var key in this.data) {
    values.push(this.data[key]);
  }
  return values;
}

HashMap.prototype.size = function() {
  return this.length;
}

HashMap.prototype.remove = function(aKey) {
  if ( this.data[aKey] !== undefined ) {
    delete this.data[aKey];
    this.length--;
  }
}

HashMap.prototype.clear = function() {
  this.data = {};
  this.length = 0;
}

HashMap.prototype.isEmpty = function() {
  return this.length == 0;
}

HashMap.prototype.containsKey = function(aKey) {
  for (var key in this.data) {
    if (key == aKey)
      return true; 
  }
  return false;
}

HashMap.prototype.containsValue = function(aValue) {
  for (var key in this.data) {
    if (this.data[key] == aValue)
      return true; 
  }
  return false;
}

HashMap.prototype.toString = function() {
  var str = "";
  for (var key in this.data) {
    str += "'" + key + "': '" + this.data[key] + "'\n";
  }
  return str;
}
