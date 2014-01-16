<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
//   - $mode : 'txt' ou 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_help($mode) {

  if ($mode == 'html')
    echo "<table>
      <tr>
        <td><pre>";

  include ('admin_data_help.inc');

  if ($mode == 'html')
    echo "</pre></td>
      </tr>
      </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the search form for data module
// Parameters:
//   - $mode        : 'txt' ou 'html'
//   - $actions     : actions allowed
//   - $modules     : target modules to show action
//   - $upd_modules : target modules to update action
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_index($mode, $actions, $modules, $upd_modules) {

  switch ($mode) {
  case 'txt':
    echo "try -h to help\n";
    break;
  case 'html':
    html_data_index($actions, $modules, $upd_modules);
    break;
  default:
    echo 'Invalid Mode ! (txt | html)';
  }

}


///////////////////////////////////////////////////////////////////////////////
// Display the HTML search form for lang module
// Parameters:
//   - $actions     : actions allowed
//   - $modules     : target modules to show action
//   - $upd_modules : target modules to update action
///////////////////////////////////////////////////////////////////////////////
function html_data_index($actions, $modules, $upd_modules) {
  global $l_execute, $l_help;

  // show : modules select
  $sel_module = "<select name=\"target_module\">";
  while (list ($key, $val) = each ($modules)) {
    $sel_module .= "<option value=\"$val\">$val";
  }
  $sel_module .= "</select>";

  // update : modules select
  $sel_upd_module = "<select name=\"target_module\">";
  while (list ($key, $val) = each ($upd_modules)) {
    $sel_upd_module .= "<option value=\"$val\">$val";
  }
  $sel_upd_module .= "</select>";

  echo "
    <form method=\"get\" action=\"" . url_prepare('admin_data_index.php'). "\">
    <div class=\"detail infos\">
    <h1>Inconsistencies</h1>
    <table class=\"spreadSheet\">
    <thead> 
    <tr>
      <th>Action : data_show</th>
      <th>module</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>Show inconsistencies in data</td>
      <td>$sel_module</td>
    </tr>
    </tbody>
    </table>
        <div class=\"buttons\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"data_show\" />
        <input type=\"submit\" value=\"$l_execute\" />
	</div>
    </div>
    </form>

    <form method=\"get\" action=\"" . url_prepare('admin_data_index.php'). "\">
    <div class=\"detail infos\">
    <h1>Update</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>Action : data_update</th>
      <th>module</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>Update inconsistencies in data</td>
      <td>$sel_upd_module</td>
    </tr>
    </tbody>
    </table>
    <div class=\"buttons\">
      <input type=\"hidden\" name=\"mode\" value=\"html\" />
      <input type=\"hidden\" name=\"action\" value=\"data_update\" />
      <input type=\"submit\" value=\"$l_execute\" />
   </div>
   </div>
    </form>


    <form method=\"get\" action=\"" . url_prepare('admin_data_index.php'). "\">
    <div class=\"detail infos\">
    <h1>Update aka and sound keys</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>action : sound_aka_update</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>Update aka and sound keys used for searches (Company, Contact)</td>
    </tr>
    </tbody>
    </table>
    
    <div class=\"buttons\">
      <input type=\"hidden\" name=\"mode\" value=\"html\" />
      <input type=\"hidden\" name=\"action\" value=\"sound_aka_update\" />
      <input type=\"submit\" value=\"$l_execute\" />
    </div>
    </div>
    </form>";
}


///////////////////////////////////////////////////////////////////////////////
// Display data inconsistency for the modules selected
// Parameters:
//   - $action : data_show || data_update (proceed the update)
//   - $mode   : 'txt' or 'html'
//   - $module : module name
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data($action, $mode, $module) {
  global $debug, $c_all;

  if ($module == '$c_all') {
    dis_admin_data_company($action, $mode);

  } else if ($module == 'company') {
    dis_admin_data_company($action, $mode);
  } else if ($module == 'deal') {
    dis_admin_data_deal($action, $mode);
  } else if ($module == 'list') {
    dis_admin_data_list($action, $mode);
  } else if ($module == 'document') {
    dis_admin_data_document($action, $mode);
  } else if ($module == 'group') {
    dis_admin_data_group($action, $mode);
  } else if ($module == 'user') {
    dis_admin_data_user($mode);
  } else {
    echo 'No data inconsistency to check here... bye.';
  }
    
}


///////////////////////////////////////////////////////////////////////////////
// Display (/and update) data inconsistency for the company module
// Parametres:
//   - $action : data_show || data_update (proceed the update)
//   - $mode : 'txt' or 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_company($action, $mode) {
  global $debug, $c_all;

  // Get the company list
  $c_q = get_admin_data_company_list();
  $cpt_c = 0;
  $cpt_cu = 0;
  $nb_comp = $c_q->num_rows();
  
  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Company : $nb_comp to parse
------------------------------------------------------------------------------";
  } else {
    if ($action == 'data_update') {
      $update = "<th>Update</th>";
    } else {
      $update = '';
    }
    echo "
      <div class=\"detail extra\">
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>Company ($nb_comp)</th>
        <th colspan=\"2\"># Contact</th>
        <th colspan=\"2\"># Deal Active</th>
        <th colspan=\"2\"># Deal Total</th>
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
      </tr>
      </thead>";
  }

  while ($c_q->next_record()) {
    $cpt_c++;

    $id = $c_q->f('company_id');
    $con_num = $c_q->f('company_contact_number');
    $deal_num = $c_q->f('company_deal_number');
    $deal_total = $c_q->f('company_deal_total');

    $new_con_num = get_admin_data_company_contact_number($id);
    $new_deal_num = get_admin_data_company_active_deal_number($id);
    $new_deal_total = get_admin_data_company_total_deal_number($id);
    //$new_deal_num = '0';
    //$new_deal_total = '0';

    if ( ($con_num != $new_con_num) || ($deal_num != $new_deal_num)
         || ($deal_total != $new_deal_total) ) {
      $cpt_cu++;
      if ($mode == 'txt') {
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

      if ($action == 'data_update') {
	$retour = update_admin_data_one_company($id, $new_con_num, $new_deal_num, $new_deal_total);
	if ($retour) {
	  if ($mode == 'txt') {
	    echo 'OK';
	  } else {
	    echo "<td>OK</td>";
	  }
	} else {
	  if ($mode == 'txt') {
	    echo 'Problem !';
	  } else {
	    echo '<td>Problem !</td>';
	  }
	}
      }

      if ($mode == 'html') {
	echo "</tr>";
      }

    }

    if ($cpt_c % 100 == 0) {
      if ($mode == 'txt') {
	echo '.';
      } else {
	echo "<tr>
          <td colspan=\"8\">$cpt_c</td>
        </tr>";
	flush();
      }
    }
  }

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Company : $nb_comp to parse, $cpt_cu inconsistencies
------------------------------------------------------------------------------
";
  } else {
    echo "
       <thead>
       <tr>
        <th colspan=\"8\">
          Company : $nb_comp to parse, $cpt_cu inconsistencies</th>
      </tr>
      </thead>
      </table>
      </div>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display (/and update) data inconsistency for the Deal module
// Parametres:
//   - $action : data_show || data_update (proceed the update)
//   - $mode : 'txt' or 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_deal($action, $mode) {
  global $path, $debug, $c_all, $php_regexp_number;

  // Get the deal list
  $d_q = get_admin_data_deal_list();
  $cpt_d = 0;
  $cpt_du = 0;
  $nb_deal = $d_q->num_rows();
  
  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Deal : $nb_deal to parse
------------------------------------------------------------------------------";
  } else {
    if ($action == 'data_update') {
      $update = "<th>Update</th>";
    } else {
      $update = '';
    }
    echo "
    <div class=\"detail extra\">
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>Deal ($nb_deal)</th>
        <th>Status</th>
        <th>Archive</th>
        <th>Hitrate</th>
        <th>Should be</th>
        $update
      </tr>
      </thead>";
  }

  while ($d_q->next_record()) {
    $cpt_d++;

    $id = $d_q->f('deal_id');
    $status = $d_q->f('dealstatus_label');
    $archive = $d_q->f('deal_archive');
    $arch = (($archive == '1') ? 'X' : "&nbsp;");
    $hitrate = $d_q->f('deal_hitrate');
    $new_hitrate = $d_q->f('dealstatus_hitrate');

    // If current hitrate doesn't match status hitrate
    // or hitrate not a number
    // or deal archived but hit rate not 0 nor 100
    // or deal archived but status tells deal not closed
    if ( ( ($new_hitrate != '') && ($new_hitrate != $hitrate) )
         || (! preg_match($php_regexp_number, $hitrate) )
	 || ( ($archive == '1') && ($hitrate != '0') && ($hitrate != '100') )
	 || ( ($archive == '1') && ($new_hitrate != '0') && ($new_hitrate != '100') )
	 ) {
      $cpt_du++;
      if ($mode == 'txt') {
	$arch = (($archive == '1') ? 'X' : ' ');
	echo "\nDeal (id=$id) : status $status, archive $arch hitrate $hitrate, new_hitrate $new_hitrate";
      } else {
	$arch = (($archive == '1') ? 'X' : '&nbsp;');
	echo "<tr>
          <td><a href=\"$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$id\">$id</a></td>
          <td>$status</td>
          <td>$arch</td>
          <td>$hitrate</td>
          <td>$new_hitrate</td>";
      }

      if ($action == 'data_update') {
	$retour = false;
	if ( ($new_hitrate != '') && ($new_hitrate != $hitrate) ) {
	  $retour = update_admin_data_one_deal($id, $new_hitrate);
	}
	if ($retour) {
	  if ($mode == 'txt') {
	    echo 'OK';
	  } else {
	    echo "<td>OK</td>";
	  }
	} else {
	  if ($mode == 'txt') {
	    echo 'Manual !';
	  } else {
	    echo "<td>Manual !</td>";
	  }
	}
      }

      if ($mode == 'html') {
	echo "</tr>";
      }

    }

    if ($cpt_d % 100 == 0) {
      if ($mode == 'txt') {
	echo '.';
      } else {
	echo "<tr>
          <td colspan=\"8\">$cpt_d</td>
        </tr>";
	flush();
      }
    }
  }

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Deal : $nb_deal to parse, $cpt_du inconsistencies
------------------------------------------------------------------------------
";
  } else {
    echo "<thead><tr>
        <td colspan=\"8\">
          Deal : $nb_deal to parse, $cpt_du inconsistencies</td>
      </tr></thead>
      </table>
      </div>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display (/and update) data inconsistency for the list module
// This concerns dynamic queries generation
// Parametres:
//   - $action : data_show || data_update (proceed the update)
//   - $mode : 'txt' or 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_list($action, $mode) {
  global $debug, $path, $c_all;

  // To include specic list queries (make_query_from_criteria,...)
  require_once("$path/list/list_query.inc");

  // Get the list list
  $l_q = get_admin_data_list_list();
  $cpt_l = 0;
  $cpt_lu = 0;
  $nb_l = $l_q->num_rows();
  
  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** List : $nb_l to parse
------------------------------------------------------------------------------";
  } else {
    if ($action == 'data_update') {
      $update = "<td class=\"adminLabel\">Update</td>";
    } else {
      $update = '';
    }
    echo "
      <div class=\"detail extra\">
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th colspan=\"2\">List ($nb_l)</th>
        <th colspan=\"2\"># Static Contact</th>
        <th colspan=\"2\"># Query Contact</th>
        <th>Query Generation =</th>
        $update
      </tr><tr>
        <th>Id</th>
        <th>Name</th>
        <th>field</th>
        <th>calculated</th>
        <th>field</th>
        <th>calculated</th>
        <th>&nbsp;</th>
        $update
      </tr>
      </thead>";
  }

  // Loop through each list
  while ($l_q->next_record()) {
    $cpt_l++;

    $id = $l_q->f('list_id');
    $name = $l_q->f('list_name');
    $sta_nb = $l_q->f('list_static_nb');
    $dyn_nb = $l_q->f('list_query_nb');
    $query = $l_q->f('list_query');

    $criteria = unserialize($l_q->f('list_structure'));

    if ($criteria != '') {
      $dynlist = make_list_query_from_db($l_q);
      $new_query = $dynlist['query'];
    } else {
      $new_query = $l_q->f('list_query');
    }

    if ($new_query != '') {
      $query_nb = get_list_query_num_rows($dynlist, $l_q);
      $query_txt = $query_nb;
    } else {
      $query_nb = '0';
      $query_txt = 'No';
    }

    $new_sta_nb = get_list_static_contact_nb($id);
    $new_dyn_nb = $query_nb;

    if (($sta_nb != $new_sta_nb) || ($query != $new_query)) {
      $query_diff = true;
      $query_diff_txt = 'X';
    } else {
      $query_diff = false;
      $query_diff_txt = 'OK';
    }

    if ( ($sta_nb != $new_sta_nb) || ($dyn_nb != $new_dyn_nb) || ($query_diff) ) {
      $cpt_lu++;
      if ($mode == 'txt') {
	echo "\nList (id=$id), Contacts : Statiques ($sta_nb, $new_sta_nb) : Dynamiques ($dyn_nb, $new_dyn_nb)";
      } else {
	echo "<tr>
          <td>$id</td>
          <td>$name</td>
          <td>$sta_nb</td>
          <td>$new_sta_nb</td>
          <td>$dyn_nb</td>
          <td>$new_dyn_nb</td>
          <td>$query_diff_txt</td>";
      }

      if ($action == 'data_update') {
	$up_query = addslashes($new_query);
	$retour = update_admin_data_one_list($id, $new_sta_nb, $new_dyn_nb, $up_query);
	if ($retour) {
	  if ($mode == 'txt') {
	    echo 'OK';
	  } else {
	    echo "<td>OK</td>";
	  }
	} else {
	  if ($mode == 'txt') {
	    echo 'Problem !';
	  } else {
	    echo "<td>Problem !</td>";
	  }
	}
      }

      if ($mode == 'html') {
	echo "</tr>";
      }

    }

    if ($cpt_l % 100 == 0) {
      if ($mode == 'txt') {
	echo '.';
      } else {
	echo "<tr>
          <td colspan=\"8\">$cpt_l</td>
        </tr>";
	flush();
      }
    }
  }

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** List : $nb_l to parse, $cpt_lu inconsistencies
------------------------------------------------------------------------------
";
  } else {
    echo "<thead><tr>
        <td class=\"adminLabel\" colspan=\"8\">
          List : $nb_l to parse, $cpt_lu inconsistencies</td>
      </tr>
      </thead>
      </table>
      </div>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display data inconsistency for the Document module
// For now, no update is done
// Parametres:
//   - $action : data_show || data_update (proceed the update)
//   - $mode : 'txt' or 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_document($action, $mode) {
  global $debug, $c_all, $php_regexp_number, $cdocument_root;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;

  // Get the document list
  $d_q = get_admin_data_document_list();
  $disk_array = get_admin_data_file_list("$cdocument_root/");
  $nb_files = count ($disk_array);
  $cpt_err = 0;
  $nb_document = $d_q->num_rows();
  $db = new DB_OBM;

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Document : $nb_document to parse in database and
at least as much in the file system.
------------------------------------------------------------------------------
Id | Document Path | Name | Kind | Disk Path | Missing DB | Missing FS
";
  } else {
    if ($action == 'data_update') {
      $update = "<th>Update</th>";
    } else {
      $update = '';
    }
    echo "
      <div class=\"detail extra\">
      <table class=\"spreadSheet\">
      <thead>
      <tr>
	<th>Id (# $nb_document)</th>
        <th>Document Path</th>
        <th>Document Name</th>
	<th>Document Kind</th>
        <th>Disk Path <br />($cdocument_root)</th>
	<th>Missing <br /> in Database</th>
        <th>Missing <br /> in File System</th>
        $update
      </tr>
      </thead>";
  }

  // First Loop through DB
  $miss_bd = '';
  while ($d_q->next_record()) {
    $is_ok = true;
    $id = $d_q->f('document_id');
    $kind = $d_q->f('document_kind');
    $dpath = $d_q->f('document_path');
    $name = $d_q->f('document_name');
    if ($kind == $cdoc_kind_dir) {
      $type = 'Directory';
    } elseif ($kind == $cdoc_kind_file) {
      $type = 'File';
    } elseif ($kind == $cdoc_kind_link) {
      $type = 'Link';
    }
    $disk_path = get_document_disk_path($id);
    $disk_fn = $disk_path . $id;
    $disk_rel = get_admin_data_document_disk_relative_path($id);
    
    // If document is a file, check if present in filesystem
    if ($kind == $cdoc_kind_file) {
      if (file_exists($disk_fn)) {
	$miss_fs = '';
      } else {
	$is_ok = false;
	$miss_fs = 'X';
      }
    }

    // If problem, display the entry
    if (! $is_ok) {
      $cpt_err++;
      if ($mode == 'txt') {
	$dis_id = $id;
	$line = "$dis_id | $dpath | $name | $type | $disk_rel | $miss_bd | $miss_fs - $update\n";
      } else {
	$dis_id = "<a href=\"$path/document/document_index.php?action=detailconsult&amp;document_id=$id\">$id</a>";
	$line = "
          <tr>
	<td>$dis_id</td>
        <td>$dpath</td>
        <td>$name</td>
        <td>$type</td>
        <td>$disk_rel</td>
	<td>$miss_bd</td>
        <td>$miss_fs</td>
        $update
      </tr>";
      }
      echo $line;

      // Entry is ok
    } else {
      // We delete it from the disk_array (only if is a file)
      if ($kind == $cdoc_kind_file) {
	unset($disk_array[$disk_rel]);
      }
    }
  } // End Loop through BD


  // Loop through Files inf filesystem not present in DB
  foreach($disk_array as $disk_rel=>$info) {
    $cpt_err++;
    $type = 'File';

    if ($mode == 'txt') {
      $line = " | | | $type | $disk_rel | X | |\n";
    } else {
      $line = "
        <tr>
	<td></td>
        <td></td>
        <td></td>
        <td>$type</td>
        <td>$disk_rel</td>
	<td>X</td>
        <td></td>
      </tr>";
    }
    echo $line;
  }

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Document : $nb_document to parse, $cpt_err inconsistencies
------------------------------------------------------------------------------
";
  } else {
    echo "<thead><tr>
        <th colspan=\"8\">
          Document : $nb_document from DB, $nb_files Files from disk, $cpt_err inconsistencies</th>
      </tr>
      </thead>
      </table>
      </div>";
  }

}


/**
 * Display and update internal group mapping
 * @param $action : data_show || data_update (proceed the update)
 * @param $mode : 'txt' or 'html'
**/
function dis_admin_data_group($action, $mode) {
  global $debug, $c_all;

  // Get the public group list
  $g_q = get_admin_data_group_list();
  $cpt_err = 0;
  $cpt_g = 0;
  $nb_group = $g_q->num_rows();

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Public Groups : $nb_group to update
------------------------------------------------------------------------------
";
  } else {
    if ($action == 'data_update') {
      $update = "<th>Update</th>";
    } else {
      $update = '';
    }
    echo "
      <div class=\"detail extra\">
      <table class=\"spreadSheet\">
      <thead>
      <tr>
	<th>#</th>
	<th>Id</th>
        <th>Group Name</th>
        $update
      </tr>
      </thead>";
  }

  while ($g_q->next_record()) {
    $cpt_g++;

    $id = $g_q->f('group_id');
    $name = $g_q->f('group_name');

    if ($mode == 'txt') {
      echo "\nGroup #$cpt_g (id=$id) $name : ";
    } else {
      echo "<tr>
          <td>$cpt_g</td>
          <td>$id</td>
          <td>$name</td>";
    }

    if ($action == 'data_update') {
      $ret = of_usergroup_update_group_node($id);
      $ret += of_contactgroup_update_group_node($id);
      if ($ret) {
	if ($mode == 'txt') {
	  echo 'OK';
	} else {
	  echo "<td>OK</td>";
	}
      } else {
	$cpt_err++;
	if ($mode == 'txt') {
	  echo 'Problem !';
	} else {
	  echo '<td>Problem !</td>';
	}
      }
    }
    
    if ($mode == 'html') {
      echo "</tr>";
    }
  }

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Group : $nb_group to parse, $cpt_err errors
------------------------------------------------------------------------------
";
  } else {
    echo "
       <thead>
       <tr>
        <th colspan=\"8\">
          Group : $nb_group to parse, $cpt_err errors</th>
      </tr>
      </thead>
      </table>
      </div>";
  }
}



/**
 * Display and update internal user index
 * @param $mode : 'txt' or 'html'
**/
function dis_admin_data_user($mode) {
  global $debug, $c_all;

  // Get the public group list
  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Updating user index 
------------------------------------------------------------------------------
";
  } else {
    if ($action == 'data_update') {
      $update = "<th>Update</th>";
    } else {
      $update = '';
    }
    echo "<h1>Updating user index</h1>";
  }
  of_index_update('user');

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
***  User index updated
------------------------------------------------------------------------------
";
  } else {
    echo "<h1>Updating user index</h1>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Update sound key representation and aka
// In case of database update or sound function change
// Parametres:
//   - $mode : 'txt' or 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_sound_aka_update($mode) {

  dis_admin_data_sound_aka_update_company($mode);
  dis_admin_data_sound_aka_update_contact($mode);

}


///////////////////////////////////////////////////////////////////////////////
// Update company sound key representation and aka
// In case of database update or sound function change
// Parametres:
//   - $mode : 'txt' or 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_sound_aka_update_company($mode) {
  global $debug, $c_all, $l_module_company;

  // Get the company list
  $c_q = get_admin_data_company_list();
  $cpt_c = 0;
  $cpt_cu = 0;
  $nb_comp = $c_q->num_rows();
  
  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Sound update : $l_module_company : $nb_comp to process
------------------------------------------------------------------------------";
  } else {

    echo "
      <div class=\"detail extra\">
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>$l_module_company ($nb_comp)</th>
        <th>Aka and Sound Update</th>
      </tr>
      </thead>";
  }

  while ($c_q->next_record()) {
    $cpt_c++;

    $id = $c_q->f('company_id');
    $name = $c_q->f('company_name');
    $aka = $c_q->f('company_aka');
    // If reduced version of name not in aka (and not same as name), we add it
    $auto_aka = format_name($name, 0, true, true);
    if ( ($auto_aka != $name) && (strpos($aka, $auto_aka) === false) ) {
      $aka .= " $auto_aka";
    }
    $sound = phonetic_key($name);

    $retour = update_admin_data_sound_aka_one_company($id, $aka, $sound);

    if ($cpt_c % 100 == 0) {
      if ($mode == 'txt') {
	echo '.';
      } else {
	echo "<tr>
          <td colspan=\"2\">$cpt_c</td>
        </tr>";
	flush();
      }
    }
  }

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Sound update : $l_module_company : $nb_comp processed
------------------------------------------------------------------------------
";
  } else {
    echo "<thead><tr>
        <td colspan=\"2\">
          Sound Update : $l_module_company : $cpt_c processed</td>
      </tr>
      </thead>
      </table>
      </div>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Update contact sound key representation and aka
// In case of database update or sound function change
// Parametres:
//   - $mode : 'txt' or 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_sound_aka_update_contact($mode) {
  global $debug, $c_all, $l_module_contact;

  // Get the contact list
  $c_q = get_admin_data_contact_list();
  $cpt_c = 0;
  $cpt_cu = 0;
  $nb = $c_q->num_rows();
  
  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Sound update : $l_module_contact : $nb to process
------------------------------------------------------------------------------";
  } else {

    echo "
      <div class=\"detail extra\">
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>$l_module_contact ($nb)</th>
        <th>Aka and Sound Update</th>
      </tr>
      </thead>";
  }

  while ($c_q->next_record()) {
    $cpt_c++;

    $id = $c_q->f('contact_id');
    $name = $c_q->f('contact_lastname');
    $aka = $c_q->f('contact_aka');

    // An empty name is not allowed, but in case
    if ($name != '') {
      // If reduced version of name not in aka (and not same as name) we add it
      $auto_aka = format_name($name, 0, true, true);
      if ($auto_aka == '') {
	$auto_aka = ' ';
      }
      if ( ($auto_aka != $name) && (strpos($aka, $auto_aka) === false) ) {
	$aka .= " $auto_aka";
      }
      $sound = phonetic_key($name);
      
      $retour = update_admin_data_sound_aka_one_contact($id, $aka, $sound);
    }

    if ($cpt_c % 100 == 0) {
      if ($mode == 'txt') {
	echo '.';
      } else {
	echo "<tr>
          <td colspan=\"2\">$cpt_c</td>
        </tr>";
	flush();
      }
    }
  }

  if ($mode == 'txt') {
    echo "
------------------------------------------------------------------------------
*** Sound update : $l_module_contact : $cpt_c processed
------------------------------------------------------------------------------
";
  } else {
    echo "
    <thead>
    <tr>
        <td colspan=\"2\">
          Sound Update : $l_module_contact : $nb processed</td>
      </tr>
      </thead>
      </table>
      </div>";
  }
}

?>
