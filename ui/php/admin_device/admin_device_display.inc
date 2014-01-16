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



$fieldnames['user'] = $l_user;
$fieldnames['identifier'] = $l_mobile_identifier;
$fieldnames['type'] = $l_type;
$fieldnames['first_sync'] = $l_first_sync;
$fieldnames['last_sync'] = $l_last_sync;
$fieldnames['partnership'] = $l_partnership;


/**
 * Display device dataset fields
 */
function dis_data_admin_device(&$OD, $fieldname) {

  // Format first_sync date
  if ($fieldname == 'first_sync' && $OD->data_set->f('first_sync')) {
   $res['name'] = new Of_Date($OD->data_set->f('first_sync'), 'GMT');
  }

  // Format last_sync date
  if ($fieldname == 'last_sync' && $OD->data_set->f('last_sync')) {
   $res['name'] = new Of_Date($OD->data_set->f('last_sync'), 'GMT');
  }

  // Partnership icon
  if ($fieldname == 'partnership') {
    $id = $OD->data_set->f('id');
    if ($OD->data_set->f('partnership') == '') {
      $res['name'] = "<img id='mobile_$id' src='$GLOBALS[ico_unsync]' onclick='obm.userMobileDeviceManager.togglePartnership($id);'/>";
    } else {
      $res['name'] = "<img id='mobile_$id' src='$GLOBALS[ico_sync]' onclick='obm.userMobileDeviceManager.togglePartnership($id);'/>";
    }
  }

  return $res;
}


/**
 * HTML Search form 
 */
function html_admin_device_search_form($device) {

  $user = stripslashes($device['user']); 
  $type = $device['type']; 
  $partnership = $device['partnership'];

  $type_q = run_query_admin_device_get_type();
  $block_type = "<select name='sel_type' id='sel_type'>
   <option value='$GLOBALS[c_none]'>$GLOBALS[l_undef]</option>";
  while($type_q->next_record()) {
    $t = $type_q->f('type');
    $selected = "";
    if ($type == $t) $selected = "selected=\"selected\"";
    $block_type .= "<option value='$t' $selected>$t</option>";
  }
  $block_type .= "</select>";

  $sel[$partnership] = "selected=\"selected\"";
  $block_partnership = "<select id='sel_partnership' name='sel_partnership'>
    <option value='$GLOBALS[c_all]' $sel[_ALL_]>$GLOBALS[l_all]</option>
    <option value='$GLOBALS[c_yes]' $sel[Yes]>$GLOBALS[l_yes]</option>
    <option value='$GLOBALS[c_no]' $sel[No]>$GLOBALS[l_no]</option>
  </select>";

  $block = "<form class=\"search\" method=\"get\" name=\"f_search\" 
    action=\"admin_device_index.php\">
    <label>
      $GLOBALS[l_user]<br />
      <input type='text' name='tf_user' size='12' value='$user' /> 
    </label> 
    <label>
      $GLOBALS[l_partnership]<br />
      $block_partnership
    </label> 
    <label>
      $GLOBALS[l_type]<br />
      $block_type
    </label> 
    <label>&nbsp;<br />
      <input type='hidden' name='action' value='search' />
      <input type='submit' value='$GLOBALS[l_search]' />
    </label>
  </form>";


  return $block;
}


/**
 * Prepare result list 
 */
function dis_admin_device_search_list($device) {
  global $obm, $display,$perm;

  $is_superman = 'obm.vars.consts.isSuperman='.($perm->is_superman('user')==true?1:0).';';
  $perms_error = 'obm.vars.labels.permsError="'.phpStringToJsString($GLOBALS['l_error_permission']).'";';
  $update_ok = 'obm.vars.labels.updateOk="'.phpStringToJsString($GLOBALS['l_update_ok']).'";';
  $update_error = 'obm.vars.labels.updateError="'.phpStringToJsString($GLOBALS['l_update_error']).'";';
  $prefs = get_display_pref($obm['uid'], 'admin_device');
	$devices_q = run_query_admin_device_search($device);
  $nb_devices = $devices_q->num_rows_total();
  if ($nb_devices == 0) {
    $display['msg'] .= display_warn_msg($GLOBALS['l_no_found']);
  } else {
    $display['msg'] .= display_info_msg("$nb_devices $GLOBALS[l_found]");
    $block = html_admin_device_search_list($device, $prefs, $devices_q);
    $devices_q->seek(0);
    while($devices_q->next_record()) {
      $device_id = $devices_q->f('id');
      $device_owner = $devices_q->f('owner');
      $userobm_delegation = $devices_q->f('userobm_delegation');
      $delegation = of_delegation_check_update_rights($device, $userobm_delegation);
      $perms_device_id = $devices_q->f('partnership');
      $last_sync = $GLOBALS['l_undef'];
      if ($devices_q->f('last_sync')) {
        $last_sync = new Of_Date($devices_q->f('last_sync'), 'GMT');
      }
      $first_sync = $GLOBALS['l_undef'];
      if ($devices_q->f('first_sync')) {
        $first_sync = new Of_Date($devices_q->f('first_sync'), 'GMT');
      }
      $userMobileDeviceManager .= "obm.userMobileDeviceManager.addDevice('$device_id', '$device_owner', '$first_sync', '$last_sync', '$perms_device_id', '$delegation');";
    }
    $block .= "
      <script type='text/javascript'>
        obm.vars.images.sync = '$GLOBALS[ico_sync]';
        obm.vars.images.desync = '$GLOBALS[ico_unsync]';
        obm.userMobileDeviceManager = new Obm.UserMobileDeviceManager();
        $userMobileDeviceManager
        $is_superman
        $perms_error
        $update_ok
        $update_error
      </script>
    ";
  }

  return $block;


}


/**
 * HTML Result list
 */
function html_admin_device_search_list($device, $prefs, $devices_q) {

  $url = url_prepare("admin_device_index.php?action=search&amp;tf_user=$device[user]&amp;sel_type=$device[type]&amp;sel_partnership=$device[partnership]");

  $dis_device = new OBM_DISPLAY('DATA', $prefs, 'admin_device');
  $dis_device->data_set = $devices_q;
  $dis_device->data_url = $url;
  $dis_device->data_header = 'both';

  // --- HTML Template --------------------------------------------------------
  $block = $dis_device->display('dis_data_admin_device');

  return $block;

}
?>
