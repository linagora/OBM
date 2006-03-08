<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : deal_js.inc                                                 //
//     - Desc  : Deal javascript functions File                              //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");
require("$obminclude/javascript/category_js.inc");


// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($ext_widget != "") || ($ext_widget_text != "")) {
  $extra_js .= "

function check_get_id(valeur,text) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_select_deal\");
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
// Check if the Deal Kind deletion is possible                               //
///////////////////////////////////////////////////////////////////////////////
function check_kind_checkdel(form) {
   if (form.sel_kind.selectedIndex >= 0)
   {
     return true;
   } else {
     alert(\"$l_j_kind_delete\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Status deletion is possible                            //
///////////////////////////////////////////////////////////////////////////////
function check_status_checkdel(form) {  
   if (form.sel_status.selectedIndex >= 0)
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
// Check if the kind update is possible                                      //
///////////////////////////////////////////////////////////////////////////////
function check_kind_upd(form,formd) {
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
// Check if the status update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_status_upd(form,formd) {
  status_pos = formd.sel_status.selectedIndex;
  if ((status_pos >= 0) && (form.tf_status.value != \"\")) {
    form.sel_status.value=formd.sel_status.options[status_pos].value;
    return true;
  } else {
    alert(\"$l_j_status_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Kind creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_kind_new(form) {
   if (form.tf_kind.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_kind_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
function fill_ext_form2(int_form) {
   size = int_form.length;
   ext_field = get_elem(window.opener.document, '$ext_target' );
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
   if (form.tf_status.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_status_create\");
      return false;
   }
}

";
