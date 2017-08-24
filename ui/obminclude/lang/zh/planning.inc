<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French
//     - File     : planning.inc
// 2008-10-14 Guillaume Lardon
///////////////////////////////////////////////////////////////////////////////
// $Id: planning.inc 16 2009-09-24 07:15:46Z glardon $
///////////////////////////////////////////////////////////////////////////////

$l_see = "查看"; //"See"
$l_overrun = "超额"; //"Overrun"
$l_reporting = "报告"; //"Reporting"
$l_activity = "活动"; //"Activity"
$l_activities_of = "活动"; //"Activities of"

$l_period = "时期"; //"Period"
$l_plannedtask_periods = array (
    'MORNING' => '上午',
    'AFTERNOON' => '下午',
    'ALLDAY' => '一整天'
  );

$l_err_plannedtask_user_empty = "您必须指定一个用户"; //"You must specify a user to affect"
$l_err_plannedtask_activity_empty = "您必须填写活动"; //"You must fill the activity"
$l_err_plannedtask_activity_one = "您必须填写仅填写一个活动"; //"You must specify only one actitivy"
$l_err_plannedtask_datebegin_empty = "您必须填写开始日期"; //"You must fill a begin date"
$l_err_plannedtask_dateend_empty = "您必须填写结束日期"; //"You must fill an end date"
$l_err_plannedtask_dateinverted = "开始日期必须在结束日期之前"; //"The begin date must be before the end date"
$l_err_plannedtask_detected_conflict = "已检测到冲突：该用户在此期间已有别的计划任务。请查看以下事件。"; //"Detected conflict : this user in already staffed during this period. See the event below."
$l_err_plannedtask_event_in_conflit = "冲突事件"; //"Event in conflict"
$l_err_planning_no_group_selected = "未选择组"; //"No group selected"
