<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : projet.inc                                               //
// 2003-07-10 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_company = "Company";
$l_tasktype = "Category";
$l_task = "Task";
$l_soldtime = "Sold time";
$l_estimatedtime = "Estimated time";
$l_datebegin = "Begin date";
$l_dateend = "End date";

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
$l_manager = "project manager";
$l_member = "Member";
$l_soldtime = "Sold time";
$l_projtime = "Projected time";
$l_total = "Total";
$l_resume = "Résumé";
$l_tasks = "tasks";
$l_members = "members";
$l_deal = "Deal";

// Titres
$l_project = "Project";
$l_members_info = "Project members";
$l_progressinfos = "Progress evaluation : time (used / missing / projected)";
$l_memberstimeinfos = "Fill in the working length (in days) projected for each person assigned to a task";
$l_task_new = "New task";
$l_task_update = "Update task";
$l_task_list = "Defined tasks";
$l_progress_info = "Fill in the working length (in days) missing for each person to complete a task";

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
$l_insert_ok = "The project has been inserted successfully.";
$l_insert_error = "The insertion of the project has failed !";
$l_update_ok = "The project has been updated successfully.";
$l_update_error = "The update of the project has failed !";
$l_delete_ok = "The project has been deleted successfully";
$l_delete_error = "The deletion of the project has failed !";
$l_link_timetask = "The project is used in time management";
$l_link_timetask_no = "The project is not used in time management";
$l_cant_delete = "The project can't be deleted !";
$l_can_delete = "The project can be deleted.";
$l_task_insert_ok = "Task inserted successfully";
$l_task_insert_error = "The task insertion has failed ! The parent task is already assigned to users";
$l_task_update_ok = "Task updated successfully";
$l_task_update_error = "The task update has failed !";
$l_task_delete_ok = "The task has been deleted successfully.";
$l_task_delete_error = "The tasks deletion has failed ! Someone is assigned to the task (or its children)";
$l_no_task_del = "No task to remove";
$l_ptask_same_task = "The parent task can't be the same as the task !";
$l_ptask_and_parent = "A parent task can't have a parent !";

$l_member_delete_error = "The deletion of the member has failed ! He should be assigned to some tasks or has spent some time on the project.";
$l_allocate_update_ok = "The project allocation update has been updated successfully.";
$l_allocate_update_error = "The update of the project allocation has failed !";
$l_progress_update_ok = "The project progress has been updated successfully.";
$l_progress_update_error = "The update of the project progress has failed !";
$l_query_error = "Problem executing the query";
$l_del_member_sel = "remove these members";
$l_member_added = "member(s) added";
$l_member_removed = "member(s) removed";
$l_no_member_add = "No member to add";
$l_no_member_del = "No member to remove";
$l_add_member = "Add members to the project";

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
$l_delete_confirm = "Confirm the project deletion ?";

// For the display preferences modification screen 
$l_project_display = "Display options for projects list";
