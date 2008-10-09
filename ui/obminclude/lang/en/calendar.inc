<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : calendar.inc                                             //
// 2002-01-06 by Pierre Baudracco                                            //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_calendar = "Calendar";
$l_attendees = "Attendees";
$l_users = "Users";
$l_owner = "Organizer";
$l_resources = "Resources";
$l_groups = "Groups";
$l_title = "Title";
$l_location = "Location";
$l_private_description = "This is a private event, you cannot see the details";
$l_back_mono = "Back to your own view";
$l_free_resources = "Free resources";
$l_go = "Go";
$l_high = "High";
$l_low = "Low";
$l_medium = "Medium";
$l_details = "Details";

$l_occupied_day = "Whole day";
$l_user_right = "Manage rights for";

$l_exception = "Exceptions";
$l_alerts = "Alerts";
$l_alerts_time = "Alerts (minutes before)";
$l_format = "(AAAA-MM-JJ)";
$l_repeat = "Repetition";
$l_periodicity = "Frequency";
$l_repeatkind = "Frequency type";
$l_repeatdays = "Repetition days (weekly type)";
$l_date_repeatend = "End repetition";
$l_too_much_user = "No more than six users";
$l_too_much_resource = "No more than six resources";
$l_waiting_events = "Waiting events";
$l_calendar_visible = "Select calendars";
$l_calendar_displayed = "Calendars displayed";
$l_resource_visible = "Select Resources";
$l_resource_displayed = "Resources displayed";
$l_resource_allocated = "Resources allocated";
$l_repeatdays2 = "Repetition days";
$l_repeatfrequency = "Frequency";
$l_everybody = "Everybody";
$l_everyresources = "Every resources";
$l_all_day = "All day";
$l_begin = "Beginning";
$l_end = "End";
$l_link_events = "linked events with this category.";
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
$l_daily_unit = "Every %s days";
$l_weekly = "weekly";
$l_weekly_unit = "Every %s weeks";
$l_monthlybydate = "monthly (date)";
$l_monthlybyday_unit = "Every %s months";
$l_monthlybyday = "monthly (day)";
$l_monthlybydate_unit = "Every %s months";
$l_yearly = "yearly";
$l_yearly_unit = "Every %s years";
$l_repeatkind = "Repetition kind";
$l_from = "from";
$l_to = "to";
$l_event_duration = "Meeting duration";
$l_conflicts = "Conflict(s) found";
$l_date = "Date";
$l_user = "User";
$l_meeting_legend = "Legend";
$l_selection = "Selection";
$l_task = "Task";
$l_category_filter = "Filter by category";
$l_events = "Events";

$l_collapse_all = "Collapse all";
$l_expand_all = "Expand all";
$l_edit = "Edit";

$l_view = "View";
$l_views = "Views";
$l_select_view = "Select view";
$l_view_label = "View label";
$l_view_save = "Save view";
$l_view_delete = "Delete view";

// actions 
$l_insert = "Insert the event";
$l_update = "Update the event";
$l_accept = "Accept";
$l_reject = "Refuse";
$l_select = "Select";
$l_repeat_update = "Update all occurrences of this appointment";
$l_go_to = "Go to";
$l_refuse = "Refuse";
$l_state = "Status";
$l_change_state = "Your event participation";
$l_force_insert = "Force insert";
$l_refuse_insert = "Refuse event";
$l_cancel_insert = "Wait...";
$l_calendar_select_group = "Select the groups.";
$l_A = "A";
$l_W = "W";
$l_R = "R";
$l_force = "Force the event insertion";
$l_carac = "Caracteristics";
$l_meeting_perform = "Seek for free time slots";

$l_event_color_tt = "Event's colors are the ones defined during the event creation.";

$l_confirm_delete ="This operation will remove this occurence of this event for all the users, are you sure to want to continue?";
$l_confirm = "Confirm";
$l_cancel = "Cancel";
$l_update_warning = "Warning: some conflicts have been detected";
$l_insert_warning = "Warning: some conflicts have been detected";
$l_update_error = "Conflicts have been detected, update cancelled";
$l_insert_error = "Conflicts have been detected, insertion cancelled";
$l_view_type = "View type";

// Messages
$l_sendamail = "Force notification by mail.";
$l_select_contacts = "You have to select a user or group.";
$l_select_repeat_day = "You have to select the repetition days.";
$l_conflict_events = "Le ou les créneaux horaires choisis sont occupés pour "; 
$l_fill_title = "You have to fill the [ $l_title ] field";
$l_fill_dateend = "You have to fill the [ $l_dateend ] field";
$l_fill_datebegin = "You have to fill the field [ $l_datebegin ]";
$l_err_no_calendar_selected = "No calendar selected";
$l_err_begin_end = "End date must be after begin date!";
$l_err_end_repeat = "Repetion end must be after end date";
$l_err_end_repeat2 = "The max duration of a repetition is one year";
$l_err_end_repeat3 = "The max duration of a repetition is ten years when the repetition kind is yearly";
$l_err_days_repeat = "You must check one or more repetition day.";
$l_err_days_repeat_not_weekly = "You can give repetition days only on weekly repetition.";
$l_err_weekly_repeat = "For weekly repetition, the end of the repetition must at least be seven day after the end of the event.";
$l_err_monthly_dateend_repeat = "Monthly repetition date end must be at least one month later than the event date end.";
$l_err_yearly_dateend_repeat = "Yearly repetition date end must be at least one year later than the event date end.";
$l_err_repeatfrequency = "Frequency is invalid";
$l_event_cant_delete = "You can only delete your own events!";

$l_fill_view_label = "You have to fill the [$l_view_label] field";
$l_confirm_delete_view = "Do you realy want to delete this view?";
$l_no_sel_view = "Can't delete: no selected view";
$l_conflict_view_label = "$l_view_label: Conflict has been detected";

$l_unknown = "Inconnu";

$l_header_planning = "Planning";
$l_header_meeting = "New meeting";
$l_header_admin = "Administration";
$l_header_waiting_events = "Waiting events";
$l_header_export = "Export";
$l_authorize_list = "Authorized people";
$l_denie_list = "Unauthorized people";
$l_read_permission = "Read access management";
$l_write_permission = "Write access management";
$l_everyone = "Authorized everybody";
$l_noone = "Authorized nobody";
$l_authorize = "Authorize";
$l_deny = "&gt;&gt;&gt;&gt;";
$l_periodic = "Periodic";
//Mail
$l_add_event_mail_head = "New event on OBM:";
$l_update_event_mail_head = "Event updated on OBM:";
$l_delete_event_mail_head = "Event deleted on OBM:";

$l_today_date = "Today";
$l_update_event_mail_body = "Instead of: ";
$l_delete_event_mail_body = "Date: ";
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

$l_view_agenda_desc = 'Calendar view, this view is editable';
$l_view_planning_desc = 'Planning view, this view cannot be edited';
$l_view_printable_desc = 'Printable view, this view cannot be edited';
$l_view_list_desc = 'List view, this view cannot be edited';
$l_view_week_desc = 'Week view';
$l_view_day_desc = 'Day view ';
$l_view_month_desc = 'Month_view';
$l_expand_all = "Expand All";
$l_edit_full = "Advanced";
$l_confirm_delete_all ="This operation will remove all occurences of this event for all the users, are you sure to want to continue?";
$l_ics_insert = 'Import';
$l_ics_file = 'ICS file to import';
$l_add_exception_mail_body = 'Updated event on OBM: %s';
$l_delete_exception_mail_subject = 'Updated event on OBM: %s';
$l_update_state_mail_subject = 'Attendees update on OBM event: %s';

$l_resource_group_search = "Slot is free if";
$l_resources_one_freebusy = "One resource is free by group";
$l_resources_all_freebusy = "All resources are free";
  
$l_add_event_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
NEW APPOINTMENT
------------------------------------------------------------------

You are invited to participate to an appointment from %s to %s,
on the subject of %s (location: %s).


:: More information about this appointment : 
%s
:: To accept this appointment : 
%s
:: To refuse this appointment : 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by %s %s from OBM 
%s";

$l_add_resource_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
NEW RESOURCE RESERVATION!
------------------------------------------------------------------

A new resource reservation was scheduled on %s to %s for %s
(location: %s).


:: To accept or refuse this reservation: 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by %s %s from OBM 
%s";

$l_update_event_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
UPDATED APPOINTMENT!
------------------------------------------------------------------

The appointment %s, initially scheduled from %s to %s (location %s),
was updated and will take place from %s to %s (location: %s).

:: More information about this update: 
%s
:: To accept this update: 
%s
:: To refuse this update: 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by %s %s from OBM 
%s";

$l_update_resource_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
UPDATED RESOURCE RESERVATION!
------------------------------------------------------------------

The resource reservation %s, initially scheduled from %s to %s
(location: %s) was updated and will take place from %s to %s
(location: %s).

:: To accept or refuse this update: 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by %s %s from OBM 
%s";

$l_delete_event_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
DELETED APPOINTMENT!
------------------------------------------------------------------

The appointment %s, initially scheduled on %s to %s (location: %s),
was deleted.

:: To contact the organiser of this event: 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by %s %s from OBM 
%s";

$l_delete_resource_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
CANCELLED RESOURCE RESERVATION !
------------------------------------------------------------------

The resource reservation %s, initially scheduled from %s to %s
(location: %s), was cancelled.

:: To contact the organiser of this event: 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by %s %s from OBM 
%s";

$l_delete_exception_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
OCCURENCE(S) RESTORED!
------------------------------------------------------------------

One or more occurrences of the appointment %s, initially cancelled 
(%s) were restored.

:: For more information: 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by %s %s from OBM 
%s";

$l_add_exception_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
OCCURENCE(S) CANCELLED!
------------------------------------------------------------------

One or more occurrences of the appointment %s, initially scheduled
on %s were cancelled.

:: For more information: 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by %s %s from OBM 
%s";

$l_update_state_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
ATTENDEE STATE UPDATED
------------------------------------------------------------------

The attendee %s to %s have changed to %s.

:: For more information about this appointment: 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by %s %s from OBM 
%s";

$l_alert_mail_body = 
"This email was automatically sent by OBM
------------------------------------------------------------------
IMPENDING APPOINTEMENT
------------------------------------------------------------------

The appointment %s(%s) will take place in %s minutes (location: %s).

:: For more information about this appointment: 
%s

-------------------------------------------------------------------
This message was sent on %s at %s by OBM 
%s";


