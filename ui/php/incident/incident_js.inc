<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco : Last Update 2000-01-13                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

echo "
<script type=\"text/javascript\">
<!--
";


require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");

echo "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


function verifier_inc(f_aff) {
  if (f_aff.tf_dateafter.value != '' && check_date(f_aff.tf_dateafter, '".addslashes($l_date_after_js)."') == false) {
    return false;
  }
  if (f_aff.tf_datebefore.value != '' && check_date(f_aff.tf_datebefore,'".addslashes($l_date_before)."') == false) {
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


function auto_archive(form) {
  if (form.sel_status.value == 'CLOSED')
    form.cb_archive.checked = 1;
  else
    form.cb_archive.checked = 0;
} 
-->
</script>
";
