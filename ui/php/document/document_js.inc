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
// OBM - File : document_js.inc                                              //
//     - Desc : Document javascript functions File                           //
// 2003-08-22 Aliacom - Rande Mehdi                                          //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

$extra_js .= "
function check_document(form) {
  // MANDATORY : Check that the Document title is not empty
  if (trim(form.tf_title.value) == \"\") {
    alert (\"$l_fill_title\");
    return false;
  }

  // Check the author
  lzip = form.tf_author.value;
  if (trim(form.tf_author.value) == \"\") {
    alert (\"$l_fill_author\");
    return false;
  }

  return true;
}

function check_dir(form) {
  // MANDATORY : Check that the Document title is not empty
  if (trim(form.tf_name.value) == \"\") {
    alert (\"$l_fill_dir_name\");
    return false;
  }

  return true;
}

function check_cat1_new(form)
{
   if (form.tf_cat1.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_cat1_create\");
      return false;
   }
}

function check_cat2_new(form)
{
   if (form.tf_cat2.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_cat2_create\");
      return false;
   }
}

function check_mime_new(form)
{
   if (form.tf_mime.value != \"\"
       && form.tf_extension.value != \"\"
       && form.tf_mimetype.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_mime_create\");
      return false;
   }
}

function check_cat1_upd(form,formd)
{
  cat1_pos = formd.sel_cat1.selectedIndex;
  if ((cat1_pos >= 0) && (form.tf_cat1.value != \"\")) {
    form.sel_cat1.value=formd.sel_cat1.options[cat1_pos].value;
    return true;
   } else {
     alert(\"$l_j_cat1_update\");
     return false;
   }
}


function check_cat2_upd(form,formd)
{
  cat2_pos = formd.sel_cat2.selectedIndex;
  if ((cat2_pos >= 0) && (form.tf_cat2.value != \"\")) {
    form.sel_cat2.value=formd.sel_cat2.options[cat2_pos].value;
    return true;
   } else {
     alert(\"$l_j_cat2_update\");
     return false;
   }
}

function check_mime_upd(form,formd)
{
  mime_pos = formd.sel_mime.selectedIndex;
  if ((mime_pos >= 0) && (form.tf_mime.value != \"\")
       && (form.tf_extension.value != \"\") && (form.tf_mimetype.value != \"\")) {
    form.sel_mime.value=formd.sel_mime.options[mime_pos].value;
    return true;
   } else {
     alert(\"$l_j_mime_update\");
     return false;
   }
}

function check_cat1_checkdel(form)
{
   if (form.sel_cat1.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_cat1_delete\");
      return false;
   }
}

function check_cat2_checkdel(form)
{
   if (form.sel_cat2.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_cat2_delete\");
      return false;
   }
}


function check_mime_checkdel(form)
{
   if (form.sel_mime.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_mime_delete\");
      return false;
   }
}

NS4 = (document.layers) ? 1 : 0;
IE4 = (document.all) ? 1 : 0;
W3C = (document.getElementById) ? 1 : 0;	


function show_hide_field()
{ 
  radios = document.f_entity.rd_kind;
  if(radios[0].checked == true) {
    basic_show('browse_img');
    basic_show('browse_field');
    basic_hide('url_field');";
    if (!isset($document["path"])) {
      $extra_js .= "
      document.f_entity.tf_path.value = '$default_path';";
    } else {
      $extra_js .= "
      document.f_entity.tf_path.value = '".dirname($document["path"])."';";
    }
    $extra_js .= "
  }
  else if(radios[1].checked == true) {
    basic_show('browse_img');
    basic_hide('browse_field');
    basic_show('url_field');";
    if (!isset($document["path"])) {
      $extra_js .= "
      document.f_entity.tf_path.value = '$default_path';";
    } else {
      $extra_js .= "
      document.f_entity.tf_path.value = '".dirname($document["path"])."';";
    }
    $extra_js .= "
    document.f_entity.preurl.value = \"http://\";
  }
  else if(radios[2].checked == true) {
    basic_show('browse_img');
    basic_hide('browse_field');
    basic_show('url_field');";
    if (!isset($document["path"])) {
      $extra_js .= "
      document.f_entity.tf_path.value = '$default_path';";
    } else {
      $extra_js .= "
      document.f_entity.tf_path.value = '".dirname($document["path"])."';";
    }
    $extra_js .= "
    document.f_entity.preurl.value =\"https://\";
  }  
}


function basic_hide(name) {
  if (W3C) {
    document.getElementById(name).style.display = \"none\";
   
  } else if (NS4) {
    document.layers[name].display = \"none\";
  } else {
    document.getElementById(name).style.display = \"none\";
  }
}


function basic_show(name)
{ 
  if (W3C) {
    document.getElementById(name).style.display = \"\";
  } else if (NS4) {
    document.layers[name].display = \"\";
  } else {
    document.all[name].style.display = \"\";

  }
}
";

if (isset($params["ext_target"])) {
  $extra_js .= "
   function fill_ext_form(dir_path) {
     ext_field = window.opener.document.$params[ext_target];
     ext_field.value = dir_path;
     window.close();
  }
  ";
}

if (isset($params["ext_widget"]) && isset($params["ext_widget_text"])){
  $extra_js .= "
    function fill_ext_get_id_from_path(id, name){
      var ext_field_id = window.opener.document.$params[ext_widget];
      var ext_field_text = window.opener.document.$params[ext_widget_text];
      ext_field_id.value = id;
      ext_field_text.value = name;
      window.close();
      return;
    }
  ";
}

?>
