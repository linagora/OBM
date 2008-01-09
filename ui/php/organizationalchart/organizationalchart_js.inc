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
  var reg = new RegExp(\"tf_ogroup\", \"g\");
  var elems = \$ES('.mandatory', 'mainContent')
  for(i=0;i<elems.length;i++) {
    node_name = elems[i].value;      
    if (node_name == \"\") {
      alert('$l_fill_ogroup_name');
      return false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Create new ogroup form 
///////////////////////////////////////////////////////////////////////////////
function add_entity(parent_level) {

  var child = $('nb_child').value;
  var new_child_id = parent_level+'.'+child;
  var container = $(parent_level);
  var num_color = (new_child_id.replace(/[0-9]/g, '').length)%7;

  var row = new Element('div').
    addClass('organizationalChartNode').
    setProperty('id', new_child_id).
    setStyle('margin-left', '3em');

  var wrap = new Element('div').
    addClass('organizationalChartWrap color'+num_color);

  // Delete button
  var delete_button = new Element('a').addEvent('click', function() {
    del_entity(new_child_id);
  }).setStyle('float','right').adopt(new Element('img').setProperty('src', '$ico_delete'));

  // Add user
  var user_input = new Element('input').
    setProperties({'type':'text','id':'user'+new_child_id, 'name':'', 'value':'', 'autocomplete':'off'});
  var ico_user = new Element('img').
    setProperty('src', '$ico_user');

  // Add group
  var group_input = new Element('input').
    setProperties({'type':'text','id':'group'+new_child_id, 'name':'', 'value':'', 'autocomplete':'off'})
  var ico_group = new Element('img').
    setProperty('src', '$ico_group');

  // div buttons
  var div_buttons = new Element('div').addClass('newButtons');

  // Add child button
  var child_button = new Element('a').addEvent('click', function() {
    add_entity(new_child_id);
  }).adopt(new Element('img').setProperties({'src':'$ico_add', 'title':'$l_add_child_tooltip'}));

  // child name text field
  var child_name = new Element('input').
    setProperties({'type':'text', 'name':'tf_ogroup['+new_child_id+']', 'size':'18', 'maxlength':'16', 'value':''}).
    addClass('mandatory');

  // parent id hidden 
  var parent_id = new Element('input').
    setProperties({'type':'hidden', 'name':'tf_parent_level['+new_child_id+']', 'value':parent_level});
 
  // entity div
  var sel_ent = new Element('div').
    setProperty('id', 'sel_ent'+new_child_id);

  row.adopt(wrap.
    adopt(delete_button).
    adopt(child_name).
    adopt(parent_id).
    adopt(new Element('br')).adopt(ico_user).adopt(user_input).
    adopt(new Element('br')).adopt(ico_group).adopt(group_input).
    adopt(sel_ent).
    adopt(div_buttons.adopt(child_button))
  );

  container.adopt(row);

  new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_ent'+new_child_id, 'user'+new_child_id, {defaultText:'$user_text'})
  new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_ent'+new_child_id, 'group'+new_child_id, {defaultText:'$user_text'})

  $('nb_child').value ++;
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
