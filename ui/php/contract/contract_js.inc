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
// OBM - File : company_js.inc                                               //
//     - Desc : Company javascript functions File                            //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($params["ext_widget"] != "") || ($params["ext_widget_text"] != "")) {
  $extra_js .= "

function check_contract_get_id(valeur,text) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_contract\");
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

function check_contract_get_id_url(p_url, valeur) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_contract\");
    return false;
  } else {
    new_url = p_url + valeur;
    window.opener.location.href=new_url;
    window.close();
    return true;
  }
}

function check_contract(f_con) {
  if (trim(f_con.tf_label.value) == \"\") {
    alert (\"$l_fill_label\");
    return false;
  } else if (f_con.tf_datebegin.value == \"\") {
    alert(\"$l_begin\" + \" : $l_fill_date\");
    return false;
  } else if (f_con.tf_dateexp.value == \"\") {
    alert(\"$l_exp\" + \" : $l_fill_date\");
    return false;
  } else
    return true;
}


function confirm_contract_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the priority check or deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_priority_checkdel(form)
{
   if (form.sel_priority.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_pri_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the priority update is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_priority_upd(form,formd)
{
  pri_pos = formd.sel_priority.selectedIndex;
  if ((pri_pos >= 0) && (form.tf_label.value != \"\")) {
    form.sel_priority.value=formd.sel_priority.options[pri_pos].value;
    return true;
  } else {
    alert(\"$l_j_pri_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the priority creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_priority_new(form)
{
   if (form.tf_priority_label.value != \"\"
       && form.tf_priority_code.value != \"\"
       && form.tf_priority_color.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_pri_create\");
      return false;
   }
}



///////////////////////////////////////////////////////////////////////////////
// Check if the Contract Status deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_status_checkdel(form)
{  
   if (form.sel_status.selectedIndex >= 0)
   { 
     return true;
   }
   else
   {
     alert(\"$l_j_sta_delete\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the status update is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_status_upd(form,formd)
{
  status_pos = formd.sel_status.selectedIndex;
  if ((status_pos >= 0) && (form.tf_label.value != \"\")) {
    form.sel_status.value=formd.sel_status.options[status_pos].value;
    return true;
  } else {
    alert(\"$l_j_sta_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the status creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_status_new(form)
{
   if (form.tf_status_label.value != \"\"
       && form.tf_status_code.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_sta_create\");
      return false;
   }
}



///////////////////////////////////////////////////////////////////////////////
// Check if the Contract Type deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_type_del(form)
{
  if (form.sel_type.selectedIndex >= 0)
    return true;
  else
  {
    alert(\"$l_j_type_delete\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the type update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_contract_type_upd(form,formd)
{
  if (formd.sel_type.selectedIndex < 0){
    alert(\"$l_j_type_update\");
    return false;
  }
  form.sel_type.value=formd.sel_type.options[formd.sel_type.selectedIndex].value;
  if ((formd.sel_type.selectedIndex >= 0) && (form.tf_label.value != \"\"))
    return true;
  else {
    alert(\"$l_j_type_update\");
    return false;
  }
}





///////////////////////////////////////////////////////////////////////////////
// Check the type of the contract                                            //
///////////////////////////////////////////////////////////////////////////////
function show_kind_contract(form) {

  return (form.rd_format[1].checked);
}



///////////////////////////////////////////////////////////////////////////////
// Check if the type creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_contract_type_new(form)
{
  if (form.tf_type_label.value != \"\")
    return true;
  else
  {
    alert(\"$l_j_type_create\");
    return false;
  }
}

";

?>
