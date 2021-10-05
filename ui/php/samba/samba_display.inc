<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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

?>
