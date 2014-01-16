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
// OBM - File : time_js.inc                                                  //
//     - Desc : Time javascript functions File                               //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

$extra_js .= "

function sel_public_group(date, element, action) {
  var item_id = element.getProperty('id').split('-');
  var id = item_id[item_id.length - 1];
  window.location=('time_index.php?action='+action+'&date='+date+'&group_id='+id);
}

function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function select_all(form) {
  data = \$('sel_user_id') ;
  longueur=data.length;
  for (i=0;i<longueur;i++) {
    data.options[i].selected = 1;
  }
  return false;
}


////////////////////////
// Task Form checking //
////////////////////////
function check_addtaskform(f_aff) {

  if (! check_sel_date(f_aff)) {
    return false;
  }
  else if (! check_sel_tasktype(f_aff)) {
    return false;
  }
  return true;
}

function check_sel_date(form) {
  error = 1;
  cb = 0;

  for (i = 0; i < form.length; i++) {
    i_name = \"\";
    if (form.elements[i].name) {
      i_name = form.elements[i].name.substr(0,6);
    }
    i_checked = form.elements[i].checked;
    
    if (i_name == \"cb_day\") { 
      cb = 1;

      if (i_checked != \"\") {
	error = 0;
      }
    }
  }

  if (error && cb) {
    alert (\"$l_j_select_day\");
    return false;
  } else {
    return true;
  }

}

function check_sel_tasktype(form) {
  if (form.sel_tasktype.options[form.sel_tasktype.selectedIndex].value == 0) {
    alert (\"$l_j_select_tasktype\");
    return false;
  } else
    return true;
}


/////////////////////////////////////
// tasklist auto-selection         //
/////////////////////////////////////
function fill_tasktype(sel_tt, project_id) {
  var i, j, k;
  var tt;
  for (i in project) {
    for (j = 0; j < project[i].length; j++) {
      if (project[i][j]['id'] == project_id) {
        tt = i;
      }
    }
  }

  for (k=0; k<(sel_tt.options.length); k++) {
    if (sel_tt.options[k].value == tt)
      sel_tt.options[k].selected=true;
  }

}

/////////////////////////////////////
// project dynamic list generation //
/////////////////////////////////////
function fill_projectall(sel_list) {
  var i, j, k;
  k = 0;
  proj_array = new Array();
  for (i in project) {
    for (j = 0 ; j < project[i].length; j++) {
      proj_array[k]=project[i][j];
      k++;
    }
  }

//  proj_array.sort();

  sel_list.options[0] = new Option('$l_undef');
  sel_list.options[0].value = 0; 

  for (i = 0; i < proj_array.length; i++) {
    sel_list.options[i+1] = new Option(proj_array[i].label);
    sel_list.options[i+1].value = proj_array[i].id; 
  }
}

/////////////////////////////////////
// project dynamic list generation //
/////////////////////////////////////
function fill_project(sel_list, tasktype_id, next_sel, deal_id) {
  var i, j, k;
  var tt_location = -1;

  // empty existing items
  for (i = sel_list.options.length-1; i >= 0; i--) {
    sel_list.options[i] = null; 
  }

  if (tasktype_id == 0) {
    fill_projectall(sel_list);

    for (i = next_sel.options.length-1; i >= 0; i--) {
      next_sel.options[i] = null; 
    }

    next_sel.options[0] = new Option('$l_undef');
    next_sel.options[0].value = 0; 

  } else {

    for (i in project) {
      if (i == tasktype_id) {
        tt_location = i; 
      }
    }

    // there are projects with the selected tasktype
    if (tt_location != -1) {
      // We construct a new array (without the tt id) to sort it case sensitive
      proj_array = new Array();
      for (i = 0; i < project[tt_location].length; i++) {
        proj_array[i] = project[tt_location][i];
      }
//      proj_array.sort();

      // Fill the project select widget
      for (i = 0; i < proj_array.length; i++) {
        sel_list.options[i] = new Option(proj_array[i].label);
        sel_list.options[i].value = proj_array[i].id; 
      }
    }
    // there are no projects with the selected tasktype
    else {
      sel_list.options[0] = new Option(\"$l_undef\");
      sel_list.options[0].value = 0; 
    }

    // select the correct project
    for (k=0; k<(sel_list.options.length); k++) {
      if (sel_list.options[k].value == deal_id)
        sel_list.options[k].selected = true;
    }

    if (deal_id > 0)
      fill_projecttask(next_sel, deal_id);
    else 
      fill_projecttask(next_sel, sel_list.options[0].value);
  }
}


/////////////////////////////////////////
// projecttask dynamic list generation //
/////////////////////////////////////////
function fill_projecttask(sel_list, deal_id) {
  var i, j;
  // location of selected deal in projecttask array
  var dl_location = -1;

  // empty existing items
  for (i = sel_list.options.length-1; i >= 0; i--) {
    sel_list.options[i] = null; 
  }

  for (i in projecttask) {
    if (i == deal_id) {
      dl_location = i;
    }
  }

  // there are projects with the selected tasktype
  if (dl_location != -1) {
    // add new items
    for (i = 0; i < projecttask[dl_location].length; i++) {
      sel_list.options[i] = new Option(projecttask[dl_location][i].label);
      sel_list.options[i].value = projecttask[dl_location][i].id; 
    }
  }

  // there are no projects with the selected tasktype
  else {
    sel_list.options[0] = new Option(\"$l_undef\");
    sel_list.options[0].value = 0; 
  }

  // select first item (prompt) for sub list
  //select_list.options[0].selected = true;
}


//////////////////////////////////////
// projecttask selection for update //
//////////////////////////////////////
function select_default(sel_list, def_value) {
  for (i=0; i<(sel_list.options.length); i++) {
    if (sel_list.options[i].value == def_value)
      sel_list.options[i].selected=true;
  }
}
";

?>
