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

  var wrap = new Element('div').addClass('organizational_chart_wrap color'+num_child);
  var row = new Element('div').addClass('new_level organizational_chart_block').setProperty('id', new_child_id);

  // Delete button
  var delete_button = new Element('a').addEvent('click', function() {
    del_entity(\"'+new_child_id+'\");
  }).adopt(new Element('img').setProperty('src', '$ico_delete'));

  // Add user button
  var user_button = new Element('a').addEvent('click', function() {
    popup('$url_user'+new_child_id,'');
  }).adopt(new Element('img').setProperty('src', '$ico_user'));

  // Add group button
  var group_button = new Element('a').addEvent('click', function() {
    popup('$url_group'+new_child_id,'');
  }).adopt(new Element('img').setProperty('src', '$ico_group'));

  // Add child button
  var child_button = new Element('a').addEvent('click', function() {
    add_entity(new_child_id);
  }).adopt(new Element('img').setProperty('src', '$ico_add'));

  // child name text field
  var child_name = new Element('input').setProperties({'type':'text', 'name':'tf_ogroup['+new_child_id+']', 'size':'24', 'maxlength':'16', 'value':''});

  // parent id hidden 
  var parent_id = new Element('input').setProperties({'type':'hidden', 'name':'tf_parent_level['+new_child_id+']', 'value':level});
 
  // entity div
  var sel_ent = new Element('div').addClass('organizational_chart_entities').setProperty('id', 'sel_ent'+new_child_id);

  row.adopt(wrap.adopt(delete_button).adopt(child_name).adopt(parent_id).adopt(user_button).adopt(group_button).adopt(child_button).adopt(sel_ent));

  container.adopt(row);

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
