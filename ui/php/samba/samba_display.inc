<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : samba_display.inc                                            //
//     - Desc : Samba Administration Display functions File                  //
// 2004-09-15 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display Samba Administration Consultation
///////////////////////////////////////////////////////////////////////////////
function dis_samba_consult() {

  $smb_i = get_samba_info();
  $block = html_samba_consult($smb_i);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Samba Administration Consultation
///////////////////////////////////////////////////////////////////////////////
function html_samba_consult(&$smb) {
  global $l_samba, $l_smb_domain, $l_smb_sid, $l_smb_pdc_name, $l_smb_profile;
  global $l_default, $l_smb_home_def, $l_smb_home_drive_def;

  $domain = $smb["domain"];
  $sid = $smb["sid"];
  $profile = $smb["profile"];

  $home_def = $smb["home"];
  $home_drive_def = $smb["drive"];
  if ($home_drive_def != "") {
    $home_drive_def .= ":";
  }

  $block .= "
  <div class=\"detail extra\">
  <h1>$l_samba</h1>

  <table>
  <tr>
    <th>$l_smb_domain</th>
    <td>$domain</td>
  </tr>
  <tr>
    <th>$l_smb_sid</th>
    <td>$sid</td>
  </tr>
  <tr>
    <th>$l_smb_profile</td>
    <td>$profile</td>
  </tr>
  </table>
  </div>

  <div class=\"detail extra\">
  <h1>$l_default</h1>

  <table>
  <tr>
    <th>$l_smb_home_def</th>
    <td>$home_def</td>
  </tr>
  <tr>
    <th>$l_smb_home_drive_def</th>
    <td>$home_drive_def</td>
  </tr>
  </table>
  </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Samba Administration Form
///////////////////////////////////////////////////////////////////////////////
function dis_samba_form($samba, $field="") {

  $smb_i = get_samba_info();
  $block .= html_samba_form($samba, $smb_i, $field);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Samba Form
// Parameters:
//   - $samba   : parameters in hash
//     kes used : all
//   - $field   : field in error
///////////////////////////////////////////////////////////////////////////////
function html_samba_form($samba, $smb, $field="") {
  global $action, $l_insert, $l_update;
  global $l_default, $l_smb_home_def, $l_smb_home_drive_def;

  // if detailupdate mode values are taken from database
  if ($action == "detailupdate") {
    $home = $smb["home"];
    $drive = $smb["drive"];
  } else {
    $home = stripslashes($samba["home"]);
    $drive = $samba["drive"];
  }

  if(isset($drive) && $drive != '') $val[$drive] = 'selected="selected"';
  for($i='D'; $i< 'Z';$i++) {
    $dis_drive_sel .= "<option ".$val["$i"]." value='$i'>$i:</option>";
  }
  $dis_drive_sel .= "<option ".$val["Z"]." value='Z'>Z:</option>";
  $dis_drive_sel = "<select style='width:4em;' name='drive'>$dis_drive_sel</select>";

  // Mark the error field
  if ($field != "") {
    $class[$field] = "error";
  }

  $block = "
    <form method=\"post\" name=\"form_samba\"
      onSubmit=\"if (check_samba(this)) return true; else return false;\"
      action=\"" . url_prepare("samba_index.php") . "\">

    <fieldset class=\"detail infos\">
    <legend>$l_default</legend>

    <table>
    <tr>
      <th class=\"$class[home]\">$l_smb_home_def</th>
      <td><input type=\"text\" name=\"tf_home\" size=\"64\" maxlength=\"255\" value=\"$home\" /></td>
    </tr>
    <tr>
      <th class=\"$class[drive]\">$l_smb_home_drive_def</th>
      <td>$dis_drive_sel</td>
    </tr>
    </table>
    </fieldset>";

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"param_host\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

  } else {
    $dis_button .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block .= "
    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>
    </form>";

  return $block;
}

</script>
