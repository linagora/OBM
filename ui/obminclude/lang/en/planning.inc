<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French
//     - File     : planning.inc
// 2008-10-14 Guillaume Lardon
///////////////////////////////////////////////////////////////////////////////
// $Id: planning.inc 16 2009-09-24 07:15:46Z glardon $
///////////////////////////////////////////////////////////////////////////////

$l_see = "See";
$l_overrun = "Overrun";
$l_reporting = "Reporting";
$l_activity = "Activity";
$l_activities_of = "Activities of";

$l_period = "Period";
$l_plannedtask_periods = array (
    'MORNING' => 'Morning',
    'AFTERNOON' => 'Afternoon',
    'ALLDAY' => 'Entire day'
  );

$l_err_plannedtask_user_empty = "You must specify a user to affect";
$l_err_plannedtask_activity_empty = "You must fill the activity";
$l_err_plannedtask_activity_one = "You must specify only one actitivy";
$l_err_plannedtask_datebegin_empty = "You must fill a begin date";
$l_err_plannedtask_dateend_empty = "You must fill an end date";
$l_err_plannedtask_dateinverted = "The begin date must be before the end date";
$l_err_plannedtask_detected_conflict = "Detected conflict : this user in already staffed during this period. See the event below.";
$l_err_plannedtask_event_in_conflit = "Event in conflict";
$l_err_planning_no_group_selected = "No group selected";
