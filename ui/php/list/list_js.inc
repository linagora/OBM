<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : maillist_js.inc                                             //
//     - Desc  : Maillist javascript functions File                          //
// 200-16-02 Vincent MARGUERIT : Last Update 2000-16-02                      //
///////////////////////////////////////////////////////////////////////////////

echo "
<SCRIPT LANGUAGE=\"JavaScript\">" ;

require("$obminclude//javascript/check_js.inc");

echo "

function check_name_list(form) {
  name_list = form.tf_name_list.value ;
  subject_list = form.tf_subject_list.value ;


  if (trim(name_list) == \"\") {
     alert(\"$l_name_list\") ;
     return false ;
  }

  if ( (name_list != \"\") && (check_list(name_list) == null)) {
    alert(\"$l_name_list_nomatch\");
    return false;
  }

  if (trim(subject_list) == \"\") {
     alert(\"$l_subject_list_empty\") ;
     return false ;
  }
  return true ;
}

</SCRIPT>" ;




