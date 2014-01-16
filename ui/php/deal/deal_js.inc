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
// OBM - File  : deal_js.inc                                                 //
//     - Desc  : Deal javascript functions File                              //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($params["ext_widget"] != "") || ($params["ext_widget_text"] != "")) {
  $extra_js .= "

function check_get_id(valeur,text) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_select_deal\");
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

function check_get_id_url(p_url, valeur) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_select_deal\");
    return false;
  } else {
    new_url = p_url + valeur;
    window.opener.location.href=new_url;
    window.close();
    return true;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Check if the deal form values are correct                                 //
///////////////////////////////////////////////////////////////////////////////
function check_form(f_aff) {
  if (trim(f_aff.tf_label.value) == \"\") {
    alert (\"$l_fill_label\");
    return false;
  }

  if (f_aff.tf_datebegin.value == \"\") {
    alert (\"$l_fill_datebegin\");
    return false;
  }

  if (trim(f_aff.tf_datealarm.value) == \"\") {
    alert (\"$l_fill_datealarm\");
    return false;
  } else {
    // Check if date alarm is in the past
    today = new Date();
    dalarm = f_aff.tf_datealarm.value;
    year = dalarm.substring(0,4);
    month = dalarm.substring(5,7) - 1;
    day = dalarm.substring(8,10);
    date_alarm = new Date(year, month, day);
    today.setHours(0);
    date_alarm.setHours(12);
    // We set the hours not to alert if alarm date is today
    if (date_alarm < today) {
      if (confirm(\"$l_confirm_datealarm\")) {
        return true;
      } else {
        return false;
      }
    }
    return true;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check the Quick update deal form
///////////////////////////////////////////////////////////////////////////////
function check_quick_form(fdeal) {

  if (trim(fdeal.tf_datealarm.value) == \"\") {
    alert (\"$l_fill_datealarm\");
    return false;
  } else {
    // Check if date alarm is in the past
    today = new Date();
    dalarm = fdeal.tf_datealarm.value;
    year = dalarm.substring(0,4);
    month = dalarm.substring(5,7) - 1;
    day = dalarm.substring(8,10);
    date_alarm = new Date(year, month, day);
    today.setHours(0);
    date_alarm.setHours(12);
    // We set the hours not to alert if alarm date is today
    if (date_alarm < today) {
      if (confirm(\"$l_confirm_datealarm\")) {
        return true;
      } else {
        return false;
      }
    }
    return true;
  }

  return true;
}


function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the obligatory fields are set while creating a new ParentDeal    //
///////////////////////////////////////////////////////////////////////////////
function check_parentdeal(f_parent) {
  if (trim(f_parent.tf_plabel.value) == \"\") {
    alert (\"$l_fill_label\");
    return false;
  }
  else
    return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Type deletion is possible                               //
///////////////////////////////////////////////////////////////////////////////
function check_type_checkdel(form) {
   if (form.sel_type_id.selectedIndex >= 0)
   {
     return true;
   } else {
     alert(\"$l_j_type_delete\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Status deletion is possible                            //
///////////////////////////////////////////////////////////////////////////////
function check_status_checkdel(form) {  
   if (form.sel_status_id.selectedIndex >= 0)
   { 
     return true;
   }
   else
   {
     alert(\"$l_j_status_delete\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the type update is possible                                      //
///////////////////////////////////////////////////////////////////////////////
function check_type_upd(form,formd) {
  type_pos = formd.sel_type_id.selectedIndex;
  if ((type_pos >= 0) && (form.tf_label.value != \"\")) {
    form.sel_type_id.value=formd.sel_type_id.options[type_pos].value;
    return true;
   } else {
     alert(\"$l_j_type_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the status update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_status_upd(form,formd) {
  status_pos = formd.sel_status_id.selectedIndex;
  if ((status_pos >= 0) && (form.tf_label.value != \"\")) {
    form.sel_status_id.value=formd.sel_status_id.options[status_pos].value;
    return true;
  } else {
    alert(\"$l_j_status_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Type creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_type_new(form) {
   if (form.tf_label.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_type_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
function fill_ext_form2(int_form) {
   size = int_form.length;
   ext_field = get_elem(window.opener.document, '$params[ext_target]' );
   for(i=0; i <size; i++) {
     if(int_form.elements[i].type == 'checkbox'){
       if(int_form.elements[i].checked == true) {
	 ext_size = ext_field.length;
	 bool = false;
	 for(j=0; j< ext_size  && bool == false; j++) {
	   if(ext_field.options[j].value == int_form.elements[i].name) {
	     ext_field.options[j].selected =true;
	     bool = true;
	   }
	 }
       }
     }
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the status creation is possible                                  //
///////////////////////////////////////////////////////////////////////////////
function check_status_new(form)
{
   if (form.tf_label.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_status_create\");
      return false;
   }
}
///////////////////////////////////////////////////////////////////////////////
// Add a new deal dc form line                                               //
///////////////////////////////////////////////////////////////////////////////
function add_DC_line() {
  dcContainer = document.getElementById('dc_container');
  dcChildren = dcContainer.childNodes;
  cpt = 0;
  for(i=0; i < dcChildren.length; i++) {
    if(dcChildren[i].nodeName == 'DIV') {
      sampleDC = dcChildren[i].childNodes;
      cpt ++;
    }
  }
  for(i=0;i < sampleDC.length; i++) {
    if(sampleDC[i].nodeName == 'SELECT') {
      option = sampleDC[i].options;
    }
  }
  div = document.createElement('div');
  dcContainer.appendChild(div);
  cat_selector = document.createElement('select');
  cat_selector.name = 'role'+cpt;
  for(i=0;i < option.length; i++) {
    cat_selector.options[i] = new Option(option[i].text,option[i].value);
  }
  div.appendChild(cat_selector);
  ipt = document.createElement('input');
  ipt.type = 'hidden';
  ipt.name='data_dc_'+cpt;
  ipt.value=cpt;
  div.appendChild(ipt);
  ipt = document.createElement('input');
  ipt.type = 'hidden';
  ipt.name='data_dccid_'+cpt;
  div.appendChild(ipt);
  ipt = document.createElement('input');
  ipt.type = 'hidden';
  ipt.name='data_dccname_'+cpt;
  div.appendChild(ipt);
  ipt = document.createElement('input');
  ipt.type = 'hidden';
  ipt.name='data_dccnewid_'+cpt;
  div.appendChild(ipt);
  eval('document.f_entity.data_dccnewid_'+cpt+' = ipt');
  span = document.createElement('span');
  onclick= \"popup('$path/company/company_index.php?action=ext_get_id&popup=1&ext_widget=f_entity.data_dccnewid_\"+cpt+\"&ext_widget_text=f_entity.data_dccnewname_\"+cpt+\"','Company'); return false;\";
  span.innerHTML = '<a href=\"\" onclick=\"'+onclick+'\"> <img src=\"$ico_company\" alt=\"\" /></a>';
  div.appendChild(span);
  ipt = document.createElement('input');
  ipt.type = 'text';
  ipt.name='data_dccnewname_'+cpt;
  ipt.readonly = true;
  ipt.setAttribute('onfocus','this.blur();');
  eval('document.f_entity.data_dccnewname_'+cpt+' = ipt');
  div.appendChild(ipt);
}
";

?>
