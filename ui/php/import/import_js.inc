<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : import_js.inc                                                //
//     - Desc : Import javascript functions File                             //
// 2004-01-16 - Aliacom - Pierre Baudracco                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script language=\"JavaScript\">" ;

require("$obminclude//javascript/check_js.inc");

$extra_js .= "
function check_import_name(iname) {
  // allow only valid import name
  return iname.match($js_regexp_import_name) ;
}

function check_import_form(form) {
  name = form.tf_name.value ;

  if (trim(name) == \"\") {
     alert(\"$l_fill_name\") ;
     return false ;
  }

  if ( (name != \"\") && (check_import_name(name) == null)) {
    alert(\"$l_name_import_nomatch\");
    return false;
  }

  return true ;
}

</script>" ;
