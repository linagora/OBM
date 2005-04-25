<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : company_js.inc                                              //
//     - Desc  : Company javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script type=\"text/javascript\">
<!--
";

require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($ext_widget != "") || ($ext_widget_text != "")) {
  $extra_js .= "

function check_get_id(valeur,text) {
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

function check_get_id_url(p_url, valeur) {
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


function confirm_del() {
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
// Check if the kind check or deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////
function check_kind_checkdel(form)
{
   if (form.sel_kind.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_kind_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the kind update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_kind_upd(form,formd)
{
  kind_pos = formd.sel_kind.selectedIndex;
  if ((kind_pos >= 0) && (form.tf_kind.value != \"\")) {
    form.sel_kind.value=formd.sel_kind.options[kind_pos].value;
    return true;
  } else {
    alert(\"$l_j_kind_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Kind creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_kind_new(form)
{
   if (form.tf_kind.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_kind_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the kind check or deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////
function check_cat_checkdel(form)
{
   if (form.sel_cat.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_cat_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Category creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_cat_new(form)
{
   var reg=new RegExp(\"[ .;]+\", \"g\");
   var tableau=form.tf_cat_code.value.split(reg);
   if(tableau.length > 3) {
      alert(\"$l_j_cat_deep\");
      return false;
   }
   if (form.tf_cat.value != \"\" && form.tf_cat_code.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_cat_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the cat update is possible
///////////////////////////////////////////////////////////////////////////////
function check_cat_upd(form,formd)
{
  cat_pos = formd.sel_cat.selectedIndex;
  if ((cat_pos >= 0) && (form.tf_cat.value != \"\" && form.tf_cat.value != \"tf_cat_code\")) {
    form.sel_cat.value=formd.sel_cat.options[cat_pos].value;
    return true;
  } else {
    alert(\"$l_j_cat_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the activity check or deletion is possible                       //
///////////////////////////////////////////////////////////////////////////////
function check_act_checkdel(form)
{
   if (form.sel_act.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_act_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the activity update is possible                                  //
///////////////////////////////////////////////////////////////////////////////
function check_act_upd(form,formd)
{
  act_pos = formd.sel_act.selectedIndex;
  if ((act_pos >= 0) && (form.tf_act.value != \"\")) {
    form.sel_act.value=formd.sel_act.options[act_pos].value;
    return true;
   } else {
     alert(\"$l_j_act_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the activity creation is possible                                //
///////////////////////////////////////////////////////////////////////////////
function check_act_new(form)
{
   if (form.tf_act.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_act_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Naf code check or deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_naf_checkdel(form)
{
   if (form.sel_naf.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_naf_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the nafcode update is possible
///////////////////////////////////////////////////////////////////////////////
function check_naf_upd(form,formd)
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
function check_naf_new(form)
{
   if (form.tf_naf_code.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_naf_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Category Functions
///////////////////////////////////////////////////////////////////////////////
NS4 = (document.layers) ? 1 : 0;
IE4 = (document.all) ? 1 : 0;
W3C = (document.getElementById) ? 1 : 0;	

function show_hide_repo(name)
{
  if (W3C) {
    if(document.getElementById('div_'+name).style.display != \"none\") {
      hide(name);
     }
     else{
        show(name);
     }
  } else if (NS4) {
    if(document.layers['div_'+name].display != \"none\") {
      hide(name);
    }
    else{
      show(name);
     }

  } else {
    if(document.all['div_'+name].style.display != \"none\") {
       hide(name);
    }
    else{
      show(name);
    }
  
  }

}

function hide ( name ) {
  if (W3C) {
    document.getElementById('div_'+name).style.display = \"none\";
    document.getElementById('div_'+name).style.height = \"0px\";
    document.getElementById('img_'+name).src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_close\";    
  } else if (NS4) {
    document.layers['div_'+name].display = \"hide\";
    document.layers['div_'+name].height = \"0px\";
    document.layers['img_'+name].src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_close\";    
  } else {
    document.all['div_'+name].style.display = \"none\";
    document.all['div_'+name].style.height = \"0px\";
    document.all['img_'+name].src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_close\";
  }
}

function show ( name ) {
  if (W3C) {
    document.getElementById('div_'+name).style.display = \"\";
    document.getElementById('div_'+name).style.height = \"auto\"; 
    document.getElementById('img_'+name).src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_open\";
  } else if (NS4) {
    document.layers['div_'+name].display = \"\";
    document.layers['div_'+name].height = \"auto\";
    document.layers['img_'+name].src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_open\";
  } else {
    document.all['div_'+name].style.display = \"\";
    document.all['div_'+name].style.height = \"auto\";
    document.all['img_'+name].src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_open\";
  }
}

function get_elem(doc, name ) {
  if (W3C) {
    return doc.getElementById(name);
  } else if (NS4) {
    return doc.layers[name]  
  } else {
    return doc.all[name];
  }
}
";

if (isset($ext_target) && $ext_target!="") {
  $extra_js .= "
   function fill_ext_form(repo_path) {
     ext_field = window.opener.document.$ext_target;
     ext_field.value = repo_path;
     window.close();
  } 
  
  function fill_ext_form2(int_form) {
   size = int_form.length;
   ext_field = get_elem(window.opener.document, '$ext_target' );
   for(i=0; i <size; i++) {
     if(int_form.elements[i].type == 'checkbox'){
       if(int_form.elements[i].checked == true) {
	 ext_size = ext_field.length;
	 bool= false;
	 for(j=0; j< ext_size  && bool == false; j++) {
	   if(ext_field.options[j].value == int_form.elements[i].name) {
	     ext_field.options[j].selected =true;
	     bool=true;
	   }
	 }
	 if(bool==false) {
	    cat_name = get_elem(document,'data'+int_form.elements[i].name);
	    cat_name_text = cat_name.innerHTML;
	    //IE Hack!
	    ext_field.length = ext_size + 1;
	    ext_field.options[ext_size].value = int_form.elements[i].name;
	    ext_field.options[ext_size].text = cat_name_text
	    ext_field.options[ext_size].selected =true;
	 }
       }
     }
   }
 }
 function fill_ext_code_form(value) {
   ext_field = window.opener.document.$ext_target;
   ext_size = ext_field.length;
   ext_field = window.opener.document.$ext_target;
   bool= false;
   for(j=0; j< ext_size  && bool == false; j++) {
     if(ext_field.options[j].value == value) {
       ext_field.options[j].selected =true;
       bool=true;
     }
   }
   window.close();
 } 

";
}
$extra_js .= "
function copy_clip(text)
{

  if (window.clipboardData) 
  {

    window.clipboardData.setData(\"Text\", text);
  }
  else if (window.netscape) 
  { 
    try {
      netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
    } catch (err) {   
      alert('$l_j_copy_privilege_alert');;	   
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

 -->
</script>
";

