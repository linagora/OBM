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
  global $l_enabled, $l_disabled, $l_vacation_datebegin, $l_vacation_dateend;

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

  $vacation_datebegin = new Of_Date();
  $vacation_datebegin->setTimestamp($usr_q->f('vacation_datebegin'));
  $vacation_datebegin = $vacation_datebegin->getOutputDateTime();
  $vacation_dateend = new Of_Date();
  $vacation_dateend->setTimestamp($usr_q->f('vacation_dateend'));
  $vacation_dateend = $vacation_dateend->getOutputDateTime();

  $block = "
  <div class=\"detail extra\">
  <h1>$l_vacation</h1>

  <table>
  <tr>
    <th>$l_vacation_enabled</th>
    <td>$status</td>
  </tr>
   <tr>
      <th>$l_vacation_datebegin</th>
      <td>$vacation_datebegin</td>
    </tr>
    <tr>
      <th>$l_vacation_dateend</th>
      <td>$vacation_dateend</td>
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
  global $obm, $l_vacation, $l_enable_vacation, $l_vacation_message;
  global $l_update, $l_vacation_not_allowed, $l_vacation_datebegin, $l_vacation_dateend;
  global $ccalendar_first_hour, $ccalendar_last_hour, $l_hour, $l_enable_auto_vacation;

  $action = $vacation["action"];

  // if update mode and first time value are taken from database
  if ($action == "detailupdate") {

    $datebegin = new Of_Date();
    $datebegin->setTimestamp($usr_q->f('vacation_datebegin'));
    $dateend = new Of_Date();
    $dateend->setTimestamp($usr_q->f('vacation_dateend'));

    $enabled = $usr_q->f("userobm_vacation_enable");
    $message = stripslashes( $usr_q->f("userobm_vacation_message") );
    $mail_enable = $usr_q->f("userobm_mail_perms");
    $vacation_datebegin = $usr_q->f("vacation_datebegin");
    $vacation_dateend = $usr_q->f("vacation_dateend");
    $auto_enable = ($vacation_datebegin != '0');
    $hourbegin = $datebegin->getHour();
    $minbegin = $datebegin->getMinute();
    $hourend = $dateend->getHour();
    $minend = $dateend->getMinute();
    $vacation_datebegin = of_date_upd_format($vacation_datebegin);
    $vacation_dateend = of_date_upd_format($vacation_dateend);
  } else if ($action == 'update') {
    $obm_q = run_query_vacation_detail($obm['uid']);
    $mail_enable = $obm_q->f("userobm_mail_perms");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($vacation["enabled"])) { $enabled = $vacation["enabled"]; }
  if (isset($vacation["message"])) { $message = $vacation["message"]; }
  if (isset($vacation["vacation_datebegin"])) { $vacation_datebegin = $vacation["vacation_datebegin"]; }
  if (isset($vacation["vacation_dateend"])) { $vacation_dateend = $vacation["vacation_dateend"]; }
  if (isset($vacation["time_begin"])) { $hourbegin = $vacation["time_begin"]; }
  if (isset($vacation["min_begin"])) {$minbegin = $vacation["min_begin"]; }
  if (isset($vacation["time_end"])) {$hourend = $vacation["time_end"]; }
  if (isset($vacation["min_end"])) {$minend = $vacation["min_end"]; }
  if (isset($vacation["auto_enable"])) {$auto_enable = $vacation["auto_enable"]; }

  if (! $mail_enable) {
    return display_warn_msg($l_vacation_not_allowed);
  }
    
  if($auto_enable) {
    $auto_enable = "checked=\"checked\"";
  } else {
    $display_auto = "style=\"display:none\"";
  }
  // vacation enabled checkbox
  if ($enabled == 1) {
    $enabled_checked = " checked=\"checked\" " ;
    $message_enabled = "";
  } else {
    $enabled_checked = " " ;
  }
  // hour begin
  $dis_hour_b = "<select name=\"sel_time_begin\" style=\"width:4em;\">";
  for ($i=$ccalendar_first_hour; $i<$ccalendar_last_hour; $i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourbegin) {
      $dis_hour_b .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_b .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_b .= "</select>";  
  $dis_min_b = "<select name=\"sel_min_begin\" style=\"width:4em;\">";
  if ($ccalendar_hour_fraction <= 60 && $ccalendar_hour_fraction > 0) {
    $hour_fraction = $ccalendar_hour_fraction;
  } else {
    $hour_fraction = 4;
  }
  $step = 60 / $hour_fraction;
  for ($i=$hour_fraction; $i>=1; $i--) {
    $current_min = substr("0".round((60 - ($step*$i))),-2);
    if ($current_min  == $minbegin) {
      $dis_min_b .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_b .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_b .= "</select>";

  // hour end
  $dis_hour_e = "<select name=\"sel_time_end\" style=\"width:4em;\">";
  for ($i=$ccalendar_first_hour; $i<=$ccalendar_last_hour; $i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourend) {
      $dis_hour_e .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_e .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_e .= "</select>";  
  $dis_min_e = "<select name=\"sel_min_end\" style=\"width:4em;\">";
  for ($i=$hour_fraction; $i>=1; $i--) {
    $current_min = substr("0".round((60 - ($step*$i))),-2);
    if ($current_min  == $minend) {
      $dis_min_e .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_e .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_e .= "</select>";

  // error and css classes : 
  $enabled_css = $field == "enabled" ?  "detailError" : "detailLabel";
  $message_css = $field == "message" ?  "detailError" : "detailLabel";
  
  $block = "
<form method=\"POST\" name=\"form_vacation\" action=\"vacation_index.php\">
  <script type=\"text/javascript\">
    function showHideVacationDate(elt) {
      if(elt.checked) {
        $('vacation_datebegin').setStyle('display','');
        $('vacation_dateend').setStyle('display','');
      } else {
        $('vacation_datebegin').setStyle('display','none');
        $('vacation_dateend').setStyle('display','none');
      }
    }
  </script>
  <fieldset class=\"detail extra\">
  <legend>$l_vacation</legend>

  <table>
  <tr>
    <th>$l_enable_vacation</th>
    <td><input type=\"checkbox\" name=\"cba_enabled\" value=\"1\" $enabled_checked /></td>
  </tr>
  <tr >
    <th>$l_enable_auto_vacation</th>
    <td><input type=\"checkbox\" name=\"cba_auto_enable\" $auto_enable value=\"1\" 
    onchange=\"showHideVacationDate(this);\"
  </tr>
  <tr $display_auto id=\"vacation_datebegin\">
    <th class=\"$class[vacation_datebegin]\">$l_vacation_datebegin</th>
    <td>
      <input type=\"text\" name=\"tf_vacation_datebegin\" value=\"$vacation_datebegin\" class=\"datePicker\"/>
      <span id=\"hour_begin\" style=\"$style\" class=\"NW\">$l_hour : $dis_hour_b : $dis_min_b</span>
    </td>
  </tr>
  <tr $display_auto id=\"vacation_dateend\">
    <th class=\"$class[vacation_datebegin]\">$l_vacation_dateend</th>
    <td>
      <input type=\"text\" name=\"tf_vacation_dateend\" value=\"$vacation_dateend\" class=\"datePicker\"/>
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


</script>
