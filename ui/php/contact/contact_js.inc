<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

echo "
<script type=\"text/javascript\">
<!--
";


require("$obminclude/javascript/check_js.inc");

echo "
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
//  if (lphone == \"\") {
//    alert (\"$l_fill_phone\");
//    return false;
//  }
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


// -->
</script>
";
