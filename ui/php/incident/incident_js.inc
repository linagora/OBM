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
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco : Last Update 2000-01-13                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

$extra_js .= "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
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
  if (form.tf_date.value == \"\") { 
      alert(\"$l_date\" + \" : $l_fill_date\");
      return false;
    }
 return true;
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
?>