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
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-0.7.5-0.8.0.php                                       //
//     - Desc : Update data from 0.7.5 to 0.8.0 (ProjectUser, password data) //
// 2004-01-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");

// Correct Project User assignations
$p_q = get_projectuser_list();
process_projectuser_list($p_q);

// Convert password to crypt form
$u_q = get_userobm_list();
process_userobm_list($u_q);


///////////////////////////////////////////////////////////////////////////////
// Query execution - UserObm list
///////////////////////////////////////////////////////////////////////////////
function get_userobm_list() {

  echo "Retrieving current Users (UserObm)\n";

  $query = "select userobm_id,
      userobm_login,
      userobm_password
    from UserObm
    ";

  $u_q = new DB_OBM;
  $u_q->query($query);

  return $u_q;
}


///////////////////////////////////////////////////////////////////////////////
// Process the UserObm list
// Parameters:
//   - $u_q : DBO UserObm list (with associated info)
///////////////////////////////////////////////////////////////////////////////
function process_userobm_list($u_q) {

  $nb_u = $u_q->num_rows();

  echo "** Processing UserObm list (converting passwords) : $nb_u entries\n";

  while ($u_q->next_record()) {
    $u_id = $u_q->f("userobm_id");
    $login = $u_q->f("userobm_login");
    $password = $u_q->f("userobm_password");
    $new_password = md5($password);

    echo "User $login ($u_id)";

    $query = "update UserObm
      set userobm_password='$new_password'
      where userobm_id='$u_id'";
    $user_q = new DB_OBM;
    $user_q->query($query);

    echo " - OK\n";
  }

  echo "** End Processing UserObm list : $nb_u entries\n";

}


///////////////////////////////////////////////////////////////////////////////
// Query execution - ProjectUser list where
//  exists an associated projecttask
//  and not exist an entry with projecttask is null
///////////////////////////////////////////////////////////////////////////////
function get_projectuser_list() {

  echo "Retrieving current ProjectUser entries that need a new record\n";

  $query = "select
      pu.projectuser_project_id,
      pu.projectuser_user_id,
      userobm_lastname,
      project_name
    from ProjectUser as pu
         left join UserObm on pu.projectuser_user_id = userobm_id
         left join Project on pu.projectuser_project_id = project_id
         left join ProjectUser as puj on
           (userobm_id = puj.projectuser_user_id
            and puj.projectuser_project_id = pu.projectuser_project_id
            and puj.projectuser_projecttask_id is null)
    where
      pu.projectuser_projecttask_id is not null
      and project_name is not null
      and puj.projectuser_user_id is null
    group by pu.projectuser_project_id,
      pu.projectuser_user_id,
      userobm_lastname,
      project_name
    order by pu.projectuser_project_id, pu.projectuser_user_id
    ";

  $p_q = new DB_OBM;
  $p_q->query($query);

  return $p_q;
}


///////////////////////////////////////////////////////////////////////////////
// Process the ProjectUser list                                              //
// Parameters:
//   - $p_q : DBO ProjectUser list (with associated info)
///////////////////////////////////////////////////////////////////////////////
function process_projectuser_list($p_q) {

  $nb_pu = $p_q->num_rows();

  echo "** Processing ProjectUser list : $nb_pu entries\n";

  while ($p_q->next_record()) {
    $pro = "";
    $pro["project_id"] = $p_q->f("projectuser_project_id");
    $pro_id = $pro["project_id"];
    $pro["user"] = $p_q->f("projectuser_user_id");
    $pro_user = $pro["user"];

    echo "Project $pro_id - User $pro_user";
    insert_one_projectuser($pro);
    echo " - OK\n";
  }

  echo "** End Processing ProjectUser list : $nb_pu entries\n";

}


///////////////////////////////////////////////////////////////////////////////
// Insert one projectuser
// Parameters:
//   - $pro : projectuser hash
///////////////////////////////////////////////////////////////////////////////
function insert_one_projectuser($pro) {

  $pid = $pro["project_id"];
  $user = $pro["user"];
  $date_now = date("Y-m-d H:i:s");

  $query = "insert into ProjectUser (
        projectuser_project_id,
        projectuser_user_id,
        projectuser_projecttask_id,
        projectuser_timecreate
        )
      values
        ('$pid',
        '$user',
        null,
        '$date_now')";

  $pro_q = new DB_OBM;
  $pro_q->query($query);

  return $new_id;
}


</script>
