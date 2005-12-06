<script language="php">
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
function dis_menu_stats() {
  global $path, $set_theme;
  global $set_theme, $action;
  global $actions, $module, $perm;
  global $l_statistic;

  $ret = "
  <div class=\"menusInner\">
  <div class=\"block\">
  <div class=\"blockTitle\">:: $l_statistic </div>
  <ul class=\"blockItem\">";

  if (is_array($actions[$module])) {
    $module_right = $perm->get_module_rights($module); 
    foreach ($actions[$module] as $key => $value) {
      if (in_array("content", $value["Condition"])) {
	if (($module_right & $value["Right"]) == $value["Right"]) {
	    $ret .= "
        <li class=\"blockItem\">
	<a href=\"".$value["Url"]."\"><img src=\"".C_IMAGE_PATH."/$set_theme/".$value["Ico"]."\" alt=\"\"></a>
         <a href=\"".$value["Url"]."\">".$value["Name"]."</a></li>";
	}
      }
    }
  }
   
  $ret .= "</ul>
        </div>
	</div>";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display Statistics About the responsibles
// Parameters:
//  - $cont_q : DB Object for contact stats
//  - $comp_q : DB Object for companies stats
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_resp_stats($cont_q,$comp_q) {
  global $path, $set_theme;
  global $set_theme, $l_module_company, $l_module_contact, $l_key;

  $max = 1;
  while($cont_q->next_record()) {
    $stat[$cont_q->f("userobm_id")]["contact"] = $cont_q->f("cont_stat");
     $stat[$cont_q->f("userobm_id")]["name"] = $cont_q->f("userobm_firstname")." ".$cont_q->f("userobm_lastname");
    if($cont_q->f("cont_stat") > $max) {
      $max = $cont_q->f("cont_stat");
    }
  } 
  while($comp_q->next_record()) {
    $stat[$comp_q->f("userobm_id")]["company"] = $comp_q->f("comp_stat");
    $stat[$comp_q->f("userobm_id")]["name"] = $comp_q->f("userobm_firstname")." ".$comp_q->f("userobm_lastname");
    if($comp_q->f("comp_stat") > $max) {
      $max = $comp_q->f("comp_stat");
    }
  } 
  $block .= "<table class=\"detail\" >";

  if (is_array($stat)) {
    foreach($stat as $id => $value) {
      $cont = round(($value["contact"]/$max)*90);
      $comp = round(($value["company"]/$max)*90);
      $block .= "<tr>
    <td class=\"detailLabel\" rowspan =\"2\">".$value["name"]."</td>
    <td class=\"detailForm\">
     <table style=\"width:100%;\">
      <tr><td style=\"width:".$cont."%; background-color:#DE5B5B;\"></td><td>".$value["contact"]."</td></tr>
     </table>
    </td>
   </tr>
   <tr>
    <td class=\"detailForm\">
    <table style=\"width:100%;\">
    <tr><td style=\"width:".$comp."%; background-color:#5C8CBC;\"></td><td>".$value["company"]."</td></tr>
    </table>   
    </td>
   </tr>";    
    }
  }
   
  $block .= "
  <tr>
    <td class=\"detailLabel\" class=\"detailLabel\" rowspan =\"2\">$l_key</td>
    <td class=\"detailForm\">
     <table>
      <tr><td style=\"width:20px;background-color:#DE5B5B;\">&nbsp;</td><td>$l_module_contact</td></tr>
     </table>
    </td>
   </tr>
   <tr>
    <td class=\"detailForm\">
    <table>
    <tr><td style=\"width:20px;background-color:#5C8CBC;\">&nbsp;</td><td>$l_module_company</td></tr>
    </table>   
    </td>
   </tr>
  </table>"; 

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display Statistics About the companies 
// Parameters:
//  - $lisy_q : DB Object with all list
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_list_select($list_q) {
  global $l_select_list, $c_all, $l_all,$l_generate_stats;
  global $path,$popup_height,$popup_width; 
  $url = "$path/list/list_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_select_list)."&amp;ext_action=company&amp;ext_url=".urlencode($path."/statistic/statistic_index.php?action=company_statistic&amp;sel_list=")."&amp;ext_target=$l_statistc";
  $block .= "
  <form action=\"statistic_index.php\">
  <div class=\"detailButton\">
  <p class=\"detailButtons\"> 
  <input type=\"button\" 
  onclick=\"window.open('$url','$l_statistc','height=$popup_height,width=$popup_width,scrollbars=yes'); return false; \" value=\"$l_select_list\" />
  <input type=\"hidden\" name=\"action\" value=\"company_statistic\" />
  <input type=\"hidden\" name=\"sel_list\" value=\"$c_all\" />
  <input type=\"submit\" value=\"$l_generate_stats\" />
  </p>
  </div>
  </form>
  ";
  return $block; 
}

///////////////////////////////////////////////////////////////////////////////
// Display Statistics About the companies 
// Parameters:
// - comp_q : DB Object with all companies datas
// - tot : Total of companies... unused for now
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_cat_stats($comp_q,$tot) {
  global $path, $set_theme;
  global $l_null,$l_totals, $l_header_country,$l_compcat;

  while ($comp_q->next_record()) {
    if ($comp_q->f("companycategory_id") == NULL) {
      $cat_id = 0;
      $cat_c = $l_null;
    } else {
      $code = $comp_q->f("companycategory_code");
      $cat_id = $comp_q->f("companycategory_id");
      $cat_c = $code; 
      $cat_d = $comp_q->f("companycategory_label");
      $cat_major_code = substr($code,0,strpos($code,'.'));
      // If code is a major code (not .)
      if ($cat_major_code == "") {
	$cat_major_code = $code;
      }
    }
    if ($comp_q->f("country_iso3166") == NULL || $comp_q->f("country_iso3166") == "") {
      $cty_iso = $l_null;
    } else {
      $cty_iso = $comp_q->f("country_iso3166") ;
      $cty_d = $comp_q->f("country_name") ;
    }
    $nb = $comp_q->f("nb");
    $stat[$cty_iso][$cat_id] += $nb;
    $cty_count[$cty_iso] += $nb;
    
    if ((!$cat_count[$cat_id]) && ($cat_id != 0)) {
      $cat_major_col[$cat_major_code] += 1;
    }
    $cat_count[$cat_id] += $nb;
    if ($cat_id != 0) {
      $cat_major_count[$cat_major_code] += $nb;
    }
    $cat[$cat_id] = $cat_c;
    $cty_desc[$cty_iso] = $cty_d;
    $cat_desc[$cat_id] = $cat_d;
  }

  $cat_link_total = array_sum($cat_count);

  natsort($cat);
  $block .= "
  <div class=\"classic\">
  <table class=\"classic\">
  <tr>
  <th></th>";

  foreach($cat as $cat_id =>$cat_name) {
    if ($cat_id != 0) {
      $onOver = "    onMouseOver=\"show(event,'cat_$cat_id'); return true;\"
    onMouseOut=\"hide('cat_$cat_id'); return true;\"";
      $infos .= "<span class=\"hiddenLegend\" id=\"cat_$cat_id\">".$cat_desc[$cat_id]."</span>";
    } else {
      $onOver = "";
    }
    $cat_label .= "<th $onOver>$cat_name</th>";
    $percent = round(($cat_count[$cat_id])*100/$cat_link_total,1);    
    $cat_percent .= "<td class=\"totals\">$percent</td>";
    $cat_tots .= "<td class=\"totals\">".$cat_count[$cat_id]."</td>";
    $infos .= "<span class=\"hiddenLegend\" id=\"cat_$cat_id\">".$cat_desc[$cat_id]."</span>";
  }

  $pair = 0;
  $block .= "$cat_label<th>$l_totals</th><th>%</th></tr>";
  foreach ($stat as $cty_iso => $data) {
    if($cty_iso != "") {
      $onOver = "    onMouseOver=\"show(event,'cty_$cty_iso'); return true;\"
    onMouseOut=\"hide('cty_$cty_iso'); return true;\"";
      $infos .= "<span class=\"hiddenLegend\" id=\"cty_$cty_iso\">".$cty_desc[$cty_iso]."</span>";
    } else {
      $onOver = "";
    }
    if ($pair == 0) {
      $pair = 1;
      $block .= "<tr class=\"pair\">";
    } else {
      $pair = 0;
      $block .= "<tr class=\"impair\">";
    }
    $block .= "<th $onOver>$cty_iso</th>";
    foreach($cat as $cat_id => $cat_name) {
      $block .= "<td>".$data[$cat_id]."</td >";
    }
    $percent = round(($cty_count[$cty_iso])*100/$cat_link_total,1);
    $block .= "<td class=\"totals\">".$cty_count[$cty_iso]."</td>";
    $block .= "<td class=\"totals\">$percent</td>";
    $block .= "</tr>";
  }
  if (is_array($cat_major_count)) {
    foreach($cat_major_count as $code => $value) {
      $super_cat .= "<td colspan=\"".$cat_major_col[$code]."\">$value</td>";
      $percent = round(($value)*100/$cat_link_total,1);
      $super_cat_p .= "<td class=\"totals\" colspan=\"".$cat_major_col[$code]."\">$percent</td>";
    }
  }
  $block .= "
  <tr><th>$l_totals</th>$cat_tots<td>$cat_link_total</td><td></td></tr>
  <tr><th>%</th>$cat_percent<td></td><td>100</td></tr>
  <tr><th></th>$cat_label<th>$l_totals</th><th>%</th></tr>
  <tr><th></th>$super_cat<th></th></th><th><th></th></tr>
  <tr><th>%</th>$super_cat_p<th></th></th><th><th></th></tr>
   </table>
   </div>
   <br />
  <div class=\"detail\">
  <div class=\"detailText\"> x = $l_compcat </div>
  <div class=\"detailText\"> y = $l_header_country</div>
  </div>
  $infos
  ";
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display Statistics About the companies 
// Parameters:
// - comp_q : DB Object with all companies datas
// - tot : Total of companies... unused for now
// Returns:
///////////////////////////////////////////////////////////////////////////////
function export_cat_stats($comp_q,$tot) {
  global $path, $set_theme;
  global $l_null,$l_totals, $l_header_country,$l_compcat;
  global $set_csv_sep;

  while ($comp_q->next_record()) {
    if ($comp_q->f("companycategory_id") == NULL) {
      $cat_id = 0;
      $cat_c = $l_null;
    } else {
      $code = $comp_q->f("companycategory_code");
      $cat_id = $comp_q->f("companycategory_id");
      $cat_c = $code; 
      $cat_d = $comp_q->f("companycategory_label");
      $cat_major_code = substr($code,0,strpos($code,'.'));
      // If code is a major code (not .)
      if ($cat_major_code == "") {
	$cat_major_code = $code;
      }
    }
    if ($comp_q->f("country_iso3166") == NULL || $comp_q->f("country_iso3166") == "") {
      $cty_iso = $l_null;
    } else {
      $cty_iso = $comp_q->f("country_iso3166") ;
      $cty_d = $comp_q->f("country_name") ;
    }
    $nb = $comp_q->f("nb");
    $stat[$cty_iso][$cat_id] += $nb;
    $cty_count[$cty_iso] += $nb;
    
    if ((!$cat_count[$cat_id]) && ($cat_id != 0)) {
      $cat_major_col[$cat_major_code] += 1;
    }
    $cat_count[$cat_id] += $nb;
    if ($cat_id != 0) {
      $cat_major_count[$cat_major_code] += $nb;
    }
    $cat[$cat_id] = $cat_c;
    $cty_desc[$cty_iso] = $cty_d;
    $cat_desc[$cat_id] = $cat_d;
  }

  $cat_link_total = array_sum($cat_count);
  natsort($cat); 
  foreach($cat as $cat_id =>$cat_name) {
    if ($cat_id != 0) {
      $cat_label .= "$set_csv_sep\"$cat_name ".$cat_desc[$cat_id]."\""; 
    } else {
      $cat_label .=  "$set_csv_sep\"$cat_name\"";
    }
    $percent = round(($cat_count[$cat_id])*100/$cat_link_total,1);    
    $cat_percent .= "$set_csv_sep\"$percent\"";
    $cat_tots .= "$set_csv_sep\"".$cat_count[$cat_id]."\"";
  }   

  
  $block .= "\"\"$cat_label$set_csv_sep\"$l_totals\"$set_csv_sep\"%\"\n";
  foreach ($stat as $cty_iso => $data) {
    if($cty_iso != $l_null) {
      $block .= "\"$cty_desc[$cty_iso]\"";
    } else {
      $block .= "\"$l_null\"";
    }
    foreach($cat as $cat_id => $cat_name) {
      $block .= "$set_csv_sep\"".$data[$cat_id]."\"";
    }
    $percent = round(($cty_count[$cty_iso])*100/$cat_link_total,1);
    $block .= "$set_csv_sep\"".$cty_count[$cty_iso]."\"";
    $block .= "$set_csv_sep\"$percent\"";
    $block .= "\n";
  }

  $block .= "\"$l_totals\"$cat_tots$set_csv_sep\"$cat_link_total\"$set_csv_sep\"\"\n";
  $block .= "\"%\"$cat_percent$set_csv_sep\"\"$set_csv_sep\"100\"\n";
  header("Content-Type: text/comma-separated-values");  
  header("Content-Disposition: inline; filename=companies_stats.csv");
  header("charset=utf-8");  
  echo $block;
  exit();
}
</script>
