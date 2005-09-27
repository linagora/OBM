<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : resource_js.inc                                             //
//     - Desc  : Resource javascript functions File                          //
// 2005-08-01 Aliacom - Florent Goalabre                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget is not defined
if ($ext_widget != "") {
  $extra_js .= "

function fill_ext_form(int_form) {
   size = int_form.length;
   target = window.opener.document;
   ext_field = window.opener.document.$ext_widget;
   ext_form = window.opener.document.frm_sel_resource;
   for(i=0; i <size ; i++) {
     if(int_form.elements[i].type == 'checkbox'){
       if(int_form.elements[i].checked == true) {
	 ext_size = ext_field.length;
         elemid = '$ext_element'+int_form.elements[i].name;
 	 elemid = 'data'+int_form.elements[i].name.substring(4);
	 span = get_elem(window.document,elemid);
         j= ext_size; is_present = false;
	 for(j=0; j< ext_size; j++) {
	   if(ext_field.options[j].value == int_form.elements[i].name) {
             is_present = true;
	   }
	 }
         if (! is_present) {
	     ext_field.options[j] = new Option(span.innerHTML,int_form.elements[i].name.substring(4));
             field = target.createElement('input');
             field.type = 'hidden';
             field.name = 'hd_resource_store[]';
             field.value = int_form.elements[i].name.substring(4);
             ext_form.appendChild(field);
         }
       }
     }
   }
   window.opener.document.frm_sel_resource.submit();
}
";
}
if ($ext_element != "") {
  $extra_js .= "
function get_elem(doc, name ) {
  return doc.getElementById(name);
}  

function fill_ext_form2(int_form) {
  size = int_form.length;
  for(i=0; i <size; i++) {
    if(int_form.elements[i].type == 'checkbox'){
      if(int_form.elements[i].checked == true) {
        elemid = '$ext_element'+int_form.elements[i].name;
        if(!get_elem(window.opener.document, elemid )) {
 	  elemid = 'data'+int_form.elements[i].name;
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


$extra_js .= "
function valider_suppression() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function check_resource(form) {
  // MANDATORY : Check that name is not empty
  if (trim(form.tf_name.value) == \"\") {
    alert (\"$l_fill_name\");
    return false;
  }

  // Check the phone number
  lqty = form.tf_qty.value;
  if ((lqty != \"\") && (check_number(lqty) == null)) {
    alert (\"$l_j_check_number\");
    return false;
  }

  return true;
}

";

