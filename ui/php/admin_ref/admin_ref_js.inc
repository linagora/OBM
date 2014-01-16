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
// OBM - File : admin_ref_js.inc                                             //
//     - Desc : Referential Data javascript functions File                   //
// 2003-12-05 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");
if ($params["ext_element"] != "") {
  require_once("$obminclude/of/of_select.inc");
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
   if (form.tf_dsrc_name.value != \"\")
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
   if (form.tf_ctry_name.value != \"\")
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
  if ((tt_pos >= 0) && (form.tf_tt_label.value != \"\")) {
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
   if (form.tf_tt_label.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_tt_create\");
      return false;
   }
}

";

?>
