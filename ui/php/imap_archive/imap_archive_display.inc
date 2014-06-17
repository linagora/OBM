<?php
/******************************************************************************
Copyright (C) 2014 Linagora

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
// OBM - File : imap_archive_display.inc                                     //
//     - Desc : IMAP Archive configuration                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

include_once("$obminclude/global.inc");

///////////////////////////////////////////////////////////////////////////////
// Display the IMAP Archive form
///////////////////////////////////////////////////////////////////////////////

function dis_imap_archive_form($backup, $params, $configuration) {
  global $l_users, $l_validate;
  global $l_service_activating, $l_activating;
  global $l_scheduling;
  global $l_next_run, $l_excluded_users, $l_name, $l_delete, $l_remove_archive;
  global $l_mailing_feedback, $l_email_address;

  $user_id = $params['user_id'];
  $url = url_prepare('imap_archive_index.php');

  $repeatKind = $configuration->repeatKind;
  $dayOfWeek = $configuration->dayOfWeek;
  $dayOfMonth = $configuration->dayOfMonth;
  $dayOfYear = $configuration->dayOfYear;
  $enabled = $configuration->enabled;
  $hour = $configuration->hour;
  $minute = $configuration->minute;
  
  $disabled = ($enabled == 0) ? "disabled" : "";
  $block_repeat_kind = get_block_repeat_kind($repeatKind, $disabled);
  $block_weekly = get_block_weekly($repeatKind, $dayOfWeek, $disabled);
  $block_monthly = get_block_monthly($repeatKind, $dayOfMonth, $disabled);
  $block_yearly = get_block_yearly($repeatKind, $dayOfYear, $disabled);
  $block_hour = get_block_hour($hour, $disabled);
  $block_minute = get_block_minute($minute, $disabled);
  
  $block = "
    <form method='get' name='f_imap_archive' action='imap_archive_index.php'>

    <fieldset class='detail infos'>
      <legend>$l_service_activating</legend>
      <table>
        <tr>
          <th>$l_activating</th>
          <td><input id='activation' name='cb_enabled' type='checkbox' value='1' ".(($enabled)?"checked='checked'":"")." onclick='enableConfiguration(this.checked);'/></td>
        </tr>
      </table>
    </fieldset>
    <fieldset class='detail infos'>
      <legend>$l_scheduling</legend>
      <table>
        <tr>
          $block_repeat_kind
        </tr>
        <tr>
          $block_weekly
        </tr>
        <tr>
          $block_monthly
        </tr>
        <tr>
          $block_yearly
        </tr>
      </table>
      <br>
      <table>
        <tr>
          $block_hour
        </tr>
        <tr>
          $block_minute
        </tr>
      </table>
      <br>
      <table>
        <tr>
          <th>$l_next_run</th>
          <td>2014-04-18 22:30:00</td>
        </tr>
      </table>
    </fieldset>
    <fieldset class='detail infos'>
      <legend>$l_excluded_users</legend>
      <table>
        <th>$l_users</th>
        <td class='autocompleteField'>
          <input type='text' name='' value='' id='userSearch' autocomplete='off' />
          <input type='hidden' id='userSearchContainer' value='sel_user_id'/>
        </td>
        <table class='spreadSheet'>
          <thead>
            <tr>
              <td>$l_name</td>
              <td>$l_delete</td>
              <td>$l_remove_archive</td>
            </tr>
          </thead>
          <tbody>
            <tr id='user_1' class='user'>
              <td>admin0</td>
              <td>
                <img onclick='obm.imapArchiveManager.removeExcludedUser(1);' src='/images/themes/default/images/ico_trash.gif'></img>
              </td>
              <td>
                <img onclick='obm.imapArchiveManager.destroyExcludedUser(1);' src='/images/themes/default/images/ico_delete.gif'></img>
              </td>
            </tr>
          </tbody>
        <table>
      </table>
    </fieldset>
    <fieldset class='detail infos'>
      <legend>$l_mailing_feedback</legend>
      <table>
        <th>$l_email_address</th>
        <td class='autocompleteField'>
          <input type='text' name='' value='' id='email' autocomplete='off' />
        </td>
        <table class='spreadSheet'>
          <thead>
            <tr>
              <td>$l_email_address</td>
              <td></td>
            </tr>
          </thead>
          <tbody>
            <tr id='user_1' class='user'>
              <td>admin0@mydomain.org</td>
              <td>
                <img onclick='obm.imapArchiveManager.removeUser(1);' src='/images/themes/default/images/ico_trash.gif'></img>
              </td>
            </tr>
          </tbody>
        </table>
      </table>
    </fieldset>
    <div class='buttons'>  
      <input type=\"hidden\" name=\"action\" value=\"update\">
      <input type='submit' value=$l_validate />
    </div>
    </form>
  ";


  return $block;
}

function get_block_repeat_kind($repeatKind, $disabled) {
  global $l_repeat_kind, $l_daily, $l_weekly, $l_monthly, $l_yearly;

  $block_repeat_kind = "";
  $repeat_kinds = array("YEARLY" => $l_yearly, "MONTHLY" => $l_monthly, "WEEKLY" => $l_weekly, "DAILY" => $l_daily);
  foreach($repeat_kinds as $repeat_kind => $repeat_kind_value) {
    if($repeat_kind == $repeatKind) {
      $block_repeat_kind .= "<option selected='selected' value='$repeat_kind'>$repeat_kind_value</option>";
    } else {
      $block_repeat_kind .= "<option value='$repeat_kind'>$repeat_kind_value</option>";
    }
  }
  
  return
    "<th>$l_repeat_kind</th>
     <td>
       <select type='text' name='sel_repeat_kind' $disabled class='configuration' onchange='redraw(this.options[this.selectedIndex].value);'>
         $block_repeat_kind
       </select>
     </td>";
}

function get_block_weekly($repeatKind, $dayOfWeek, $disabled) {
  global $l_weekly_repeat_kind;
  global $l_monday, $l_tuesday, $l_wednesday, $l_thursday, $l_friday, $l_saturday, $l_sunday;

  $visibility = ($repeatKind == 'WEEKLY') ? 'visible' : 'hidden';

  $block_weekly = "";
  $l_days = array(1 => $l_monday, 2 => $l_tuesday, 3 => $l_wednesday, 4 => $l_thursday, 5 => $l_friday, 6 => $l_saturday, 7 => $l_sunday);
  foreach ($l_days as $l_day => $l_day_value) {
    if ($l_day == $dayOfWeek) {
      $block_weekly .= "<option selected='selected' value='$l_day'>$l_day_value</option>";
    } else {
      $block_weekly .= "<option value='$l_day'>$l_day_value</option>";
    }
  }
  
  return 
    "<th class='class_weekly' style='visibility:$visibility'>$l_weekly_repeat_kind</th>
     <td>
       <select name='sel_day_of_week' class='configuration class_weekly' style='visibility:$visibility' $disabled>
         $block_weekly
       </select>
     </td>";
}

function get_block_monthly($repeatKind, $dayOfMonth, $disabled) {
  global $l_monthly_repeat_kind;
  global $l_last;

  $visibility = ($repeatKind == 'MONTHLY') ? 'visible' : 'hidden';
  $last_day_of_month = -1;

  $block_monthly = "";
  for ($i = 1; $i <= 27; $i++) {
    if ($i == $dayOfMonth) {
      $block_monthly .= "<option selected='selected' value='$i'>$i</option>";
    } else {
      $block_monthly .= "<option value='$i'>$i</option>";
    }
  }
  if ($dayOfMonth == -1) {
    $block_monthly .= "<option selected='selected' value='$last_day_of_month'>$l_last</option>";
  } else {
    $block_monthly .= "<option value='$last_day_of_month'>$l_last</option>";
  }
  
  return 
    "<th class='class_monthly' style='visibility:$visibility'>$l_monthly_repeat_kind</th>
     <td>
       <select type='text' name='sel_day_of_month' class='configuration class_monthly' style='visibility:$visibility' $disabled>
         $block_monthly
       </select>
     </td>";
}

function get_block_yearly($repeatKind, $dayOfYear, $disabled) {
  global $l_yearly_repeat_kind;
  
  $visibility = ($repeatKind == 'YEARLY') ? 'visible' : 'hidden';

  $january = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[0]; }, range(1, 31));
  $february = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[1]; }, range(1, 28));
  $march = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[2]; }, range(1, 31));
  $april = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[3]; }, range(1, 30));
  $may = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[4]; }, range(1, 31));
  $june = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[5]; }, range(1, 30));
  $july = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[6]; }, range(1, 31));
  $august = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[7]; }, range(1, 31));
  $september = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[8]; }, range(1, 30));
  $october = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[9]; }, range(1, 31));
  $november = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[10]; }, range(1, 30));
  $december = array_map(function ($day) { global $l_monthsofyear; return $day." ".$l_monthsofyear[11]; }, range(1, 31));
  
  $year = array_merge(array(0), $january, $february, $march, $april, $may, $june, $july, $august, $september, $october, $november, $december);
  for ($i = 1; $i <= 365; $i++) {
    if ($i == $dayOfYear) {
      $block_yearly .= "<option selected='selected' value='$i'>$year[$i]</option>";
    } else {
      $block_yearly .= "<option value='$i'>$year[$i]</option>";
    }
  }
  return 
    "<th class='class_yearly' style='visibility:$visibility'>$l_yearly_repeat_kind</th>
     <td>
       <select type='text' name='sel_day_of_year' class='configuration class_yearly' style='visibility:$visibility' $disabled>
         $block_yearly
       </select>
     </td>";
}

function get_block_hour($hour, $disabled) {
  global $l_hour;
  
  $block_hour = "";
  for ($i = 0; $i <= 23; $i++) {
    if ($i == $hour) {
      $block_hour .= "<option selected='selected' value='$i'>$i</option>";
    } else {
      $block_hour .= "<option value='$i'>$i</option>";
    }
  }
  return
    "<th>$l_hour</th>
     <td>
       <select type='text' name='sel_hour' class='configuration' $disabled>
         $block_hour
       </select>
     </td>";
}

function get_block_minute($minute, $disabled) {
  global $l_minute;
  
  $block_minute = "";
  for ($i = 0; $i <= 59; $i++) {
    if ($i == $minute) {
      $block_minute .= "<option selected='selected' value='$i'>$i</option>";
    } else {
      $block_minute .= "<option value='$i'>$i</option>";
    }
  }
  return
    "<th>$l_minute</th>
     <td>
       <select type='text' name='sel_minute' class='configuration' $disabled>
         $block_minute
       </select>
     </td>";
}

///////////////////////////////////////////////////////////////////////////////
// Display the IMAP Archive consult
///////////////////////////////////////////////////////////////////////////////

function dis_imap_archive_consult($backup, $params) {

  global $l_past_operations, $l_start_time, $l_end_time, $l_status, $l_next_run;
  global $l_last_fail;
  
  $user_id = $params['user_id'];
  $url = url_prepare('imap_archive_index.php');

  $block = "
    <div class='detail infos'>
      <h1>$l_past_operations</h1>
      <table class='spreadSheet'>
        <thead>
          <tr>
            <td>$l_start_time</td>
            <td>$l_end_time</td>
            <td>$l_status</td>
          </tr>
        </thead>
        <tbody>
          <tr id='run_1' class='run'>
            <td>2012-10-01 00:00:00</td>
            <td>2012-10-01 00:05:23</td>
            <td>OK</td>
          </tr>
          <tr id='run_2' class='run'>
            <td>2012-12-01 00:00:00</td>
            <td>2012-12-01 00:03:05</td>
            <td><a href='http://192.168.56.3/imap_archive'>Erreur</a></td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class='detail infos'>
      <h1>$l_next_run</h1>
      <table>
      <tr>
        <th>2014-04-18 22:30:00</th>
        <td></td>
      </tr>
      </table>
    </div>
    <div class='detail infos'>
      <h1>$l_last_fail</h1>
      <table class='spreadSheet'>
        <thead>
          <tr>
            <td>$l_start_time</td>
            <td>$l_end_time</td>
            <td>$l_status</td>
          </tr>
        </thead>
        <tbody>
          <tr id='run_1' class='run'>
            <td>2012-12-01 00:00:00</td>
            <td>2012-12-01 00:03:05</td>
            <td><a href='http://192.168.56.3/imap_archive'>Erreur</a></td>
          </tr>
        </tbody>
      </table>
    </div>
  ";


  return $block;
}

 ?>
