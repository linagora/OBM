<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : todo_display.inc                                             //
//     - Desc : Todo Display File                                            //
// 2003-09-15 Aliacom - Bastien Continsouzas                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
$fieldnames["date_todo"] = $l_date;
$fieldnames["date_deadline"] = $l_deadline;
$fieldnames["todo_priority"] = $l_priority;
$fieldnames["todo_percent"] = $l_percent;
$fieldnames["todo_title"] = $l_todotitle;
$fieldnames["todo_percent"] = $l_percent;
$fieldnames["todo_update"] = $l_update;


///////////////////////////////////////////////////////////////////////////////
// Display Company specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_todo(&$OD, $fieldname, $link_ok) {
  global $path,$ico_update ;

  if ($fieldname == "date_deadline") {
    $deadline = $OD->data_set->f("date_deadline");
    $res["name"] = ($deadline) ? of_date_format($deadline) : " ";
    $res["align"] = "center";
  }

  else if ($fieldname == "todo_title") {
    $t_id = $OD->data_set->f("todo_id");
    $res["url"] = "todo_index.php?action=detailconsult&amp;todo_id=$t_id";
  }

  else if ($fieldname == "todo_priority") {
    $res["align"] = "center";
  }

  else if ($fieldname == "todo_percent") {
    $res["align"] = "center";
    $percent = $OD->data_set->f("todo_percent");
    if ($percent != "") {
      $res["name"] = "$percent %";
    }
  }

  else if ($fieldname == "date_todo") {
    $res["align"] = "center";
  }

  else if ($fieldname == "todo_update") {
    $t_id = $OD->data_set->f("todo_id");
    $res["popup"] = true;
    $res["popup_width"] = 700;
    $res["popup_height"] = 260;
    $res["url"] = "todo_index.php?action=detailupdate&amp;todo_id=$t_id&amp;popup=1";
    $res["name"] = "<img src=\"$ico_update\" alt=\"\" />";
    $res["txt_name"] = " ";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Todo search result
// Parameters:
//   - $todo[]    : todo search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_todo_search_list($todo) {
  global $display, $obm, $l_no_found;

  $todo_q = run_query_todo_todolist($todo);
  $nb_todo = $todo_q->num_rows_total();
  if ($nb_todo == 0) {
    $display["msg"] .= display_info_msg($l_no_found);
  } else {
    $prefs = get_display_pref($obm["uid"], "todo", 0);
    $display["result"] .= html_todo_search_list($todo, $todo_q, $prefs);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Todo Full list
// Parameters:
//   - $todo[] : hash values
//   - $todo_q : todo list
//   - $prefs  : fields to display in the todo list
///////////////////////////////////////////////////////////////////////////////
function html_todo_search_list($todo, $todo_q, $prefs) {
  global $obm, $l_del, $l_delete;

  $uid = $obm["uid"];
  $title = $todo["title"];
  $users = $todo["userid"];
  $user = $users[0];
  $priority = $todo["priority"];

  $url = url_prepare("todo_index.php?action=index&amp;tf_title=$title&amp;tf_user=$user&amp;sel_priority=$priority");


  $todo_d = new OBM_DISPLAY("DATA", $prefs, "todo");
  $todo_d->data_set = $todo_q;
  $todo_d->data_url = $url;
  $todo_d->data_idfield = "todo_id";
  $todo_d->data_cb_text = "$l_del";
  $todo_d->data_cb_side = "left";
  $todo_d->data_cb_name = "delete_";
  $todo_d->data_cb_show = 1;
  $todo_d->data_header = "both";
  $todo_d->data_form_head = "
     <form method=\"post\" name=\"f_todo_del\" action=\"".url_prepare("todo_index.php").
    "\" onsubmit=\"if (confirm_del()) return true; else return false\" >";

  $todo_d->data_form_end = "
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_delete\" />
  </form>";

  $block .= $todo_d->display("dis_data_todo");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Todo Detail (all informations)
// Parameters:
//   - $todo_q    : todo list
//   - $todo[]    : default values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_todo_detail($todo, $todo_q) {
  global $display;
  global $l_todo, $l_todotitle, $l_user, $l_author, $l_priority, $l_date;
  global $l_deadline, $l_content, $l_no_deadline, $l_percent;

  $title = $todo_q->f("todo_title");
  $user = $todo_q->f("user_firstname") ." ". $todo_q->f("user_lastname");
  $author = $todo_q->f("author_firstname") ." ". $todo_q->f("author_lastname");
  $priority = $todo_q->f("todo_priority");
  $percent = $todo_q->f("todo_percent");

  $date = of_date_format($todo_q->f("date_todo"));
  $deadline = $todo_q->f("date_deadline");
  $deadline = ($deadline != 0) ? of_date_format($deadline) : $l_no_deadline;

  $content = $todo_q->f("todo_content");

  $display["title"] = "<h1 class=\"title\">$l_todo : $title</h1>";

  $block = "
  <div class=\"detail infos\">
    <h1>$l_todo</h1>
    <table>
     <tr>
      <th>$l_todotitle</th>
      <td>$title</td>
     </tr><tr>
      <th>$l_author</th>
      <td>$author</td>
     </tr><tr>
      <th>$l_priority</th>
      <td>$priority</td>
     </tr><tr>
      <th>$l_percent</th>
      <td>$percent</td>
     </tr><tr>
      <th>$l_date</th>
      <td>$date</td>
     </tr><tr>
      <th>$l_deadline</th>
      <td>$deadline</td>
     </tr>
     </table>
   </div>

   <div class=\"detail infos\">
    <h1>$l_user</h1>
    <table>
     <tr>
      <th>$l_user</th>
      <td>$user</td>
     </tr>
    </table>
   </div>
  
  <div class=\"detail extra\">
    <h1>$l_content</h1> 
      $content
  </div>
    ";
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Todo Form
// Parameters:
//   - $todo[] : default values
//   - $todo_q : default values
///////////////////////////////////////////////////////////////////////////////
function dis_todo_form($todo, $todo_q = 0) {
  global $path, $obm;
  global $set_date, $ico_mini_cal, $ico_add, $ico_delete;
  global $l_add, $l_update, $l_close,$display,$l_todo,$l_user;
  global $l_deadline, $l_priority, $l_todotitle, $l_percent, $l_content;

  $max_priority = 5;
  $action = $todo["action"];
  $popup = $todo["popup"];
 
  if ($action == "detailupdate") {
    $t_id = $todo["todo_id"];
    $u_id = $todo_q->f("todo_user");
    $title = $todo_q->f("todo_title");
    $content = $todo_q->f("todo_content");
    $priority = $todo_q->f("todo_priority");
    $percent = $todo_q->f("todo_percent");
    $deadline = $todo_q->f("date_deadline");
    $deadline = ($deadline != 0) ? of_date_upd_format($deadline, true) : $l_no_deadline;
  } else {
    $u_id = $obm["uid"];
    $priority = intval(($max_priority+1)/2);
    $deadline = of_date_upd_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($todo["todo_id"])) { $t_id = $todo["todo_id"]; }
  if (isset($todo["title"])) { $title = $todo["title"]; }
  if (isset($todo["priority"])) { $priority = $todo["priority"]; }
  if (isset($todo["percent"])) { $percent = $todo["percent"]; }
  if (isset($todo["deadline"])) { $deadline = of_date_upd_format($todo["deadline"],true); };
  if (isset($todo["content"])) { $content = $todo["content"]; }
  if (is_array($todo["userid"])) {
    $p_user_array = $todo["userid"];
  } else if ($u_id > 0) {
    $p_user_array = array($u_id);
  } else {
    $p_user_array = array($obm["uid"]);
  }

  $users = get_userobm_from_ids($p_user_array);
  // user select
  if (is_array($users["entity"])) {
    foreach ($users["entity"] as $u_id => $u_ent) {
      $u_name = $u_ent["label"];
      $sel_id = "data-user-$u_id";
      $div_id = "sel_user_id-$sel_id";
      $dis_sel_user .= "<div class=\"elementRow\" id=\"$div_id\">
      <a href=\"javascript: remove_element('$div_id','sel_user_id');\">
      <img src=\"$ico_delete\" alt=\"[delete]\" />
      </a>
      $u_name
      <input value=\"$sel_id\" name=\"sel_user_id[]\" type=\"hidden\" />
      </div>";
    }
  }

  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_user_id";
   
  $block_user = "
  <tr>
    <th>
      $l_users
      <a href=\"javascript: return false;\" 
      onclick=\"popup('$url',''); return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>
    </th>    
    <td id=\"sel_user_id\">
      $dis_sel_user
    </td></tr>";

  $display["title"] = "<h1 class=\"title\">$l_todo : $title</h1>";

  // Priority select
  $sel_priority = "<select id=\"sel_priority\" name=\"sel_priority\">";
  for ($i=1; $i<=$max_priority; $i++) {
    $selected = ($i == $priority) ? "selected=\"selected\"" : "";
    $sel_priority .= "<option value=\"$i\" $selected>$i</option>";
  }
  $sel_priority .= "</select>";

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" id=\"todo_id\" name=\"todo_id\" value=\"$t_id\" />
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" id=\"popup\" name=\"popup\" value=\"$popup\" />
      <input type=\"submit\" value=\"$l_update\" />
    ";
  } else {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_add\" />
    ";
  }

  if ($popup)
    $dis_button .=  "<input type=\"submit\" value=\"$l_close\" onclick=\"window.close();\" />";

  $block = "
 
  <form method=\"post\" name=\"f_entity\"
    onsubmit=\"if (check_addtodoform(this)) return true; else return false;\"
    action=\"".url_prepare("todo_index.php")."\">
 
 <fieldset class=\"detail infos\">
 <legend>$l_todo</legend>
  <table>
  <tr>
    <th>$l_todotitle</th>
    <td><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"100\" size=\"100\" value=\"$title\" /></td>
    </tr><tr>
    <th>$l_priority</th>
    <td>$sel_priority</td>
  </tr><tr>
    <th>$l_percent</th>
    <td><input type=\"text\" id=\"tf_percent\" name=\"tf_percent\" maxlength=\"3\" size=\"3\" value=\"$percent\" />%</td>
  </tr><tr>
    <th>$l_deadline</th>
    <td><input type=\"text\" name=\"tf_deadline\" value=\"$deadline\" class=\"datePicker\"/></td>
  </tr>
  </table>
  </fieldset>

  
 <fieldset class=\"detail infos\">
 <legend>$l_user</legend>
  <table>
  $block_user
  </table>
 </fieldset>


  <fieldset class=\"detail extra\">
  <legend>$l_content</legend>
    <textarea id=\"ta_content\" name=\"ta_content\" rows=\"3\" cols=\"80\">$content</textarea>
  </fieldset>
  
  <fieldset class=\"buttons\">
    $dis_button
  </fieldset>
  </form>
  ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Todo display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_todo_display_pref($prefs) {
  global $l_todo_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "todo");
  $dis_pref->pref_title = $l_todo_display;

  $block .= "<td>";
  $block .= $dis_pref->display();
  $block .= "</td></tr></table>";

  $block .= $dis_pref->dis_pref_help();
  return $block;
}

?>
