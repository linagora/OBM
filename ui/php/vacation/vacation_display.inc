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
  global $l_enabled, $l_disabled, $l_vacation_datebegin, $l_vacation_dateend;

  $enabled = $usr_q->f('userobm_vacation_enable');
  if ($enabled == 1) {
    $status = $l_enabled;
  } else {
    $status = $l_disabled;
  }

  if ($usr_q->f('userobm_vacation_message') != '') {
    $message = nl2br(stripslashes( $usr_q->f('userobm_vacation_message') ));
  } else {
    $message = '---';
  }

  $vacation_datebegin = new Of_Date($usr_q->f('userobm_vacation_datebegin'), 'GMT');
  if($vacation_datebegin->error()) {
    $vacation_datebegin = '---';
  } else {
    $vacation_datebegin = $vacation_datebegin->getOutputDateTime();
  }
  $vacation_dateend = new Of_Date($usr_q->f('userobm_vacation_dateend'), 'GMT');
  if($vacation_dateend->error()) {
    $vacation_dateend = '---';
  } else {
    $vacation_dateend = $vacation_dateend->getOutputDateTime();
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
      <th>".__("Vacation programmation")."</th>
      <td>"
        .__(
             "The vacation message will be on starting the %date_start% and until the %date_end%",
             array("%date_start%"=>$vacation_datebegin, "%date_end%"=>$vacation_dateend)
           ).
      "</td>
    </tr>
    <tr>
      <th>$l_vacation_message</th>
      <td>$message</td>
    </tr>
  </table>
  </div>";
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Vacation Form
// Parameters:
//   - $usr_q : User Vacation database result
//   - $vacation : parameters hash
//   - $field : if set, field in error
///////////////////////////////////////////////////////////////////////////////
function html_vacation_form($usr_q, $vacation, $field='') {
  global $obm, $l_vacation, $l_enable_vacation, $l_vacation_message;
  global $l_update, $l_vacation_not_allowed, $l_vacation_datebegin, $l_vacation_dateend;
  global $ccalendar_first_hour, $ccalendar_last_hour, $l_hour, $l_enable_auto_vacation;

  $action = $vacation['action'];
  
  // if update mode and first time value are taken from database
  if ($action == 'detailupdate') {

    $vacation_datebegin = new Of_Date($usr_q->f('userobm_vacation_datebegin'), 'GMT');
    $vacation_dateend = new Of_Date($usr_q->f('userobm_vacation_dateend'), 'GMT');
    if(!$vacation_datebegin->error()) {
      $auto_enable = true;
    } else {
      $vacation_datebegin->addHour(1)->setMinute(0)->setSecond(0);
      $vacation_dateend = clone $vacation_datebegin;
      $vacation_dateend->addWeek(1);
    }

    $enabled = $usr_q->f('userobm_vacation_enable');
    $message = stripslashes( $usr_q->f('userobm_vacation_message') );
    $mail_enable = $usr_q->f('userobm_mail_perms');
  } else if ($action == 'update') {
    $obm_q = run_query_vacation_detail($obm['uid']);
    $mail_enable = $obm_q->f('userobm_mail_perms');
  }

  // If parameters have been given, they supercede the default action value
  if (isset($vacation['enabled'])) { $enabled = $vacation['enabled']; }
  if (isset($vacation['message'])) { $message = $vacation['message']; }
  if (isset($vacation['vacation_datebegin'])) { $vacation_datebegin = $vacation['vacation_datebegin']; } 
  elseif(!$vacation_dateend) { $vacation_datebegin = new Of_Date();$vacation_datebegin->addHour(1)->setMinute(0)->setSecond(0);}
  if (isset($vacation['vacation_dateend'])) { $vacation_dateend = $vacation['vacation_dateend']; }
  elseif(!$vacation_dateend) { $vacation_dateend = clone $vacation_datebegin; $vacation_dateend->addWeek(1); }
  if (isset($vacation['auto_enable'])) {$auto_enable = $vacation['auto_enable']; }

  if (! $mail_enable) {
    return display_warn_msg($l_vacation_not_allowed);
  }
    
  if($auto_enable) {
    $schedule_enable = "checked=\"checked\"";
  } else {
    $schedule_disabled = "disabled=\"disabled\"";
    $display_auto = "style=\"display:none\"";
  }
  // vacation enabled checkbox
  if ($enabled == 1) {
    $enabled_checked = " checked=\"checked\" ";
    $message_enabled = '';
  } else {
    $enabled_checked = ' ';
  }
  // hour begin
  $dis_hour_b = "<select $schedule_disabled name=\"sel_time_begin\" style=\"width:4em;\">";
  //for ($i=$ccalendar_first_hour; $i<$ccalendar_last_hour; $i++) {
  for ($i=0; $i<24; $i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $vacation_datebegin->getHour()) {
      $dis_hour_b .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_b .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_b .= "</select>";  
  $dis_min_b = "<select $schedule_disabled name=\"sel_min_begin\" style=\"width:4em;\">";
  if ($ccalendar_hour_fraction <= 60 && $ccalendar_hour_fraction > 0) {
    $hour_fraction = $ccalendar_hour_fraction;
  } else {
    $hour_fraction = 4;
  }
  $step = 60 / $hour_fraction;
  for ($i=$hour_fraction; $i>=1; $i--) {
    $current_min = substr('0'.round((60 - ($step*$i))),-2);
    if ($current_min  == $vacation_datebegin->getMinute()) {
      $dis_min_b .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_b .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_b .= "</select>";

  // hour end
  $dis_hour_e = "<select $schedule_disabled name=\"sel_time_end\" style=\"width:4em;\">";
  for ($i=$ccalendar_first_hour; $i<=$ccalendar_last_hour; $i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $vacation_dateend->getHour()) {
      $dis_hour_e .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_e .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_e .= "</select>";  
  $dis_min_e = "<select $schedule_disabled name=\"sel_min_end\" style=\"width:4em;\">";
  for ($i=$hour_fraction; $i>=1; $i--) {
    $current_min = substr('0'.round((60 - ($step*$i))),-2);
    if ($current_min  == $vacation_dateend->getMinute()) {
      $dis_min_e .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_e .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_e .= "</select>";

  // error and css classes : 
  $enabled_css = $field == 'enabled' ?  'detailError' : 'detailLabel';
  $message_css = $field == 'message' ?  'detailError' : 'detailLabel';
  
  $block = "
<form method=\"POST\" name=\"form_vacation\" action=\"vacation_index.php\">
  <script type=\"text/javascript\">
    function showHideVacationDate(elt) {
      if(elt.checked) {
        $('vacation_datebegin').getElements('input').setProperty('disabled',false);
        $('vacation_datebegin').getElements('select').setProperty('disabled',false);
        $('vacation_dateend').getElements('input').setProperty('disabled',false);
        $('vacation_dateend').getElements('select').setProperty('disabled',false);        
        $('vacation_datebegin').setStyle('display','');
        $('vacation_dateend').setStyle('display','');
      } else {
        $('vacation_datebegin').setStyle('display','none');
        $('vacation_dateend').setStyle('display','none');
        $('vacation_datebegin').getElements('input').setProperty('disabled',true);
        $('vacation_datebegin').getElements('select').setProperty('disabled',true);
        $('vacation_dateend').getElements('input').setProperty('disabled',true);
        $('vacation_dateend').getElements('select').setProperty('disabled',true);        
      }
    }
  </script>
  <fieldset class=\"detail extra\">
  <legend>$l_vacation</legend>

  <table>
  <tr>
    <th><label for=\"cba_enabled\">$l_enable_vacation</label></th>
    <td><input type=\"checkbox\" id=\"cba_enabled\" name=\"cba_enabled\" value=\"1\" $enabled_checked /></td>
  </tr>
  <tr >
    <th><label for=\"cba_auto_enable\">$l_enable_auto_vacation</label></th>
    <td><input type=\"checkbox\" id=\"cba_auto_enable\" name=\"cba_auto_enable\" $schedule_enable value=\"1\" 
    onclick=\"showHideVacationDate(this);\"
  </tr>
  <tr $display_auto id=\"vacation_datebegin\">
    <th class=\"$class[vacation_datebegin]\">$l_vacation_datebegin</th>
    <td>
    <input $schedule_disabled type=\"text\" name=\"tf_vacation_datebegin\" value=\"".$vacation_datebegin->getInputDate()."\" class=\"datePicker\"/>
      <span id=\"hour_begin\" style=\"$style\" class=\"NW\">$l_hour : $dis_hour_b : $dis_min_b</span>
    </td>
  </tr>
  <tr $display_auto id=\"vacation_dateend\">
    <th class=\"$class[vacation_datebegin]\">$l_vacation_dateend</th>
    <td>
      <input $schedule_disabled type=\"text\" name=\"tf_vacation_dateend\" value=\"".$vacation_dateend->getInputDate()."\" class=\"datePicker\"/>
      <span id=\"hour_end\" style=\"$style\" class=\"NW\">$l_hour : $dis_hour_e : $dis_min_e</span>
    </td>
  </tr>
  <tr>
    <th>$l_vacation_message</th>
    <td><textarea name=\"ta_message\" rows=\"10\" cols=\"72\">$message</textarea></td>
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


?>
