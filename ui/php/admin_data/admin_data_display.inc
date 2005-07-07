<script language="php">
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
    echo "<table class=\"detail\">
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
  $sel_module = "<select name=\"target_module\">";
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

    <hr />

    <form method=\"get\" action=\"" . url_prepare("admin_data_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\">action : sound_aka_update</td>
      <td class=\"adminLabel\" rowspan=\"2\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"sound_aka_update\" />
        <input type=\"submit\" value=\"$l_execute\" /></td>
    </tr><tr>
      <td class=\"adminText\">Update aka and sound keys used for searches (Company)</td>
    </tr>
    </table>
    </form>";
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
  } else if ($module == "deal") {
    dis_data_deal($action, $mode);
  } else if ($module == "list") {
    dis_data_list($action, $mode);
  } else if ($module == "document") {
    dis_data_document($action, $mode);
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
    //$new_deal_num = "0";
    //$new_deal_total = "0";

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


///////////////////////////////////////////////////////////////////////////////
// Display (/and update) data inconsistency for the Deal module
// Parametres:
//   - $action : data_show || data_update (proceed the update)
//   - $mode : "txt" or "html"
///////////////////////////////////////////////////////////////////////////////
function dis_data_deal($action, $mode) {
  global $path, $debug, $c_all, $php_regexp_number;

  // Get the deal list
  $d_q = get_deal_list();
  $cpt_d = 0;
  $cpt_du = 0;
  $nb_deal = $d_q->num_rows();
  
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** Deal : $nb_deal to parse
------------------------------------------------------------------------------
";
  } else {
    if ($action == "data_update") {
      $update = "<td class=\"adminHead\">Update</td>";
    } else {
      $update = "";
    }
    echo "
      <table class=\"admin\">
      <tr>
        <td class=\"adminHead\">Deal ($nb_deal)</td>
        <td class=\"adminHead\">Status</td>
        <td class=\"adminHead\">Archive</td>
        <td class=\"adminHead\">Hitrate</td>
        <td class=\"adminHead\">Should be</td>
        $update
      </tr>";
  }

  while ($d_q->next_record()) {
    $cpt_d++;

    $id = $d_q->f("deal_id");
    $status = $d_q->f("dealstatus_label");
    $archive = $d_q->f("deal_archive");
    $arch = (($archive == "1") ? "X" : "&nbsp;");
    $hitrate = $d_q->f("deal_hitrate");
    $new_hitrate = $d_q->f("dealstatus_hitrate");

    // If current hitrate doesn't match status hitrate
    // or hitrate not a number
    // or deal archived but hit rate not 0 nor 100
    // or deal archived but status tells deal not closed
    if ( ( ($new_hitrate != "") && ($new_hitrate != $hitrate) )
         || (! preg_match($php_regexp_number, $hitrate) )
	 || ( ($archive == "1") && ($hitrate != "0") && ($hitrate != "100") )
	 || ( ($archive == "1") && ($new_hitrate != "0") && ($new_hitrate != "100") )
	 ) {
      $cpt_du++;
      if ($mode == "txt") {
	$arch = (($archive == "1") ? "X" : " ");
	echo "\nDeal (id=$id) : status $status, archive $arch hitrate $hitrate, new_hitrate $new_hitrate";
      } else {
	$arch = (($archive == "1") ? "X" : "&nbsp;");
	echo "<tr>
          <td class=\"adminText\"><a href=\"$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$id\">$id</a></td>
          <td class=\"adminText\">$status</td>
          <td class=\"adminText\">$arch</td>
          <td class=\"adminText\">$hitrate</td>
          <td class=\"adminText\">$new_hitrate</td>";
      }

      if ($action == "data_update") {
	$retour = false;
	if ( ($new_hitrate != "") && ($new_hitrate != $hitrate) ) {
	  $retour = update_one_deal($id, $new_hitrate);
	}
	if ($retour) {
	  if ($mode == "txt") {
	    echo "OK";
	  } else {
	    echo "<td class=\"adminLabel\">OK</td>";
	  }
	} else {
	  if ($mode == "txt") {
	    echo "Manual !";
	  } else {
	    echo "<td class=\"adminLabel\">Manual !</td>";
	  }
	}
      }

      if ($mode == "html") {
	echo "</tr>";
      }

    }

    if ($cpt_d % 100 == 0) {
      if ($mode =="txt") {
	echo ".";
      } else {
	echo "<tr>
          <td class=\"adminLabel\" colspan=\"8\">$cpt_d</td>
        </tr>";
	flush();
      }
    }
  }

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** Deal : $nb_deal to parse, $cpt_du inconsistencies
------------------------------------------------------------------------------
";
  } else {
    echo "<tr>
        <td class=\"adminLabel\" colspan=\"8\">
          Deal : $nb_deal to parse, $cpt_du inconsistencies</td>
      </tr>
      </table>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display (/and update) data inconsistency for the list module
// This concerns dynamic queries generation
// Parametres:
//   - $action : data_show || data_update (proceed the update)
//   - $mode : "txt" or "html"
///////////////////////////////////////////////////////////////////////////////
function dis_data_list($action, $mode) {
  global $debug, $path, $c_all;

  // To include specic list queries (make_query_from_criteria,...)
  require_once("$path/list/list_query.inc");

  // Get the list list
  $l_q = get_list_list();
  $cpt_l = 0;
  $cpt_lu = 0;
  $nb_l = $l_q->num_rows();
  
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** List : $nb_l to parse
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
        <td class=\"adminHead\" colspan=\"2\">List ($nb_l)</td>
        <td class=\"adminHead\" colspan=\"2\"># Static Contact</td>
        <td class=\"adminHead\" colspan=\"2\"># Query Contact</td>
        <td class=\"adminHead\">Query Generation =</td>
        $update
      </tr><tr>
        <td class=\"adminHead\">Id</td>
        <td class=\"adminHead\">Name</td>
        <td class=\"adminHead\">field</td>
        <td class=\"adminHead\">calculated</td>
        <td class=\"adminHead\">field</td>
        <td class=\"adminHead\">calculated</td>
        <td class=\"adminHead\">&nbsp;</td>
        $update
      </tr>";
  }

  while ($l_q->next_record()) {
    $cpt_l++;

    $id = $l_q->f("list_id");
    $name = $l_q->f("list_name");
    $sta_nb = $l_q->f("list_static_nb");
    $dyn_nb = $l_q->f("list_query_nb");
    $query = $l_q->f("list_query");

    $criteria = unserialize($l_q->f("list_structure"));

    if ($criteria != "") {
      $new_query = make_query_from_db($l_q);
    } else {
      $new_query = $l_q->f("list_query");
    }

    if ($new_query != "") {
      $query_nb = get_list_query_num_rows($new_query);
      $query_txt = $query_nb;
    } else {
      $query_nb = "0";
      $query_txt = "No";
    }

    $new_sta_nb = get_list_static_contact_nb($id);
    $new_dyn_nb = $query_nb;

    if (($sta_nb != $new_sta_nb) || ($query != $new_query)) {
      $query_diff = true;
      $query_diff_txt = "X";
    } else {
      $query_diff = false;
      $query_diff_txt = "OK";
    }

    if ( ($sta_nb != $new_sta_nb) || ($dyn_nb != $new_dyn_nb) || ($query_diff) ) {
      $cpt_lu++;
      if ($mode == "txt") {
	echo "\nList (id=$id), Contacts : Statiques ($sta_nb, $new_sta_nb) : Dynamiques ($dyn_nb, $new_dyn_nb)";
      } else {
	echo "<tr>
          <td class=\"adminText\">$id</td>
          <td class=\"adminText\">$name</td>
          <td class=\"adminText\">$sta_nb</td>
          <td class=\"adminText\">$new_sta_nb</td>
          <td class=\"adminText\">$dyn_nb</td>
          <td class=\"adminText\">$new_dyn_nb</td>
          <td class=\"adminText\">$query_diff_txt</td>";
      }

      if ($action == "data_update") {
	$up_query = addslashes($new_query);
	$retour = update_one_list($id, $new_sta_nb, $new_dyn_nb, $up_query);
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

    if ($cpt_l % 100 == 0) {
      if ($mode =="txt") {
	echo ".";
      } else {
	echo "<tr>
          <td class=\"adminLabel\" colspan=\"8\">$cpt_l</td>
        </tr>";
	flush();
      }
    }
  }

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** List : $nb_l to parse, $cpt_lu inconsistencies
------------------------------------------------------------------------------
";
  } else {
    echo "<tr>
        <td class=\"adminLabel\" colspan=\"8\">
          List : $nb_l to parse, $cpt_lu inconsistencies</td>
      </tr>
      </table>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display (/and update) data inconsistency for the Document module
// Parametres:
//   - $action : data_show || data_update (proceed the update)
//   - $mode : "txt" or "html"
///////////////////////////////////////////////////////////////////////////////
function dis_data_document($action, $mode) {
  global $debug, $c_all, $php_regexp_number,$document_path;

  // Get the deal list
  $d_q = get_document_list();
  $f_q = get_file_list("$document_path/");
  $cpt_du = 0;
  $nb_document = $d_q->num_rows();
  $db = new DB_OBM;
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** Document : $nb_document to parse in database and
at least as much in the file system.
------------------------------------------------------------------------------
";
  } else {
    if ($action == "data_update") {
      $update = "<td class=\"adminHead\">Update</td>";
    } else {
      $update = "";
    }
    echo "
      <table class=\"admin\">
       <tr>
	<td class=\"adminHead\">Document($nb_document)</td>
        <td class=\"adminHead\">Document Path</td>
        <td class=\"adminHead\">Document Name</td>
        <td class=\"adminHead\">Document Kind</td>
	<td class=\"adminHead\">Missing <br> in Database</td>
        <td class=\"adminHead\">Missing <br> in File System</td>
        $update
      </tr>";
  }
  if($mode == "html") {
    $bool_bd = $d_q->next_record();
    $bool_fs = true;
    while ($bool_bd || $bool_fs) {
      $fs_err ="";
      $bd_err = "";
      $cur_f = current($f_q);
      $file_name = $cur_f[1];
      $file_path = $cur_f[0];
      $kind = $d_q->f("document_kind");
      $id = $d_q->f("document_id");
      $fpath = $d_q->f("document_path");
      $name = $d_q->f("document_name");
      if($kind == 0) {
	$type = "Repository";
      }
      elseif($kind == 1) {
	$type = "File";
      }
      elseif($kind == 2) {
	$type = "Link";
      }

      if(($fpath != $file_path || $file_name != $name) && $kind != 2) {
	if(($f_q[$fpath.$name][0] == "" || !$bool_fs) && $bool_bd) {
	  $cpt_du ++;
	  $fs_err = "X";
	  $bool_bd = $d_q->next_record();
	  $url = "<a href=\"$path/document/document_index.php?action=detailconsult&amp;param_document=$id\">$id</a>";
	  if($action == "data_update") {
	    $update = "<td class=\"adminText\">Manual</td>";
	  }
	}
	else {
	  $cpt_du ++;
	  $bd_err = "X";
	  $bool_fs = next($f_q);
	  $name = $file_name;
	  $fpath = $file_path;
	  if(is_dir($document_path.$file_path.$file_name)) {
	    $type = "Repository";
	    $kind = 0;
	    $author = "";
	    $bd_name = "";
	  }
	  else {
	    $type = "File";
	    $kind = 1;
	    $author = "System";
	    $bd_name = $file_name;
	  }
	  $url ="";
	  if($action == "data_update") {
	    if($kind != 2) {
	      $query = "insert into Document (
		document_timeupdate,
		document_timecreate,
		document_userupdate,
		document_usercreate,
		document_title,
		document_author,
		document_name,
		document_path,
		document_kind,
		document_size,
		document_category1_id,
		document_category2_id,
		document_mimetype,
		document_privacy
	      )
	      values (
		null,
		'" . date("Y-m-d H:i:s") . "',
		null,
		'0',
		'$bd_name',
		'$author',
		'$file_name',
		'$file_path',
		'$kind',
		'',
		'',
		'',
		'',
		'0'
	      )";
	      if($db->query($query)) {
		$update = "<td class=\"adminText\">Updated</td>";
	      }
	      else {
		$update = "<td class=\"adminText\">Error</td>";
	      }
	    }else {
	      unlink($document_path.$file_path.$file_name);
	      $update = "<td class=\"adminText\">Deleted</td>";
	    }
	  }
	}
	
	echo "<tr>
	 <td class=\"adminText\">$url</td>
	 <td class=\"adminText\" style=\"text-align:left;\" >$fpath</td>
	 <td class=\"adminText\">$name</td>
	 <td class=\"adminText\">$type</td>
	 <td class=\"adminText\">$bd_err</td>
	 <td class=\"adminText\">$fs_err</td>
	 $update
	 </tr>";
      }
      elseif ($kind==2) {
	$bool_bd = $d_q->next_record();
      } else {
	$bool_fs = next($f_q);
	$bool_bd = $d_q->next_record();
      }
    } 
  }
  if ($mode == "txt") {
    $bool_bd = $d_q->next_record();
    $bool_fs = true;
    while ($bool_bd || $bool_fs) {
     $fs_err ="";
      $bd_err = "";
      $cur_f = current($f_q);
      $file_name = $cur_f[1];
      $file_path = $cur_f[0];
      $kind = $d_q->f("document_kind");
      $id = $d_q->f("document_id");
      $fpath = $d_q->f("document_path");
      $name = $d_q->f("document_name");
      if($kind == 0) {
	$type = "Repository";
      }
      elseif($kind == 1) {
	$type = "File";
      }
      elseif($kind == 2) {
	$type = "Link";
      }

      if($fpath != $file_path || $file_name != $name) {
	if(($f_q[$fpath.$name][0] == "" || !$bool_fs) && $bool_bd) {
	  $cpt_du ++;
	  $err = "FS inconsistencie";
	  $bool_bd = $d_q->next_record();
	  $url = "(id=$id)";
	  if($action == "data_update") {
	    $update = "Have to update manually";
	  }
	}
	else {
	  $cpt_du ++;
	  $err = "DB inconsistencie";
	  $bool_fs = next($f_q);
	  $name = $file_name;
	  $fpath = $file_path ;
	  if(is_dir($document_path.$file_path.$file_name)) { 
	    $type = "Repository";
	    $kind = 0;
	    $author = "";
	    $bd_name = "";
	  }
	  else {
	    $type = "File";
	    $kind = 1;
	    $author = "System";
	    $bd_name = $file_name;
	  }
	  $url ="";
	  if($action == "data_update") {
	    $query = "insert into Document (
	      document_timeupdate,
	      document_timecreate,
	      document_userupdate,
	      document_usercreate,
	      document_title,
	      document_author,
	      document_name,
	      document_path,
	      document_kind,
	      document_size,
	      document_category1_id,
	      document_category2_id,
	      document_mimetype,
	      document_privacy
	    )
	    values (
	      null,
	      '" . date("Y-m-d H:i:s") . "',
	      null,
	      '0',
	      '$bd_name',
	      '$author',
	      '$file_name',
	      '$file_path',
	      '$kind',
	      '',
	      '',
	      '',
	      '',
	      '0'
	    )";
	    
	    if($db->query($query)) {
	      $update = "Updated";
	    }
	    else {
	      $update = "Error during the update";
	    }
	  }

	}
	echo "\Document (id=$id) : File : $fpath  $name Kind : $type Status : $err  $update\n";

      }
      else {
	$bool_fs = next($f_q);
	$bool_bd = $d_q->next_record();
      }
    } 
  }
  if ($mode == "txt") {

    echo "
------------------------------------------------------------------------------
*** Document : $nb_document to parse, $cpt_du inconsistencies
------------------------------------------------------------------------------
";
  } else {
    echo "<tr>
        <td class=\"adminLabel\" colspan=\"8\">
          Document : $nb_document to parse, $cpt_du inconsistencies</td>
      </tr>
      </table>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Update company sound key representation and aka
// In case of database update or sound function change
// Parametres:
//   - $mode : "txt" or "html"
///////////////////////////////////////////////////////////////////////////////
function dis_sound_aka_update($mode) {
  global $debug, $c_all;

  // Get the company list
  $c_q = get_company_list();
  $cpt_c = 0;
  $cpt_cu = 0;
  $nb_comp = $c_q->num_rows();
  
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** Sound Update : Company : $nb_comp to process
------------------------------------------------------------------------------
";
  } else {

    echo "
      <table class=\"admin\">
      <tr>
        <td class=\"adminHead\">Company ($nb_comp)</td>
        <td class=\"adminHead\">Aka and Sound Update</td>
      </tr>";
  }

  while ($c_q->next_record()) {
    $cpt_c++;

    $id = $c_q->f("company_id");
    $name = $c_q->f("company_name");
    $aka = $c_q->f("company_aka");
    // If reduced version of name not in aka (and not same as name), we add it
    $auto_aka = format_name($name, 0, true, true);
    if ( ($auto_aka != $name) && (strpos($aka, $auto_aka) === false) ) {
      $aka .= " $auto_aka";
    }
    $sound = phonetic_key($name);

    $retour = update_sound_aka_one_company($id, $aka, $sound);

    if ($cpt_c % 100 == 0) {
      if ($mode =="txt") {
	echo ".";
      } else {
	echo "<tr>
          <td class=\"adminLabel\" colspan=\"2\">$cpt_c</td>
        </tr>";
	flush();
      }
    }
  }

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** Sound update : Company $nb_comp processed
------------------------------------------------------------------------------
";
  } else {
    echo "<tr>
        <td class=\"adminLabel\" colspan=\"2\">
          Sound Update : Company : $nb_comp processed</td>
      </tr>
      </table>";
  }
}
