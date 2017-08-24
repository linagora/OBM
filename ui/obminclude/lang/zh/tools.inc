<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : tools.inc                                                //
// 2002-09-26 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id: tools.inc 2171 2007-10-18 15:00:45Z anthony $
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-09T17:18:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:21:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:23:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:30:10+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:30:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:35:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:38:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:43:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:43:45+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:46:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:46:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:51:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T17:58:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T18:07:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T18:15:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T18:15:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-09T18:16:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:32:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:33:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:34:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:37:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:38:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:40:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:40:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:40:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:42:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:43:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:43:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:45:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:45:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:47:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:53:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:00:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:01:06+02:00 using OBMtranslation (0.4.1)

$l_tools = "管理工具";  //"Administration tools"
$l_upd = "更新系统设置";  //"Update system configuration"
$l_upd_user = "我的更新";  //"My updates"
$l_upd_domain = "域更新";  //"Domain updates"
$l_upd_delegation = "我的授权更新";  //"My delegation updates"
$l_upd_running = "系统正在配置中...";  //"System is configuring..."
$l_upd_already_running = "无法更改，系统正在配置中...";  //"Impossible, system is already configuring..."
$l_upd_not_needed = "系统配置是<b>最新的</b>";  //"System configuration is <b>up to date</b>"
$l_upd_need_domain_update = "域已更改！ 域需要更新";  //"Domain modified ! Domain update needed"
$l_upd_needed = "系统配置已更新";  //"System configuration is updated"
$l_upd_run = "应用新的系统配置";  //"Apply new system configuration"
$l_upd_cancel = "取消更新";  //"Cancel updates"
$l_upd_ok = "系统配置已应用";  //"System configuration is applied"
$l_upd_error = "系统配置失败！";  //"System configuration failed !"
$l_upd_cancel_ok = "系统更新取消成功"; //"System updates cancellation was taken into account"
$l_upd_cancel_error = "系统更新删除失败！";  //"System updates deletion failed !"
$l_progress = '系统更新进度'; //'System update progress'
$l_created = "已创建"; //"Created"
$l_updated = "已更新"; //"Updates"
$l_deleted = "已删除"; //"Deleted"
$l_links = "链接"; //"Links"
$l_unspecified = "未指定"; //"unspecified"
$l_js_ask_confirm = "确定？"; //"Are you sure ?"
$l_halt = "系统停止";  //"System stop"
$l_halt_aliamin = "关闭系统"; //"Switch off system"
$l_show_details = '显示细节'; //'Show details'
$l_js_created_user_confirm = "该用户及其所有数据将被删除，且此操作无法撤销，确定继续吗？"; //"This user will be deleted without a possible undo, all his data will be lost, do you want to continue ?"
$l_js_created_host_confirm = "该主机将被删除，且此操作无法撤销，确定继续吗？";  //"This host will be deleted without a possible undo, do you want to continue ?"/
$l_js_created_group_confirm = "该分组将被删除，且此操作无法撤销，确定继续吗？"; //"This group will be deleted without a possible undo, do you want to continue ?"
$l_js_created_mailshare_confirm = "该mailshare将被删除，且此操作无法撤销，确定继续吗？"; //"This mailshare will be deleted without a possible undo, do you want to continue ?"
$l_js_updated_user_confirm = "该用户自上次系统更新后的任何更新将被取消，且此操作无法撤销，确定继续吗？";  //"Any update on this user since the last system update will be canceled without a possible undo, do you want to continue ?"
$l_js_updated_host_confirm = "该主机自上次系统更新后的任何更新将被取消，且此操作无法撤销，确定继续吗？"; //"Any update on this host since the last system update will be canceled without a possible undo, do you want to continue ?"
$l_js_updated_group_confirm = "该分组自上次系统更新后的任何更新将被取消，且此操作无法撤销，确定继续吗？";  //"Any update on this group since the last system update will be canceled without a possible undo, do you want to continue ?"
$l_js_updated_mailshare_confirm = "该mailshare自上次系统更新后的任何更新将被取消，且此操作无法撤销，确定继续吗？"; //"Any update on this mailshare since the last system update will be canceled without a possible undo, do you want to continue ?"
$l_js_deleted_user_confirm = "此操作将恢复该用户上次系统更新的数据，所有与系统无关联的数据（日历，联系人...）将不会被恢复，确定继续吗？";  //"The user will be restored with the data of the last system update, all information not linked to the system (calendar, contacts...) will not be restored, do you want to continue?"
$l_js_deleted_host_confirm = "此操作将恢复该主机上次系统更新的数据，所有与系统无关联的数据将不会被恢复，确定继续吗？";  //"The host will be restored with the data of the last system update, all information not linked to the system will not be restored, do you want to continue?"
$l_js_deleted_group_confirm = "此操作将恢复该分组上次系统更新的数据，所有与系统无关联的数据将不会被恢复，确定继续吗？";  //"The group will be restored with the data of the last system update, all information not linked to the system will not be restored, do you want to continue?"
$l_js_deleted_mailshare_confirm = "此操作将恢复该mailshare上次系统更新的数据，所有与系统无关联的数据将不会被恢复，确定继续吗？";  //"The mailshare will be restored with the data of the last system update, all information not linked to the system will not be restored, do you want to continue?"
$l_cant_undo_created_user = "试图撤销用户创建操作时发生错误";  //"Error while trying to undo user creation"
$l_cant_undo_created_mailshare = "试图撤销mailshare创建操作时发生错误";  //"Error while trying to undo mailshare creation"
$l_cant_undo_created_host = "试图撤销主机创建操作时发生错误"; //"Error while trying to undo host creation"
$l_cant_undo_created_group = "试图撤销分组创建操作时发生错误"; //"Error while trying to undo group creation"
$l_cant_undo_updated_user = "试图撤销用户更新操作时发生错误"; //"Error while trying to undo user update"
$l_cant_undo_updated_mailshare = "试图撤销mailshare更新操作时发生错误";  //"Error while trying to undo mailshare update";
$l_cant_undo_updated_host = "试图撤销主机更新操作时发生错误"; // "Error while trying to undo host update";
$l_cant_undo_updated_group = "试图撤销分组更新操作时发生错误"; //"Error while trying to undo group update";
$l_cant_undo_deleted_user = "试图撤销用户删除操作时发生错误";  //"Error while trying to undo user deletion";
$l_cant_undo_deleted_mailshare = "试图撤销mailshare删除操作时发生错误"; //"Error while trying to undo mailshare delete";
$l_cant_undo_deleted_host = "试图撤销删除主机操作时发生错误";  //"Error while trying to undo host deletion"; 
$l_cant_undo_deleted_group = "试图撤销删除分组操作时发生错误"; //"Error while trying to undo group";
$l_undo_success = "操作成功"; //"Operation was successful";

