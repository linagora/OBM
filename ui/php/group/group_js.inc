<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : group_js.inc                                                 //
//     - Desc : Group javascript functions File                              //
// 2003-08-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script language=\"JavaScript\">";

require("obminclude/javascript/check_js.inc");

$extra_js .= "

function fill_ext_form(int_form) {
   size = int_form.length;
   ext_field = window.opener.document.$ext_target;
   for(i=0; i <size ; i++) {
     if(int_form.elements[i].type == 'checkbox'){
       if(int_form.elements[i].checked == true) {
	 ext_size = ext_field.length;
	 for(j=0; j< ext_size; j++) {
	   if('cb_g' + ext_field.options[j].value == int_form.elements[i].name) {
	     window.opener.document.$ext_target.options[j].selected =true;
	   }
	 }
       }
     }
   }
}
</script>" ;




