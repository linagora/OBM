<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_js.inc                                                  //
//     - Desc : Time javascript functions File                               //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

echo "
<SCRIPT LANGUAGE=\"JavaScript\">
<!--
";

require("$obminclude/javascript/check_js.inc");


echo "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function select_all(form) {
  data = form.elements[0];

  longueur=data.length;
  for(i=0;i<longueur;i++){
    data.options[i].selected = 1;
  }
  return false;
}
";

////////////////////////
// Deal Form checking //
////////////////////////
echo "
function check_addtaskform(f_aff) {

  if (! check_sel_date(f_aff)) {
    return false;
  }
  else if (! check_sel_tasktype(f_aff)) {
    return false;
  }
  else
    return true;
}
";

echo "
function check_sel_date(form) {
  error = 1;

  for (i = 0; i < form.length; i++) {
    i_name = form.elements[i].name.substr(0,6);
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

function fill_tasktype(sel_tt, deal_id) {
  var i, j, k;
  var tt;

  for (i = 0; i < project.length; i++) {
    for (j = 1; j < project[i].length; j++) {
      if (project[i][j][1] == deal_id) {
        tt = project[i][0];
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

  for (i = 0; i < project.length; i++) {
    for (j = 1; j < project[i].length; j++) {
      proj_array[k]=project[i][j];
      k++;
    }
  }

  proj_array.sort();

  sel_list.options[0] = new Option('$l_none');
  sel_list.options[0].value = 0; 

  for (i = 0; i < proj_array.length; i++) {
    sel_list.options[i+1] = new Option(proj_array[i][0]);
    sel_list.options[i+1].value = proj_array[i][1]; 
  }
}

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

    next_sel.options[0] = new Option('$l_none');
    next_sel.options[0].value = 0; 

  } else {

    for (i = 0; i < project.length; i++) {
      if (project[i][0] == tasktype_id) {
        tt_location = i; 
      }
    }

    // there are projects with the selected tasktype
    if (tt_location != -1) {
      // add new items
      for (i = 1; i < project[tt_location].length; i++) {
        sel_list.options[i-1] = new Option(project[tt_location][i][0]);
        sel_list.options[i-1].value = project[tt_location][i][1]; 
      }
    }
    // there are no projects with the selected tasktype
    else {
      sel_list.options[0] = new Option(\"$l_none\");
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

  for (i = 0; i < projecttask.length; i++) {
          if (projecttask[i][0] == deal_id) {
            dl_location = i; 
    }
  }

  // there are projects with the selected tasktype
  if (dl_location != -1) {
    // add new items
    for (i = 1; i < projecttask[dl_location].length; i++) {
      sel_list.options[i-1] = new Option(projecttask[dl_location][i][0]);
      sel_list.options[i-1].value = projecttask[dl_location][i][1]; 
    }
  }

  // there are no projects with the selected tasktype
  else {
    sel_list.options[0] = new Option(\"$l_none\");
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

// -->
</SCRIPT>
";

