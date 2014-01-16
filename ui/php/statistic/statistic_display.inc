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
// OBM - File : statistic_display.php                                        //
//     - Desc : Statistic Display File                                       //
// 2004-04-19 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display Statistics differents actions
// Parameters:
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_statistic_index() {
  global $path, $cgp_user,  $action, $actions, $module, $perm;
  global $l_stats, $l_company, $l_contact, $l_stats_manager;
  global $l_stats_contact_date_evolution;

  if (is_array($cgp_user['company']['category'])) {
    foreach($cgp_user['company']['category'] as $cat_name => $one_cat) {
      global ${"l_$cat_name"};
      $l_cat = ${"l_$cat_name"};
      $block_company .= dis_statistic_category_index_item('company', $cat_name, $l_cat);
    }
  }

  if (is_array($cgp_user['contact']['category'])) {
    foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
      global ${"l_$cat_name"};
      $l_cat = ${"l_$cat_name"};
      $block_contact .= dis_statistic_category_index_item('contact', $cat_name, $l_cat);
    }
  }

  $block = "
    <div class=\"detail infos\">
    <h1>$l_stats</h1>
    <a
    href=\"$path/statistic/statistic_index.php?action=manager\">$l_stats_manager</a><br/>
    <a href=\"$path/statistic/statistic_index.php?action=contact_date_evolution\">$l_stats_contact_date_evolution</a>
    </div>
    <div class=\"detail infos\">
    <h1>$l_company</h1>
    $block_company
    </h1>
    </div>
    <div class=\"detail infos\">
    <h1>$l_contact</h1>
    $block_contact
    </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display a category index item
// Parameters:
//   - $entity   : entity to calculate the stats
//   - $category : category to calculate the stats
//   - $title    : category name or title
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_statistic_category_index_item($entity, $category, $title) {
  global $path, $c_all;
  global $l_stats_from_list, $l_stats_from_all;

  $url = "$path/list/list_index.php?action=ext_get_id&amp;popup=1&amp;ext_url=".urlencode($path."/statistic/statistic_index.php?action=stats_category&amp;category=$category&amp;entity=$entity&amp;list_id=")."&amp;ext_target=$l_statistc";
  $block .= "
  <form action=\"statistic_index.php\">
  <div class=\"detail extra\">
  <h1>$title</h1>
  <div class=\"buttons\">
  <input type=\"button\"
  onclick=\"popup('$url','$l_statistc'); return false; \" value=\"$l_stats_from_list\" />
  <input type=\"hidden\" name=\"action\" value=\"stats_category\" />
  <input type=\"hidden\" name=\"category\" value=\"$category\" />
  <input type=\"hidden\" name=\"entity\" value=\"$entity\" />
  <input type=\"hidden\" name=\"list_id\" value=\"$c_all\" />
  <input type=\"submit\" value=\"$l_stats_from_all\" />
  </div>
  </div>
  </form>";

  return $block; 
}


///////////////////////////////////////////////////////////////////////////////
// Display Statistics About the responsibles
// Parameters:
//  - $cont_q : DB Object for contact stats
//  - $comp_q : DB Object for companies stats
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_statistic_resp_stats($cont_q,$comp_q) {
  global $path;
  global $l_module_company, $l_module_contact, $l_key;

  $max = 1;
  while($cont_q->next_record()) {
    $stat[$cont_q->f('userobm_id')]['contact'] = $cont_q->f('cont_stat');
     $stat[$cont_q->f('userobm_id')]['name'] = $cont_q->f('userobm_firstname').' '.$cont_q->f('userobm_lastname');
    if($cont_q->f('cont_stat') > $max) {
      $max = $cont_q->f('cont_stat');
    }
  } 
  while($comp_q->next_record()) {
    $stat[$comp_q->f('userobm_id')]['company'] = $comp_q->f('comp_stat');
    $stat[$comp_q->f('userobm_id')]['name'] = $comp_q->f('userobm_firstname').' '.$comp_q->f('userobm_lastname');
    if($comp_q->f('comp_stat') > $max) {
      $max = $comp_q->f('comp_stat');
    }
  } 
  $block .= "<table class=\"spreadSheet\">";

  if (is_array($stat)) {
    foreach($stat as $id => $value) {
      $cont = round(($value['contact']/$max)*90)+0.1;
      $comp = round(($value['company']/$max)*90)+0.1;
      $block .= "
  <tr>
    <th rowspan=\"2\" style=\"width:150px;\">".$value['name']."</th>
    <td>
    <table style=\"width:100%;\">
    <tr>
      <td style=\"width:".$cont."%; background-color:#DE5B5B;\"></td>
      <td>".$value['contact']."</td>
    </tr>
    </table>
    </td>
  </tr>
  <tr>
    <td>
    <table style=\"width:100%;\">
    <tr>
      <td style=\"width:".$comp."%; background-color:#5C8CBC;\"></td>
      <td>".$value['company']."</td>
    </tr>
    </table>   
    </td>
  </tr>";
    }
  }
   
  $block .= "
  <tr>
    <th rowspan=\"2\">$l_key</th>
    <td>
    <table>
    <tr>
      <th style=\"width:20px;background-color:#DE5B5B;\">&nbsp;</td>
      <td>$l_module_contact</td>
    </tr>
    </table>
    </td>
  </tr>
  <tr>
    <td>
    <table>
    <tr>
      <td style=\"width:20px;background-color:#5C8CBC;\">&nbsp;</td>
      <td>$l_module_company</td>
    </tr>
    </table>   
    </td>
  </tr>
  </table>"; 

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Statistics : category / country repartition for an entity
// Parameters:
// - $obm_q : DB Object with all entity datas
// - $tot : Total of companies... unused for now
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_statistic_cat_stats($obm_q, $tot) {
  global $path;
  global $l_null,$l_totals, $l_header_country, $l_category;

  while ($obm_q->next_record()) {
    if ($obm_q->f('category_id') == NULL) {
      $cat_id = 0;
      $cat_code = $l_null;
    } else {
      $code = $obm_q->f('category_code');
      $cat_id = $obm_q->f('category_id');
      $cat_code = trim($code);
      $cat_label = $obm_q->f('category_label');
      $cat_major_code = substr($cat_code,0,strpos($cat_code,'.'));
      // If code is a major code (not .)
      if ($cat_major_code == '') {
	$cat_major_code = $cat_code;
      }
    }
    if ($obm_q->f('country_iso3166') == NULL || $obm_q->f('country_iso3166') == '') {
      $cty_iso = $l_null;
    } else {
      $cty_iso = $obm_q->f('country_iso3166');
      $cty_d = $obm_q->f('country_name');
    }
    $nb = $obm_q->f('nb');
    $stat[$cty_iso][$cat_id] += $nb;
    $cty_count[$cty_iso] += $nb;
    
    if ((! isset($cat_count[$cat_id])) && ($cat_id != 0)) {
      $cat_major_col[$cat_major_code] += 1;
    }
    $cat_count[$cat_id] += $nb;
    if ($cat_id != 0) {
      $cat_major_count[$cat_major_code] += $nb;
    }
    $cat[$cat_id] = $cat_code;
    $cty_desc[$cty_iso] = $cty_d;
    $cat_desc[$cat_id] = $cat_label;
  }

  $cat_link_total = array_sum($cat_count);

  natsort($cat);
  $block .= "
  <div class=\"detail extra\">
  <table class=\"spreadSheet\">
  <thead><tr>
  <th></th>";

  foreach($cat as $cat_id =>$cat_name) {
    if ($cat_id != 0) {
      $title = "title=\"$cat_desc[$cat_id]\"";
    } else {
      $onOver = '';
    }
    $cat_label .= "<th $title>$cat_name</th>";
    $percent = round(($cat_count[$cat_id])*100/$cat_link_total,1);    
    $cat_percent .= "<td>$percent</td>";
    $cat_tots .= '<td>'.$cat_count[$cat_id].'</td>';
  }

  $pair = 0;
  $block .= "$cat_label<th>$l_totals</th><th>%</th></tr></thead>";
  foreach ($stat as $cty_iso => $data) {
    if ($cty_iso != '') {
      $title = "title=\"$cty_desc[$cty_iso]\"";
    } else {
      $onOver = '';
    }
    if ($pair == 0) {
      $pair = 1;
      $block .= "<tr class=\"pair\">";
    } else {
      $pair = 0;
      $block .= "<tr class=\"impair\">";
    }
    $block .= "<th $title>$cty_iso</th>";
    foreach($cat as $cat_id => $cat_name) {
      $block .= '<td>'.$data[$cat_id].'</td>';
    }
    $percent = round(($cty_count[$cty_iso])*100/$cat_link_total,1);
    $block .= '<td>'.$cty_count[$cty_iso].'</td>';
    $block .= "<td>$percent</td>";
    $block .= '</tr>';
  }
  if (is_array($cat_major_count)) {
    // sort by major codes
    ksort($cat_major_count);
    foreach($cat_major_count as $cat_code => $value) {
      $super_cat .= "<td colspan=\"".$cat_major_col[$cat_code]."\">$value</td>";
      $percent = round(($value)*100/$cat_link_total,1);
      $super_cat_p .= "<td colspan=\"".$cat_major_col[$cat_code]."\">$percent</td>";
    }
  }
  $block .= "
  <tr class=\"highlight\"><th>$l_totals</th>
  $cat_tots<td>$cat_link_total</td><td></td></tr>
  <tr><th>%</th>$cat_percent<td></td><td>100</td></tr>
  <tr><th></th>$cat_label<th>$l_totals</th><th>%</th></tr>
  <tr><th></th>$super_cat<th></th><th></th></tr>
  <tr><th>%</th>$super_cat_p<th></th><th></th></tr>
   </table>
   </div>
   <br />
  <div class=\"detail infos\">
  <h1> x = $l_category </h1>
  <h1> y = $l_header_country</h1>
  </div>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Export Statistics : Category / country entity repartition
// Parameters:
// - obm_q : DB Object with all entity datas
// - tot : Total of companies... unused for now
// Returns:
///////////////////////////////////////////////////////////////////////////////
function export_statistic_cat_stats($obm_q,$tot) {
  global $path;
  global $l_null,$l_totals, $l_header_country,$l_compcat;

  while ($obm_q->next_record()) {
    if ($obm_q->f('category_id') == NULL) {
      $cat_id = 0;
      $cat_code = $l_null;
    } else {
      $code = $obm_q->f('category_code');
      $cat_id = $obm_q->f('category_id');
      $cat_code = trim($code);
      $cat_label = $obm_q->f('category_label');
      $cat_major_code = substr($cat_code,0,strpos($cat_code,'.'));
      // If code is a major code (not .)
      if ($cat_major_code == '') {
	$cat_major_code = $cat_code;
      }
    }
    if ($obm_q->f('country_iso3166') == NULL || $obm_q->f('country_iso3166') == '') {
      $cty_iso = $l_null;
    } else {
      $cty_iso = $obm_q->f('country_iso3166');
      $cty_d = $obm_q->f('country_name');
    }
    $nb = $obm_q->f('nb');
    $stat[$cty_iso][$cat_id] += $nb;
    $cty_count[$cty_iso] += $nb;
    
    if ((!$cat_count[$cat_id]) && ($cat_id != 0)) {
      $cat_major_col[$cat_major_code] += 1;
    }
    $cat_count[$cat_id] += $nb;
    if ($cat_id != 0) {
      $cat_major_count[$cat_major_code] += $nb;
    }
    $cat[$cat_id] = $cat_code;
    $cty_desc[$cty_iso] = $cty_d;
    $cat_desc[$cat_id] = $cat_label;
  }

  $cat_link_total = array_sum($cat_count);
  natsort($cat); 
  foreach($cat as $cat_id =>$cat_name) {
    if ($cat_id != 0) {
      $cat_label .= $_SESSION['set_csv_sep']."\"$cat_name ".$cat_desc[$cat_id]."\""; 
    } else {
      $cat_label .=  $_SESSION['set_csv_sep']."\"$cat_name\"";
    }
    $percent = round(($cat_count[$cat_id])*100/$cat_link_total,1);    
    $cat_percent .= $_SESSION['set_csv_sep']."\"$percent\"";
    $cat_tots .= $_SESSION['set_csv_sep']."\"".$cat_count[$cat_id]."\"";
  }   
  
  $block .= "\"\"$cat_label".$_SESSION['set_csv_sep']."\"$l_totals\"".$_SESSION['set_csv_sep']."\"%\"\n";
  foreach ($stat as $cty_iso => $data) {
    if($cty_iso != $l_null) {
      $block .= "\"$cty_desc[$cty_iso]\"";
    } else {
      $block .= "\"$l_null\"";
    }
    foreach($cat as $cat_id => $cat_name) {
      $block .= $_SESSION['set_csv_sep']."\"".$data[$cat_id]."\"";
    }
    $percent = round(($cty_count[$cty_iso])*100/$cat_link_total,1);
    $block .= $_SESSION['set_csv_sep']."\"".$cty_count[$cty_iso]."\"";
    $block .= $_SESSION['set_csv_sep']."\"$percent\"";
    $block .= "\n";
  }

  $block .= "\"$l_totals\"$cat_tots".$_SESSION['set_csv_sep']."\"$cat_link_total\"".$_SESSION['set_csv_sep']."\"\"\n";
  $block .= "\"%\"$cat_percent".$_SESSION['set_csv_sep']."\"\"".$_SESSION['set_csv_sep']."\"100\"\n";
  header('Content-Type: text/comma-separated-values');  
  header('Content-Disposition: inline; filename=companies_stats.csv');
  header('charset=utf-8');  
  echo $block;
  exit();
}


///////////////////////////////////////////////////////////////////////////////
// Display Statistics about Contact number evolution by date
// Parameters:
// - stats : contact total number evolution by date 
// Returns:
// - image inline
///////////////////////////////////////////////////////////////////////////////
function dis_statistic_contact_date_evolution_graph($stats) {
  global $obminclude;

  require_once "$obminclude/Artichow/LinePlot.class.php";
  if (count($stats['data']) > 0) {
    $graph = new Graph(600, 400);
    $graph->setAntiAliasing(TRUE);
    $blue = new Color(0, 0, 200);
    $red = new Color(200, 0, 0);
    $green = new Color(0, 200, 0);

    $group = new PlotGroup;
    $group->setBackgroundColor(new Color(240, 240, 240));
    $group->setPadding(40, 40);

    $values = $stats['data'];
    $plot = new LinePlot($values);
    $plot->setColor($blue);
    $group->add($plot);

    $group->axis->bottom->setLabelText($stats['legend']);
    $group->axis->bottom->setNumberByTick('minor', 'major', 12);
    $group->axis->bottom->setTickInterval(12);

    $graph->add($group);
    $graph->draw();
  } else { 
    false;
  }

}

?>
