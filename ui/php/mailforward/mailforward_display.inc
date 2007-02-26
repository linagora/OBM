<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailforward_display.inc                                      //
//     - Desc : Mail forwarding Display functions File                       //
// 2002-09-01 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// HTML Display Mail Forwarding Consultation
// Parameters:
//   - $usr_q : User mail forwarding database result
///////////////////////////////////////////////////////////////////////////////
function html_forward_consult($usr_q) {
  global $l_mailforward, $l_internal_email;
  global $l_nomade_not_allowed,$l_local_copy;
  global $l_email_nomade, $l_enable, $l_disable, $l_forbid, $l_state;
  global $obm, $c_yes, $c_no;

  $id = $usr_q->f("userobm_id");
  $email = $usr_q->f("userobm_email");
  $email_nomade = $usr_q->f("userobm_email_nomade");
  $nomade_perms = $usr_q->f("userobm_nomade_perms");
  $nomade_enable = $usr_q->f("userobm_nomade_enable");
  $nomade_local_copy = $usr_q->f("userobm_nomade_local_copy");    

  print_r($obm);
  $domain = $obm["domain_name"];

  if (! $nomade_perms) {
    $dis_nomade = "<td class=\"detailLabel\">$l_nomade_not_allowed</td>";
  } else {
    if ($nomade_enable) $state = $l_enable;
    else $state = $l_disable;
    if ($nomade_local_copy) $state_local = $l_enable;
    else $state_local = $l_disable;
 
    $dis_nomade = "
    <tr>
      <th>$l_state</th>
      <td>$state</td>
    </tr>
    <tr>
      <th>$l_email_nomade</th>
      <td>$email_nomade</td>
    </tr>
    <tr>      
      <th>$l_local_copy</th>    
      <td>$state_local</td>
    </tr>";
  }

  $block = "
    <div class=\"detail extra\">
    <h1>$l_mailforward</h1>
    <table>
    <tr>
      <th>$l_internal_email</th>
      <td>$email (@$obm[domain_name])</td>
    </tr>
    $dis_nomade
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mail Forwarding Form
// Parameters:
//   - $usr_q   : User mail forwarding database result
//   - $forward : default values or updated values (if error)
//   - $field   : [optionnal] field in error
///////////////////////////////////////////////////////////////////////////////
function html_forward_form($usr_q, $forward, $field="") {
  global $l_mailforward, $l_internal_email, $l_nomade_not_allowed;
  global $l_email_nomade, $l_enable,$l_local_copy;
  global $obm, $action, $c_yes, $c_no, $l_update;

  $id = $usr_q->f("userobm_id");
  $email = $usr_q->f("userobm_email");
  $nomade_perms = $usr_q->f("userobm_nomade_perms");

  $domain = $obm["domain_name"];
  
  // if update mode and first time value are taken from database
  if ($action == "detailupdate") {
    $email_nomade = $usr_q->f("userobm_email_nomade");
    $nomade_enable = $usr_q->f("userobm_nomade_enable");
    $nomade_local_copy = $usr_q->f("userobm_nomade_local_copy");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($forward["email_nomade"])) { $email_nomade = $forward["email_nomade"]; }
  if (isset($forward["nomade_enable"])) { $nomade_enable = $forward["nomade_enable"]; }
  if (isset($forward["nomade_local_copy"])) { $nomade_local_copy = $forward["nomade_local_copy"]; }

  // Mark the error field
  if ($field != "") {
    $class[$field] = "error";
  }

  // Nomad enable chekbox checked value
  if ($nomade_enable) $nomade_enable_c = " checked";
  if ($nomade_local_copy) $nomade_local_copy_c = " checked";
  if (! $nomade_perms) {
    $block = display_ok_msg($l_nomade_not_allowed);
    return $block;
  } else {
    if ($nomade_enable) $state = $l_enable;
    else $state = $l_disable;

    $dis_nomade = "
    <tr>
      <th class=\"$class[nomade_enable]\">$l_enable</th>
      <td><input name=\"cb_nomade_enable\" type=\"checkbox\" value=\"1\" $nomade_enable_c></td>
    </tr>
    <tr>
      <th class=\"$class[email_nomade]\">$l_email_nomade</th>
      <td><input name=\"tf_email_nomade\" maxlength=\"64\" size=\"32\" value=\"$email_nomade\"></td>
    </tr>";
  }


  $block = "
    <form method=\"post\" name=\"form_forward\"
      onSubmit=\"if (check_user(this)) return true; else return false;\"
      action=\"" . url_prepare("mailforward_index.php") . "\">

    <fieldset class=\"detail extra\">
    <legend>$l_mailforward</legend>

    <table>
    <tr>
      <th>$l_internal_email</th>
      <td>$email (@$domain)</td>
    </tr>
    $dis_nomade      
    <tr>
      <th class=\"$class[nomade_local_copy]\">$l_local_copy</th>
      <td><input name=\"cb_nomade_local_copy\" type=\"checkbox\" value=\"1\" $nomade_local_copy_c /></td>
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
