<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : admin_data_display.inc                                      //
//     - Desc  : Data admin display File                                     //
// 2002-07-01 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display the help screen
// Parameters:
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_help($mode) {

  if ($mode == "html") echo "<table border=1><tr><td><pre>\n";
  include ("admin_data_help.inc");
  if ($mode == "html") echo "\n</pre></td></tr></table>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the search form for data module
// Parameters:
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_data_index($mode, $actions, $modules) {

  switch ($mode) {
  case "txt":
    echo "try -h to help\n";
    break;
  case "html":
    html_data_index($actions, $modules);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }

}


///////////////////////////////////////////////////////////////////////////////
// Display the HTML search form for lang module
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_data_index($actions, $modules) {
  global $sess;
  global $l_execute, $l_help;
  global $col_textem, $col_a_table, $col_a_text, $col_a_tableh;

  // modules SELECT
  $sel_module = "<SELECT name=module>";
  while (list ($key, $val) = each ($modules)) {
    $sel_module .= "<OPTION value=\"$val\">$val";
  }
  $sel_module .= "</SELECT>";

  echo "
    <a href=\"" . $sess->url("admin_data_index.php?action=help&amp;mode=html"). "\">$l_help</a>
    <p>
    <FORM method=get action=\"" . $sess->url("admin_data_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : data_show<br>
        <font color=\"#$col_textem\">Show inconsistencies in data</font></td>
      <td>module :<br>$sel_module</td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=data_show>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
    </table>
    </FORM>

    <HR>

    <FORM method=get action=\"" . $sess->url("admin_data_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : data_update<br>
        <font color=\"#$col_textem\">Update inconsistencies in data</font></td>
      <td>module :<br>$sel_module</td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=data_update>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
    </table>
    </FORM>

    <HR>
";

}


///////////////////////////////////////////////////////////////////////////////
// Display data inconsistency for the modules selected
// Parameters:
//   - $action : data_show || data_update (proceed the update)
//   - $mode   : "txt" or "html"
//   - $module : module name
///////////////////////////////////////////////////////////////////////////////
function dis_data($action, $mode, $module) {
  global $debug, $c_all;

  if ($module == '$c_all') {
    dis_data_company($action, $mode);

  } else if ($module == "company") {
    dis_data_company($action, $mode);
  } else {
    echo "No data inconsistency to check here... bye.";
  }
    
}


///////////////////////////////////////////////////////////////////////////////
// Display (/and update) data inconsistency for the company module
// Parametres:
//   - $action : data_show || data_update (proceed the update)
//   - $mode : "txt" or "html"
///////////////////////////////////////////////////////////////////////////////
function dis_data_company($action, $mode) {
  global $debug, $c_all;

  // Get the company list
  $c_q = get_company_list();
  $cpt_c = 0;
  $cpt_cu = 0;
  $nb_comp = $c_q->num_rows();
  
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** Company : $nb_comp to parse
------------------------------------------------------------------------------
";
  } else {
    if ($action == "data_update") {
      $update = "<th>Update</th>";
    } else {
      $update = "";
    }
    echo "<table border=1>
      <tr>
        <th>Company ($nb_comp)</th>
        <th colspan=2># Contact</th>
        <th colspan=2># Deal Active</th>
        <th colspan=2># Deal Total</th>
        $update
      </tr><tr>
        <th>Id</th>
        <th>field</th>
        <th>calculated</th>
        <th>field</th>
        <th>calculated</th>
        <th>field</th>
        <th>calculated</th>
        $update
      </tr>";
  }

  while ($c_q->next_record()) {
    $cpt_c++;

    $id = $c_q->f("company_id");
    $con_num = $c_q->f("company_contact_number");
    $deal_num = $c_q->f("company_deal_number");
    $deal_total = $c_q->f("company_deal_total");

    $new_con_num = get_company_contact_number($id);
    $new_deal_num = get_company_active_deal_number($id);
    $new_deal_total = get_company_total_deal_number($id);

    if ( ($con_num != $new_con_num) || ($deal_num != $new_deal_num)
         || ($deal_total != $new_deal_total) ) {
      $cpt_cu++;
      if ($mode == "txt") {
	echo "\nCompany (id=$id) : Contact ($con_num, $new_con_num) : Deal ($deal_num, $new_deal_num) : Deal total ($deal_total, $new_deal_total)";
      } else {
	echo "<tr>
          <td>$id</td>
          <td>$con_num</td>
          <td>$new_con_num</td>
          <td>$deal_num</td>
          <td>$new_deal_num</td>
          <td>$deal_total</td>
          <td>$new_deal_total</td>";
      }

      if ($action == "data_update") {
	$retour = update_one_company($id, $new_con_num, $new_deal_num, $new_deal_total);
	if ($retour) {
	  if ($mode == "txt") {
	    echo "OK";
	  } else {
	    echo "<td>OK</td>";
	  }
	} else {
	  if ($mode == "txt") {
	    echo "Problem !";
	  } else {
	    echo "<td>Problem !</td>";
	  }
	}
      }

      if ($mode == "html") {
	echo "</tr>";
      }

    }

    if ($cpt_c % 100 == 0) {
      if ($mode =="txt") {
	echo ".";
      } else {
	echo "<tr><th colspan=8 align=center>$cpt_c</th></tr>";
	flush();
      }
    }
  }

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** Company : $nb_comp to parse, $cpt_cu inconsistencies
------------------------------------------------------------------------------
";
  } else {
    echo "<tr>
        <td colspan=6>Company : $nb_comp to parse, $cpt_cu inconsistencies</td>
      </tr>
      </table>";
  }
}

