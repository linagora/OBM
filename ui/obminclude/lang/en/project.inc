<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : projet.inc                                               //
// 2003-07-10 Aliacom - Bastien Continsouzas                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_tasktype = "Category";
$l_task = "Task";
$l_soldtime = "Sold time";
$l_estimatedtime = "Estimated time";
$l_shortname = "Short name";
$l_sold = "Sold";
$l_estimated = "Estimated";
$l_allotted = "Allocated";
$l_projected = "Projected";
$l_used = "Used";
$l_missing = "Missing";

$l_alloestimated = "Estimated time allocation";
$l_usedestimated = "Estimated time usage";
$l_progress = "Project progress";
$l_progeval = "Advance/Lateness evaluation";

// Labels et textes
$l_parent = "Parent task";
$l_soldtime = "Sold time";
$l_projtime = "Projected time";
$l_total = "Total";
$l_resume = "Résumé";
$l_tasks = "Tasks";
$l_reftask_manage = "Manage reference tasks"; 
$l_delete_reftask = "You're about to delete a Reference Task, do you want to continue?";
$l_reftask = 'Reference Task';

// Titres
$l_members_info = "Project members";
$l_progressinfos = "Progress evaluation : time (used / missing / projected)";
$l_memberstimeinfos = "Fill in the working length (in days) projected for each person assigned to a task";
$l_fill_advance_form = "Fill in the working time : planned / remaining";
$l_task_new = "New task";
$l_task_update = "Update task";
$l_task_list = "Defined tasks";
$l_progress_info = "Fill in the working length (in days) missing for each person to complete a task";
$l_select_project = "Select a project";

// Actions
$l_insert = "Create the project";
$l_update = "Update the project";
$l_delete = "Delete the project";
$l_progress_update = "Update the project progress";
$l_task_add = "Add the task";
$l_task_upd = "Update task";
$l_task_del = "Delete selected tasks";

// Messages
$l_no_display = "Fill the search fields and press < Find > to retrieve the projects.";
$l_found = "project(s) found.";
$l_no_found = "No project found matching the query !";
$l_no_tasks = "No tasks defined";
$l_no_members = "No members defined";
$l_no_allocation = "The project members have not been assigned to a task yet";
$l_link_timetask = "The project is used in time management";
$l_link_timetask_no = "The project is not used in time management";
$l_cant_delete = "The project can't be deleted !";
$l_can_delete = "The project can be deleted.";
$l_no_task_del = "No task to remove";
$l_ptask_same_task = "The parent task can't be the same as the task !";
$l_ptask_and_parent = "A parent task can't have a parent !";
$l_ptask_has_parent = "The task given as parent has a parent now !";
$l_ptask_has_user = "The task given as parent is assigned to user !";

$l_query_error = "Problem executing the query";
$l_del_member_sel = "remove these members";
$l_member_added = "member(s) added";
$l_member_removed = "member(s) removed";
$l_no_member_add = "No member to add";
$l_no_member_del = "No member to remove";
$l_add_member = "Add members to the project";

$l_board_no_data = "Dashboard can't be displayed if tasks or members definitions are missing !";
$l_select_task = "You have to select a task";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_name = "You have to fill the name before submitting.";
$l_fill_tt = "You have to fill the category before submitting.";
$l_fill_soldtime = "You have to fill the soldtime before submitting.";
$l_fill_estimated = "You have to fill the estimated time before submitting.";
$l_fill_tasklabel = "You have to fill the name for this task.";
$l_fill_soldnumber = "The sold time number is not valid.";
$l_fill_projected = "Each member must be assigned to a task";
$l_fill_projnumber = "The projected time number is not valid.";
$l_fill_missing = "The missing time must be filled for every member";
$l_fill_missnumber = "The missing time number is not valid.";
$l_tolong_shortname = "The short name is too long";
$l_delete_confirm = "Confirm the project deletion ?";

// For the display preferences modification screen 
$l_project_display = "Display options for projects list";
