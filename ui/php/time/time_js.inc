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

function uncheck_days(data) {
 longueur=data.length;
 for(i=3;i<longueur;i++){
  data.elements[i].checked = 0;
 }

}
";

////////////////////////
// Deal Form checking //
////////////////////////
echo "
function check_form(f_aff) {

  if (! check_sel_tasktype(f_aff)) {
    return false;
  }
  else if (! check_sel_project(f_aff)) {
    return false;
  }
  else
    return true;
}
";

echo "
function check_sel_tasktype(form) {
  //  alert(\"value\" + form.sel_tasktype.selectedIndex);
  if (form.sel_tasktype.options[form.sel_tasktype.selectedIndex].value == 0) {
    alert (\"$l_j_select_tasktype\");
    return false;
  } else
    return true;
}

function check_sel_project(form) {
  //alert(\"value\" + form.sel_tasktype.selectedIndex);
  if ((form.sel_project.options[form.sel_project.selectedIndex].value == 0) &&
       (form.sel_tasktype.options[form.sel_tasktype.selectedIndex].value < 10)) {
    alert (\"$l_j_select_project\");
    return false;
  } else
    return true;
}
// -->

/////////////////////////////////////
// project dynamic list generation //
/////////////////////////////////////
function fill_project(sel_list, tasktype_id) {
	var i, j;
        // location of selected tasktype in project array
	var tt_location = -1;

	// empty existing items
	for (i = sel_list.options.length-1; i >= 0; i--) {
	  sel_list.options[i] = null; 
	}

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

	// select first item (prompt) for sub list
	//select_list.options[0].selected = true;
	}

function select_default(sel_list, def_value) {
  for (i=0; i<(sel_list.options.length); i++) {
    if (sel_list.options[i].value == def_value)
      sel_list.options[i].selected=true;
  }
}

</SCRIPT>
";

