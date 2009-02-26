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

var utils = {

  _setPreference: function(aName, aTypedValue, aSet) {
    var prefService = Components.classes["@mozilla.org/preferences-service;1"]
                                .getService(Components.interfaces.nsIPrefService);    
    var prefBranch_User = prefService.getBranch(null);
    var prefBranch_Default = prefService.getDefaultBranch(null);

    var prefBranch = prefBranch_User;

    var prefMethod;

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
  },

  _getPreference: function(aName, aDefaultValue) {
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
    return value;
  },

  _deletePrefBranch: function(aName) {
    var prefBranch = Components.classes["@mozilla.org/preferences-service;1"]
                               .getService(Components.interfaces.nsIPrefBranch);
    prefBranch.deleteBranch(aName);
  },

  _prefBranchExists: function(aBranchName) {
    var prefBranch = Components.classes["@mozilla.org/preferences-service;1"]
                               .getService(Components.interfaces.nsIPrefBranch);
    var childList = prefBranch.getChildList(aBranchName, {});
    return childList.length > 0;
  },
  
  _getFileURLFromProfile: function(aFilename) {
    const ioService = Components.classes["@mozilla.org/network/io-service;1"]
                                .getService(Components.interfaces.nsIIOService);

    var file = Components.classes["@mozilla.org/file/directory_service;1"]
                         .getService(Components.interfaces.nsIProperties)
                         .get("ProfD", Components.interfaces.nsIFile);
    var fileHandler = ioService.getProtocolHandler("file")
                               .QueryInterface(Components.interfaces.nsIFileProtocolHandler);

    file.append(aFilename);
    return fileHandler.getURLSpecFromFile(file);
  },
  
  _getExtensionVersion: function(aExtensionID) {
    const rdfService = Components.classes["@mozilla.org/rdf/rdf-service;1"]
                                 .getService(Components.interfaces.nsIRDFService);

    var extensionsDS = rdfService.GetDataSourceBlocking(this._getFileURLFromProfile("extensions.rdf"));

    var itemResource = rdfService.GetResource("urn:mozilla:item:" + aExtensionID);
    var versionResource = rdfService.GetResource("http://www.mozilla.org/2004/em-rdf#version");

    var target = extensionsDS.GetTarget(itemResource, versionResource, true);
    var version = target.QueryInterface(Components.interfaces.nsIRDFLiteral).Value;
    
    return version;
  },

  _isExtensionUpdated: function(aOldVersion, aNewVersion) {
    var isUpdated = false;
    
    const comparator = Components.classes["@mozilla.org/xpcom/version-comparator;1"]
                                 .getService(Components.interfaces.nsIVersionComparator);
    
    isUpdated = comparator.compare(aNewVersion, aOldVersion) > 0;                          
    
    return isUpdated;
  },

};

