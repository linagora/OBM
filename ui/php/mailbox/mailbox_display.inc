<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailbox_display.php                                          //
//     - Desc : Mailbox Display File                                         //
// 2007-03-28 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

/**
 * dis_mailbox_right_dis_admin
 * Display mailbox rights admin form
 *
 * @param mixed $id mailbox id to admin
 * @return mixed xhtml display
 */
function dis_mailbox_right_dis_admin($id="") {
  global $obm, $display, $l_mailbox;

  if ($id == "") {
    $id = $obm["uid"];
  }
  $infos = get_user_info($id);
  $name = $infos["firstname"] . " " . $infos["lastname"];
  $display["title"] = "$obm[login] ($name)";
  $block = of_right_dis_admin("mailbox", $id);

  return $block;
}


</script>
