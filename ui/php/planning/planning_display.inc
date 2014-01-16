<script language="php">
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
// OBM - File : planning_display.php                                         //
//     - Desc : Planning Display File                                        //
// 2008-10-14 Guillaume Lardon                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


// Constants statistiques de groupes de taches
// Doivent correspondre a ce qu'il y a en BD
// TODO : traduire ces constantes en anglais
// TODO : rendre cela plus generique
define("S_PROJET",1);
define("S_REGIE",2);
define("S_FORMATION",3);
define("S_INTERNE",4);
define("S_DEPASSEMENT",5);
define("S_CONTRIBUTION",6);
define("S_AVANTVENTE",7);
define("S_CONGE",8);
define("S_FERIE_NONPRESENT",9);
define("S_MALADIE",10);
define("S_INTERCONTRAT",11);
define("S_TOTAL",12);
define("S_R&D_PILOTE",13);
define("S_R&D_DEPASSEMENT",14);

define("S_OUVRE",20);
define("S_CONGE_MALADIE",21);
define("S_DISPO",22);
define("S_DISPO_NINTERCO",23);
define("S_NFACT",24);
define("S_DISPO_NINTERCO_NNFACT",25);
define("S_DISPO_NINTERCO_NNFACT_NRD",26);
define("S_DISPO_NINTERCO_NNFACT_NRD_NDEP",27);

///////////////////////////////////////////////////////////////////////////////
// Display the planning according to the interval given
// Parameters:
//   - $plarams[] : parameters hash
///////////////////////////////////////////////////////////////////////////////
function dis_planning_view($params) {

  if(count($params['group_id'])==0)
  {
    global $display,$l_err_planning_no_group_selected;
    $display['msg'] .= display_warn_msg($l_err_planning_no_group_selected);
    return "";
  }

  $obm_q = run_query_planning_month($params);
  $block = dis_planning_user_month($obm_q, $params);

  return $block;
}

function html_plannedtask_cell($colspan,$rowspan,$bgcolor,$label,$urlparams=false)
{
  global $perm, $cright_write;

  if(!$perm->check_right('planning', $cright_write))
  $urlparams = false;

  $block="<td colspan=\"$colspan\" rowspan=\"$rowspan\" style=\"background-color: $bgcolor; text-align: center; color:".($bgcolor=="#0000ff"?"white":"black")."\">";
  if($urlparams) $block.="<a style=\"color:".($bgcolor=="#0000ff"?"white":"black")."\" href=\"".url_prepare("planning_index.php?$urlparams")."\">";
  $block.=$label;
  if($urlparams) $block.="</a>";
  $block.="</td>";
  return $block;
}

function inc_stats(&$stats,$day,$duration,$rows,$type)
{
  if(!is_array($stats))
  return;

  for($i=0;$i<$duration;$i++)
  {
    $stats[$day+$i][$type] += $rows;
    $stats[$day+$i][S_TOTAL] += $rows;
  }
  $stats["user"][$type] += $duration*$rows;
  $stats["user"][S_TOTAL] += $duration*$rows;
}

function html_pourcent_cell($value,$bgcolor=true)
{ 
  if($bgcolor)
  {
    if($value>=0.7) $bgcolor = "#23ff23"; // = Vert 8 OOo
    else if( $value>=0.5) $bgcolor = "#ff9966"; // = Orange 3 OOo
    else $bgcolor="#ff3333"; // = Rouge 4 OOo
    $style = "background-color: $bgcolor; color: black";
  }
  return "<td".($style?" style=\"$style\"":"").">&nbsp;".sprintf("%.0f",$value*100)."%&nbsp;</td>";
}

function html_line_stat(&$stats,&$array_res,$label,$first_day,$last_day,$formula_type,$formula_params,$store=false,$colors,$reportingmode)
{
  if($colors)
  {
    $bgcolor = $colors[0];
    $fgcolor = $colors[1];
  }
  $block="<tr style=\"text-align: center";
  if($fgcolor)
  $block.="; color: $fgcolor";
  // TODO : la balise <b>, c'est mal
  $block.="\"><td style=\"text-align: left";
  if($bgcolor) $block.=" ; background-color: $bgcolor";
  $block.="\" colspan=\"2\"><b>$label</b></td>";

  $week=0;
  $month=0;

  for($day=$first_day;$day<=$last_day;$day++)
  if($array_res[$day]['dow']!=6 && $array_res[$day]['dow']!=0 )
  { 
    $value = 0;
    if($formula_type == "+")
    {
      foreach($formula_params as $op)
      {
        if($op[0] == "+")
        $value += $stats[$day][$op[1]] / $op[2];
        else if($op[0] == "-")
        $value -= $stats[$day][$op[1]] / $op[2];
      }
      $week += $value;
      $month += $value;
    }
    elseif($formula_type == "%")
    {
      if($stats[$day][$formula_params[1]] != 0)
      $value = sprintf('%.0f',100*$stats[$day][$formula_params[0]] / $stats[$day][$formula_params[1]]) ."%";
      else
      $value = "inf";

      $week_op[0] += $stats[$day][$formula_params[0]];
      $week_op[1] += $stats[$day][$formula_params[1]];
    }
    if(!$reportingmode)
    $block.="<td>$value</td>";

    if($store) $stats[$day][$store] = $value;
  }
  else if($array_res[$day]['dow']==6)
  {
    if($formula_type == "%")
    {
      if($week_op[1] != 0)
      $week = "<b>".sprintf('%.0f',100*$week_op[0] / $week_op[1]) ."%"."</b>";
      else
      $week = "inf";

      $month_op[0] += $week_op[0];
      $month_op[1] += $week_op[1];
      $week_op[0] = 0; 
      $week_op[1] = 0;
    }

    $block.="<td colspan=\"2\"";
    if($bgcolor) $block.=" style=\"background-color: $bgcolor; border: 2px; border-style: solid\"";
    $block.=">$week</td>";
    $week=0;
    $day++;
  }
  else if($array_res[$day]['dow']==0)
  $block.="<td></td>";

  if($formula_type == "%")
  {
    $month_op[0] += $week_op[0];
    $month_op[1] += $week_op[1];
    if($month_op[1] != 0)
    $month = "<b>".sprintf('%.0f',100*$month_op[0] / $month_op[1]) ."%"."</b>";
    else
    $month = "inf";
  } 
  $block.="<td";
  if($bgcolor) $block.=" style=\"background-color: $bgcolor; border: 2px; border-style: solid\"";
  $block.=">$month</td>";
  $block.= "</tr>\n";
  return $block;
} 
/*  $block .= html_line_stat($stats,$array_res,"Nombre de jours ouvr&eacute;s",$first_day,$last_day,array (
     array("+",S_TOTAL),
     array("-",S_FERIE_NONPRESENT) ); */

// TODO : fonction recopiee depuis module calendar
// TODO : remplacer par date('%W',$date) ?
// return the number of the week of the argument.
//------------------------------------------------------------------------
// Argument:
// ---------
//     - $date
///////////////////////////////////////////////////////////////////////////////
function get_calendar_week_num($date) {
  global $ccalendar_weekstart;

  $num = date('w', strtotime($ccalendar_weekstart));
  $delta_thursday = date('w',strtotime("-$num days",strtotime('thursday')))-date('w',strtotime("-$num days",$date));
  $week_num = date('W',strtotime("$delta_thursday days",$date));
  return $week_num;
}

///////////////////////////////////////////////////////////////////////////////
// Display the planning for a month
// Parameters : 
//   - $obm_q  : tasks list to display 
//   - $params[] : params hash
///////////////////////////////////////////////////////////////////////////////
function dis_planning_user_month($obm_q, $params ) {
  global $c_week_first_day;
  global $ico_previous, $ico_next,$l_monthsofyear;
  global $perm, $cright_write;

  $iso_date = $params['date'];
  $ts_date = strtotime($iso_date);
  $month = of_date_get_month($ts_date);
  $year = of_date_get_year($ts_date);

  // first day of the month in second
  $first_day_sec = mktime(0,0,0,$month, 1, $year);
  $last_day_sec = mktime(23,0,0,$month+1, 0, $year);
  $day_in_sec = $first_day_sec;
  $dow = date('w', $day_in_sec);
  $obm_dow = ($dow - $c_week_first_day + 7) % 7;

  // Fill the day array -------------------------------------------------------
  $day = 1;
  while ($day_in_sec < $last_day_sec) {
    //$fday = ($day>9) ? "$day" : "0$day";
    $fday = $day;
    $array_res["$fday"]['length'] = 0;
    $array_res["$fday"]['dow'] = $dow;
    $array_res["$fday"]['obm_dow'] = $obm_dow;
    $day++;

    $day_in_sec += 86400;
    // DIRTY HACK pour corriger le bug du au changement d'heure
    // TODO : corriger ca mieux (voir ce qui a été fait ailleurs dans OBM)
    if($month=="10" && $day_in_sec==mktime(23,0,0,$month, 25, $year))
    $day_in_sec += 3600;
    
    $dow = date('w', $day_in_sec);
    $obm_dow = ($dow - $c_week_first_day + 7) % 7;
  }
  // Initialisation du 1er et du dernier jour du mois
  // on saute les samedi/dimanche qui seraient en debut de mois
  if($array_res[1]['dow']==6)
  $first_day = 3;
  else if($array_res[1]['dow']==0)
  $first_day = 2; 
  else 
  $first_day = 1;

  $last_day = $day-1;

  $day_amount = $day - 1;
  $planning = array();
  $task_days = array();
  $line = array();
  $css = array();
  while ($obm_q->next_record())  {
    // TODO : faire ce remplacement de "0" mieux qu'avec ereg_replace
    $day = ereg_replace("^0","",date('d',$obm_q->f('datebegin')));
    $dayend = ereg_replace("^0","",date('d',$obm_q->f('dateend')));
    $period = $obm_q->f('plannedtask_period');
    $project_id = $obm_q->f('id');
    $u_id = $obm_q->f('plannedtask_user_id');
    $type = $obm_q->f('type');
    $shortname = $obm_q->f('shortname');
    $label = $obm_q->f('label');
    if (mb_strlen($label) > 15) {
      $dis_label = substr($label,0,15) . '..';
    } else {
      $dis_label = $label;
    }
    if(ereg("([0-9]+)-[0-9]",$shortname,$matches)) {
      $shortname = $matches[1];
    }
    else if ($shortname == '') {
      $shortname = $dis_label;
    }
    
    /* si journée complete (0) cas classique + rowspan=2 pour le haut, et sautage pour le bas
       si matin (1) => cas classique mais sans rowspan
       si après-midi (2) => cas en bas mais sans rowspan
    */

    // Matin ou aprem
    if($period=='MORNING' || $period=='AFTERNOON')
    $planning[$u_id][$day][$period]["rowspan"]=1;
    // Journée complete
    else
    {
      $period='MORNING';
      $planning[$u_id][$day]['MORNING']["rowspan"]=2;
      $skip = $dayend-$day;
      $planning[$u_id][$day]['AFTERNOON'] = $skip;  // cellules vides, à sauter (car colspan)
    } 

    $planning[$u_id][$day][$period]["type"] = $type;
    $planning[$u_id][$day][$period]["label"] = "<abbr title=\"$label\">$shortname</abbr>";
    $planning[$u_id][$day][$period]["taskgroup"] = $obm_q->f('tasktype_tasktypegroup_id');

    $planning[$u_id][$day][$period]["end"] = $dayend;
    $planning[$u_id][$day][$period]["id"] = $obm_q->f('plannedtask_id');
    $planning[$u_id][$day][$period]["overrun"] = $obm_q->f('plannedtask_overrun');
    // TODO : ce truc est-il toujours utile ?
    $planning[$u_id]['length'][$type.$project_id] += 1;

  }

  $reportingmode = $params['reportingmode'];

  $d_prev = of_isodate_format(mktime(0,0,0,$month-1,1,$year));
  $d_next = of_isodate_format(mktime(0,0,0,$month+1,1,$year));
  $title = "$user ". $l_monthsofyear[$month-1] . " $year";;

  $url_prev = url_prepare("planning_index.php?date=$d_prev");
  $url_next = url_prepare("planning_index.php?date=$d_next");
  if($reportingmode)
  {
    $url_prev.="&reportingmode=1";
    $url_next.="&reportingmode=1";
  }

  $block = "
  <h1>
  <a href=\"$url_prev\"><img src=\"$ico_previous\" alt=\"[Previous Week]\" /></a>     
  $title
  <a href=\"$url_next\"><img src=\"$ico_next\" alt=\"[Next Week]\" /></a>     
  </h1>";

  $block .= "<table class=\"planning\">\n";

  $d = dis_planning_month_userslines($year,$month,$first_day,$last_day,$params['group_id'],$array_res,$planning,$stats);

  if(! $reportingmode )
  {
    $block .= $d;
    $block.="<tr><td colspan=\"".($last_day+1)."\"><hr></td></tr>\n";
  }
   
  if($perm->check_right('planning', $cright_write))
  $block .= dis_planning_month_stats($year,$month,$first_day,$last_day,$array_res,$stats,$title,$reportingmode, $group_labels);

  $block.="</table>";
  return $block;
}

function dis_planning_month_userslines($year,$month,$first_day,$last_day,$group_ids,&$array_res,&$planning,&$stats)
{
  global $path, $perm, $cright_read_admin;
  global $l_daysofweekfirst;
  global $taskgroups;

  // Chargement des groupes d'utilisateurs
  $groups = get_planning_groups($group_ids);
  // Chargement des groupes de taches
  $taskgroups = get_tasktypegroups_infos();

  for($day=$first_day;$day<=$last_day;$day++)
  {
    $lday = $l_daysofweekfirst[$array_res[$day]['dow']];

    $line0 .= "<th>$lday</th>\n";
    $line1 .= "<td style=\"text-align: center\">$day</td>\n";
  }

  $block .= "
  <colgroup>
  </colgroup>
  <colgroup class=\"main\" span=\"$day_amount\">
  </colgroup> 
  <colgroup class=\"total\" span=\"2\">
  </colgroup>    
  <thead>
  <tr><th colspan=\"2\" rowspan=\"2\"></th>$line0</tr>
  <tr>$line1</tr>
  </thead>";
  if(true)
  //if($groups[$g_id]['stats'])
  {
    $stats = array("user"=>array());
  }
  else unset($stats);


  $i=0; $j=0;

  foreach($groups as $g_id => $u)
  {
    $block.="<tr><td colspan=\"2\"><b>".$groups[$g_id]["label"]."</b></td></td>\n";
    $stats["grouplabels"][] = $groups[$g_id]["label"];
    
    // TODO : ecrire ca mieux
    if($u["users"])
    foreach($u["users"] as $u_id => $user_name)
    {
      if($i%15==14)
      {
        $j++;
        $block.="<thead>
        <tr><th colspan=\"2\" rowspan=\"2\"><a name=\"line$j\"></a></th>$line0</tr>
        <tr>$line1</tr>
        </thead>\n";
      }

      $user_planning = $planning[$u_id];
      foreach(array('MORNING','AFTERNOON') as $period)
      {
        if($period=='MORNING')
        {
          $block.= "<tr><td rowspan=\"2\" style=\"color:#aaaaaa\">".($i+1)."</td>";
          $block .= "<td rowspan=\"2\">$user_name</td>";
        }
        else
        $block .= "<tr>";
        for($day=$first_day;$day<=$last_day;$day++)
        {
          // C'est un week-end (samedi+dimanche)
          if($array_res[$day]['dow']==6 )
          {
            if($period=='MORNING')
            {
              $s = 2;
              $r = 2;
              $block.= html_plannedtask_cell($s,$r,"#cccccc",' ');
              $type = S_FERIE_NONPRESENT;
              inc_stats($stats,$day,$s,$r,$type);
            }
            $day++;
          }
          else if(is_array($user_planning[$day][$period]))
          {
            $s = $user_planning[$day][$period]["end"]-$day+1;
            $v = $user_planning[$day][$period]["label"];
//if($period==2) die("OK: $s; $v");
    
            $type = $user_planning[$day][$period]["taskgroup"];

            if($user_planning[$day][$period]["type"] == "project")
            {
              if($user_planning[$day][$period]["overrun"])
              $type = S_DEPASSEMENT;  
            }
           
            $c = $taskgroups[$type]['bgcolor'];
            $r = $user_planning[$day][$period]["rowspan"];
            $block.= html_plannedtask_cell($s,$r,$c,$v,"action=detailupdate&amp;plannedtask_id=".$user_planning[$day][$period]["id"].($j>0?"&returnline=$j":""));
            inc_stats($stats,$day,$s,$r,$type);
            $day+=$s-1;
          }
	  else if(is_integer($user_planning[$day][$period]))
          {
            $skip = $user_planning[$day][$period];
            $day+=$skip;
            // TODO : pas de stats à compter dans ce cas là
          }
          else
          {
            $mdatebegin = "$year-$month-".sprintf("%02d",$day);
            $s = 1;
            $r = 1; //$user_planning[$day][$period]["rowspan"];
            $block.= html_plannedtask_cell($s,$r,"white","_","action=new&amp;datebegin=$mdatebegin&amp;user=$u_id&period=ALLDAY".($j>0?"&line=$j":""));
            $type=S_INTERCONTRAT;
            inc_stats($stats,$day,$s,$r,$type);
          }
 //         else $block.="<td>".$day."|".$period."</td>";
     
        }
        
        $block.="</tr>\n";
        $stats['user'] = array();
      }  
      $i++;
    }
  }
  return $block;
}


// -------- AFFICHAGE DES STATISTIQUES ---------------*/
function dis_planning_month_stats($year,$month,$first_day,$last_day,&$array_res,&$stats,$title,$reportingmode)
{
  global $l_daysofweekfirst, $l_activities_of;
  global $taskgroups;

  $line0 = "";
  $line1 = "";
  for($day=$first_day;$day<=$last_day;$day++)
  {
    $dow = $array_res[$day]['dow'];
    if(!$week || $dow == 1)
    // TODO : pk ca marche pas cette fonction ?
    $week = get_calendar_week_num(strtotime("$year-$month-".sprintf("%02d",$day) ));

    if($dow == 6)
    {
      $line0 .= "<th colspan=\"2\" rowspan=\"2\" style=\"background-color: lightgray\">S$week</th>";
      $day++;
    }
    else if($dow != 0 && !$reportingmode)
    {
 /*   if($dow == 1)
    $line0 .= "<th colspan=\"$nwd\" style=\"text-align: center; background-color: lightgray\">S$week</th>\n";
*/
      $lday = $l_daysofweekfirst[$array_res[$day]['dow']];
      $line0 .= "<th>$lday</th>\n";
      $line1 .= "<td>$day/$month</td>\n";
    }
  }
  $block.="<tr>
    <th rowspan=\"2\" colspan=\"2\" style=\"background-color: lightgray\">";
  if($reportingmode)
  $block.= $l_activities_of." ".implode(" & ",$stats["grouplabels"]);
  $block.="  </th>
    $line0
    <th rowspan=\"2\" style=\"background-color: lightgray\">$title</th>
  </tr>  
  <tr>$line1</tr>\n";
//$label = "<abbr title=\"blablabla\">XXX</abbr>";

  $formules = array(
    array(""),
    array("Nombre de jours ouvr&eacute;s", S_OUVRE, "+", array(
        array("+",S_TOTAL,2),
        array("-",S_FERIE_NONPRESENT,2) ) ), 
    array(""),
    array("Cong&eacute;s / Maladie", S_CONGE_MALADIE, "+", array(
        array("+",S_CONGE,2),
        array("+",S_MALADIE,2) ) , S_CONGE ),
    array("Total jours dispos", S_DISPO, "+", array(
        array("+",S_OUVRE,1), 
        array("-",S_CONGE_MALADIE,1) ) ),
    array("TDCE en %", NULL, "%", array(S_DISPO,S_OUVRE) ),
    array(""),
    array("Intercontrat",NULL, "+", array(
        array("+",S_INTERCONTRAT,2) ) ),
    array("<abbr title=\"J*H Dispo - Intercontrat\">JPCE</abbr>",S_DISPO_NINTERCO, "+", array(
        array("+",S_DISPO,1), 
        array("-",S_INTERCONTRAT,2) ) ),
    array("TPCE en %", NULL, "%", array(S_DISPO_NINTERCO,S_DISPO) ),
    array(""),
    array("Activit&eacute;s non facturables",S_NFACT, "+", array(
        array("+",S_INTERNE,2),
        array("+",S_AVANTVENTE,2) ) , S_INTERNE ),
    array("<abbr title=\"J*H Dispo -Interco -Non fact.\">JCCE</abbr>",S_DISPO_NINTERCO_NNFACT, "+", array(
        array("+",S_DISPO_NINTERCO,1), 
        array("-",S_NFACT,1) ) ),
    array("TCCE en %", NULL, "%", array(S_DISPO_NINTERCO_NNFACT,S_DISPO) ),
    array(""),
    array("Activit&eacute; de R&D",NULL, "+", array(
        array("+",S_CONTRIBUTION,2) ) , S_CONTRIBUTION ),
    array("<abbr title=\"Dispo -Interco -Non fact. -R&D\">JOCE</abbr>",S_DISPO_NINTERCO_NNFACT_NRD, "+", array(
        array("+",S_DISPO_NINTERCO_NNFACT,1), 
        array("-",S_CONTRIBUTION,2) ) ),
    array("TOCE en %", NULL, "%", array(S_DISPO_NINTERCO_NNFACT_NRD,S_DISPO) ),
    array(""),
    array("D&eacute;passement",NULL, "+", array(
        array("+",S_DEPASSEMENT,2) ) , S_DEPASSEMENT ),
    array("<abbr title=\"Dispo -Interco -Non fact. -R&D -Dep.\">JACE</abbr>",S_DISPO_NINTERCO_NNFACT_NRD_NDEP, "+", array(
        array("+",S_DISPO_NINTERCO_NNFACT_NRD,1), 
        array("-",S_DEPASSEMENT,2) ) ),
    array("TACE en %", NULL, "%", array(S_DISPO_NINTERCO_NNFACT_NRD_NDEP,S_DISPO) ),
    array(""),
    array("Jours projets",NULL, "+", array(
        array("+",S_PROJET,2) ), S_PROJET ),
    array("Jours regies",NULL, "+", array(
        array("+",S_REGIE,2) ) , S_REGIE ),
    array("Jours formations",NULL, "+", array(
        array("+",S_FORMATION,2) ), S_FORMATION )
    );
    
    foreach($formules as $f)
    {
      if($f[0] != "")
      {
        if(isset($f[4]) )
        $colors = array($taskgroups[$f[4]]['bgcolor'],$taskgroups[$f[4]]['fgcolor']);
	else $colors=NULL;

        $block .= html_line_stat($stats,$array_res,$f[0],$first_day,$last_day,$f[2], $f[3], $f[1], $colors , $reportingmode);
      }
      else
      $block.="<tr><td>&nbsp;</td></tr>";
    }
// TODO : faire une fonction(planning_query?) qui va calculer ces stats en bouclant sur les jours :
// puis affichage

  return $block;
}


/**
 * Display PlanningManager Group search Form
 * @param $planning[] default form values
 * @param $grp_q  DBO : Group list
 * @param $multi if false, mono select else select multi
 **/
function dis_group_select($planning, $grp_q, $multi=false) {
  global $l_see, $l_group, $l_all, $c_all, $c_none;

  $sel_group_ids = $planning['group_id'];

  if ($multi) {
    $size = 12;
    $type = "multiple=\"multiple\"";
    $alt = "[]";
  } else {
    $size = 6;
  }
  
  // Group Select
  $sel_group = "<select id=\"sel_group_id\" name=\"sel_group_id$alt\" $type>";
  $selected = false;
  while ($grp_q->next_record()) {
    $g_id = $grp_q->f('group_id');
    $name = $grp_q->f('group_name');
    $sel_group .= "\n<option value=\"$g_id\" ";
    
    if (in_array($g_id, $sel_group_ids)) {
      $sel_group .= "selected=\"selected\"";
    }
    
    $sel_group .= " >$name</option>";
  }
  $sel_group .= '</select>';
  
  $url = url_prepare('planning_index.php');
  
  $block = "
    <div class=\"portlet\">
    <h1>$l_group</h1>
    <form method=\"post\" name=\"f_time\" action=\"$url\">
    <fieldset class=\"detail extra\">
      $sel_group
    </fieldset>
    <fieldset class=\"buttons\">
      <input type=\"hidden\" name=\"date\" value=\"".$planning['date']."\" />";
    if($planning['reportingmode'])
    $block.="<input type=\"hidden\" name=\"reportingmode\" value=\"1\">";

    $block.="
      <input type=\"submit\" name=\"submit\" value=\"$l_see\" />
    </fieldset>
    </form>
    </div>
";

  return $block;
}

/**
 * XHTML Display PlannedTask Detail
 *
 * @param mixed $obm_q : plannedtask database data
 * @return XHTML Display block
 */
function html_plannedtask_consult($plannedtask_q) {
  global $cgp_use, $l_domain, $l_insert, $l_update, $l_delete, $l_back,$l_web_list;
  global $l_module_planning, $l_begin, $l_end, $l_project;
  global $l_tasktype, $l_overrun, $l_comment, $l_user;
  global $period, $l_plannedtask_periods;
  global $l_activity, $l_header_man_affect;
  global $display, $obm;
  
  $name = $plannedtask_q->f('project_name') . $plannedtask_q->f('tasktype_label') . $plannedtask_q->f('plannedtask_label');
  $project = $plannedtask_q->f('project_name');
  $task = $plannedtask_q->f('plannedtask_tasktype_id');
  $overrun = $plannedtask_q->f('plannedtask_overrun');
  $comment = $plannedtask_q->f('plannedtask_comment');
  $datebegin = of_date_upd_format($plannedtask_q->f('datebegin'),true);
  $dateend = of_date_upd_format($plannedtask_q->f('dateend'),true);
  $period = $plannedtask_q->f('plannedtask_period');
  $user = $plannedtask_q->f('plannedtask_user_id');
  $dis_title = $name;

  $display['detailInfo'] = display_record_info($plannedtask_q);
  $display['title'] = $dis_title;

/*  $sel_user = "<select name=\"sel_user\">
     <option value=\"$c_undef\">$l_undef</option>";
  foreach($plannedtask['group_id'] as $g_id)
  {
    $users_p = of_usergroup_get_group_users($g_id, true, array($user),'userobm_lastname,userobm_firstname');
    $sel_user.= "<option>--------------------------------------------------</option>\n";
    foreach ($users_p as $cid => $one_user) {
      $cname = $one_user['lastname'] . ' ' . $one_user['firstname'];
      $sel_user .= "<option value=\"$cid\"";
      if ($user == $cid) $sel_user .= " selected = \"selected\""; 
      $sel_user .= ">$cname</option>\n";
    }
  }
  $sel_user .= '</select>';
*/
  // TODO : la date n'est pas necssaire. Verifier si c'est coherent (a priori oui)
  //$obm_q = run_query_plannedtask_projects($user,of_isodate_convert($datebegin));
/*
  $obm_q = run_query_plannedtask_projects($user,$project);
  $sel_project = "<select name=\"sel_project\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($obm_q->next_record())  {
    $p_id = $obm_q->f('project_id');
    $sel_project .= "<option value=\"$p_id\"";
    if ($project == $p_id) $sel_project .= " selected = \"selected\""; 
    $sel_project .= ">".$obm_q->f('project_name').' - '.$obm_q->f('company_name')."</option>\n";
  }
  $sel_project .= '</select>';
*/
  $obm_q = run_query_plannedtask_tasks("tasktype_internal!=0");
  $sel_task = "<select name=\"sel_task\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($obm_q->next_record())  {
    $t_id = $obm_q->f('tasktype_id');
    $sel_task .= "<option value=\"$t_id\"";
    if ($task == $t_id) $sel_task .= " selected = \"selected\""; 
    $sel_task .= ">".$obm_q->f('tasktype_label')."</option>\n";
  }
  $sel_task .= '</select>';

  $sel_period = "<select name=\"sel_period\">";
  foreach($l_plannedtask_periods as $p => $l) {
    $sel_period .= "<option value=\"$p\"";
    if ($period == $p) $sel_period .= " selected = \"selected\""; 
    $sel_period .= ">$l</option>\n";
  }
  $sel_period .= '</select>';

  $returnline = $plannedtask['returnline'];
  if($returnline)
  $returnline="#line$returnline";

  $block = "

  <fieldset class=\"detail infos\">
  <legend>$l_activity</legend>

  <table>
  <tr>
    <th>$l_project</th>
    <td>
    $project
    </td
  </tr>
  <tr>
    <th>$l_tasktype</th>
    <td>
    $sel_task
    </td
  </tr>
  <tr>
    <th>$l_overrun</th>
    <td>
      <input name=\"cb_overrun\" type=\"checkbox\" value=\"1\" $overrun_state>
    </td>
  </tr>
  <tr>
    <th>$l_comment</th>
    <td>
      <input name=\"tf_comment\" value=\"$comment\" size=\"32\" maxlength=\"128\" />
    </td
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_header_man_affect</legend>
  <table>
  <tr>
    <th>$l_user</th>
    <td>$sel_user</td>
  </tr>
  <tr>
    <th>$l_begin</th>
    <td><input type=\"text\" name=\"tf_datebegin\" value=\"".of_date_upd_format($datebegin,true)."\" class=\"datePicker\"/> </td>
  </tr>
  <tr>
    <th>$l_end</th>
    <td><input type=\"text\" name=\"tf_dateend\" value=\"".of_date_upd_format($dateend,true)."\" class=\"datePicker\"/> </td>
  </tr>
  <tr>
    <th>$l_period</th>
    <td>$sel_period</td>
  </tr>
  </table>
  </fieldset>";

  $block .= "<fieldset class=\"buttons\">";

   if (($action == 'detailupdate') || ($action == 'update')) {
    $block .= "
      <input type=\"hidden\" name=\"id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

  } else {
    $block .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }
  
  $dateback="<input type=\"hidden\" name=\"date\" value=\"".of_isodate_convert($datebegin)."\" />";

  $block .="</form>
  <form name=\"form_back\" action=\"" .url_prepare("planning_index.php$returnline") . "\">
    $dateback
    <input type=\"submit\" value=\"$l_back\" />
  </form>";
     
  if (($action == 'detailupdate') || ($action == 'update')) 
  $block .= "<form name=\"f_plannedtask_delete\" action=\"".url_prepare("planning_index.php$returnline")."\">
    <input type=\"hidden\" name=\"plannedtask_id\" value=\"$id\" />
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    $dateback
    <input type=\"submit\" value=\"$l_delete\" />
  </form>";

  $block .="</fieldset>";
  
  return $block;
}

/**
 * get_plannedtask_string
 * plannedtask text representation (used in error messages)
 *
 * @param integer $plannedtask_id plannedtask to convert
 * @return string 
 */
function get_plannedtask_string($plannedtask_id)
{
  $obm_q = run_query_plannedtask_detail($plannedtask_id);

  if($obm_q->f('plannedtask_project_id') )
  $str = "projet ".$obm_q->f('project_name');
  else if($obm_q->f('plannedtask_tasktype_id') )
  $str = "la tache ".$obm_q->f('tasktype_label');
  
  $str.=" du ".of_date_upd_format($obm_q->f('plannedtask_datebegin')) 
    ." au ".of_date_upd_format( $obm_q->f('plannedtask_dateend') );
  return $str;

}

///////////////////////////////////////////////////////////////////////////////
// Display PlannedTask Form
// Parameters :
//   - $action   : action called
//   - $plannedtask_q : DBO : information about the planned task (null for new)
//   - $plannedtask[]   : default or transmitted values
//     keys used : name, desc, usercreate
//   - $field    : field in error
//   - $conflit_id : (optional) id of the plannedtask causing a conflict
//                   in such case
///////////////////////////////////////////////////////////////////////////////
function html_plannedtask_form($action, $plannedtask_q, $plannedtask, $field='', $conflict_id=null) {
  global $cgp_use, $l_domain, $l_insert, $l_update, $l_delete, $l_back,$l_web_list;
  global $l_module_planning, $l_begin, $l_end, $l_project, $l_tt, $l_overrun, $l_comment, $l_user;
  global $l_period, $l_plannedtask_periods;
  global $l_activity, $l_header_man_affect, $l_err_plannedtask_event_in_conflit;
  global $display, $obm;
  
  // if update mode and first time, values are taken from db
  if ($action == 'detailupdate') {
    $id = $plannedtask_q->f('plannedtask_id');
    
    $name = $plannedtask_q->f('project_name') . $plannedtask_q->f('tasktype_label') . $plannedtask_q->f('plannedtask_label');
    $project = $plannedtask_q->f('plannedtask_project_id');
    $task = $plannedtask_q->f('plannedtask_tasktype_id');
    $overrun = $plannedtask_q->f('plannedtask_overrun');
    $comment = $plannedtask_q->f('plannedtask_comment');
    $datebegin = of_date_upd_format($plannedtask_q->f('datebegin'),true);
    $dateend = of_date_upd_format($plannedtask_q->f('dateend'),true);
    $period = $plannedtask_q->f('plannedtask_period');
    $user = $plannedtask_q->f('plannedtask_user_id');
    $dis_title = $name;
  }
  
  // If parameters have been given, they supercede the default action value
  if (isset($plannedtask['id'])) { $id = $plannedtask['id']; }
  if (isset($plannedtask['datebegin'])) { $datebegin = of_date_upd_format($plannedtask['datebegin'],true); }
  if (isset($plannedtask['dateend'])) { $dateend = of_date_upd_format($plannedtask['dateend'],true); }
  if (isset($plannedtask['user'])) { $user = $plannedtask['user']; }
  if (isset($plannedtask['period'])) { $period = $plannedtask['period']; }
  if (isset($plannedtask['project'])) { $project = $plannedtask['project']; }
  if (isset($plannedtask['task'])) { $task = $plannedtask['task']; }
  if (isset($plannedtask['overrun'])) { $overrun = $plannedtask['overrun']; }
  if (isset($plannedtask['comment'])) { $comment = $plannedtask['comment']; }

  if(!$dateend) $dateend = $datebegin;
  if(!$period) $period='ALLDAY';
  // overrun checkbox checked value
  $overrun_state  = ($overrun ? 'checked' : '');

  // Mark the error field
  if ($field != '') {
    $class[$field] = 'error';
  }

  $display['detailInfo'] = display_record_info($plannedtask_q);
  $display['title'] = $dis_title;

  $sel_user = "<select name=\"sel_user\">
     <option value=\"$c_undef\">$l_undef</option>";
  foreach($plannedtask['group_id'] as $g_id)
  {
    $users_p = of_usergroup_get_group_users($g_id, true, array($user),'userobm_lastname,userobm_firstname');
    $sel_user.= "<option>--------------------------------------------------</option>\n";
    foreach ($users_p as $cid => $one_user) {
      $cname = $one_user['lastname'] . ' ' . $one_user['firstname'];
      $sel_user .= "<option value=\"$cid\"";
      if ($user == $cid) $sel_user .= " selected = \"selected\""; 
      $sel_user .= ">$cname</option>\n";
    }
  }
  $sel_user .= '</select>';

  // TODO : la date n'est pas necssaire. Verifier si c'est coherent (a priori oui)
  //$obm_q = run_query_plannedtask_projects($user,of_isodate_convert($datebegin));
  $obm_q = run_query_plannedtask_projects($user,$project);
  $sel_project = "<select name=\"sel_project\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($obm_q->next_record())  {
    $p_id = $obm_q->f('project_id');
    $sel_project .= "<option value=\"$p_id\"";
    if ($project == $p_id) $sel_project .= " selected = \"selected\""; 
    $sel_project .= ">".$obm_q->f('project_name').' - '.$obm_q->f('company_name')."</option>\n";
  }
  $sel_project .= '</select>';

  $obm_q = run_query_plannedtask_tasks("tasktype_internal!=0");
  $sel_task = "<select name=\"sel_task\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($obm_q->next_record())  {
    $t_id = $obm_q->f('tasktype_id');
    $sel_task .= "<option value=\"$t_id\"";
    if ($task == $t_id) $sel_task .= " selected = \"selected\""; 
    $sel_task .= ">".$obm_q->f('tasktype_label')."</option>\n";
  }
  $sel_task .= '</select>';

  $sel_period = "<select name=\"sel_period\">";
  foreach($l_plannedtask_periods as $p => $l) {
    $sel_period .= "<option value=\"$p\"";
    if ($period == $p) $sel_period .= " selected = \"selected\""; 
    $sel_period .= ">$l</option>\n";
  }
  $sel_period .= '</select>';

  $returnline = $plannedtask['returnline'];
  if($returnline)
  $returnline="#line$returnline";

  $block = "";
  if($conflict_id)
  // In case of a conflict, show a link to the event
  {
    $block.="<p>$l_err_plannedtask_event_in_conflit : <a href=\"".url_prepare("planning_index.php?action=detailupdate&plannedtask_id=$conflict_id")."\">".get_plannedtask_string($conflict_id)."</a></p>";
  }
  $block.="<form method=\"get\" name=\"f_plannedtask\" action=\"planning_index.php$returnline\">

  <fieldset class=\"detail infos\">
  <legend>$l_activity</legend>

  <table>
  <tr>
    <th>$l_project</th>
    <td>
    $sel_project
    </td
  </tr>
  <tr>
    <th>$l_tt</th>
    <td>
    $sel_task
    </td
  </tr>
  <tr>
    <th>$l_overrun</th>
    <td>
      <input name=\"cb_overrun\" type=\"checkbox\" value=\"1\" $overrun_state>
    </td>
  </tr>
  <tr>
    <th>$l_comment</th>
    <td>
      <input name=\"tf_comment\" value=\"$comment\" size=\"32\" maxlength=\"128\" />
    </td
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_header_man_affect</legend>
  <table>
  <tr>
    <th>$l_user</th>
    <td>$sel_user</td>
  </tr>
  <tr>
    <th>$l_begin</th>
    <td><input type=\"text\" name=\"tf_datebegin\" value=\"".of_date_upd_format($datebegin,true)."\" class=\"datePicker\"/> </td>
  </tr>
  <tr>
    <th>$l_end</th>
    <td><input type=\"text\" name=\"tf_dateend\" value=\"".of_date_upd_format($dateend,true)."\" class=\"datePicker\"/> </td>
  </tr>
  <tr>
    <th>$l_period</th>
    <td>$sel_period</td>
  </tr>
  </table>
  </fieldset>";

  $block .= "<fieldset class=\"buttons\">";

   if (($action == 'detailupdate') || ($action == 'update')) {
    $block .= "
      <input type=\"hidden\" name=\"id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

  } else {
    $block .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }
  
  $dateback="<input type=\"hidden\" name=\"date\" value=\"".of_isodate_convert($datebegin)."\" />";

  $block .="</form>
  <form name=\"form_back\" action=\"" .url_prepare("planning_index.php$returnline") . "\">
    $dateback
    <input type=\"submit\" value=\"$l_back\" />
  </form>";
     
  if (($action == 'detailupdate') || ($action == 'update')) 
  $block .= "<form name=\"f_plannedtask_delete\" action=\"".url_prepare("planning_index.php$returnline")."\">
    <input type=\"hidden\" name=\"plannedtask_id\" value=\"$id\" />
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    $dateback
    <input type=\"submit\" value=\"$l_delete\" />
  </form>";

  $block .="</fieldset>";
  
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display Planning Administration Index
///////////////////////////////////////////////////////////////////////////////
function dis_planning_admin_index() {
  $ttg_q = run_query_tasktypegroup_list();
  $tasktypegroups =  get_tasktypegroups_infos();

  $block = html_planning_admin_form($ttg_q,$tasktypegroups);
  return $block;
}

function html_tasktypegroup_select($tasktype_id,$tasktypegroup_sel,&$tasktypegroups)
{
  $bgcolor = $tasktypegroups[$tasktypegroup_sel]['bgcolor'];
  $fgcolor = $tasktypegroups[$tasktypegroup_sel]['fgcolor'];
  $block = "<select style=\"color: $fgcolor; background-color: $bgcolor\" id=\"rd_tasktype_tasktypegroup_id_$tasktype_id\">";
  foreach($tasktypegroups as $ttg_id => $ttg_infos)
  {
    $block.= "<option value=\"".$ttg_id."\"";
    if($ttg_id==$tasktypegroup_sel)
    $block.= " selected";

    $block.="/>".$ttg_infos['label']."</option>";
  }
  $block .="</select>";
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// XHTML Planning Administration Forms
// Parametres:
//   - $ttg_q : DBO : TaskType Group list
///////////////////////////////////////////////////////////////////////////////
function html_planning_admin_form($ttg_q,$tasktypegroups) {
  global $l_label, $l_code, $l_bgcolor, $l_fgcolor ;
  global $l_tasktypegroup_insert, $l_tasktypegroup_update;
  global $l_tasktypegroup_manage,$l_status_exist,$l_status_new,$l_c_new;
  global $l_type_label,$l_order,$ico_delete,$ico_ok;
  global $l_tasktypegroup_checkdelete, $l_tasktypegroup_update, $l_tasktypetype_insert;
  global $l_header_tasktype, $l_tasktypegroup;
  global $l_tasktype_affect,$l_group;

  $char_title = '=';
  $key = 0;
  $ttg_list="";
  while ($ttg_q->next_record()) {

   $id = $ttg_q->f('tasktypegroup_id');
   $label = $ttg_q->f('tasktypegroup_label');
   $code = $ttg_q->f('tasktypegroup_code');
   $bgcolor = $ttg_q->f('tasktypegroup_bgcolor');
   $fgcolor = $ttg_q->f('tasktypegroup_fgcolor');

    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $ttg_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"tasktypegroup_$id\" value=\"$id\" />
    <a href=\"?action=tasktypegroup_checklink&amp;tasktypegroup=$id\">
    <img src=\"$ico_delete\" alt=\"$l_tasktypegroup_checkdelete\" title=\"$l_tasktypegroup_checkdelete\" />
    </a></td>
    <td><input type=\"text\" id=\"tf_tasktypegroup_label_$id\" value=\"$label\" /></td>
    <td><input type=\"text\" id=\"tf_tasktypegroup_code_$id\" value=\"$code\" size=\"8\"/></td>
    <td><input type=\"text\" id=\"tf_tasktypegroup_bgcolor_$id\" value=\"$bgcolor\" size=\"6\"/></td>
    <td><input type=\"text\" id=\"tf_tasktypegroup_fgcolor_$id\" value=\"$fgcolor\" size=\"6\"/></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'tasktypegroup');\">
    <img src=\"$ico_ok\" alt=\"$l_tasktypegroup_update\" title=\"$l_tasktypegroup_update\" />
    </a>
    </td>
    </tr>";
  }


  $tt_q = run_query_plannedtask_tasks();
  $tt_list = "";
  $char_title = '=';
  $key = 0;
  while ($tt_q->next_record()) {
    $key++;
    $id = $tt_q->f('tasktype_id');
    $label = $tt_q->f('tasktype_label');
    $ttg = $tt_q->f('tasktype_tasktypegroup_id');
   
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $tt_list .= "
    <tr $class>
    <input type=\"hidden\" id=\"tasktype_$id\" value=\"$id\" />
    <td>$label</td>
    <td> ".html_tasktypegroup_select($id,$ttg,$tasktypegroups)." </td>
    <td>
      <a href=\"#\" onclick=\"return updateLine($id,'tasktype');\">
      <img src=\"$ico_ok\" alt=\"$l_type_update\" title=\"$l_type_update\" />
      </a>
    </td>
    </tr>";
  }

  $block = "
  <div class=\"detail admin\" >
  <form id=\"form_tasktypegroup_update\" action=\"?action=tasktypegroup_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_tasktypegroup_label\" value=\"\" />
  <input type=\"hidden\" name=\"tf_tasktypegroup_code\" value=\"\" />
  <input type=\"hidden\" name=\"tf_tasktypegroup_bgcolor\" value=\"\" />
  <input type=\"hidden\" name=\"tf_tasktypegroup_fgcolor\" value=\"\" />
  <input type=\"hidden\" name=\"tasktypegroup\" value=\"\" />
  </form>    
  <h1>$l_tasktypegroup_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
    <td>&nbsp;</td>
    <td>$l_label</td>
    <td>$l_code</td>
    <td>$l_bgcolor</td>
    <td>$l_fgcolor</td>
    <td>&nbsp;</td>  
  </tr>    
  </thead>
  <tbody>
  $ttg_list
  </tbody>
  </table>
  </div>

  <form id=\"form_tasktypegroup_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_category(this.tf_tasktypegroup_label,'$jslabel')\">
  <table>
  <thead>
  <tr>
  <td colspan=\"4\">$l_c_new</td>
  </tr>
  <tbody>
  <tr>
  <td>$l_code</td>
  <td>$l_label</td>
  <td>$l_bgcolor</td>
  <td>$l_fgcolor</td>
  </tr>
  <tr>
  <td>
  <input type=\"text\" name=\"tf_label\" value=\"\" />
  </td>
  <td>
  <input type=\"text\" name=\"tf_code\" size=\"10\" maxlength=\"10\" value=\"\" />
  </td>
  <td>
  <input type=\"text\" name=\"tf_bgcolor\" size=\"7\" maxlength=\"7\" value=\"\" />
  </td>
  <td>
  <input type=\"text\" name=\"tf_fgcolor\" size=\"7\" maxlength=\"7\" value=\"\" />
  </td>
  </tr>
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"4\"><input type=\"submit\" value=\"$l_tasktypegroup_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"tasktypegroup_insert\" />
  </form>
  </div>

  <div class=\"detail admin\" >
  <form id=\"form_tasktype_update\" action=\"?action=tasktype_update\" method=\"post\">
  <input type=\"hidden\" name=\"rd_tasktype_tasktypegroup_id\" value=\"\" />
  <input type=\"hidden\" name=\"tasktype\" value=\"\" />
  </form>    
  <h1>$l_tasktype_affect</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>$l_header_tasktype</td>
  <td>$l_tasktypegroup</td>
  <td>&nbsp;</td>  
  </tr>    
  </thead>
  <tbody>
  $tt_list
  </tbody>
  </table>
  </div>";
/*
  <form id=\"form_status_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_category(this.tf_status_label,'$jslabel')\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  <tbody>   
  <tr>
  <td>
  $l_order
  </td>
  <td>
  $l_status_new
  </td>
  <td>
  $l_hitrate
  </td> 
  </tr>
  <tr>
  <td>
  <input type=\"text\" name=\"tf_status_order\" value=\"\" />
  </td>
  <td>
  <input type=\"text\" name=\"tf_status_label\" value=\"\" />
  </td>
  <td>
  <input type=\"text\" name=\"tf_status_hitrate\" value=\"\" />
  </td>
  </tr>
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_status_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"status_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"status\" />
  </form>
  </div>";
*/
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the tasktypegroup links
// Parameters:
//   - $id : ttg id
///////////////////////////////////////////////////////////////////////////////
function dis_tasktypegroup_links($id) {
  global $display, $l_tasktypegroup_link_tasktype, $l_tasktypegroup_link_tasktype_no, $l_back;
  global $l_tasktypegroup_can_delete, $l_tasktypegroup_cant_delete, $l_tasktypegroup_delete;

  $delete_ok = true;
  $label = get_tasktypegroup_label($id);
  $obm_q = run_query_planning_tasktypegroup_links($id);

  $nb_stat = $obm_q->num_rows();
  if ($nb_stat > 0) {
    $delete_ok = false;
    $dis_link_head = "<h1>$label : $l_tasktypegroup_link_tasktype ($nb_stat)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $ttid = $obm_q->f('tasktype_id');
      $ttlabel = $obm_q->f('tasktype_label');
      $dis_link.= "<li><a href=\"" .url_prepare("planning_index.php?action=admin") . "\">$ttlabel</a></li>";
    }
    if ($cpt < $nb_stat) {
      $dis_link .= '<li>...</li>';
    }

  } else {
    $dis_link_head = "<h1>$l_tasktypegroup_link_tasktype_no</h1>";
  }

  $block .= "<div class=\"detail\">
   $dis_link_head
   <ul>
   $dis_link
   </ul>
   </div>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare('planning_index.php') . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display['msg'] .= display_ok_msg($l_tasktypegroup_can_delete);
    $dis_del = "<form name=\"form_type_delete\".
        method=\"post\" action=\"" . url_prepare('planning_index.php') . "\">
      <input type=\"hidden\" name=\"action\" value=\"tasktypegroup_delete\" />
      <input type=\"hidden\" name=\"sel_tasktypegroup_id\" value=\"$id\" />
      <input type=\"submit\" name=\"sub_tasktypegroup\" value=\"$l_tasktypegroup_delete\" />
      </form>";

    $block .= "<div class=\"buttons\">
        $dis_del
        $dis_back
      </div>";
  } else {
    $display['msg'] .= display_warn_msg($l_tasktypegroup_cant_delete);
    $block .= "
      <div class=\"buttons\">
        $dis_back
      </div>";
  }

  return $block;
}

</script>
