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

function dis_imap_archive_form($backup, $params) {
  global $l_users, $l_validate;
  global $l_service_activating, $l_activating;
  global $l_scheduling, $l_repeat_kind, $l_weekly_repeat_kind, $l_monthly_repeat_kind, $l_yearly_repeat_kind;
  global $l_hour, $l_minute, $l_next_run, $l_excluded_users, $l_name, $l_delete, $l_remove_archive;
  global $l_mailing_feedback, $l_email_address;

  $user_id = $params['user_id'];
  $url = url_prepare('imap_archive_index.php');

  $block_repeat_kind = get_block_repeat_kind();
  $block_weekly = get_block_weekly();
  $block_monthly = get_block_monthly();
  $block_yearly = get_block_yearly();
  $block_hour = get_block_hour();
  $block_minute = get_block_minute();


  $block = "
    <form method='get' name='f_imap_archive' action='imap_archive_index.php'>

    <fieldset class='detail infos'>
      <legend>$l_service_activating</legend>
      <table>
        <tr>
          <th>$l_activating</th>
          <td><input id='activation' name='activation' type='checkbox' value='0'/></td>
        </tr>
      </table>
    </fieldset>
    <fieldset class='detail infos'>
      <legend>$l_scheduling</legend>
      <table>
        <tr>
          <th>$l_repeat_kind</th>
          <td>
            <select type='text' name='sel_repeat_kind_cron'>
              $block_repeat_kind
            </select>
          </td>
        </tr>
        <tr>
          <th>$l_weekly_repeat_kind</th>
          <td>
            <select type='text' name='sel_weekly'>
              $block_weekly
            </select>
          </td>
        </tr>
        <tr>
          <th>$l_monthly_repeat_kind</th>
          <td>
            <select type='text' name='sel_monthly'>
              $block_monthly
            </select>
          </td>
        </tr>
        <tr>
          <th>$l_yearly_repeat_kind</th>
          <td>
            <select type='text' name='sel_yearly'>
              $block_yearly
            </select>
          </td>
        </tr>
      </table>
      <br>
      <table>
        <tr>
          <th>$l_hour</th>
          <td>
            <select type='text' name='sel_hour'>
              $block_hour
            </select>
          </td>
        </tr>
        <tr>
          <th>$l_minute</th>
          <td>
            <select type='text' name='sel_minute'>
              $block_minute
            </select>
          </td>
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
        <input type='submit' value=$l_validate />
    </div>
    </form>
  ";


  return $block;
}

function get_block_repeat_kind() {
  global $l_daily, $l_weekly, $l_monthly, $l_yearly;

  $l_repeat_kinds = array($l_yearly, $l_monthly, $l_weekly, $l_daily);
  foreach($l_repeat_kinds as $l_repeat_kind) {
    if($l_repeat_kind == $l_yearly) {
      $block_repeat_kind .= "<option selected='selected' value='$l_repeat_kind'>$l_repeat_kind</option>";
    } else {
      $block_repeat_kind .= "<option value='$l_repeat_kind'>$l_repeat_kind</option>";
    }
  }
  return $block_repeat_kind;
}

function get_block_weekly() {
  global $l_monday, $l_tuesday, $l_wednesday, $l_thursday, $l_friday, $l_saturday, $l_sunday;

  $l_days = array($l_monday, $l_tuesday, $l_wednesday, $l_thursday, $l_friday, $l_saturday, $l_sunday);
  foreach ($l_days as $l_day) {
    if ($l_day == $l_monday) {
      $block_weekly = "<option selected='selected' value='$l_day'>$l_day</option>";
    } else {
      $block_weekly .= "<option value='$l_day'>$l_day</option>";
    }
  }
  return $block_weekly;
}

function get_block_monthly() {
  global $l_last;

  $block_monthly = "<option selected='selected' value='1'>1</option>";
  for ($i = 1; $i <= 27; $i++) {
    $block_monthly .= "<option value='$i'>$i</option>";
  }
  $block_monthly .= "<option value='$l_last'>$l_last</option>";
  return $block_monthly;
}

function get_block_yearly() {
  $block_yearly = "<option selected='selected' value='1 Janvier'>1 Janvier</option>";
  $block_yearly .= "<option selected='selected' value='...'>...</option>";
  $block_yearly .= "<option selected='selected' value='31 Décembre'>31 Décembre</option>";
  return $block_yearly;
}

function get_block_hour() {
  $block_hour = "<option selected='selected' value='0'>0</option>";
  for ($i = 1; $i <= 23; $i++) {
    $block_hour .= "<option value='$i'>$i</option>";
  }
  return $block_hour;
}

function get_block_minute() {
  $block_minute = "<option selected='selected' value='0'>0</option>";
  for ($i = 1; $i <= 59; $i++) {
    $block_minute .= "<option value='$i'>$i</option>";
  }
  return $block_minute;
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
