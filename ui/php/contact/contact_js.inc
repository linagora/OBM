<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script type=\"text/javascript\">
<!--
";


require("$obminclude/javascript/check_js.inc");

$extra_js .= "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function check_contact(form) {
  // MANDATORY : Check that Contact name is not empty
  if (trim(form.tf_lname.value) == \"\") {
    alert (\"$l_fill_lastname\");
    return false;
  }

  // MANDATORY : Check the phone number
  lphone = form.tf_phone.value;

  if ((lphone != \"\") && (check_phone(lphone) == null)) {
    alert (\"$l_j_check_phone\");
    return false;
  }

  // Check the Mobile or personnal phone
  hphone = form.tf_hphone.value;
  if ((hphone != \"\") && (check_phone(hphone) == null)) {
    alert (\"$l_j_check_hphone\");
    return false;
  }
  mphone = form.tf_mphone.value;
  if ((mphone != \"\") && (check_phone(mphone) == null)) {
    alert (\"$l_j_check_mphone\");
    return false;
  }

  // Check the fax
  lfax = form.tf_fax.value;
  if ((lfax != \"\") && (check_fax(lfax) == null)) {
    alert (\"$l_j_check_fax\");
    return false;
  }

  // Check the email
  lemail = form.tf_email.value;
  if ((lemail != \"\") && (check_email(lemail) == null)) {
    alert (\"$l_j_check_email\");
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the function check or deletion is possible                       //
///////////////////////////////////////////////////////////////////////////////
function check_func_checkdel(sel)
{
   if (sel.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_func_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Category check or deletion is possible                       //
///////////////////////////////////////////////////////////////////////////////
function check_cat1_checkdel(form)
{
   if (form.sel_cat1.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_cat1_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Category check or deletion is possible                       //
///////////////////////////////////////////////////////////////////////////////
function check_cat2_checkdel(form)
{
   if (form.sel_cat2.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_cat2_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the function update is possible                                  //
///////////////////////////////////////////////////////////////////////////////
function check_func_upd(form, sel_del_func)
{
  func_pos = sel_del_func.selectedIndex;
  if ((func_pos >= 0) && (form.tf_func.value != \"\")) {
    form.sel_func.value=sel_del_func.options[func_pos].value;
    return true;
  } else {
    alert(\"$l_j_func_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the function creation is possible                                //
///////////////////////////////////////////////////////////////////////////////
function check_func_new(form)
{
   if (form.tf_func.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_func_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Category creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_cat1_new(form)
{
   if (form.tf_cat1.value != \"\")
      return true;
   else {
      alert(\"$l_j_cat1_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Category creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_cat2_new(form)
{
   if (form.tf_cat2.value != \"\")
      return true;
   else {
      alert(\"$l_j_cat2_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the kind check or deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////
function check_kind_checkdel(sel)
{
  if (sel.selectedIndex >= 0) {
    return true;
  } else {
    alert(\"$l_j_kind_delete\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Category update is possible
///////////////////////////////////////////////////////////////////////////////
function check_cat1_upd(form,formd)
{
  cat1_pos = formd.sel_cat1.selectedIndex;
  if ((cat1_pos >= 0) && (form.tf_cat1.value != \"\")) {
    form.sel_cat1.value=formd.sel_cat1.options[cat1_pos].value;
    return true;
   } else {
     alert(\"$l_j_cat1_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the Category update is possible
///////////////////////////////////////////////////////////////////////////////
function check_cat2_upd(form,formd)
{
  cat2_pos = formd.sel_cat2.selectedIndex;
  if ((cat2_pos >= 0) && (form.tf_cat2.value != \"\")) {
    form.sel_cat2.value=formd.sel_cat2.options[cat2_pos].value;
    return true;
   } else {
     alert(\"$l_j_cat2_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the kind update is possible
///////////////////////////////////////////////////////////////////////////////
function check_kind_upd(form, sel_del)
{
  kind_pos = sel_del.selectedIndex;
  if ((kind_pos >= 0) && (form.tf_label.value != \"\")) {
    form.sel_kind.value=sel_del.options[kind_pos].value;
    return true;
  } else {
    alert(\"$l_j_kind_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the kind creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_kind_new(form)
{
   if ( (trim(form.tf_lang.value) == \"\")
        || (trim(form.tf_label.value) == \"\")
        || (trim(form.tf_header.value) == \"\") ) {
      alert(\"$l_j_kind_create\");
      return false;
   } else {
      return true;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Fill the Kind's mini label select.
///////////////////////////////////////////////////////////////////////////////
var lang_array;

function fill_select_label(source) 
{
  destination = source.form.sel_kind_label;
  kind = source.form.sel_kind;
  destIndex = source.options[source.selectedIndex].value;
  for (i = destination.options.length-1; i >= 0; i--) {
    destination.options[i] = null; 
  }  
  for (i = kind.options.length-1; i >= 0; i--) {
    kind.options[i] = null; 
  }
  kind.options[0]=new Option('$l_none');
  destination.options[0]=new Option('$l_none');
  i=1;
  for (var destValue in lang_array[destIndex]) {
    destination.options[i]= new Option(destValue,destValue);
    i++;
  }
  destination.selectedIndex=0;
  kind.selectedIndex=0;
}


///////////////////////////////////////////////////////////////////////////////
//  Add an element line in the list form                                   //
///////////////////////////////////////////////////////////////////////////////
function add_element(fieldName,fieldValue,fieldLabel) {
  target = window.opener.document;
  container = get_elem(target, '$ext_target' );
  row = target.createElement('div');
  row.id = '$ext_target'+fieldValue;
  row.className = 'elementRow';
  field = target.createElement('input');
  field.type = 'hidden';
  field.name = fieldName;
  field.value = fieldValue;
  link = target.createElement('a');
  link.href = 'javascript: remove_element(\'$ext_target'+fieldValue+'\',\'$ext_target\');';
  link.onclick = '';
  image = target.createElement('img');
  image.src = '".C_IMAGE_PATH."/$set_theme/$ico_crow';
  link.appendChild(image);
  content = target.createTextNode(fieldLabel);
  row.appendChild(link);
  row.appendChild(content);
  row.appendChild(field);
  container.appendChild(row);
} 
  
///////////////////////////////////////////////////////////////////////////////
//  Add an element line in the list form                                   //
///////////////////////////////////////////////////////////////////////////////
function remove_element(elemId,parentId) {
  target = window.document;
  content = target.getElementById(elemId);
  container = target.getElementById(parentId)
  container.removeChild(content); 
} 
  
  

///////////////////////////////////////////////////////////////////////////////
// Fill the Kind's header select.
///////////////////////////////////////////////////////////////////////////////
function fill_select_header(source) 
{
  destination = source.form.sel_kind;
  lang = source.form.sel_kind_lang;
  destIndex = source.options[source.selectedIndex].value;
  for (i = destination.options.length-1; i >= 0; i--) {
    destination.options[i] = null; 
  }  
  lang.options[0]=new Option('$l_none');
  destination.options[0]=new Option('$l_none');
  i=1;
  for (var destValue in lang_array[lang.options[lang.selectedIndex].value][destIndex]) {
    destination.options[i]= new Option(destValue,lang_array[lang.options[lang.selectedIndex].value][destIndex][destValue]);
    i++;
  }
  destination.selectedIndex=0;
  
}
";

if (isset($ext_target) && $ext_target!="") {
  $extra_js .= "
  function get_elem(doc, name ) {
    return doc.getElementById(name);
  }  

  function fill_ext_form(repo_path) {
    ext_field = window.opener.document.$ext_target;
    ext_field.value = repo_path;
    window.close();
  } 
  
  function fill_ext_form2(int_form) {
   size = int_form.length;
   for(i=0; i <size; i++) {
     if(int_form.elements[i].type == 'checkbox'){
       if(int_form.elements[i].checked == true) {
	 elemid = '$ext_target'+int_form.elements[i].name;
	 if(!get_elem(window.opener.document, elemid )) {
	   elemid = 'data'+int_form.elements[i].name;
	   alert(elemid);
	   span = get_elem(window.document,elemid);
	   add_element('$ext_target' + '[]',int_form.elements[i].name, span.innerHTML);
	 }
       }
     }
   }
 }

";
}
$extra_js .= "
// -->
</script>
";
