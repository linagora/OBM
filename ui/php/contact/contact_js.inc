<SCRIPT language="php">
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
// Check if the kind check or deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////
function check_kind_checkdel(form)
{
  if (form.sel_kind.selectedIndex >= 0)
  {
    return true;
  }
  else
  {
    alert(\"$l_j_kind_delete\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the kind update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_kind_upd(form,formd)
{
  kind_pos = formd.sel_kind.selectedIndex;
  if ((kind_pos >= 0) && (form.tf_kind.value != \"\")) {
    form.sel_kind.value=formd.sel_kind.options[kind_pos].value;
    return true;
  } else {
    alert(\"$l_j_kind_update\");
    return false;
  }
}


// -->
</script>
";
