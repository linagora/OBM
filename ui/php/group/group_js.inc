<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : group_js.inc                                                 //
//     - Desc : Group javascript functions File                              //
// 2003-08-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


require("obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget is not defined
if ($ext_widget != "") {
  $extra_js .= "

function fill_ext_form(int_form) {
   size = int_form.length;
   ext_field = window.opener.document.$ext_widget;
   for(i=0; i <size ; i++) {
     if(int_form.elements[i].type == 'checkbox'){
       if(int_form.elements[i].checked == true) {
	 ext_size = ext_field.length;
	 for(j=0; j< ext_size; j++) {
	   if('cb_g' + ext_field.options[j].value == int_form.elements[i].name) {
	     window.opener.document.$ext_widget.options[j].selected =true;
	   }
	 }
       }
     }
   }
}";
}
if ($ext_element != "") {
  $extra_js .= "
function get_elem(doc, name ) {
  return doc.getElementById(name);
}  

function fill_ext_element(int_form) {
  size = int_form.length;
  for(i=0; i <size; i++) {
    if (int_form.elements[i].type == 'checkbox') {
      if (int_form.elements[i].checked == true) {
        elemid = '$ext_element'+int_form.elements[i].name;
        if (!get_elem(window.opener.document, elemid )) {
 	  elemid = 'data-group-'+int_form.elements[i].name;
	  span = get_elem(window.document,elemid);
	  add_element('$ext_element' + '[]',int_form.elements[i].name, span.innerHTML);
        }
      }
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
//  Add an element line in the list form                                     //
///////////////////////////////////////////////////////////////////////////////
function add_element(fieldName,fieldValue,fieldLabel) {
  target = window.opener.document;
  container = get_elem(target, '$ext_element' );
  row = target.createElement('div');
  row.id = '$ext_element'+fieldValue;
  row.className = 'elementRow';
  field = target.createElement('input');
  field.type = 'hidden';
  field.name = fieldName;
  field.value = fieldValue;
  link = target.createElement('a');
  link.href = 'javascript: remove_element(\'$ext_element'+fieldValue+'\',\'$ext_element\');';
  link.onclick = '';
  image = target.createElement('img');
  image.src = '".C_IMAGE_PATH."/$set_theme/$ico_delete';
  link.appendChild(image);
  content = target.createTextNode(fieldLabel);
  row.appendChild(link);
  row.appendChild(content);
  row.appendChild(field);
  container.appendChild(row);
} 
  
";
}
