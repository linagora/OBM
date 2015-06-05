<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Tiếng việt
//     - File     : planning.inc
///////////////////////////////////////////////////////////////////////////////
// $Id: planning.inc 
///////////////////////////////////////////////////////////////////////////////

$l_see = "Xem";
$l_overrun = "Vượt quá";
$l_reporting = "Báo cáo";
$l_activity = "Hoạt động";
$l_activities_of = "Hoạt động của";

$l_period = "Khoảng thời gian";
$l_plannedtask_periods = array (
    'MORNING' => 'Buổi sáng',
    'AFTERNOON' => 'Buổi chiều',
    'ALLDAY' => 'Cả ngày'
  );

$l_err_plannedtask_user_empty = "Bạn phải nhập một người dùng";
$l_err_plannedtask_activity_empty = "Bạn phải chọn một hoạt động";
$l_err_plannedtask_activity_one = "Chỉ chọn một hoạt động";
$l_err_plannedtask_datebegin_empty = "Bạn phải điền vào một ngày bắt đầu";
$l_err_plannedtask_dateend_empty = "Bạn phải điền vào ngày kết thúc";
$l_err_plannedtask_dateinverted = "Ngày bắt đầu phải trước ngày kết thúc";
$l_err_plannedtask_detected_conflict = "Phát hiện xung đột : người sử dụng này đã được phân việc trong giai đoạn này. Xem các sự kiện dưới đây.";
$l_err_plannedtask_event_in_conflit = "Sự kiện bị xung đột";
$l_err_planning_no_group_selected = "Không có nhóm được lựa chọn";
