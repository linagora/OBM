<script language="php">

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($ext_widget != "") || ($ext_widget_text != "")) {
  $extra_js .= "

  function check_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_company\");
      return false;
    } else {
      window.opener.document.$ext_widget.value=valeur;
      window.opener.document.$ext_widget_text.value=text;
      window.close();
      return true;
    }
  }";
}

</script>
