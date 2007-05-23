<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : of_select.inc                                                //
//     - Desc : OBM Framework Rich Select helpers functions                  //
// 2005-10-04 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


if (($params["ext_element"] != "")
    || (true)) {
  $extra_js .= "

///////////////////////////////////////////////////////////////////////////////
// Fill the of_select from checked checkboxes from a form result set
// Parameters:
//   - int_form : source form where to get values (eg: from popup windows)
//   - entity   : [optionnal] entity to select [default to $module]
///////////////////////////////////////////////////////////////////////////////
function of_select_fill_from_checkbox(int_form, entity) {
  
  if (typeof(entity) == 'undefined') {
    entity = '$module';
  }
  size = int_form.length;
  for (i=0; i <size; i++) {
    // For each checkbox of the form, if filled we send the value
    if (int_form.elements[i].type == 'checkbox') {
      if (int_form.elements[i].checked == true) {
        id = int_form.elements[i].name;
        div_id = '$params[ext_element]'+'-data-'+entity+'-'+id;
        // If the element not already present in the caller window
        if (! get_elem(window.opener.document, div_id )) {
 	  sel_id = 'data-'+entity+'-'+id;
          // Get the label associated to the id from the int_form row
          // hidden in an hidden span
	  span = get_elem(window.document, sel_id);
	  of_select_add_element(window.opener.document, '$params[ext_element]', sel_id, span.innerHTML);
        }
      }
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Add an element (div with img, name and hidden input) target given
// Parameters:
//   - element_name : parent element where to add the div, an input name []
//   - sel_id       : id to register in the input field value
//   - fieldLabel   : Label to display
///////////////////////////////////////////////////////////////////////////////
function of_select_add_element(target, element_name, sel_id, fieldLabel) {

  container = get_elem(target, element_name);

  // Create the div row
  row = target.createElement('div');
  row.id = element_name+sel_id;
  row.className = 'elementRow';

  // Create the input field
  field = target.createElement('input');
  field.type = 'hidden';
  field.name = element_name+'[]';
  field.value = sel_id;
  field.text = fieldLabel;

  // Create the delete link associated to the div
  link = target.createElement('a');
  link.href = 'javascript: remove_element(\''+element_name+sel_id+'\',\''+element_name+'\');';
  link.onclick = '';

  // Create the icon image
  image = target.createElement('img');
  image.src = '$ico_delete';
  link.appendChild(image);
  l_ritem= document.createTextNode(\"$l_ritem \");

  // Create the label
  content = target.createTextNode(fieldLabel);

  // Add these elements to the div row
  row.appendChild(link);
  row.appendChild(content);
  row.appendChild(field);

  // Add the div row to the container
  container.appendChild(row);
} 


///////////////////////////////////////////////////////////////////////////////
// Add an element (div with img, name and hidden input) target given
// Parameters:
//   - element_name : parent element where to add the div, an input name []
//   - sel_id       : id to register in the input field value
//   - fieldLabel   : Label to display
///////////////////////////////////////////////////////////////////////////////
function of_select_set_tree(target, element_name, value) {

  checkbox = get_elem(target, element_name+'_tree');

  checkbox.checked = value;
} 


///////////////////////////////////////////////////////////////////////////////
// Return the of_select element list in an array
// Returns:
// el[cb] = checkbox status, el[0]=item0 ([id],[text])
///////////////////////////////////////////////////////////////////////////////
function of_select_get_element_array(elementName) {

  target = window.document;
  cb = target.getElementById(elementName+'_tree');

  el = Array();
  try {
    el[cb] = cb.checked;
  } catch(e) {
    el[cb] = false;
  }

  container = target.getElementById(elementName);

  nb_child = 0;
  while (container.firstChild) {
    div = container.firstChild;
    while (div.firstChild) {
      if (div.firstChild.name == elementName+'[]') {
        el[nb_child] = new Array();
        el[nb_child]['id'] = div.firstChild.value;
        el[nb_child]['text'] = div.firstChild.text;
        nb_child++;
      }
      div.removeChild(div.firstChild);
    }
    container.removeChild(container.firstChild);
  }

  return el;
} 

";
}


//=============================================================================
// OF Select API
//=============================================================================

// Display functions ----------------------------------------------------------

///////////////////////////////////////////////////////////////////////////////
// Display: global select block 
// Parameters:
//   - $data_array : data array $data_array[id]=[label]
//   - $data_name  : data name to construct the fields
//   - $url        : url to access external select data
//   - $tree       : display the tree option
///////////////////////////////////////////////////////////////////////////////
function of_select_dis_search_select($data_array, $data_name, $url, $tree=false) {
  global  $ico_add, $ico_delete, $l_cat_tree;

  $l_name = "l_${data_name}"; 
  global $$l_name;

  $sel = _of_select_select($data_array, $data_name);

  if ($tree) {
    $dis_tree = "<input type=\"checkbox\" id=\"sel_${data_name}_tree\" name=\"cb_$data_name\" value=\"1\">$l_cat_tree</input>";
  }

  $block = "
    <div class=\"searchLabel\">${$l_name}
      <a href=\"javascript: return false;\"
      onclick=\"popup('$url','','360','240'); return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" /></a>
      &nbsp;$dis_tree&nbsp;
      <a href=\"javascript: return false;\"
        onclick=\"remove_all_element('sel_${data_name}');
        return false;\">
        <img src=\"$ico_delete\" alt=\"[Clear]\" /></a>
      <br />
      <div class=\"elementRow\" id=\"sel_$data_name\"><div class=\"elementRowEmpty\"></div>$sel</div>
    </div>";

  return $block;
}


//=============================================================================
// OF Select internal helper API
//=============================================================================


///////////////////////////////////////////////////////////////////////////////
// Display: category html select component
// Parameters:
//   - $data_array : data array $data_array[id]=[label]
//   - $data_name  : data name to construct the fields
///////////////////////////////////////////////////////////////////////////////
function _of_select_select($data_array, $data_name) {
  global  $ico_delete;

  if (is_array($data_array)) {
    foreach ($data_array as $id => $one_data) {
      $data_label = htmlentities($one_data["label"]);
      $data_id = "data-$data_name-$id";
      $div_id = "sel_$data_name-$data_id";
      $sel_data .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_$data_name');\"><img src=\"$ico_delete\"></a>
        $data_label
        <input value=\"$data_id\" name=\"sel_${data_name}[]\" type=\"hidden\" />
        </div>";
    }
  }

  return $sel_data;
}


?>
