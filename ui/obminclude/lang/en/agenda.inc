<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : agenda.inc                                               //
// 2002-01-06 by Pierre Baudracco                                            //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_agenda = "Calendar";
$l_users = "Users";
$l_groups = "Groups";
$l_title = "Title";
$l_priority = "Priority";
$l_private = "Private";
$l_public  = "Public";
$l_category = "Category";
$l_description = "Description";
$l_state = "Status";
$l_back_mono = "Back to your own view";

$l_high = "High";
$l_low = "Low";
$l_medium = "Medium";

$l_datebegin = "Begin date";
$l_dateend = "End date";
$l_timebegin = "Begin time";
$l_timeend = "End time";
$l_occupied_day = "Whole day";

$l_format = "(AAAA-MM-JJ)";
$l_repeat = "REPETITION";
$l_periodicity = "Frequency";
$l_repeatkind = "Frequency type";
$l_repeatdays = "Repetition days (weekly type)";
$l_use_endrepeat = "End repetition date";
$l_too_much_user = "No more than six users";
$l_waiting_events = "Waiting events";
$l_user_calendar = "Multi-user view";

$l_accepted = "Accept";
$l_rejected = "Reject";
$l_waiting  = "Waiting for confirmation";
$l_short_accepted = "A";
$l_short_rejected = "R";
$l_short_waiting  = "Wait";
$l_wait = "Waiting";
$l_free = "Free but restricted";
$l_possible = "Free";
$l_occupied = "Unavailable";
$l_daily = "daily";
$l_weekly = "weekly";
$l_monthlybydate = "monthly (date)";
$l_monthlybyday = "monthly (day)";
$l_yearly = "yearly";

$l_from = "from";
$l_to = "to";
$l_event_duration = "Meeting duration";

$l_conflicts = "Conflit(s) found";
$l_date = "Date";
$l_user = "User";
$l_meeting_legend = "Legend";
// actions 
$l_insert = "Insert the event";
$l_update = "Update the event";
$l_accept = "Accept";
$l_reject = "Refuse";
$l_select = "Select";
$l_repeat_update = "Update all occurrences of this appointment";
$l_go_to = "Go to";
$l_refuse = "Refuse";
$l_change_state = "Your event participation";
$l_force_all = "Force all";
$l_replace_all = "Replace all";
$l_cancel_all = "Cancel all";
$l_force_event = "Force";
$l_cancel_event = "Cancel";
$l_replace_event = "Replace";
$l_category_update = "Update Category";
$l_category_checkdelete = "Check or Delete Category";
$l_category_delete = "Delete Category";
$l_category_insert = "Insert Category";
$l_force = "Force the event insertion";
$l_carac = "Caracteristics";
$l_meeting_perform = "Seek for free crenels";


$l_confirm_delete ="This operation will remove all the occurences of this event for all the users, are you sure to want to continue?";
$l_confirm = "Confirm";
$l_cancel = "Cancel";
$l_insert_ok = "Insertion successful";
$l_update_ok = "Update successful";
$l_update_warning = "Warning : some conflicts have been detected";
$l_insert_warning = "Warning : some conflicts have been detected";
$l_update_error = "Conflicts have been detected, update cancelled";
$l_insert_error = "Conflicts have been detected, insertion cancelled";


$l_daysofweek = array (	'Sunday',
			'Monday',
			'Tuesday',
			'Wednesday',
			'Thursday',
			'Friday',
			'Saturday');

$l_daysofweekshort = array (	'Sun',
				'Mon',
				'Tue',
				'Wed',
				'Thu',
				'Fri',
				'Sat');

$l_daysofweekreallyshort = array ('S','M','T','W','T','F','S');

$l_monthsofyear = array ('January','February', 'March','April','May','June',
                         'July','August','September','October','November',
			 'December');
$l_monthsofyearshort = array ('jan','feb','mar','apr','may','jun','jul',
			      'aug','sep','oct','nov','dec');

// Messages
$l_sendamail = "Force the notification by mail.";
$l_select_contacts = "Vous devez choisir au moins un contact ou un groupe.";
$l_select_repeat_day = "Vous devez s&eacute;lectionner le ou les jours de r&eacute;p&eacute;tition.";
$l_conflict_events = "Le ou les cr&eacute;neaux horaires choisis sont occup&eacute;s pour "; 
$l_fill_title = "You have to fill the [ $l_title ] field";
$l_fill_dateend = "You have to fill the [ $l_dateend ] field";
$l_fill_datebegin = "You have to fill the field [ $l_datebegin ]";
$l_err_datebegin = "[ $l_datebegin ] format incorrect !";
$l_err_dateend = "[ $l_dateend ] format incorrect !";
$l_err_repeat = "[ $l_use_endrepeat ] format incorrect !";
$l_err_begin_end = "End date must be after begin date !";
$l_err_end_repeat = "Repetion end must be after end date";
$l_err_end_repeat2 = "The max duration of a repetition is one year";
$l_err_end_repeat3 = "The max duration of a repetition is ten years when the repeatition kind is yearly";
$l_err_days_repeat = "You must check one or more repeatition day.";
$l_err_weekly_repeat = "For weekly repeatition, the end of the repeatition must at least be seven day after the end of the event.";
$l_category_insert_ok = "The category has been inserted successfully.";
$l_category_insert_error = "The insertion of the category has failed !";
$l_category_delete_ok = "The category has been deleted successfully.";
$l_category_delete_error = "The deletion of the category has failed !";
$l_category_update_ok = "The category has been updated successfully.";
$l_category_update_error = "The update of the category has failed !";
$l_category_link_company = "These companies are referenced as";
$l_category_link_company_no = "No company referenced as";
$l_category_cant_delete = "The category can't be deleted !";
$l_category_can_delete = "The category can be deleted.";

$l_unknown="Inconnu";

$l_category_manage = "Calendar Category Management";
$l_category_exist = "Existing Categorys";
$l_category_new = "New Category";
$l_header_right = "Right management";
$l_header_meeting = "New meeting";
$l_header_admin = "Administration";
$l_header_export = "Export";
$l_authorize_list = "Authorized people";
$l_denie_list = "Unauthorized people";
$l_read_permission = "Read access management";
$l_write_permission = "Write access management";
$l_everyone = "Authorized everybody";
$l_noone = "Authorized nobody";
$l_list = "Authorized list people";
$l_authorize = "&lt;&lt;&lt;&lt;";
$l_deny = "&gt;&gt;&gt;&gt;";

//Mail
$l_add_event_mail_head = "A new event has been set for you on the obm application.\nThis event title is :";
$l_update_event_mail_head = "A event has been updated for you on the obm application.\nThis event title is :";
$l_delete_event_mail_head = "A event has been deleted for you on the obm application.\nThis event title was :";

$l_event_mail_body = "This event will take place : ";
$l_update_event_mail_body = "Instade of :";
$l_delete_event_mail_body = "This event should have take place : ";
$l_confirm_mail = "Confirm+send a mail";
$l_add_event_mail_subject = "New event on obm";
$l_update_event_mail_subject = "Updated event on obm";
$l_delete_event_mail_subject = "Deleted event on obm";
// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_interval_null = "Duration cannot be null";
$mj_fill_title_event = "\\n Vous devez saisir un titre pour le rdv.";
$mj_fill_time_begin = "\\n Vous devez saisir une heure de debut.";
$mj_fill_time_end = "\\n Vous devez saisir une heure de fin.";
$mj_fill_valid_time_begin = "\\n Vous devez saisir une heure de debut valide.";
$mj_fill_valid_time_end = "\\n Vous devez saisir une heure de fin valide.";
$mj_delete_confirm = "Confirmer la suppression du rdv ?";
$mj_invalid_day = "Ce jour n'existe pas pour le mois selectionne.";
$mj_date_incompatibility = "La date de fin ne peut etre anterieure a la date de debut.";
$mj_repeat_date_incompatibility = "La date de fin de repetition ne peut etre anterieure a la date de debut."; 
$mj_event_delete_confirm = "Confirmer la suppression du rdv ?"; 
$mj_event_delete_all_confirm = "Confirmer la suppression du rdv et de toutes ses repetitions ?"; 
$l_j_category_create = "Please specify a label for the new category";
$l_j_category_update = "Please specify a new label, and select a category for the update";
$l_j_category_delete = "Please select a category to delete it";


?>
