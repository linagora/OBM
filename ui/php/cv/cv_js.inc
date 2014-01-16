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
// OBM - File  : cv_js.inc                                                   //
//     - Desc  : CV javascript functions File                                //
// 2006-05-23 Vincent Coulette                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$             //          
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

  $extra_js .= "

  function check_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_cv\");
      return false;
    } else {
      container = window.opener.document.getElementById('ref_location');
      container.innerHTML = text;
      window.close();
      return true;
    }
  }

";

$extra_js .= "

///////////////////////////////////////////////////////////////////////////////
// Check if a project has already been added (returns true if already added)
///////////////////////////////////////////////////////////////////////////////
function alreadyAdded(id){
  var projectIds = document.getElementsByName('project_id[]');
  for (i = 0; i<projectIds.length; i++){
    if (projectIds[i].value == id)
      return true;
  }
  return false;
}

///////////////////////////////////////////////////////////////////////////////
// Add a row to the refs table
///////////////////////////////////////////////////////////////////////////////
function newRow(id, name) {

  if (alreadyAdded(id)){
    alert(\"$l_j_already_added\");
    return;
  }

  // table
  table = document.getElementById('table_refs');

  // row
  num = table.rows.length - 1;
  row = table.insertRow(num);

  // td text : widget_text  + widget (hidden)
  cell = row.insertCell(0);
  cell.className = 'detailForm';
  cell.setAttribute('style', 'text-align: center;');
  projectName = document.createTextNode(name);
  cell.appendChild(projectName);
  inputProjectId = document.createElement('input');
  inputProjectId.type = 'hidden';
  inputProjectId.name = 'project_id[]';
  inputProjectId.value = id;
  cell.appendChild(inputProjectId);
  

  // td form
  cell = row.insertCell(1);
  cell.className = 'detailForm';
  cell.setAttribute('style', 'text-align: center;');
  input = document.createElement('input');
  input.name = 'project_role['+id+']';
  input.type = 'text';
  input.size = '64';
  input.maxlength = '128';
  cell.appendChild(input);
  
  // td delete link
  cell = row.insertCell(2);
  cell.setAttribute('style', 'text-align: center;');
  cell.className = 'detailForm';
  cell.innerHTML = '<a href=\"\" onclick=\"deleteRow(this) ;return false;\">$l_delete</a>';
        
}

///////////////////////////////////////////////////////////////////////////////
// Add a row to the additionnalrefs table
///////////////////////////////////////////////////////////////////////////////
function newRow2() {

  // table
  table = document.getElementById('table_additionalrefs');

  // row
  num = table.rows.length - 1;
  row = table.insertRow(num);

  // td form
  cell = row.insertCell(0);
  cell.className = 'detailForm';
  cell.setAttribute('style', 'text-align: center;');
  input = document.createElement('input');
  input.name = 'additionalrefs_date[]';
  input.type = 'text';
  input.size = '16';
  cell.appendChild(input);

  // td form
  cell = row.insertCell(1);
  cell.className = 'detailForm';
  cell.setAttribute('style', 'text-align: center;');
  input = document.createElement('input');
  input.name = 'additionalrefs_duration[]';
  input.type = 'text';
  input.size = '16';
  cell.appendChild(input);

  // td form
  cell = row.insertCell(2);
  cell.className = 'detailForm';
  cell.setAttribute('style', 'text-align: center;');
  input = document.createElement('input');
  input.name = 'additionalrefs_project[]';
  input.type = 'text';
  input.size = '32';
  cell.appendChild(input);

  // td form
  cell = row.insertCell(3);
  cell.className = 'detailForm';
  cell.setAttribute('style', 'text-align: center;');
  input = document.createElement('input');
  input.name = 'additionalrefs_role[]';
  input.type = 'text';
  input.size = '32';
  cell.appendChild(input);

  // td form
  cell = row.insertCell(4);
  cell.className = 'detailForm';
  cell.setAttribute('style', 'text-align: center;');
  textarea = document.createElement('textarea');
  textarea.name = 'additionalrefs_desc[]';
  cell.appendChild(textarea);

  // td form
  cell = row.insertCell(5);
  cell.className = 'detailForm';
  cell.setAttribute('style', 'text-align: center;');
  textarea = document.createElement('textarea');
  textarea.name = 'additionalrefs_tech[]';
  cell.appendChild(textarea);
  
  // td delete link
  cell = row.insertCell(6);
  cell.setAttribute('style', 'text-align: center;');
  cell.className = 'detailForm';
  cell.innerHTML = '<a href=\"\" onclick=\"deleteRow(this) ;return false;\">$l_delete</a>';
        
}

///////////////////////////////////////////////////////////////////////////////
// Find the next parent of the element with the given tag (parentTagName)
///////////////////////////////////////////////////////////////////////////////
function getParent(element, parentTagName) 
{
  if  (! element )
    return null;
  else if ( element.nodeType == 1 && element.tagName.toLowerCase() == parentTagName.toLowerCase() )
    return element;
  else
    return getParent(element.parentNode, parentTagName);
}

///////////////////////////////////////////////////////////////////////////////
// Delete a row from a table
///////////////////////////////////////////////////////////////////////////////
function deleteRow(row) 
{
  var td = row.parentNode;
  var table = getParent(td, 'table');
  // get the tbody
  var tbody = table.tBodies[0];
  
  // delete the row
  tbody.removeChild(getParent(td, 'tr'));
}

///////////////////////////////////////////////////////////////////////////////
// Check form
///////////////////////////////////////////////////////////////////////////////
function check_cv(form) {
  if (form.tf_title.value == '') {
    alert('$l_js_fill_title');
    return false;
  }
  return true;
}

///////////////////////////////////////////////////////////////////////////////
// Confirm delete
///////////////////////////////////////////////////////////////////////////////
function confirm_cv_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

///////////////////////////////////////////////////////////////////////////////
// Show or Hide template choice fields depending on the selected template kind
///////////////////////////////////////////////////////////////////////////////
function showHide(nb){
  if (document.getElementById('default_template'))
    document.getElementById('default_template').style.display = 'none';
  document.getElementById('other_template').style.display = 'none';
  /* document.getElementById('unreferenced_template').style.display = 'none';*/
  switch (nb) {
    case 0 : 
      document.getElementById('default_template').style.display = '';
    break;
    case 1 :
      document.getElementById('other_template').style.display = '';
    break;
  /*
    case 2 : 
      document.getElementById('unreferenced_template').style.display = '';
    break;
  */
  }
  return;
}

";
?>
