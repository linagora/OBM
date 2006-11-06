<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : company_js.inc                                              //
//     - Desc  : Company javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($ext_widget != "") || ($ext_widget_text != "")) {
  $extra_js .= "

function check_company_get_id(valeur,text) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_company\");
    return false;
  } else {
    window.opener.document.$ext_widget.value=valeur;
    window.opener.document.$ext_widget_text.value=text;
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

  // Check the zip code
  lzip = form.tf_zip.value;
  if ((lzip != \"\") && (check_zip(lzip) == null)) {
    alert (\"$l_j_check_zip\");
    return false;
  }

  // Check the phone number
  lphone = form.tf_phone.value;
  if ((lphone != \"\") && (check_phone(lphone) == null)) {
    alert (\"$l_j_check_phone\");
    return false;
  }

  // Check the fax number
  lfax = form.tf_fax.value;
  if ((lfax != \"\") && (check_fax(lfax) == null)) {
    alert (\"$l_j_check_fax\");
    return false;
  }

  // Check the Email
  lemail = form.tf_email.value;
  if ((lemail != \"\") && (check_email(lemail) == null)) {
    alert (\"$l_j_check_email\");
//    return false;
  }

  // Check the web
  lweb = form.tf_web.value;
  if ((lweb != \"\") && (check_web(lweb) == null)) {
    alert (\"$l_j_check_web\");
    return false;
  }
  else
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
