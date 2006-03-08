<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : project_js.inc                                              //
//     - Desc  : Project javascript functions File                           //
// 2003-07-08 Aliacom - Bastien Continsouzas                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($ext_widget != "") || ($ext_widget_text != "")) {
  $extra_js .= "

function check_get_id(valeur,text) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_select_project\");
    return false;
  } else {
    window.opener.document.$ext_widget.value=valeur;
    window.opener.document.$ext_widget_text.value=text;
    window.close();
    return true;
  }
}";
}

$extra_js .= "

function check_get_id_url(p_url, valeur) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_select_project\");
    return false;
  } else {
    new_url = p_url + valeur;
    window.opener.location.href=new_url;
    window.close();
    return true;
  }
}


function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the kind form values are correct                                 //
///////////////////////////////////////////////////////////////////////////////
function check_project(form) {

  // MANDATORY : Check that the Project name is not empty
  if (trim(form.tf_name.value) == \"\") {
    alert (\"$l_fill_name\");
    return false;
  }

  // MANDATORY : Check that the Project tasktype is filled
  else if ((form.sel_tt.value) == 0) {
    alert (\"$l_fill_tt\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is not empty
  else if (trim(form.tf_soldtime.value) == \"\") {
    alert (\"$l_fill_soldtime\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is a number
  else if (!(check_number(form.tf_soldtime.value))) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is not null
  else if ((form.tf_soldtime.value) == 0) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }
  return true;
} 


///////////////////////////////////////////////////////////////////////////////
// Select the task add form                                                  //
///////////////////////////////////////////////////////////////////////////////
function check_taskadd(form)
{
  // MANDATORY : Check that the ProjectTask label is not empty
  if (trim(form.tf_tasklabel.value) == \"\") {
    alert (\"$l_fill_tasklabel\");
    return false;
  }
  else {
    return true;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check the task update form                                                //
///////////////////////////////////////////////////////////////////////////////
function check_taskupd(form)
{
  // MANDATORY : Check that the ProjectTask label is not empty
  if (trim(form.tf_tasklabel.value) == \"\") {
    alert (\"$l_fill_tasklabel\");
    return false;
  }
  else {
    return true;
  }
}

function submit_memberdel(form)
{
  form.action = \"project_index.php?action=member_del\"
  form.submit();
}

///////////////////////////////////////////////////////////////////////////////
// Select the project members form                                           //
///////////////////////////////////////////////////////////////////////////////
// each member should be assigned to at least one task

function change_status(user_id, user_status, form)
{
  for (i = 0; i < form.length; i++) {
    i_name = form.elements[i].name;
    i_value = form.elements[i].value;
    
    if (i_name == \"param_user\")
      form.elements[i].value = user_id;
    else if (i_name == \"param_status\")
      form.elements[i].value = user_status;
    else if (i_name == \"action\")
      form.elements[i].value = \"member_update\";
    else
      null;
  }

  form.submit();
}

// Function that check the allocation form validation
function check_allo_valid(form, nb_mem)
{
  error = 0;
  idx = 0;
  i_nb = new Array(nb_mem);

  for (i = 0; i < document.forms[0].length; i++) {
    i_name = document.forms[0].elements[i].name.substr(0,12);
    i_value = document.forms[0].elements[i].value;
    
    if (i_name == \"tf_projected\") { 
      if ((i_value != \"\") && (!(check_number(i_value)))) {
	error = 1;
      } else if (i_value != \"\") {
	// we mark this user as having a task
	i_nb[idx]=1;
      } else {
	;
      }

      idx++;

      // return to the first user
      if (idx == nb_mem) {
	idx = 0;
      }
    }
  }

  // Require that each member be assigned to at least one task
  //
  //  for (idx=0; idx<nb_mem; idx++) {
  //    if (i_nb[idx] != 1) {
  //      error += 2;
  //    }
  //  }

  if (error == 1) {
    alert (\"$l_fill_projnumber\");
    return false;
  } else if (error != 0) {
    alert (\"$l_fill_projected\");
    return false;
  } else {
    //    form.submit();
    return true;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Select the progress update form
///////////////////////////////////////////////////////////////////////////////
function check_progressvalid(form)
{
  error = 0;

  for (i = 0; i < document.forms[0].length; i++) {
    i_name = form.elements[i].name.substr(0,10);
    i_value = form.elements[i].value;

    if (i_name == \"tf_missing\") { 
      if ((i_value != \"\") && (!(check_number(i_value)))) {
	error = 1;
      } else if (i_value == \"\") {
	error += 2 ;
      } else {
	;
      }
    }
  }

  if (error == 1) {
    alert (\"$l_fill_missnumber\");
    return false;
  } else if (error != 0) {
    alert (\"$l_fill_missing\");
    return false;
  } else {
    return true;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check that the advance form is valid
///////////////////////////////////////////////////////////////////////////////
function check_advance_valid(form)
{
  error = 0;

  for (i = 0; i < document.forms[0].length; i++) {
    i_name = document.forms[0].elements[i].name.substr(0,10);
    i_value = document.forms[0].elements[i].value;
    
    if (i_name == \"tf_project\") { 
      if ((i_value != \"\") && (!(check_number(i_value)))) {
	error = 1;
      }
    } else if (i_name == \"tf_missing\") { 
      if ((i_value != \"\") && (!(check_number(i_value)))) {
	error = 2;
      }
    }
  }

  if (error == 1) {
    alert (\"$l_fill_projnumber\");
    return false;
  } else if (error == 2) {
    alert (\"$l_fill_missnumber\");
    return false;
  } else if (error != 0) {
    alert (\"$l_fill_projected\");
    return false;
  } else {
    return true;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Open the userobm list form window                                         //
///////////////////////////////////////////////////////////////////////////////
function submit_memberadd(form, p_id)
{
    window.name='Liste';
    window.open('../user/user_index.php?action=ext_get_ids&popup=1&title=$l_add_member&ext_action=member_add&ext_url=../project/project_index.php&ext_id='+ p_id +'&ext_target=Liste','','height=400,width=620,scrollbars=yes');
    return false;
}


///////////////////////////////////////////////////////////////////////////////
// parent task auto select on task selection (task update form)
///////////////////////////////////////////////////////////////////////////////
function fill_parenttask(sel_pt, sel_t, tf_tname) {
  var task_id, ptask_id;

  task_id = sel_t.value;
  ptask_id = parent_task[task_id];
  pos = parent_pos[ptask_id];
  sel_pt.options[pos].selected=true;
  stext = sel_t.options[sel_t.selectedIndex].text;
  // Delete special space chars at the beginning (&nbsp; ?)
  new_text = stext.replace(/(\s+)(\S)/, '$2');
  tf_tname.value = trim(new_text);
}

";
