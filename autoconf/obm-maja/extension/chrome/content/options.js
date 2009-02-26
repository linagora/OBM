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

const Cc = Components.classes;
const Ci = Components.interfaces;

var scriptLoader = Cc["@mozilla.org/moz/jssubscript-loader;1"].createInstance(Ci.mozIJSSubScriptLoader);

scriptLoader.loadSubScript("chrome://obmmaja/content/utils.js");
scriptLoader.loadSubScript("chrome://obmmaja/content/UtilsOBM.js");

var obmmaja = {
  
  resetAutoconf: function() {
    utils._setPreference("config.obm.autoconfigStatus", 0, "user" );
  },
  resetExtension: function() {
    var extensionManager = Cc["@mozilla.org/extensions/manager;1"].getService(Ci.nsIExtensionManager);
    var inconnue = {};
    var TabItems = extensionManager.getItemList(2,inconnue);
    for( var i=0;i<TabItems.length;i++) {
      extensionManager.uninstallItem(TabItems[i].id);
    }
  },
  resetPrefs: function() {
    //unset all user prefs
    var prefService = Cc["@mozilla.org/preferences-service;1"].getService(Ci.nsIPrefBranch);
    var prefs = prefService.getChildList("",{});
    var no_resets = new Array(/mail\.server\.server1\.directory/);
    var do_clear;
  	for ( var i=0; i < prefs.length; i++) {
  	  if (prefService.prefHasUserValue(prefs[i])) {
        do_clear = true;
        for ( var j=0; j < no_resets.length; j++) {
          if (prefs[i].match(no_resets[j])) {
            do_clear = false;
          }
        }
        if (do_clear) {
          prefService.clearUserPref(prefs[i]);
        }
      }
   	}
  },
  visibleValid: function() {
    var checkAutoconf = document.getElementById("resetAutoconf").getAttribute("checked");
    var checkExtension = document.getElementById("resetExtension").getAttribute("checked");
    var checkPrefs = document.getElementById("resetPrefs").getAttribute("checked");
    if (checkAutoconf || checkExtension || checkPrefs) {
      var accept = document.documentElement.getButton("accept");
      accept.setAttribute("hidden", "false");
    } else {
      var accept = document.documentElement.getButton("accept");
      accept.setAttribute("hidden", "true");
    }
  },
  resetAction:  function()  {
    var checkAutoconf = document.getElementById("resetAutoconf").getAttribute("checked");
    var checkExtension = document.getElementById("resetExtension").getAttribute("checked");
    var checkPrefs = document.getElementById("resetPrefs").getAttribute("checked");
    if (checkAutoconf) {
      this.resetAutoconf();
    }
    if (checkExtension) {
      this.resetExtension();
    }
    if (checkPrefs) {
      this.resetPrefs();
    }
    var promptService = Cc["@mozilla.org/embedcomp/prompt-service;1"].getService(Ci.nsIPromptService);
                                    
    // restart now/later ?
    var check = {value: false};
    var flags = promptService.BUTTON_TITLE_IS_STRING * promptService.BUTTON_POS_0 +
                promptService.BUTTON_TITLE_IS_STRING * promptService.BUTTON_POS_1;

    var title = UtilsOBM.getLocalizedFileMessage("obmmaja/locale/obmmaja.properties","obmreinittitle");
    var message = UtilsOBM.getLocalizedFileMessage("obmmaja/locale/obmmaja.properties","obmreinitmessage");
    var rightnow = UtilsOBM.getLocalizedFileMessage("obmmaja/locale/obmmaja.properties","obmreinitrightnow");
    var later = UtilsOBM.getLocalizedFileMessage("obmmaja/locale/obmmaja.properties","obmreinitlater");
    var button = promptService.confirmEx(window, title,
     message, flags, rightnow , later, "", null, check);

    if (button == 0) {
      // restart now
      var nsIAppStartup = Ci.nsIAppStartup;
      Cc["@mozilla.org/toolkit/app-startup;1"].getService(nsIAppStartup)
            .quit(nsIAppStartup.eForceQuit | nsIAppStartup.eRestart);   
   }
  } 
};
