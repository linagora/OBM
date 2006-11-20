<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Theme : Standard                                                    //
//     - File  : theme.inc                                                   //
// 2002-07-18 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$theme_path = "../standard";

$css_obm = "$theme_path/style.css";
$css_calendar = "$theme_path/calendar.css";
$css_category = "$theme_path/category.css";
$css_document = "$theme_path/ document.css";
$css_portal = "$theme_path/portal.css";
$css_project = "$theme_path/project.css";
$css_ressource = "$theme_path/resource.css";
$css_time = "$theme_path/time.css";

// Colors used //
$col_ok = "EE9B00";       // (only used in deal for date alamr!)
$col_error = "FF0000";    // (only used in deal for date alamr!)

// accounting stuff colors 
$col_client = "00FF00"; // facture client (vert)
$col_frs = "FF0000";    // facture fournisseur (rouge)
$col_balance_neg = "FF0000";  // when the balance is <= 0  (red)
$col_balance_pos = "00FF00";  // when the balance is >  0  (green)


////////////
// Images //
////////////
$ico_principal      = "$theme_path/aliacom.jpg";  // BIG LOG
$ico_contact        = "$theme_path/contact.gif";            // Contact Icon
$ico_obm            = "$theme_path/obm.jpg";                    // OBM icon
$ico_validate	    = "$theme_path/oui2.gif";
$ico_cancel	    = "$theme_path/non2.gif";
$ico_add 	    = "$theme_path/ico_select04.gif";
$ico_delete	    = "$theme_path/ico_delete.gif";

// Menus Icons
$ico_contact        = "$theme_path/ico_contact.gif";         // Contact Icon
$ico_contact_new    = "$theme_path/ico_contact_new.gif";     // New Contact Icon
$ico_lead           = "$theme_path/ico_lead.gif";            // Lead Icon
$ico_deal           = "$theme_path/ico_deal.gif";            // Deal Icon
$ico_parentdeal     = "$theme_path/ico_deal.gif";            // Deal Icon
$ico_deal_new       = "$theme_path/ico_deal_new.gif";        // New Deal Icon
$ico_company        = "$theme_path/ico_company.gif";         // Company Icon
$ico_change_company = "$theme_path/ico_change_company.gif";  // Contract Icon
$ico_list           = "$theme_path/ico_list.gif";            // List Icon
$ico_agenda         = "$theme_path/ico_agenda.gif";          // Agenda Icon
$ico_todo           = "$theme_path/ico_todo.png";            // Todo Icon
$ico_publication    = "$theme_path/ico_publication.gif";     // Publication Icon
$ico_publication_new= "$theme_path/ico_publication_new.gif";     // Publication Icon
$ico_clipboard      = "$theme_path/clipoard.jpg";

$ico_time           = "$theme_path/ico_time.gif";
$ico_project        = "$theme_path/ico_project.png";
$ico_contract       = "$theme_path/ico_phone.gif";
$ico_incident       = "$theme_path/ico_incident.gif";

$ico_account        = "$theme_path/ico_account.gif";
$ico_invoice        = "$theme_path/ico_invoice.gif";
$ico_payment        = "$theme_path/ico_payment.gif";
$ico_document       = "$theme_path/ico_document.gif";
$ico_document_new   = "$theme_path/ico_document_new.gif";
$ico_statistic      = "$theme_path/ico_stat.gif";
//$ico_time           = "$theme_path/ico_time.gif";
//$ico_project        = "$theme_path/ico_project.png";
$ico_user           = "$theme_path/ico_user.gif";
$ico_add_user       = "$theme_path/ico_user_new.gif";
$ico_resource       = "$theme_path/ico_resource.gif";
$ico_add_resource   = "$theme_path/ico_resource_new.gif";
$ico_group          = "$theme_path/ico_group.gif";
$ico_add_group      = "$theme_path/ico_group_new.gif";
$ico_resourcegroup  = "$theme_path/ico_resourcegroup.gif";
$ico_add_resourcegroup = "$theme_path/ico_resourcegroup_new.gif";
$ico_setting        = "$theme_path/ico_setting.gif";

// Admin icons
$ico_admin          = "$theme_path/ico_admin.gif";
$ico_code           = "$theme_path/ico_code.gif";
$ico_pref           = "$theme_path/ico_pref.gif";
$ico_data           = "$theme_path/ico_data.gif";
$ico_lang           = "$theme_path/ico_lang.gif";
$ico_ref            = "$theme_path/ico_ref.gif";

// OBM agDISPLAY Icons
$ico_export         = "$theme_path/ico_export.gif";
$ico_export_all     = "$theme_path/ico_export_all.gif";
$ico_begin          = "$theme_path/ico_begin.gif";
$ico_end            = "$theme_path/ico_end.gif";
$ico_previous       = "$theme_path/ico_previous.gif";
$ico_next           = "$theme_path/ico_next.gif";

$ico_created        = "$theme_path/ico_created.gif";
$ico_updated        = "$theme_path/ico_updated.gif";

$ico_browse         = "$theme_path/ico_directory_browse.gif";
$ico_browse_file    = "$theme_path/ico_file_browse.gif";
$ico_directory_open = "$theme_path/ico_directory_opened.gif";
$ico_directory_close= "$theme_path/ico_directory_closed.gif";
$ico_directory      = "$theme_path/ico_directory.gif";
$ico_del_directory  = "$theme_path/ico_del_directory.gif";
$ico_file           = "$theme_path/ico_file.gif";

$ico_web            = "$theme_path/ico_web.gif";        // WWW Icon
$ico_mail           = "$theme_path/ico_mail.gif";       // Mail Icon
$ico_arrow_up       = "$theme_path/arrow_up.gif";       // The up arrow
$ico_arrow_down     = "$theme_path/arrow_down.gif";     // The down arrow
$ico_crow           = "$theme_path/crow-red.gif";
$ico_ball           = "$theme_path/ball-green.gif";
$ico_back           = "$theme_path/arrow_back.gif" ;
$ico_forbidden      = "$theme_path/forbidden.gif";
$ico_warning        = "$theme_path/warning.gif";

//Agenda Icons
$ico_confirm_mail   = "$theme_path/ico_confirmmail.gif";
$ico_new_event      = "$theme_path/ico_calendar_new.gif";
$ico_event          = "$theme_path/ico_calendar_evt.gif";
$ico_left_day       = "$theme_path/left_day.gif";
$ico_right_day      = "$theme_path/right_day.gif";
$ico_spacer         = "$theme_path/spacer.gif";
$ico_calendar_day   = "$theme_path/ico_calendar_day.gif";
$ico_calendar_user0 = "$theme_path/ico_calendar0.gif";
$ico_calendar_user1 = "$theme_path/ico_calendar1.gif";
$ico_calendar_user2 = "$theme_path/ico_calendar2.gif";
$ico_calendar_user3 = "$theme_path/ico_calendar3.gif";
$ico_calendar_user4 = "$theme_path/ico_calendar4.gif";
$ico_calendar_user5 = "$theme_path/ico_calendar5.gif";
$ico_calendar_nouser= "$theme_path/ico_calendarno.gif";
$ico_mini_larrow    = "$theme_path/arrows_l_still.gif";
$ico_mini_larrow2   = "$theme_path/arrows_l_anim.gif";
$ico_mini_rarrow    = "$theme_path/arrows_r_still.gif";
$ico_mini_rarrow2   = "$theme_path/arrows_r_anim.gif";
$ico_mini_cal       = "$theme_path/calendar.png";
$ico_cal_user1      = "$theme_path/ico_cal_user1.gif";
$ico_cal_user2      = "$theme_path/ico_cal_user2.gif";
$ico_cal_user3      = "$theme_path/ico_cal_user3.gif";
$ico_cal_user4      = "$theme_path/ico_cal_user4.gif";
$ico_cal_user5      = "$theme_path/ico_cal_user5.gif";
$ico_cal_user6      = "$theme_path/ico_cal_user6.gif";
$ico_cal_resource1  = "$theme_path/ico_cal_res1.gif";
$ico_cal_resource2  = "$theme_path/ico_cal_res2.gif";
$ico_cal_resource3  = "$theme_path/ico_cal_res3.gif";
$ico_cal_resource4  = "$theme_path/ico_cal_res4.gif";
$ico_cal_resource5  = "$theme_path/ico_cal_res5.gif";
$ico_cal_resource6  = "$theme_path/ico_cal_res6.gif";
// Export icons
$ico_html = "$theme_path/ico_netscape.png";
$ico_excel = "$theme_path/ico_excel.png";
$ico_rtf = "$theme_path/ico_label.png";
//List
$ico_minus_on = "$theme_path/ico_minus_on.png";
$ico_minus_off = "$theme_path/ico_minus_off.png";
$ico_plus_on = "$theme_path/ico_plus_on.png";
$ico_plus_off = "$theme_path/ico_plus_off.png";
$ico_update = "$theme_path/modify.png";
//Portal Icons 

$ico_agenda_portal = "$theme_path/ico_portal_agenda.gif";
$ico_time_portal = "$theme_path/ico_portal_time.gif";
$ico_contract_portal = "$theme_path/../images/1px.gif";
$ico_incident_portal = "$theme_path/../images/1px.gif";
$ico_warning = "$theme_path/ico_warning.gif";
