<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File  : company_js.inc                                              //
//     - Desc  : Company javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($params["ext_widget"] != "") || ($params["ext_widget_text"] != "")) {
  $extra_js .= "

function check_company_get_id(valeur,text) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_company\");
    return false;
  } else {
    window.opener.document.$params[ext_widget].value=valeur;
    window.opener.document.$params[ext_widget_text].value=text;
    window.close();
    return true;
  }
}";
}


$extra_js .= "

function check_company_get_id_url(p_url, valeur) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_company\");
    return false;
  } else {
    new_url = p_url + valeur;
    window.opener.location.href=new_url;
    window.close();
    return true;
  }
}


function confirm_company_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


function check_company(form) {

  // MANDATORY : Check that the Company name is not empty
  if (trim(form.tf_name.value) == \"\") {
    alert (\"$l_fill_name\");
    return false;
  }
  return true;
} 


///////////////////////////////////////////////////////////////////////////////
// Check if the Naf code check or deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_company_naf_checkdel(form) {

  if (form.sel_naf.selectedIndex >= 0) {
    return true;
  } else {
    alert(\"$l_j_naf_delete\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the nafcode update is possible
///////////////////////////////////////////////////////////////////////////////
function check_company_naf_upd(form,formd)
{
  naf_pos = formd.sel_naf.selectedIndex;
  if ((naf_pos >= 0) && (form.tf_naf_code.value != \"\")) {
    form.sel_naf.value=formd.sel_naf.options[naf_pos].value;
    return true;
   } else {
     alert(\"$l_j_naf_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the naf code creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_company_naf_new(form)
{
   if (form.tf_naf_code.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_naf_create\");
      return false;
   }
}


function company_copy_clip(text)
{

  if (window.clipboardData) {

    window.clipboardData.setData(\"Text\", text);
  }
  else if (window.netscape) 
  { 
    try {
      netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
    } catch (err) {   
      alert('$l_j_copy_privilege_alert');	   
      return false;
    }
    
    var clip = Components.classes['@mozilla.org/widget/clipboard;1'].createInstance(Components.interfaces.nsIClipboard);
    if (!clip) return;

    var trans = Components.classes['@mozilla.org/widget/transferable;1'].createInstance(Components.interfaces.nsITransferable);
    if (!trans) return;

    trans.addDataFlavor('text/unicode');

    var str = new Object();
    var len = new Object();

    var str = Components.classes[\"@mozilla.org/supports-string;1\"].createInstance(Components.interfaces.nsISupportsString);

    var copytext=text;

    str.data=copytext;

    trans.setTransferData(\"text/unicode\",str,copytext.length*2);

    var clipid=Components.interfaces.nsIClipboard;

    if (!clip) return false;

    clip.setData(trans,null,clipid.kGlobalClipboard);

  }
  return false;
}

";

?>
