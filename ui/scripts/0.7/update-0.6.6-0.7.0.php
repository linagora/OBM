<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm.php                                                      //
//     - Desc : OBM Home Page (Login / Logout)                               //
// 2003-07-28 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
require("$obminclude/phplib/obmlib.inc");

$nreq = 0;

init_projects();
fill_projecttask();
fill_projectuser();
// fill_preferences();

echo "update successfully completed : $nreq requests performed\n";

///////////////////////////////////////////////////////////////////////////////
// Query execution - User list                                               //
///////////////////////////////////////////////////////////////////////////////
function init_projects() {
  global $nreq;

  // search the projects that allready appear in the Task table
  
  echo "updating the project list\n";
  echo "-> begin\n";

  $query = "
    select
      timetask_projecttask_id as id
    from TimeTask
    where timetask_projecttask_id != 0
    group by timetask_projecttask_id";

  //echo "$query \n ---------------------------------------------\n";

  $toinit_q = new DB_OBM;
  $toinit_q->query($query);
  $nreq ++;

  $toinit_q->next_record();
  $toinit_tab = "(" . $toinit_q->f("id");
  
  while($toinit_q->next_record()) {
    $toinit_tab .= ", " . $toinit_q->f("id");
  }
  
  $toinit_tab .= ")";

  //init these projects
  
  $query = "
    update Deal
    set deal_project_status = 1,
      deal_soldtime = 1
    where deal_id in $toinit_tab";

  //echo "$query \n ---------------------------------------------\n";

  $init_q = new DB_OBM;
  $init_q->query($query);
  $nreq ++;

  echo "-> end\n\n";
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - User list                                               //
///////////////////////////////////////////////////////////////////////////////
function fill_projecttask() {
  global $nreq;

  // get labels corresponding to the different projects in TimeTask

  echo "retrieving timetask table informations\n";
  echo "-> begin\n";

  $query = "
    select distinct
      timetask_projecttask_id as deal,
      deal_label as name,
      trim(timetask_label) as label
    from Deal, TimeTask
    where timetask_projecttask_id = deal_id";
  
  //echo "$query \n ---------------------------------------------\n";
  
  $tasks_q = new DB_OBM;
  $tasks_q->query($query);
  $nreq ++;

  echo "-> end\n\n";
  echo "filling projettask table\n";
  echo "-> begin\n";

  $project_q = new DB_OBM;
  $newtask_q = new DB_OBM;
  $time_q = new DB_OBM;
  
  while($tasks_q->next_record()) {
    
    $deal = $tasks_q->f("deal");
    $name = $tasks_q->f("name");
    $label = $tasks_q->f("label");

    if ($label == "")
      $text = addslashes($name);
    else 
      $text = trim(addslashes($label));

    // create a new projecttask corresponding to a timetask label

    $query = "
      insert into ProjectTask (
        projecttask_deal_id,
        projecttask_label,
        projecttask_rank)
      values (
        $deal,
        '$text',
        0
      )";

    //echo "$query \n ---------------------------------------------\n";
    
    $project_q->query($query);
    $nreq ++;

    // get the id of the inserted projecttask

    $query = "
      select projecttask_id as task
      from ProjectTask
      where projecttask_deal_id = $deal
        and projecttask_label = '$text'
    ";

   //echo "$query \n ---------------------------------------------\n";
    
    $newtask_q->query($query);
    $nreq ++;

    $newtask_q->next_record();
    $task = $newtask_q->f("task");

    // update time task to use the new projecttask

    $query = "
      update TimeTask
      set timetask_projecttask_id = $task
      where timetask_projecttask_id = $deal
      ";

    if ($label == "")
      $query .= "and timetask_label = ''";
    else
      $query .= "and trim(timetask_label) like '$text'";

      //echo "$query \n ---------------------------------------------\n";

    $time_q->query($query);
    $nreq ++;
  }

  echo "-> end\n\n";
}

///////////////////////////////////////////////////////////////////////////////
// Query execution - User list                                               //
///////////////////////////////////////////////////////////////////////////////
function fill_projectuser() {
  global $nreq;

  // Find who has taken part in projects
  
  echo "retrieving informations from the timetask table\n";
  echo "-> begin\n";

  $query = "
    select distinct
      deal_id as deal,
      timetask_projecttask_id as ptask,
      timetask_user_id as user
    from Deal, ProjectTask, TimeTask
    where timetask_projecttask_id = projecttask_id
      and projecttask_deal_id = deal_id
      and deal_project_status = 1";
  
  //echo "$query \n ---------------------------------------------\n";

  $members_q = new DB_OBM;
  $members_q->query($query);
  $nreq ++;

  echo "-> end\n\n";
  echo "filling projetuser table\n";
  echo "-> begin\n";

  $projuser_q = new DB_OBM;
  
  while($members_q->next_record()) {
  
    $deal = $members_q->f("deal");
    $ptask = $members_q->f("ptask");
    $user = $members_q->f("user");
    $date = date("Ymd");
    
    // fill the ProjectUser table

    $query = "
      insert into ProjectUser (
        projectuser_user_id,
        projectuser_deal_id,
        projectuser_projecttask_id,
        projectuser_projectedtime,
        projectuser_missingtime,
        projectuser_validity,
        projectuser_manager)
      values (
        $user,
        $deal,
        $ptask,
        1,
        1,
        $date,
        0
        )";

    //echo "$query \n ---------------------------------------------\n";
    
    $projuser_q->query($query);
    $nreq ++;
  }

  echo "-> end\n\n";
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - User list                                               //
///////////////////////////////////////////////////////////////////////////////
// function fill_preferences() {
//   global $nreq;

//   // Retrieve userobm ids
  
//   echo "retrieving informations from the userobm table\n";
//   echo "-> begin\n";

//   $query = "
//     select 
//       userobm_id as id
//     from UserObm";
  
//   //echo "$query \n ---------------------------------------------\n";

//   $users_q = new DB_OBM;
//   $users_q->query($query);
//   $nreq ++;

//   echo "-> end\n\n";
//   echo "filling display pref table\n";
//   echo "-> begin\n";

//   $prefs_q = new DB_OBM;
  
//   while($users_q->next_record()) {
  
//     $id = $users_q->f("id");
    
//     // fill the DisplayPref table

//     $query = "
//       insert into DisplayPref (
//         display_user_id,
//         display_entity,
//         display_fieldname,
//         display_fieldorder,
//         display_display )
//       values
//         ($id,'project','project_label',1,2),
//         ($id,'project','project_company_name',2,1),
//         ($id,'project','project_tasktype',3,1),
//         ($id,'project','project_status',4,1),
//         ($id,'project','project_archive',5,1),
//         ($id,'time','date_task',1,2),
//         ($id,'time','timetask_deal_label',2,2),
//         ($id,'time','timetask_company_name',3,1),
//         ($id,'time','timetask_label',4,1),
//         ($id,'time','tasktype_label',5,1),
//         ($id,'time','timetask_length',6,2),
//         ($id,'time','timetask_id',7,2),
//         ($id,'time_proj','deal_label',1,2),
//         ($id,'time_proj','company_name',2,2),
//         ($id,'time_proj','total_length',3,1),
//         ($id,'time_proj','total_before',4,1),
//         ($id,'time_proj','total_after',5,1),
//         ($id,'time_tt','tasktype_label',1,2),
//         ($id,'time_tt','total_length',2,1),
//         ($id,'time_tt','total_before',3,1),
//         ($id,'time_tt','total_after',4,1)
//     ";

//     //echo "$query \n ---------------------------------------------\n";
    
//     $prefs_q->query($query);
//     $nreq ++;
//   }

//   echo "-> end\n\n";
// }

</script>
