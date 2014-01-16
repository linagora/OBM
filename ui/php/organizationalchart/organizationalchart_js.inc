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
// Collapse/Expande node 
///////////////////////////////////////////////////////////////////////////////
function node_toggle(level) {

  if ($('child_'+level).getStyle('display') == 'block') {
    $('child_'+level).setStyle('display', 'none');
    $('img_'+level).src = obm.vars.images.plus;
  } else {
    $('child_'+level).setStyle('display', 'block');
    //$('img_'+level).src = obm.vars.images.minus;
    var children_ids = $('child_'+level).getChildren().getProperty('id');
    for(i=0;i<children_ids.length;i++) {
      id = children_ids[i];
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_ent'+id, 'user'+id);    
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_ent'+id, 'group'+id);    
    }
  }

}


///////////////////////////////////////////////////////////////////////////////
// Zoom function
///////////////////////////////////////////////////////////////////////////////
function zoom(z) {

  var nodestart = $$('.nodeStart');
  var nodeend = $$('.nodeEnd');
  var nodewrap = $$('.nodeWrap');

  if (z == 'small') {
    newClass = 'nodeSmall';
    oldClass = 'node';

    for(i=0;i<nodestart.length;i++) {
      var el = nodestart[i];
      w = $(el).getStyle('width').split('px');
      $(el).setStyle('width', ((w[0]/2))+'px');
    }
    for(i=0;i<nodeend.length;i++) {
      var el = nodeend[i];
      w = $(el).getStyle('width').split('px');
      $(el).setStyle('width', ((w[0]/2))+'px');
    }
    for(i=0;i<nodewrap.length;i++) {
      var el = nodewrap[i];
      w = $(el).getStyle('width').split('px');
      $(el).setStyle('width', ((w[0]/2))+'px');
    }
  } else if (z == 'big') {
    newClass = 'node';
    oldClass = 'nodeSmall';

    for(i=0;i<nodestart.length;i++) {
      var el = nodestart[i];
      w = $(el).getStyle('width').split('px');
      $(el).setStyle('width', (w[0]*2)+'px');
    }
    for(i=0;i<nodeend.length;i++) {
      var el = nodeend[i];
      w = $(el).getStyle('width').split('px');
      $(el).setStyle('width', (w[0]*2)+'px');
    }
    for(i=0;i<nodewrap.length;i++) {
      var el = nodewrap[i];
      w = $(el).getStyle('width').split('px');
      $(el).setStyle('width', (w[0]*2)+'px');
    }
  }

  var nodes = $$('.'+oldClass);    
  for(i=0;i<nodes.length;i++) {
    var node = nodes[i];
    $(node).addClass(newClass);
    $(node).removeClass(oldClass);
  }

}


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
  var elems = $('mainContent').getElements('.mandatory');
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

  if ($('child_'+parent_level).getStyle('display') == 'none') {
    node_toggle(parent_level);
  }

  // var child = $('nb_child_'+parent_level).value;
  // var n = parseInt(child) + 1;
  var n = 1;
  var new_child_id = parent_level+'.' + n;
  while($(new_child_id)) {
    n++;
    new_child_id = parent_level+'.' + n;
  }
  var container = $('child_'+parent_level);
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
  }).setProperty('id', 'trash_'+new_child_id).adopt(new Element('img').setProperty('src', '$ico_delete'));

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
    setProperties({'type':'text', 'name':'tf_ogroup['+new_child_id+']', 'id': 'tf_ogroup['+new_child_id+']', 'size':'18', 'maxlength':'32', 'value':''}).
    addClass('mandatory');

  // parent id hidden 
  var parent_id = new Element('input').
    setProperties({'type':'hidden', 'id':'tf_parent_level['+new_child_id+']', 'name':'tf_parent_level['+new_child_id+']', 'value':parent_level});
 
  // entity div
  var sel_ent = new Element('div').
    setProperty('id', 'sel_ent'+new_child_id);

  // nb_child div hidden
  var nb_child_div = new Element('input').
    setProperties({'type':'hidden', 'name':'nb_child_'+new_child_id, 'id':'nb_child_'+new_child_id, 'value':'0'});

  // Child div
  var child_div = new Element('div').setProperty('id', 'child_'+new_child_id);

  row.adopt(wrap.
    adopt(child_name).
    adopt(delete_button).
    adopt(parent_id).
    adopt(nb_child_div).
    adopt(new Element('br')).adopt(ico_user).adopt(user_input).
    adopt(new Element('br')).adopt(ico_group).adopt(group_input).
    adopt(sel_ent).
    adopt(div_buttons.adopt(child_button))
  ).adopt(child_div);

  container.adopt(row);

  // Autocomplete fields  
  new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_ent'+new_child_id, 'user'+new_child_id, {defaultText:'$user_text'})
  new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_ent'+new_child_id, 'group'+new_child_id, {defaultText:'$user_text'})

  if ($('nb_child_'+parent_level).value < 1) {
    // Add toggle icon on parent node
    var img_toggle = new Element('a').addEvent('click', function() {
      node_toggle(parent_level);
    }).adopt(new Element('img').setProperties({'id':'img_'+parent_level, 'src':obm.vars.images.minus}).setStyle('float','right'));
    img_toggle.injectBefore($('tf_ogroup['+parent_level+']'));
  }

  // Increase nb child
  $('nb_child_'+parent_level).value ++;

  // Give focus on last entity created
  $('tf_ogroup['+new_child_id+']').focus();
}


///////////////////////////////////////////////////////////////////////////////
// remove an ogroup + its child 
///////////////////////////////////////////////////////////////////////////////
function del_entity(level) {
  if(confirm(\"$l_js_delete_node\")) {
    var parent_level = $('tf_parent_level['+level+']').value;
    var target = window.document;
    var container = target.getElementById(level);
    while (container.firstChild) {
      container.removeChild(container.firstChild);
    }
    container.destroy();

    $('nb_child_'+parent_level).value--;
    if ( $('nb_child_'+parent_level).value == 0) {
      $('img_'+parent_level).dispose();
    }

  }
}

function confirm_del() {
  return confirm('$l_js_delete_chart');
}

";
?>
