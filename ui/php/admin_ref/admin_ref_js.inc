<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_ref_js.inc                                             //
//     - Desc : Referential Data javascript functions File                   //
// 2003-12-05 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");
if ($ext_element != "") {
  require_once("$obminclude/of/of_extmod.inc");
}

$extra_js .= "

///////////////////////////////////////////////////////////////////////////////
// Check if the datasource check or deletion is possible                     //
///////////////////////////////////////////////////////////////////////////////
function check_admin_ref_dsrc_checkdel(sel)
{
   if (sel.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_dsrc_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the data source update is possible                               //
///////////////////////////////////////////////////////////////////////////////
function check_admin_ref_dsrc_upd(form, sel_del_dsrc)
{
  dsrc_pos = sel_del_dsrc.selectedIndex;
  if ((dsrc_pos >= 0) && (form.tf_name.value != \"\")) {
    form.sel_datasource_id.value=sel_del_dsrc.options[dsrc_pos].value;
    return true;
   } else {
     alert(\"$l_j_dsrc_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the data source creation is possible                             //
///////////////////////////////////////////////////////////////////////////////
function check_admin_ref_dsrc_new(form)
{
   if (form.tf_name.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_dsrc_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the country check or deletion is possible                        //
///////////////////////////////////////////////////////////////////////////////
function check_admin_ref_ctry_checkdel(sel)
{
   if (sel.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_ctry_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the country update is possible                                   //
///////////////////////////////////////////////////////////////////////////////
function check_admin_ref_ctry_upd(form, sel_del_ctry)
{
  ctry_pos = sel_del_ctry.selectedIndex;
  if ((ctry_pos >= 0) && (form.tf_name.value != \"\")) {
    form.sel_country.value=sel_del_ctry.options[ctry_pos].value;
    return true;
   } else {
     alert(\"$l_j_ctry_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the country creation is possible                                 //
///////////////////////////////////////////////////////////////////////////////
function check_admin_ref_ctry_new(form)
{
   if (form.tf_name.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_ctry_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the task type check or deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_admin_ref_tt_checkdel(sel)
{
   if (sel.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_tt_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the task type update is possible
///////////////////////////////////////////////////////////////////////////////
function check_admin_ref_tt_upd(form, sel_del_tt)
{
  tt_pos = sel_del_tt.selectedIndex;
  if ((tt_pos >= 0) && (form.tf_label.value != \"\")) {
    form.sel_tasktype_id.value=sel_del_tt.options[tt_pos].value;
    return true;
   } else {
     alert(\"$l_j_tt_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the tasktype creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_admin_ref_tt_new(form)
{
   if (form.tf_label.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_tt_create\");
      return false;
   }
}

";

?>
