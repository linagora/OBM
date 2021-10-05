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

$l_tools = "Administration tools";
$l_upd = "Update system configuration";
$l_upd_user = "My updates";
$l_upd_domain = "Domain updates";
$l_upd_delegation = "My delegation updates";
$l_upd_running = "System is configuring...";
$l_upd_already_running = "Impossible, system is already configuring...";
$l_upd_not_needed = "System configuration is <b>up to date</b>";
$l_upd_need_domain_update = "Domain modified ! Domain update needed";
$l_upd_needed = "System configuration is updated";
$l_upd_run = "Apply new system configuration";
$l_upd_cancel = "Cancel updates";
$l_upd_ok = "System configuration is applied";
$l_upd_error = "System configuration failed !";
$l_upd_cancel_ok = "System updates cancellation was taken into account";
$l_upd_cancel_error = "System updates deletion failed !"; 
$l_progress = 'System update progress';
$l_created = "Created";
$l_updated = "Updates";
$l_deleted = "Deleted";
$l_links = "Links";
$l_unspecified = "unspecified";
$l_js_ask_confirm = "Are you sure ?";
$l_halt = "System stop";
$l_halt_aliamin = "Switch off system";
$l_show_details = 'Show details';
$l_js_created_user_confirm = "This user will be deleted without a possible undo, all his data will be lost, do you want to continue ?"; 
$l_js_created_host_confirm = "This host will be deleted without a possible undo, do you want to continue ?"; 
$l_js_created_group_confirm = "This group will be deleted without a possible undo, do you want to continue ?";
$l_js_created_mailshare_confirm = "This mailshare will be deleted without a possible undo, do you want to continue ?";
$l_js_updated_user_confirm = "Any update on this user since the last system update will be canceled without a possible undo, do you want to continue ?";
$l_js_updated_host_confirm = "Any update on this host since the last system update will be canceled without a possible undo, do you want to continue ?";
$l_js_updated_group_confirm = "Any update on this group since the last system update will be canceled without a possible undo, do you want to continue ?";
$l_js_updated_mailshare_confirm = "Any update on this mailshare since the last system update will be canceled without a possible undo, do you want to continue ?";
$l_js_deleted_user_confirm = "The user will be restored with the data of the last system update, all information not linked to the system (calendar, contacts...) will not be restored, do you want to continue?";
$l_js_deleted_host_confirm = "The host will be restored with the data of the last system update, all information not linked to the system will not be restored, do you want to continue?";
$l_js_deleted_group_confirm = "The group will be restored with the data of the last system update, all information not linked to the system will not be restored, do you want to continue?";
$l_js_deleted_mailshare_confirm = "The mailshare will be restored with the data of the last system update, all information not linked to the system will not be restored, do you want to continue?";
$l_cant_undo_created_user = "Error while trying to undo user creation";
$l_cant_undo_created_mailshare = "Error while trying to undo mailshare creation";
$l_cant_undo_created_host = "Error while trying to undo host creation";
$l_cant_undo_created_group = "Error while trying to undo group creation";
$l_cant_undo_updated_user = "Error while trying to undo user update";
$l_cant_undo_updated_mailshare = "Error while trying to undo mailshare update";
$l_cant_undo_updated_host = "Error while trying to undo host update";
$l_cant_undo_updated_group = "Error while trying to undo group update";
$l_cant_undo_deleted_user = "Error while trying to undo user deletion"; 
$l_cant_undo_deleted_mailshare = "Error while trying to undo mailshare delete";
$l_cant_undo_deleted_host = "Error while trying to undo host deletion"; 
$l_cant_undo_deleted_group = "Error while trying to undo group";
$l_undo_success = "Operation was successful";

