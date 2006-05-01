<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : of_extmod.inc                                                //
//     - Desc : OBM Framework External modules call helpers functions        //
// 2005-10-04 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


if ($ext_element != "") {
  $extra_js .= "

///////////////////////////////////////////////////////////////////////////////
// Fill the source form with checked values from a result set
// Parameters:
//   - int_form : source form where to get values (eg: from popup windows)
//   - entity   : [optionnal] entity to select [default to $module]
///////////////////////////////////////////////////////////////////////////////
function of_extmod_fill_ext_element(int_form, entity) {
  
  if (typeof(entity) == 'undefined') {
    entity = '$module';
  }
  size = int_form.length;
  for (i=0; i <size; i++) {
    // For each checkbox of the form, if filled we send the value
    if (int_form.elements[i].type == 'checkbox') {
      if (int_form.elements[i].checked == true) {
        id = int_form.elements[i].name;
        div_id = '$ext_element'+'-data-'+entity+'-'+id;
        // If the element not already present in the caller window
        if (! get_elem(window.opener.document, div_id )) {
 	  sel_id = 'data-'+entity+'-'+id;
          // Get the label associated to the id from the int_form row
          // hidden in an hidden span
	  span = get_elem(window.document, sel_id);
	  of_extmod_add_element('$ext_element', sel_id, span.innerHTML);
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
function of_extmod_add_element(element_name, sel_id, fieldLabel) {
  target = window.opener.document;
  container = get_elem(target, '$ext_element' );

  // Create the div row
  row = target.createElement('div');
  row.id = '$ext_element-'+sel_id;
  row.className = 'elementRow';

  // Create the input field
  field = target.createElement('input');
  field.type = 'hidden';
  field.name = element_name+'[]';
  field.value = sel_id;

  // Create the link associated to the div
  link = target.createElement('a');
  link.href = 'javascript: remove_element(\'$ext_element-'+sel_id+'\',\'$ext_element\');';
//alert (link.href);
  link.onclick = '';

  // Create the icon image
  image = target.createElement('img');
  image.src = '".C_IMAGE_PATH."/$set_theme/$ico_delete';
  link.appendChild(image);

  // Create the label
  content = target.createTextNode(fieldLabel);

  // Add these elements to the div row
  row.appendChild(link);
  row.appendChild(content);
  row.appendChild(field);

  // Add the div row to the container
  container.appendChild(row);
} 

";
}


//=============================================================================
// OF Extmod API
//=============================================================================

// Display functions ----------------------------------------------------------

///////////////////////////////////////////////////////////////////////////////
// Display: extmod select block 
// Parameters:
//   - $data_array : data array $data_array[id]=[label]
//   - $data_name  : data name to construct the fields
//   - $url        : url to access external select data
///////////////////////////////////////////////////////////////////////////////
function of_extmod_dis_search_select($data_array, $data_name, $url) {
  global $set_theme, $ico_add, $ico_delete;

  $l_name = "l_${data_name}"; 
  global $$l_name;

  $sel = _of_extmod_select($data_array, $data_name);

  $block = "
    <div class=\"searchLabel\">${$l_name}
      <a href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=360,width=240,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add\" alt=\"[Add]\" /></a>
      &nbsp;
      <a href=\"javascript: return false;\"
        onclick=\"remove_all_element('sel_${data_name}');
        return false;\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\" alt=\"[Clear]\" /></a>
      <br />
      <div class=\"elementRow\" id=\"sel_$data_name\"><div class=\"elementRowEmpty\"></div>$sel</div>
    </div>";

  return $block;
}


//=============================================================================
// OF Extmod internal helper API
//=============================================================================


///////////////////////////////////////////////////////////////////////////////
// Display: category html select component
// Parameters:
//   - $data_array : data array $data_array[id]=[label]
//   - $data_name  : data name to construct the fields
///////////////////////////////////////////////////////////////////////////////
function _of_extmod_select($data_array, $data_name) {
  global $set_theme, $ico_delete;

  if (is_array($data_array)) {
    foreach ($data_array as $id => $one_data) {
      $data_label = htmlentities($one_data["label"]);
      $data_id = "data-$data_name-$id";
      $div_id = "sel_$data_name-$data_id";
      $sel_data .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_$data_name');\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\"></a>
        $data_label
        <input value=\"$data_id\" name=\"sel_${data_name}[]\" type=\"hidden\" />
        </div>";
    }
  }

  return $sel_data;
}


?>
