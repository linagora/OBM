<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco : Last Update 2000-01-13                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");

$extra_js .= "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


function check_search_inc(f_inc) {
  if (f_inc.tf_dateafter.value != '' && check_date(f_inc.tf_dateafter, '".addslashes($l_date_after_js)."') == false) {
    return false;
  }
  if (f_inc.tf_datebefore.value != '' && check_date(f_inc.tf_datebefore,'".addslashes($l_date_before)."') == false) {
    return false;
  }
  return true;
}


function check_incident(form) {
  // MANDATORY : Check that Contact name is not empty
  if (trim(form.tf_lincident.value) == \"\") {
    alert (\"$l_fill_label\");
    return false;
  }
  if (form.tf_lincident.value.length > 100) {
    alert (\"$l_length_label\");
    return false;
  }
  if (check_date(form.tf_date,'".addslashes($l_incident_date)."') == false) { 
      return false;
    }
 return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the priority check or deletion is possible                       //
///////////////////////////////////////////////////////////////////////////////
function check_priority_checkdel(form)
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
// Check if the priority update is possible                                  //
///////////////////////////////////////////////////////////////////////////////
function check_priority_upd(form,formd)
{
  pri_pos = formd.sel_priority.selectedIndex;
  if ((pri_pos >= 0) && (form.tf_pri.value != \"\")) {
    form.sel_priority.value=formd.sel_priority.options[pri_pos].value;
    return true;
  } else {
    alert(\"$l_j_pri_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the priority creation is possible                                //
///////////////////////////////////////////////////////////////////////////////
function check_priority_new(form)
{
   if (form.tf_pri.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_pri_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Status deletion is possible                            //
///////////////////////////////////////////////////////////////////////////////
function check_status_checkdel(form)
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
// Check if the status update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_status_upd(form,formd)
{
  status_pos = formd.sel_status.selectedIndex;
  if ((status_pos >= 0) && (form.tf_status.value != \"\")) {
    form.sel_status.value=formd.sel_status.options[status_pos].value;
    return true;
  } else {
    alert(\"$l_j_sta_update\");
    return false;
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
      alert(\"$l_j_sta_create\");
      return false;
   }
}




///////////////////////////////////////////////////////////////////////////////
// Check if the Incident Category 1 deletion is possible
///////////////////////////////////////////////////////////////////////////////
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


///////////////////////////////////////////////////////////////////////////////
// Check if the Category 1 update is possible
///////////////////////////////////////////////////////////////////////////////
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


///////////////////////////////////////////////////////////////////////////////
// Check if the Category 1 creation is possible
///////////////////////////////////////////////////////////////////////////////
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

";
