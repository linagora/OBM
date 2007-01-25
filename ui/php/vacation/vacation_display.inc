<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : vacation_display.inc                                         //
//     - Desc : vacation display File                                        //
// 2007-01-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// HTML Display Vacation Consultation
// Parameters:
//   - $usr_q : User Vacation database result
///////////////////////////////////////////////////////////////////////////////
function html_vacation_consult($usr_q) {
  global $l_vacation, $l_vacation_enabled, $l_vacation_message;
  global $l_enabled, $l_disabled;

  $enabled = $usr_q->f("userobm_vacation_enable");
  if ($enabled == 1) {
    $status = $l_enabled;
  } else {
    $status = $l_disabled;
  }

  if ($usr_q->f("userobm_vacation_message") != "") {
    $message = nl2br(stripslashes( $usr_q->f("userobm_vacation_message") ));
  } else {
    $message = "---";
  }

  $block = "
  <div class=\"detail extra\">
  <h1>$l_vacation</h1>

  <table>
  <tr>
    <th>$l_vacation_enabled</th>
    <td>$status</td>
  </tr>
  <tr>
    <th>$l_vacation_message</th>
    <td>$message</td>
  </tr>
  </table>";
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Vacation Form
// Parameters:
//   - $usr_q : User Vacation database result
//   - $vacation : parameters hash
//   - $field : if set, field in error
///////////////////////////////////////////////////////////////////////////////
function html_vacation_form($usr_q, $vacation, $field="") {
  global $l_vacation, $l_enable_vacation, $l_vacation_message;
  global $l_update, $l_vacation_not_allowed;

  $action = $vacation["action"];

  // if update mode and first time value are taken from database
  if ($action == "detailupdate") {
    $enabled = $usr_q->f("userobm_vacation_enable");
    $message = stripslashes( $usr_q->f("userobm_vacation_message") );
    $mail_enable = $usr_q->f("userobm_mail_perms");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($vacation["enabled"])) { $enabled = $vacation["enabled"]; }
  if (isset($vacation["message"])) { $message = $vacation["message"]; }

  if (! $mail_enable) {
    return display_ok_msg($l_vacation_not_allowed);
  }

  // vacation enabled checkbox
  if ($enabled == 1) {
    $enabled_checked = " checked=\"checked\" " ;
    $message_enabled = "";
  }else {
    $enabled_checked = " " ;
    $message_enabled = " disabled=\"disabled\" ";
  }

  // error and css classes : 
  $enabled_css = $field == "enabled" ?  "detailError" : "detailLabel";
  $message_css = $field == "message" ?  "detailError" : "detailLabel";
  
  $block = "
<form method=\"POST\" name=\"form_vacation\" action=\"vacation_index.php\">

  <fieldset class=\"detail extra\">
  <legend>$l_vacation</legend>

  <table>
  <tr>
    <th>$l_enable_vacation</th>
    <td><input type=\"checkbox\" name=\"cba_enabled\" value=\"1\" $enabled_checked onchange=\"disable_message_editing(this.form, this)\"/></td>
  </tr>
  <tr>
    <th>$l_vacation_message</th>
    <td><textarea name=\"ta_message\" $message_enabled rows=\"10\" cols=\"72\">$message</textarea></td>
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"buttons\">
  <input type=\"hidden\" name=\"action\" value=\"update\">
  <input type=\"submit\" value=\"$l_update\">
  </fieldset>
  </form>";

  return $block;
}


</script>
