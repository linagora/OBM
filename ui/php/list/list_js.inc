<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_js.inc                                                  //
//     - Desc : List javascript functions File                               //
// 200-16-02 Vincent MARGUERIT                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script language=\"JavaScript\">" ;

require("$obminclude//javascript/check_js.inc");

$extra_js .= "

function check_name_list(form) {
  name_list = form.tf_name.value ;
  subject_list = form.tf_subject.value ;


  if (trim(name_list) == \"\") {
     alert(\"$l_fill_name\") ;
     return false ;
  }

  if ( (name_list != \"\") && (check_list(name_list) == null)) {
    alert(\"$l_name_list_nomatch\");
    return false;
  }

  if (trim(subject_list) == \"\") {
     alert(\"$l_fill_subject\") ;
     return false ;
  }
  return true ;
}

</script>" ;
