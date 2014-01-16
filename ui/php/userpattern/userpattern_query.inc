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
// OBM - File : userpattern_query.inc                                        //
//     - Desc : User Pattern Query File                                      //
// 2010-01-21 Vincent ALQUIER                                                //
///////////////////////////////////////////////////////////////////////////////


/**
 * User Pattern search query execution
 * @param array   $userpattern   parameters (keys used: title, sql_order_dir, sdl_order_field)
 * @return DB_OBM                query result
 **/
function run_query_userpattern_search($userpattern) {
  global $cdg_sql, $obm, $ctu_sql_limit;

  $title = sql_search_text_parse($userpattern["title"]);
  $sql_order_dir = $userpattern["sql_order_dir"];
  $sql_order_field = $userpattern["sql_order_field"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timecreate = sql_date_format($db_type, "timecreate", "timecreate");
  $timeupdate = sql_date_format($db_type, "timeupdate", "timeupdate");

  // SELECT construction
  $select = "SELECT
      id,
      domain_id,
      $timecreate,
      $timeupdate,
      title,
      description
    FROM userpattern
      ";

  // WHERE construction
  $where = array();

  //multidomain
  if (!$obm['domain_global']) {
    $domain_id = sql_parse_id($obm['domain_id'], true);
    $where[] = "(domain_id $domain_id)";
  }

  if ($title != "") {
    $where[]= "title $like '%".$obm_q->escape($title)."%'";
  }

  if (!empty($where))
    $whereq = 'WHERE '.implode(' AND ',$where);

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "title";
  $orderq = "ORDER BY $order $sql_order_dir";

  // Querying
  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM userpattern $whereq";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    $query = "$select $whereq $orderq $limit";
    display_debug_msg($query, $cdg_sql, "run_query_userpattern_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}

/**
 * User Pattern get query
 * @param  array       $params     the userpattern properties
 * @param  UserPattern $pattern    User Pattern to update
 **/
function check_userpattern_data_form($params, &$pattern) {
  global $err;

  $title = trim($params['userpattern']['title']);
  $description = trim($params['userpattern']['description']);

  if (!isset($params['attributes']['hidden']))            $params['attributes']['hidden'] = 0;
  if (!isset($params['attributes']['noexperie']))         $params['attributes']['noexperie'] = 0;
  if (!isset($params['attributes']['web_perms']))         $params['attributes']['web_perms'] = 0;
  if (!isset($params['attributes']['mail_perms']))        $params['attributes']['mail_perms'] = 0;
  if (!isset($params['attributes']['nomade_perms']))      $params['attributes']['nomade_perms'] = 0;
  if (!isset($params['attributes']['nomade_enable']))     $params['attributes']['nomade_enable'] = 0;
  if (!isset($params['attributes']['nomade_local_copy'])) $params['attributes']['nomade_local_copy'] = 0;

  if (empty($title)) {
    $err['msg'] = $GLOBALS['l_no_title_error'];
    return false;
  }

  if ($pattern instanceof UserPattern) {
    $pattern->title = $title;
    $pattern->description = $description;
  } else {
    $pattern = new UserPattern($title,$description);
  }
  return $pattern->set_attributes($params['attributes']);
}

/**
 * FUNCTION - automatic mail server chooser hooks
 * @return boolean
 **/
function has_mail_server_auto () {
  global $obm;
  
  {
    $obm_q = new DB_OBM();
    $query = "SELECT serviceproperty_value as domain_mail_server_auto FROM ServiceProperty 
      INNER JOIN DomainEntity ON serviceproperty_entity_id = domainentity_entity_id 
      WHERE domainentity_domain_id = $obm[domain_id] AND serviceproperty_property = 'imap_autoselect'";
    display_debug_msg($query, $cdg_sql, 'has_mail_server_auto()');
    $obm_q->query($query);
    
    if ($obm_q->num_rows() == 1) {
      $obm_q->next_record();
      if ($obm_q->f('domain_mail_server_auto')) return true;
    }
  }
  
  return false;
}


?>
