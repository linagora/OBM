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
function valid_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function valid_mail() {
  if (confirm (\"$l_mail_confirm\"))
    return true;
  else
    return false;
}

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

function check_form_mail(form) {
  // NAME :  Contact name is not empty
  if (trim(form.tf_name_sender.value) == \"\") {
    alert (\"$l_name_empty\");
    return false;
  }

  // E-MAIL : Check that e-mail is not empty
  if (trim(form.tf_mail_sender.value) == \"\") {
    alert (\"$l_mail_empty\");
    return false;
  }


  // Check the email
  mail_sender = form.tf_mail_sender.value;
  if ( (mail_sender != \"\") && (check_email(mail_sender) == null)) {
    alert (\"$l_mail_false\");
    return false;
  }

  // Check the email rcpt
  mail_rcpt = form.tf_mail_rcpt.value;
  if(form.tf_mail_sender.value == form.tf_mail_rcpt.value) {
    alert(\"$l_mail_same\") ;
    return false ;
  } else { 
    if ( (mail_rcpt != \"\") && (check_email(mail_rcpt) == null)) {
      alert (\"$l_mail_rcpt_false\");
      return false;
    }
  }

  //Check if the subject is not empty
  if (trim(form.tf_mail_subject.value) == \"\") {
   alert(\"$l_subject_empty\") ;
   return false ;
  }

  //Check if the body is not empty
  if (trim(form.ta_mail_body.value) == \"\") {
   alert(\"$l_body_empty\") ;
   return false ;
  }

  if (confirm (\"$l_confirm_send\"))
    return true;
  else
    return false;

}
</SCRIPT>" ;




