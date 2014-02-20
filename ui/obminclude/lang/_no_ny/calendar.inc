<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Norvegian                                                //
//     - File     : calendar.inc                                             //
// 2001-06-28 by Francois Bloque                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$l_calendar = "Calendar";
$l_users = "Users";
$l_groups = "Groups";
$l_title = "Title";
$l_private_description = "This is a private event, you cannot see the details";
$l_back_mono = "Back to your own view";

$l_go = "Go";
$l_high = "High";
$l_low = "Low";
$l_medium = "Medium";

$l_occupied_day = "Whole day";
$l_user_right = "Manage rights for";

$l_format = "(AAAA-MM-JJ)";
$l_repeat = "Repeatition";
$l_periodicity = "Frequency";
$l_repeatkind = "Frequency type";
$l_repeatdays = "Repetition days (weekly type)";
$l_date_repeatend = "End repetition date";
$l_too_much_user = "No more than six users";
$l_waiting_events = "Waiting events";
$l_calendar_visible = "Select calendars";
$l_calendar_displayed = "Calendars displayed";
$l_repeatdays2 = "Repeatition days";
$l_exception = "Exceptions";

$l_link_events = "linked events with this category.";
$l_accepted = "Accept";
$l_wait = "Waiting";
$l_free = "Free but restricted";
$l_possible = "Free";
$l_occupied = "Unavailable";
$l_daily = "daily";
$l_weekly = "weekly";
$l_monthlybydate = "monthly (date)";
$l_monthlybyday = "monthly (day)";
$l_yearly = "yearly";
$l_repeatkind = "Repeatition kind";
$l_from = "from";
$l_to = "to";
$l_event_duration = "Meeting duration";
$l_conflicts = "Conflict(s) found";
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
$l_force_insert = "Force insert";
$l_refuse_insert = "Refuse event";
$l_cancel_insert = "Wait...";
$l_category_update = "Update Category";
$l_category_checkdelete = "Check or Delete Category";
$l_category_delete = "Delete Category";
$l_category_insert = "Insert Category";
$l_calendar_select_group = "Select the groups.";

$l_force = "Force the event insertion";
$l_carac = "Caracteristics";
$l_meeting_perform = "Seek for free crenels";

$l_confirm_delete ="This operation will remove all the occurences of this event for all the users, are you sure to want to continue?";
$l_confirm = "Confirm";
$l_cancel = "Cancel";
$l_update_warning = "Warning : some conflicts have been detected";
$l_insert_warning = "Warning : some conflicts have been detected";
$l_update_error = "Conflicts have been detected, update cancelled";
$l_insert_error = "Conflicts have been detected, insertion cancelled";

// Messages
$l_sendamail = "Force the notification by mail.";
$l_select_contacts = "You have to select an user or group.";
$l_select_repeat_day = "You have to select the repetition days.";
$l_conflict_events = "Le ou les créneaux horaires choisis sont occupés pour "; 
$l_fill_title = "You have to fill the [ $l_title ] field";
$l_fill_dateend = "You have to fill the [ $l_dateend ] field";
$l_fill_datebegin = "You have to fill the field [ $l_datebegin ]";
$l_err_begin_end = "End date must be after begin date !";
$l_err_end_repeat = "Repetion end must be after end date";
$l_err_end_repeat2 = "The max duration of a repetition is one year";
$l_err_end_repeat3 = "The max duration of a repetition is ten years when the repeatition kind is yearly";
$l_err_days_repeat = "You must check one or more repeatition day.";
$l_err_days_repeat_not_weekly = "You can give repeatition days only on weekly repeatition.";
$l_err_weekly_repeat = "For weekly repeatition, the end of the repeatition must at least be seven day after the end of the event.";
$l_category_link_company = "These companies are referenced as";
$l_category_link_company_no = "No company referenced as";
$l_category_cant_delete = "The category can't be deleted !";
$l_category_can_delete = "The category can be deleted.";

$l_unknown="Inconnu";

$l_category_manage = "Calendar Category Management";
$l_category_exist = "Existing Categorys";
$l_category_new = "New Category";
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
$l_add_event_mail_head = "New event on OBM :";
$l_update_event_mail_head = "Event updated on OBM :";
$l_delete_event_mail_head = "Event deleted on OBM :";

$l_event_mail_body = "Date : ";
$l_update_event_mail_body = "Instead of : ";
$l_delete_event_mail_body = "Date : ";
$l_confirm_mail = "Confirm+Force notification";
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

//occurence consultation
$l_consult_occurrence = "Consult this event"; 
$l_consult_occurrence_message = "Consult only this event or the series ?";
$l_consult_event_occurrence = "Consult recurring event"; 
$l_consult_all_occurrences = "Consult series"; 

$l_resource_rights_information = "Read right on selected resource(s) is required";