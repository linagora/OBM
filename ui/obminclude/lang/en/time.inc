<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : time.inc                                                 //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//for list
$l_task_fac = "Factured Tasks";
$l_task_int = "Internal Tasks";


// Database fields
$l_taskdate="Task date";
$l_tasklabel="Task label";
$l_deallabel="Project/Deal";
$l_task_del="Remove ?";
$l_company = "Company";
$l_delete = "Remove Tasks";
$l_length="Duration";
$l_total_length="Total duration";
$l_total_before="Previous total duration";
$l_total_period="Total duration on this period";
$l_total="Total";
$l_day="Day";
$l_task_del="Remove.";
$l_week="Week";
$l_month="Month";
$l_user="User";
$l_all_users="All ?";
$l_see = "See";
$l_valid = "Validation";
$l_validate_task = "Validate";
$l_add = "Add";
$l_update = "Update";
$l_period="Period";

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["task_date"] = $l_taskdate;
$fieldnames["task_label"] = $l_tasklabel;
$fieldnames["task_deal_label"] = $l_deallabel;
$fieldnames["task_company_name"] = $l_company;
$fieldnames["task_length"] = $l_length;
$fieldnames["task_totallength"] = $l_total_length;
$fieldnames["tasktype_label"] = $l_tasktype;
$fieldnames["task_totalbefore"] = $l_total_before;
$fieldnames["task_totalperiod"] = $l_total_period;
$fieldnames["task_id"] = $l_update;
$fieldnames["contact_function"] = $l_function;
$fieldnames["contact_phone"] = $l_phone;
$fieldnames["contact_homephone"] = $l_hphone;
$fieldnames["contact_mobilephone"] = $l_mphone;
$fieldnames["contact_email"] = $l_email;
// Calculated fields
$fieldnames["contact_company_name"] = $l_company;

$l_week_prev = "Previous week";
$l_week_next = "Next week";
$l_month_next = "Next month";
$l_month_prev = "Previous month";
$l_date = "Date:";
$l_project = "Project:";
$l_task = "Task:";
$l_label = "Label:";
$l_duration = "Duration:";

// Titres
$l_time = "Time management";

// Messages
$l_projects_manager = "Projects managers can validate tasks here:";
$l_found_task = "Here are realised and submited tasks for the current week:";
$l_found = "found tasks.";
$l_no_found = "No task for this user";
$l_addtask_start = "Put information for the current week. Duration is fraction of day (between 1/";
$l_addtask_end = " and 1).";
$l_update_task = "Here is the form where you can update your task:";
$l_total_day = "Summary by day. If a day is complete,<BR> Confirm it by selecting complet days and validate form.";
$l_total_deal = "Summary by task:";
$l_total_month = "Summary by month:";

// Type de tâches à rechercher
$l_tasktype = "Task type";
$l_task_all = "All";
$l_task_uservalid = "Validated by an admin or a user";
$l_task_adminvalid= "Validated by an admin only";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_j_select_tasktype = "You must select a task type.";
$l_j_select_project = "You must select a projet with a billed task type.";
$l_delete_confirm = "Confirm task(s) deletion ?";

