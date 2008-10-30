<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : mailforward_js.inc                                          //
//     - Desc  : Mailforward javascript functions File                       //
// 2008-10-30 Vincent Bernard                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: mailforward_js.inc 2865 2008-04-09 14:27:51Z mehdi $
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($params["ext_widget"] != "") || ($params["ext_widget_text"] != "")) {
  $extra_js .= "

  function check_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_company\");
      return false;
    } else {
      window.opener.document.$params[ext_widget].value=valeur;
      window.opener.document.$params[ext_widget_text].value=text;
      window.close();
      return true;
    }
  }";
}

$extra_js .= "

// Check form data for mailforward
function check_mailforward(form) {
	var errMsg = '';
	var ret = true;
	
	// Email nomade must be filled if nomade activated
	if (form.cb_nomade_enable.checked && form.tf_email_nomade.value == '') {
		errMsg += '$l_j_err_email\\n';
		ret = false;
	}

	// User nomade local copy only if autorized and activated
	if (!form.cb_nomade_enable.checked && form.cb_nomade_local_copy.checked) {
		errMsg += '$l_j_err_nomade_local_copy';
		ret = false;
	}

	if (!ret) {
		alert(errMsg);
	}
	
	return ret;
}
";  


</script>