<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_ref_js.inc                                             //
//     - Desc : Referential Data javascript functions File                   //
// 2003-12-05 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


$extra_js .= "
<script type=\"text/javascript\">
<!--
";

require("$obminclude/javascript/check_js.inc");

$extra_js .= "

///////////////////////////////////////////////////////////////////////////////
// Check if the datasource check or deletion is possible                     //
///////////////////////////////////////////////////////////////////////////////
function check_dsrc_checkdel(sel)
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
function check_dsrc_upd(form, sel_del_dsrc)
{
  dsrc_pos = sel_del_dsrc.selectedIndex;
  if ((dsrc_pos >= 0) && (form.tf_dsrc.value != \"\")) {
    form.sel_dsrc.value=sel_del_dsrc.options[dsrc_pos].value;
    return true;
   } else {
     alert(\"$l_j_dsrc_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the data source creation is possible                             //
///////////////////////////////////////////////////////////////////////////////
function check_dsrc_new(form)
{
   if (form.tf_dsrc.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_dsrc_create\");
      return false;
   }
}


// -->
</script>
";
