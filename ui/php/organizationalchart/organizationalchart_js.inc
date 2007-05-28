<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : organizationalchart_js.inc                                  //
//     - Desc  : Organizational chart javascript functions File              //
// 2007-02-27 David PHAN                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id: organizationalchart_js.inc,v 1.35 2007/02/20 15:16:10 mehdi Exp $ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

$url_user = "$path/user/user_index.php?action=ext_get_ids&popup=1&ext_element=sel_ent";
$url_group = "$path/group/group_index.php?action=ext_get_ids&popup=1&ext_element=sel_ent";

$extra_js .= "

///////////////////////////////////////////////////////////////////////////////
// Check organizational chart data form 
///////////////////////////////////////////////////////////////////////////////
function check_form(form) {

  // MANDATORY : Check that name is not empty
  if (trim(form.tf_oc_name.value) == \"\") {
    alert (\"$l_fill_name\");
    return false;
  }

  // MANDATORY : Check that all ogroup name is not empty
  forms = document.forms[\"f_entity\"].elements;
  var reg = new RegExp(\"tf_ogroup\", \"g\");
  for(var elem in forms) {
    name = new String(forms[elem].name);
    if (name.match(reg) && forms[elem].value == \"\") {
      alert('$l_fill_ogroup_name');
      return false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Create new ogroup form 
// TODO: new Element('input'). ...
///////////////////////////////////////////////////////////////////////////////
function add_entity(level) {

  var child = window.document.f_entity.nb_child.value;
  var new_child_id = level+'.'+child;

  var target = window.document;
  var container = get_elem(target, level);

  var num_child = (new_child_id.replace(/[0-9]/g, '').length)%7;

  // Create the div row
  row = target.createElement('div');
  row.id = new_child_id;
  row.className = 'new_level organizational_chart_block';

  // Create the delete link
  del_child_link = target.createElement('a');
  del_child_link.href = 'javascript: del_entity(\"'+new_child_id+'\");';
  del_child = target.createElement('img');
  del_child.src = '$ico_delete';
  del_child_link.appendChild(del_child);

  // Create the add user link
  add_user_link = target.createElement('a');
  add_user_link.href= '#';
  add_user_link.onclick = function() { popup('$url_user'+new_child_id,''); };
  add_user = target.createElement('img');
  add_user.src = '$ico_user';
  add_user_link.appendChild(add_user);

  // Create the add group link
  add_group_link = target.createElement('a');
  add_group_link.href= '#';
  add_group_link.onclick = function() { popup('$url_group'+new_child_id,''); };
  add_group = target.createElement('img');
  add_group.src = '$ico_group';
  add_group_link.appendChild(add_group);

  // Create the add child link
  add_child_link = target.createElement('a');
  add_child_link.href = 'javascript: add_entity(\"'+new_child_id+'\");';
  add_child = target.createElement('img');
  add_child.src = '$ico_add';
  add_child_link.appendChild(add_child);

  // Create the input field
  field = target.createElement('input');
  field.type = 'text';
  field.name = 'tf_ogroup['+new_child_id+']';
  field.size = '24';
  field.setAttribute('maxlength', '16');
  field.value = '';

  // Create hidden input field
  parent_id = target.createElement('input');
  parent_id.type = 'hidden';
  parent_id.name = 'tf_parent_level['+new_child_id+']';
  parent_id.value = level;

  // Create the sel_ent div
  sel_ent = target.createElement('div');
  sel_ent.id = 'sel_ent'+new_child_id;
  sel_ent.className = 'organizational_chart_entities';

  wraprow = target.createElement('div');
  wraprow.className = 'organizational_chart_wrap color'+num_child;

  // Add these elements to the div row
  wraprow.appendChild(del_child_link);
  wraprow.appendChild(field);
  wraprow.appendChild(parent_id);
  wraprow.appendChild(add_user_link);
  wraprow.appendChild(add_group_link);
  wraprow.appendChild(add_child_link);
  wraprow.appendChild(sel_ent);
  row.appendChild(wraprow);

  // Add the div row to the container
  container.appendChild(row);

  window.document.f_entity.nb_child.value ++;
}


///////////////////////////////////////////////////////////////////////////////
// remove an ogroup + its child 
///////////////////////////////////////////////////////////////////////////////
function del_entity(parentId) {
  if(confirm(\"$l_js_delete_node\")) {
    var target = window.document;
    var container = target.getElementById(parentId);
    while (container.firstChild) {
      container.removeChild(container.firstChild);
    }
  }
}

";
?>
