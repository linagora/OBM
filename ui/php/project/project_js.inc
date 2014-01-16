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
// OBM - File  : project_js.inc                                              //
//     - Desc  : Project javascript functions File                           //
// 2003-07-08 Aliacom - Bastien Continsouzas                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

$extra_js .= "
var projects;
var tasks;";

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($params['ext_widget'] != '') || ($params['ext_widget_text'] != '')) {
  
  $extra_js .= "

function check_get_id(valeur,text) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_select_project\");
    return false;
  } else {
    window.opener.document.$params[ext_widget].value=valeur;
    window.opener.document.$params[ext_widget_text].value=text;
    window.close();
    return true;
  }
}

function check_get_task_id(value_project,value_task,value_type) {

  if ((value_task < 1) || (value_task == null)) {
    alert (\"$l_select_task\");
    return false;
  } else {
    text = projects[value_type][value_project] + ' -- ' + tasks[value_project][value_task];
    window.opener.document.$params[ext_widget].value=value_task;
    window.opener.document.$params[ext_widget_text].value=text;
    window.close();
    return false;
  }
}
";
}
if ($params['ext_element'] != '') {
  require_once("$obminclude/of/of_select.inc");
}

$extra_js .= "

function check_get_id_cv(id, name) {
  if ((id < 1) || (id == null)) {
    alert (\"$l_select_project\");
    return false;
  } else {
    window.opener.newRow(id, name)
    window.close();
    return true;
  }
}


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
  try {

    // MANDATORY : Check that the Project soldtime is not empty
    if (trim(form.tf_soldtime.value) == \"\") {
      alert (\"$l_fill_soldtime\");
      return false;
    }

    // MANDATORY : Check that the Project soldtime is a number
    else if (!(check_money(form.tf_soldtime.value))) {
      alert (\"$l_fill_soldnumber\");
      return false;
    }

    // MANDATORY : Check that the Project soldtime is not null
    else if ((form.tf_soldtime.value) == 0) {
      alert (\"$l_fill_soldnumber\");
      return false;
    }
  } catch (err) {
  }
  return true;
} 


///////////////////////////////////////////////////////////////////////////////
// Select the task add form                                                  //
///////////////////////////////////////////////////////////////////////////////
function check_taskadd(form)
{
    return true;
}

///////////////////////////////////////////////////////////////////////////////
// Select the task add form                                                  //
///////////////////////////////////////////////////////////////////////////////
function check_tasktype(sel_tasktype) {
  if(sel_tasktype.selectedIndex >= 0)
    return true;
  else {
    alert (\"$l_fill_tt\");
    return false;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Check the add new task form                                               //
///////////////////////////////////////////////////////////////////////////////
function check_task_new(form) {
  if (form.tf_reftask_label.value != \"\") {
    return true;
  } else {
    alert('$l_fill_tasklabel');
    return false;
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
// Set the hidden fields to update (or delete) the user
function change_status(user_id, user_status, form)
{
  for (i = 0; i < form.length; i++) {
    i_name = form.elements[i].name;
    i_value = form.elements[i].value;

    if (i_name == \"user_id\")
      form.elements[i].value = user_id;
    else if (i_name == \"user_status\")
      form.elements[i].value = user_status;
    else if (i_name == \"action\")
      form.elements[i].value = \"member_update\";
    else
      null;
  }

  form.submit();
}

///////////////////////////////////////////////////////////////////////////////
// Fill the project select input
///////////////////////////////////////////////////////////////////////////////
function fill_project(tasktype_id,sel_project,sel_task) {
  current_project = projects[tasktype_id];
  empty_select(sel_project,true);
  empty_select(sel_task,true);
  for (var id in current_project) {
    sel_project.options.add(new Option(current_project[id], id));
  }
}
function fill_projecttask(project_id,sel_task) {
  empty_select(sel_task,true);  
  current_task = tasks[project_id];  
  for (var id in current_task) {
    sel_task.options.add(new Option(current_task[id], id));
  }
}
function empty_select(sel,have_undef) {
 for (i = sel.options.length-1; i >= 0; i--) {
    sel.options[i] = null; 
  }
  if(have_undef)
    sel.options.add(new Option('$l_undef','$c_undef'));
} 

function fill_projectreftask(tasktype_id,sel_task) {
  current_task = tasks[tasktype_id]; 
  container = document.getElementById('task_checkbox');
  while( container.rows.length > 0) {
    container.deleteRow(0);
  }
  index = 0;
  for (var id in current_task) {
    row = container.insertRow(index);
    cell = row.insertCell(0);
    cell.className='detailForm';    
    cell.style.width = '50%'
    cell.innerHTML =  '<input id=\"'+id+'\" name=\"'+id+'\" type=\"checkbox\">';   ;
    cell = row.insertCell(1);    
    cell.className='detailForm';
    cell.style.width = '50%'
    cell.innerHTML = '<span id=\"data-task-'+id+'\" style=\"display: none;\">'+current_task[id]+'</span>'+current_task[id];
    index++;
  }
}
";

?>
