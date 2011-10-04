<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : host_js.inc                                                 //
//     - Desc  : Host javascript functions File                              //
// 2007-02-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
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
function on_backup_ftp_click() {
  if ($('cb_backup_ftp').checked) {
    $('row_backup_ftp_login').style.display = '';
    $('row_backup_ftp_password').style.display = '';
    $('row_backup_ftp_root').style.display = '';
  } else {
    $('row_backup_ftp_login').style.display = 'none';
    $('row_backup_ftp_password').style.display = 'none';
    $('row_backup_ftp_root').style.display = 'none';
  }
}
";

</script>
