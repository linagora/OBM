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
//   - $mode : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_help($mode) {

  if ($mode == "html")
    echo "<table class=\"details\">
      <tr>
        <td><pre>";

  include ("admin_data_help.inc");

  if ($mode == "html")
    echo "</pre>
        </td>
      </tr>
      </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the search form for data module
// Parameters:
//   - $mode : "txt" ou "html"
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
  global $l_execute, $l_help;

  // modules SELECT
  $sel_module = "<select name=module>";
  while (list ($key, $val) = each ($modules)) {
    $sel_module .= "<option value=\"$val\">$val";
  }
  $sel_module .= "</select>";

  echo "
    <br />
    <form method=\"get\" action=\"" . url_prepare("admin_data_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\">action : data_show</td>
      <td class=\"adminHead\">module</td>
      <td class=\"adminLabel\" rowspan=\"2\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"data_show\" />
        <input type=\"submit\" value=\"$l_execute\" /></td>
    </tr><tr>
      <td class=\"adminText\">Show inconsistencies in data</td>
      <td class=\"adminText\">$sel_module</td>
    </tr>
    </table>
    </form>

    <hr />

    <form method=\"get\" action=\"" . url_prepare("admin_data_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\">action : data_update</td>
      <td class=\"adminHead\">module</td>
      <td class=\"adminLabel\" rowspan=\"2\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"data_update\" />
        <input type=\"submit\" value=\"$l_execute\" /></td>
    </tr><tr>
      <td class=\"adminText\">Update inconsistencies in data</td>
      <td class=\"adminText\">$sel_module</td>
    </tr>
    </table>
    </form>
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
      $update = "<td class=\"adminLabel\">Update</td>";
    } else {
      $update = "";
    }
    echo "
      <table class=\"admin\">
      <tr>
        <td class=\"adminHead\">Company ($nb_comp)</td>
        <td class=\"adminHead\" colspan=\"2\"># Contact</td>
        <td class=\"adminHead\" colspan=\"2\"># Deal Active</td>
        <td class=\"adminHead\" colspan=\"2\"># Deal Total</td>
        $update
      </tr><tr>
        <td class=\"adminHead\">Id</td>
        <td class=\"adminHead\">field</td>
        <td class=\"adminHead\">calculated</td>
        <td class=\"adminHead\">field</td>
        <td class=\"adminHead\">calculated</td>
        <td class=\"adminHead\">field</td>
        <td class=\"adminHead\">calculated</td>
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
          <td class=\"adminText\">$id</td>
          <td class=\"adminText\">$con_num</td>
          <td class=\"adminText\">$new_con_num</td>
          <td class=\"adminText\">$deal_num</td>
          <td class=\"adminText\">$new_deal_num</td>
          <td class=\"adminText\">$deal_total</td>
          <td class=\"adminText\">$new_deal_total</td>";
      }

      if ($action == "data_update") {
	$retour = update_one_company($id, $new_con_num, $new_deal_num, $new_deal_total);
	if ($retour) {
	  if ($mode == "txt") {
	    echo "OK";
	  } else {
	    echo "<td class=\"adminLabel\">OK</td>";
	  }
	} else {
	  if ($mode == "txt") {
	    echo "Problem !";
	  } else {
	    echo "<td class=\"adminLabel\">Problem !</td>";
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
	echo "<tr>
          <td class=\"adminLabel\" colspan=\"8\">$cpt_c</td>
        </tr>";
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
        <td class=\"adminLabel\" colspan=\"8\">
          Company : $nb_comp to parse, $cpt_cu inconsistencies</td>
      </tr>
      </table>";
  }
}
