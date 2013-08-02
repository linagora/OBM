<script language="php">
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
// OBM - File  : theme.inc                                                   //
//     - Desc : Resources linkage                                            //
// 2006-11-08 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

global $path, $resources_path;

$theme_path = "$resources_path/themes/default/images";
$internal_theme_path = "$path/../resources/themes/default/images";

// CSS
$css_obm = "$resources_path/themes/default/styles/style.css";
$css_print_obm = "$resources_path/themes/default/styles/print.css";
$css_calendar = "$resources_path/themes/default/styles/calendar.css";
$css_contact = "$resources_path/themes/default/styles/contact.css";
$css_organizationalchart = "$resources_path/themes/default/styles/organizationalchart.css";
$css_portal = "$resources_path/themes/default/styles/portal.css";
$css_ext_color_picker = "$resources_path/themes/default/styles/mooRainbow.css" ;
$css_tools = "$resources_path/themes/default/styles/tools.css";
$css_host = "$resources_path/themes/default/styles/host.css";

// Templates 
$login_template = "$path/../resources/themes/default/templates/login.tpl";

// Internal resources
$ico_pdf_logo         = "$internal_theme_path/logo_obm_org.png";
$ico_pdf_private      = "$internal_theme_path/ico_pdf_private.png";
$ico_pdf_private_alt  = "$internal_theme_path/ico_pdf_private_alt.png";
$ico_pdf_meeting      = "$internal_theme_path/ico_pdf_meeting.png";
$ico_pdf_meeting_alt  = "$internal_theme_path/ico_pdf_meeting_alt.png";
$ico_pdf_periodic     = "$internal_theme_path/ico_pdf_calendar_repeat.png";
$ico_pdf_periodic_alt = "$internal_theme_path/ico_pdf_calendar_repeat_alt.png";
$ico_pdf_allday       = "$internal_theme_path/ico_pdf_allday.png";
$ico_pdf_allday_alt   = "$internal_theme_path/ico_pdf_allday_alt.png";

// Menus Icons
$ico_account        = "$theme_path/ico_account.gif";
$ico_admin          = "$theme_path/ico_admin.gif";
$ico_backup         = "$theme_path/ico_backup.gif";
$ico_calendar       = "$theme_path/ico_calendar.gif";          
$ico_big_calendar   = "$theme_path/ico_big_calendar.gif";          
$ico_code           = "$theme_path/ico_code.gif";
$ico_company        = "$theme_path/ico_company.gif";         
$ico_contact        = "$theme_path/ico_contact.gif";         
$ico_contract       = "$theme_path/ico_contract.gif";
$ico_big_contract   = "$theme_path/ico_big_contract.gif";
$ico_cv             = "$theme_path/ico_cv.gif";
$ico_control        = "$theme_path/ico_control.gif";
$ico_data           = "$theme_path/ico_data.gif";
$ico_deal           = "$theme_path/ico_deal.gif";            
$ico_big_deal       = "$theme_path/ico_big_deal.gif";            
$ico_document       = "$theme_path/ico_document.gif";
$ico_group          = "$theme_path/ico_group.gif";
$ico_host           = "$theme_path/ico_host.gif";
$ico_lang           = "$theme_path/ico_lang.gif";
$ico_lead           = "$theme_path/ico_lead.gif";            
$ico_big_lead       = "$theme_path/ico_big_lead.gif";            
$ico_list           = "$theme_path/ico_list.gif";    
$ico_mailbox        = "$theme_path/ico_mailbox.gif";
$ico_mailforward    = "$theme_path/ico_mailforward.gif";
$ico_mailshare      = "$theme_path/ico_mailshare.gif";
$ico_todo           = "$theme_path/ico_todo.gif";            
$ico_publication    = "$theme_path/ico_publication.gif";     
$ico_incident       = "$theme_path/ico_incident.gif";
$ico_big_incident   = "$theme_path/ico_big_incident.gif";
$ico_invoice        = "$theme_path/ico_invoice.gif";
$ico_big_invoice    = "$theme_path/ico_big_invoice.gif";
$ico_import         = "$theme_path/ico_import.gif";
$ico_parentdeal     = "$theme_path/ico_deal.gif";            
$ico_payment        = "$theme_path/ico_payment.gif";
$ico_pref           = "$theme_path/ico_pref.gif";
$ico_project        = "$theme_path/ico_project.gif";
$ico_big_project    = "$theme_path/ico_big_project.gif";
$ico_publication    = "$theme_path/ico_publication.gif";    
$ico_password       = "$theme_path/ico_password.gif";    
$ico_userpattern    = "$theme_path/ico_group_edit.png";
$ico_profile        = "$theme_path/ico_group.gif";
$ico_ref            = "$theme_path/ico_ref.gif";
$ico_resource       = "$theme_path/ico_resource.gif";
$ico_resourcegroup  = "$theme_path/ico_resourcegroup.gif";
$ico_report         = "$theme_path/ico_report.png";
$ico_samba          = "$theme_path/ico_samba.gif";
$ico_settings       = "$theme_path/ico_setting.gif";
$ico_big_settings   = "$theme_path/ico_setting.gif";
$ico_statistic      = "$theme_path/ico_stat.gif";
$ico_tools          = "$theme_path/ico_tools.gif";
$ico_webmail        = "$theme_path/ico_mailshare.gif";
$ico_time           = "$theme_path/ico_time.gif";
$ico_big_time       = "$theme_path/ico_big_time.gif";
$ico_very_big_time  = "$theme_path/ico_very_big_time.png";
$ico_todo           = "$theme_path/ico_todo.gif";
$ico_user           = "$theme_path/ico_user.gif";
$ico_vacation       = "$theme_path/ico_vacation.gif";
$ico_organizationalchart = "$theme_path/ico_organizational_chart.gif";
$ico_load            = "$theme_path/ico_load.gif";
$ico_phone          = "$theme_path/ico_phone.gif";

// Globals Icons
$ico_arrow_up       = "$theme_path/arrow_up.gif";
$ico_arrow_down     = "$theme_path/arrow_down.gif";
$ico_arrow_left     = "$theme_path/arrow_left.gif";
$ico_arrow_right    = "$theme_path/arrow_right.gif";
$ico_arrow_mount    = "$theme_path/arrow_single_up.gif";
$ico_arrow_umount   = "$theme_path/arrow_single_down.gif";
$ico_arrow_off      = "$theme_path/arrow_off.gif";
$ico_logout         = "$theme_path/logout.png"; 
$ico_login          = "$theme_path/arrow_off.gif";  
$ico_datepicker     = "$theme_path/ico_datepicker.png";
$ico_colorpicker    = "$theme_path/ico_colorpicker.gif";
$ico_delete         = "$theme_path/ico_trash.gif";
$ico_cancel         = "$theme_path/ico_delete.gif";
$ico_validate       = "$theme_path/ico_validate.gif";
$ico_update         = "$theme_path/ico_update.gif";
$ico_edit           = "$theme_path/ico_modify.gif";
$ico_ok             = "$theme_path/ico_validate.gif";
$ico_nok            = "$theme_path/ico_delete.gif";
$ico_lock           = "$theme_path/ico_lock.gif";
$ico_obm            = "$theme_path/logo_obm.png";
$ico_obm_org        = "$theme_path/logo_obm_org.jpg";
$ico_print          = "$theme_path/ico_print.gif";
$ico_web            = "$theme_path/ico_web.gif";
$ico_mail           = "$theme_path/ico_mail.gif";
$ico_plus           = "$theme_path/ico_plus.gif";
$ico_minus          = "$theme_path/ico_minus.gif";
$ico_nav_close      = "$theme_path/ico_nav_close.gif";
$ico_nav_open       = "$theme_path/ico_nav_open.gif";
$ico_add            = "$theme_path/ico_add.gif";
$ico_new            = "$theme_path/ico_add.gif";
$ico_scope          = "$theme_path/ico_scope.gif";
$ico_privacy        = "$theme_path/ico_privacy.gif";
$ico_manager        = "$theme_path/ico_manager.gif";
$ico_nophoto        = "$theme_path/ico_nophoto.png";
$ico_search         = "$theme_path/ico_search.gif";

// OBM Globals images and icons
$flag_de            = "$theme_path/flag_de.gif";
$flag_en            = "$theme_path/flag_en.gif";
$flag_es            = "$theme_path/flag_es.gif";
$flag_fr            = "$theme_path/flag_fr.gif";
$flag_it            = "$theme_path/flag_it.gif";
$flag_no_ny         = "$theme_path/flag_no_ny.gif";
$flag_ro            = "$theme_path/flag_ro.gif";
$flag_zh            = "$theme_path/flag_zh.gif";
$flag_vi            = "$theme_path/flag_vi.png";
$preview_default    = "$theme_path/preview_theme.gif";
$img_home           = "$theme_path/home_obm.png";

// Agenda Icons
$ico_resize                 = "$theme_path/ico_resize.gif";
$ico_previous               = "$theme_path/ico_previous.png";
$ico_far_previous           = "$theme_path/ico_far_previous.gif";
$ico_next                   = "$theme_path/ico_next.png";
$ico_far_next               = "$theme_path/ico_far_next.gif";
$ico_private                = "$theme_path/ico_private.gif";
$ico_private_alt            = "$theme_path/ico_private_alt.gif";
$ico_meeting                = "$theme_path/ico_meeting.gif";
$ico_meeting_alt            = "$theme_path/ico_meeting_alt.gif";
$ico_periodic               = "$theme_path/ico_calendar_repeat.gif";
$ico_periodic_alt           = "$theme_path/ico_calendar_repeat_alt.gif";
$ico_allday                 = "$theme_path/ico_allday.gif";
$ico_allday_alt             = "$theme_path/ico_allday_alt.gif";
$ico_extension_left         = "$theme_path/ico_calendar_extension_left.gif";
$ico_extension_right        = "$theme_path/ico_calendar_extension_right.gif";
// $ico_popup_close            = "$theme_path/ico_popup_close.gif";
$ico_comment                  = "$theme_path/ico_small_comment.png";
$ico_popup_close            = "$theme_path/ico_close.png";
$popup_queue                = "$theme_path/popup_queue.gif";
$ico_day_view               = "$theme_path/ico_calendar_day_view.gif";
$ico_week_view              = "$theme_path/ico_calendar_week_view.gif";
$ico_month_view             = "$theme_path/ico_calendar_month_view.gif";
$ico_day_view_current       = "$theme_path/ico_calendar_day_view_current.gif";
$ico_week_view_current      = "$theme_path/ico_calendar_week_view_current.gif";
$ico_month_view_current     = "$theme_path/ico_calendar_month_view_current.gif";
$ico_year_view_current      = "$theme_path/ico_calendar_year_view_current.gif";
$ico_list_view              = "$theme_path/ico_calendar_list.gif";
$ico_agenda_view            = "$theme_path/ico_calendar_agenda.gif";
$ico_planning_view          = "$theme_path/ico_calendar_planning.gif";
$ico_printable_view         = "$theme_path/ico_calendar_printable.gif";
$ico_list_view_current      = "$theme_path/ico_calendar_list_current.gif";
$ico_agenda_view_current    = "$theme_path/ico_calendar_agenda_current.gif";
$ico_planning_view_current  = "$theme_path/ico_calendar_planning_current.gif";
$ico_printable_view_current = "$theme_path/ico_calendar_printable_current.gif";
$ico_spinner                = "$theme_path/spinner.gif";
$ico_undo                   = "$theme_path/ico_trash.gif";
$ico_warning                = "$theme_path/ico_warning.gif";
$ico_html                   = "$theme_path/ico_html.png";
$ico_ical                   = "$theme_path/ico_ical.png";

// OrganizationalChart icon
$ico_outline_view = "$theme_path/ico_outline_view.gif";
$ico_outline_view_current = "$theme_path/ico_outline_view_current.gif";
$ico_organizationalchart_view = "$theme_path/ico_organizationalchart_view.gif";
$ico_organizationalchart_view_current = "$theme_path/ico_organizationalchart_view_current.gif";

// Company Icons
$ico_clipboard      = "$theme_path/ico_clipboard.gif";
$ico_google         = "$theme_path/ico_google.jpg";


// Project Icons
$ico_hierarchy      =  "$theme_path/ico_hierarchy.png";

// Document Icons 

$ico_folder_close   = "$theme_path/ico_folder_close.gif";
$ico_folder         = "$theme_path/ico_folder.gif";
$ico_folder_empty   = "$theme_path/ico_folder_empty.gif";
$ico_file           = "$theme_path/ico_file.gif";

// Contact Icons
$ico_sync           = "$theme_path/ico_sync.png";
$ico_unsync         = "$theme_path/ico_unsync.png";
$ico_sync_lock      = "$theme_path/ico_sync_lock.png";
$ico_unsync_lock    = "$theme_path/ico_unsync_lock.png";
$icons['edit']     = "$theme_path/edit.png";
$icons['delete']   = "$theme_path/delete.png";
$icons['others']   = "$theme_path/othersDropDown.png";
$icons['add']      = "$theme_path/addDropDown.png";
$icons['close']    = "$theme_path/ico_close.png";
$icons['dropdown'] = "$theme_path/dropDown.png";
