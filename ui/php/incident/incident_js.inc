<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco : Last Update 2000-01-13                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script type=\"text/javascript\">
<!--
";


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
// Check if the Deal Solution type deletion is possible                      //
///////////////////////////////////////////////////////////////////////////////
function check_solutiontype_checkdel(form)
{  
   if (form.sel_solutiontype.selectedIndex >= 0)
   { 
     return true;
   }
   else
   {
     alert(\"$l_j_soltype_delete\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the solutiontype update is possible                              //
///////////////////////////////////////////////////////////////////////////////
function check_solutiontype_upd(form,formd)
{
  solutiontype_pos = formd.sel_solutiontype.selectedIndex;
  if ((solutiontype_pos >= 0) && (form.tf_solutiontype.value != \"\")) {
    form.sel_solutiontype.value=formd.sel_solutiontype.options[solutiontype_pos].value;
    return true;
  } else {
    alert(\"$l_j_soltype_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the solutiontype creation is possible                            //
///////////////////////////////////////////////////////////////////////////////
function check_solutiontype_new(form)
{
   if (form.tf_solutiontype.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_soltype_create\");
      return false;
   }
}




-->
</script>
";
