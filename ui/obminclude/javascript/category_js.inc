<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : JavaScript Embedded in PHP                               //
//     - File     : check_js.inc                                             //
// 2005-12-01 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// JAVASCRIPT functions for categories                                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// TODO : Ce fichier devrait etre appellé limite en src.

$extra_js .= "
///////////////////////////////////////////////////////////////////////////////
// Check if the Category creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_category(label,category) {
   if (label.value != \"\") {
      return true;
   } else {
      msg = category+' : $l_c_j_create';
      alert(msg);
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Fill the content of the table line into the update form
///////////////////////////////////////////////////////////////////////////////
function updateLine(id,category) {
  form = \$('form_' + category + '_update');
  for(i=0;i<form.elements.length;i++) {
    elem = $(form.elements[i].name + '_' + id);
    console.log(elem);
    if(elem) {
      form.elements[i].value = elem.get('inputValue');
    }
  }
  form.submit();
  return false;
}
";
</script>
