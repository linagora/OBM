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
require_once("$obminclude/javascript/check_js.inc");
require_once("$obminclude/of/of_query.inc");
require_once('imap_archive_ordering.php');

///////////////////////////////////////////////////////////////////////////////
// Display the IMAP Archive form
///////////////////////////////////////////////////////////////////////////////

function dis_imap_archive_form($params, $configuration, $next_treatment_date) {
  global $l_validate;
  global $l_service_activating, $l_activating;
  global $l_scheduling;
  global $l_archive_main_folder, $l_excluded_folder, $l_folder, $l_excluded_users;
  global $l_mailing_feedback;

  $user_id = $params['user_id'];
  $url = url_prepare('imap_archive_index.php');

  $repeatKind = $configuration->repeatKind;
  $dayOfWeek = $configuration->dayOfWeek;
  $dayOfMonth = $configuration->dayOfMonth;
  $dayOfYear = $configuration->dayOfYear;
  $enabled = $configuration->enabled;
  $hour = $configuration->hour;
  $minute = $configuration->minute;
  $archiveMainFolder = $configuration->archiveMainFolder;
  $excludedFolder = $configuration->excludedFolder;
  $excludedUserIdToLoginMap = $configuration->excludedUserIdToLoginMap;
  $mailingEmails = $configuration->mailingEmails;

  $disabled = ($enabled == 0) ? "disabled" : "";
  $block_repeat_kind = get_block_repeat_kind($repeatKind, $disabled);
  $block_weekly = get_block_weekly($repeatKind, $dayOfWeek, $disabled);
  $block_monthly = get_block_monthly($repeatKind, $dayOfMonth, $disabled);
  $block_yearly = get_block_yearly($repeatKind, $dayOfYear, $disabled);
  $block_hour = get_block_hour($hour, $disabled);
  $block_minute = get_block_minute($minute, $disabled);
  $block_next_treatment_date = get_block_next_treatment_date($next_treatment_date);
  $block_excluded_users = get_block_excluded_users($excludedUserIdToLoginMap);
  $block_mailing = get_block_mailing($mailingEmails);

  $user_js = "new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_excluded_user_id', 'userSearch', {filter_pattern:'access', item_id_property:'ext_id', filter_archived:false});";

  $block = "
    <form method='post' name='f_imap_archive' action='imap_archive_index.php'>

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
          $block_next_treatment_date
        </tr>
      </table>
    </fieldset>
    <fieldset class='detail infos'>
      <legend>$l_archive_main_folder</legend>
      <table>
        <th>$l_folder</th>
        <td>
          <input type='text' name='tf_archive_main_folder' id='tf_archive_main_folder' class='configuration' $disabled value='$archiveMainFolder'/>
        </td>
      </table>
    </fieldset>
    <fieldset class='detail infos'>
      <legend>$l_excluded_folder</legend>
      <table>
        <th>$l_folder</th>
        <td>
          <input type='text' name='tf_excluded_folder' id='tf_excluded_folder' class='configuration' $disabled value='$excludedFolder'/>
        </td>
      </table>
    </fieldset>
    <fieldset class='detail infos'>
      <legend>$l_excluded_users</legend>
      $block_excluded_users
    </fieldset>
    <fieldset class='detail infos'>
      <legend>$l_mailing_feedback</legend>
      $block_mailing
    </fieldset>
    <div class='buttons'>
      <input type=\"hidden\" name=\"action\" value=\"update\">
      <input type='submit' value=$l_validate onclick=\"enableConfiguration(true);\"/>
    </div>
    </form>

    <script type=\"text/javascript\">
      obm.initialize.chain(function () {
        $user_js
      });
    </script>";

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
       <select type='text' id='repeat_kind' name='sel_repeat_kind' $disabled class='configuration' onchange='redraw(this.options[this.selectedIndex].value); nextTreatmentDate();'>
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
       <select id='day_of_week' name='sel_day_of_week' class='configuration class_weekly' style='visibility:$visibility' $disabled onchange='nextTreatmentDate();'>
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
       <select type='text' id='day_of_month' name='sel_day_of_month' class='configuration class_monthly' style='visibility:$visibility' $disabled onchange='nextTreatmentDate();'>
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
       <select type='text' id='day_of_year' name='sel_day_of_year' class='configuration class_yearly' style='visibility:$visibility' $disabled onchange='nextTreatmentDate();'>
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
       <select type='text' id='hour' name='sel_hour' class='configuration' $disabled onchange='nextTreatmentDate();'>
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
       <select type='text' id='minute' name='sel_minute' class='configuration' $disabled onchange='nextTreatmentDate();'>
         $block_minute
       </select>
     </td>";
}

function get_block_next_treatment_date($next_treatment_date) {
  global $l_next_run;

  return
    "<th>$l_next_run</th>
     <td id='nextTreatmentDate' >".formatStringAsDateString($next_treatment_date)."</td>";
}

function formatStringAsDateString($dateAsString) {
  global $l_not_configured;

  if ($dateAsString) {
    try {
      $date = new DateTime($dateAsString);
      $date->setTimezone(new DateTimeZone('UTC'));
      $formattedDate = $date->format('Y-m-d H:i');
      return $formattedDate." UTC";
    } catch (Exception $e) {
      error_log("Couldn't parse to date ".print_r($dateAsString, true));
      return '';
    }
  }
  return $l_not_configured;
}

function get_block_excluded_users($excludedUserIdToLoginMap) {
  global $l_users, $ico_delete;
  global $l_name, $l_delete, $l_remove_archive;

  $excludedUserIds = array();
  foreach (get_object_vars($excludedUserIdToLoginMap) as $name => $value) {
    array_push($excludedUserIds, $name);
  }
  $users = (count($excludedUserIds) > 0) ? get_userobm_from_ext_ids($excludedUserIds, false) : array();
  if (is_array($users['entity'])) {
    $dis_sel_user = "";
    foreach ($users['entity'] as $u_ext_id => $u_ent) {
      $u_name = $u_ent['label'];
      $sel_id = $u_ext_id;
      $div_id = "sel_excluded_user_id-$sel_id";
      $dis_sel_user .= "<div class=\"elementRow\" id=\"$div_id\">
      <a href=\"javascript: remove_element('$div_id', 'sel_excluded_user_id');\">
      <img src=\"$ico_delete\" alt=\"[Delete]\" />
      </a>
      $u_name
      <input value=\"$sel_id\" name=\"sel_excluded_user_id[]\" type=\"hidden\" />
      </div>";
    }
  }

  return "
    <table>
      <tr>
        <th>$l_users</th>
        <td class='autocompleteField'>
          <input type='text' name='' value='' id='userSearch' autocomplete='off' />
          <input type='hidden' id='userSearchContainer' value='sel_excluded_user_id'/>
        </td>
      </tr>
      <tr>
        <th></th>
        <td class='autocompleteResult'>
          <span class='legend'>$GLOBALS[l_user]</span>
          <div id='sel_excluded_user_id' class='userList'>$dis_sel_user</div>
        </td>
      </tr>
    </table>";
}

function get_block_mailing($mailingEmails) {
  global $l_mailing_email, $ico_delete, $ico_add;

  if (is_array($mailingEmails)) {
  	$dis_mailing_emails = "";
    foreach ($mailingEmails as $mailingEmail) {
      $dis_mailing_emails .=
        "<tr id='$mailingEmail'>
           <td>
             <div>$mailingEmail</div>
             <input value='$mailingEmail' name='sel_mailing_emails[]' type='hidden'/>
           </td>
           <td>
             <a href='javascript: deleteMailingEmail(\"$mailingEmail\");'>
               <img src='$ico_delete' alt='[Delete]' />
             </a>
           </td>
         </tr>";
    }
  }

  return "
    <table>
      <tr>
        <th>$l_mailing_email</th>
        <td>
          <input type='email' name='' value='' id='mailing_email' onKeyPress='enterHandler(event);' />
          <a><img src='$ico_add' onclick='addMailingEmail(event);' /></a>
        </td>
      </tr>
      <table class='spreadSheet'>
        <thead>
          <tr>
            <th>$l_mailing_email</th>
            <th></th>
          </tr>
        </thead>
        <tbody id='mailing_emails'>
          $dis_mailing_emails
        </tbody>
      </table>
    </table>";
}

///////////////////////////////////////////////////////////////////////////////
// Display the IMAP Archive consult
///////////////////////////////////////////////////////////////////////////////

function dis_imap_archive_consult($params, $configuration, $next_treatment_date, $history, $last_failure) {
  global $l_next_run, $l_simulation, $l_manual_launch;
  global $path, $l_confirm_manual_launch, $l_domain_configuration_disable;

  $user_id = $params['user_id'];
  $url = url_prepare('imap_archive_index.php');

  $enabled = $configuration->enabled;
  $class = $enabled ? "" : "unavailable";
  $buttonTitle = $enabled ? "" : "\"$l_domain_configuration_disable\"";

  $block_treatments_history = get_block_treatments_history($history);
  $block_next_treatment_date = get_block_next_treatment_date($next_treatment_date);
  $block_last_failure = get_block_last_failure($last_failure);

  $confirm = "<script type='text/javascript'>

    function confirm_launch() {
      return confirm('$l_confirm_manual_launch');
    }
    </script>";

  $block = "
    <div class='detail infos'>
      $block_treatments_history
    </div>
    <div class='buttons' title=$buttonTitle>
      <a class='$class' href='$path/imap_archive/imap_archive_index.php?action=manual_launch&archive_treatment_kind=DRY_RUN' target='_blank' onclick='return confirm_launch();'>
      $l_simulation
      </a>
      <a class='$class' href='$path/imap_archive/imap_archive_index.php?action=manual_launch&archive_treatment_kind=REAL_RUN' target='_blank' onclick='return confirm_launch();'>
      $l_manual_launch
      </a>
    </div>
    <div class='detail infos'>
      <h1>$l_next_run</h1>
      <table>
      <tr>
        $block_next_treatment_date
      </tr>
      </table>
    </div>
    <div class='detail infos'>
      $block_last_failure
    </div>
    $confirm
  ";


  return $block;
}

function get_block_treatments_history($history) {
  global $l_past_operations;
  global $l_start_time, $l_end_time, $l_status;

  $block_history = "";
  if ($history) {
    foreach ($history->archiveTreatmentDtos as $treatment) {
      $block_history .=
        "<tr id='$treatment->runId' class='run'>
          <td>".formatStringAsDateString($treatment->startTime)."</td>
          <td>".formatStringAsDateString($treatment->endTime)."</td>
          <td>".statusAsString($treatment->archiveStatus, $treatment->runId)."</td>
        </tr>";
    }
  }
  return "<h1>$l_past_operations</h1>
    <table class='spreadSheet'>
      <thead>
        <tr>
          <td>$l_start_time</td>
          <td>$l_end_time</td>
          <td>$l_status</td>
        </tr>
      </thead>
    <tbody>
      $block_history
    </tbody>
  </table>";
}

function get_block_last_failure($history) {
  global $l_last_fail;
  global $l_start_time, $l_end_time, $l_status;

  $block_history = "";
  if ($history) {
    foreach ($history->archiveTreatmentDtos as $treatment) {
      $block_history .=
        "<tr id='$treatment->runId' class='run'>
          <td>".formatStringAsDateString($treatment->startTime)."</td>
          <td>".formatStringAsDateString($treatment->endTime)."</td>
          <td>".statusAsString($treatment->archiveStatus, $treatment->runId)."</td>
        </tr>";
    }
  }

  return "<h1>$l_last_fail</h1>
    <table class='spreadSheet'>
      <thead>
        <tr>
          <td>$l_start_time</td>
          <td>$l_end_time</td>
          <td>$l_status</td>
        </tr>
      </thead>
      <tbody>
        $block_history
      </tbody>
    </table>";
}

function statusAsString($status, $runId) {
  global $obm, $path;
  global $l_status_error, $l_status_success;

  $link = "$path/imap_archive/imap_archive_index.php?action=log_page&run_id=$runId";
  if ($status == 'ERROR') {
  	return "<a href='$link'>$l_status_error</a>";
  }
  return "<a href='$link'>$l_status_success</a>";
}

///////////////////////////////////////////////////////////////////////////////
// Display the log IMAP Archive page
///////////////////////////////////////////////////////////////////////////////

function dis_log_page($params) {
  global $l_archiving_running, $l_unsupported_iframe;

  $runId = $params['run_id'];

  $queryParams = array("run_id" => $runId,
      "action" => "archiving_logs");

  $block = "
    <div class='detail extra' >
      <h1>$l_archiving_running</h1>
  	  <iframe id='archivingLogs' frameborder='0'>
  	    <p>$l_unsupported_iframe</p>
  	  </iframe>
  	</div>

    <script type=\"text/javascript\">
      obm.initialize.chain(function () {
        loadLogs(\"$runId\");
      });
    </script>";

  return $block;
}

 ?>
