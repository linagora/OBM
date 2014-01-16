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

/**
 * Get share contact for entity
 */
function get_calendar_entity_share($entity_id, $entity, $type, $readAcces = 'calendar') {
  global $obm, $cdg_sql;

  if($type == 'private'){
    $uid = $obm['uid'];
  } else {
    $uid = 0;
  }
  $tokens = get_entity_token($entity_id,$entity,$type);

  if(!isset($tokens[$uid][$type])) {
    $tokens[$uid][$type] = generateUniqueKey(false);
    run_query_calendar_insert_token($tokens[$uid][$type], $mail, $entity, $entity_id, $readAcces, $type, $format);
  }

  return $tokens[$uid][$type];
}

function run_query_calendar_insert_token($token, $mail=null, $entity_type, $entity_id ,$readAcces, $type, $format=null) {
  global $obm, $cdg_sql;

  $domain = $obm['domain_id'];
  $delegation = $obm['delegation'];
  if($type == 'private'){
    $uid = $obm['uid'];
  } else {
    $uid = 0;
  }

  $obm_q = new DB_OBM();

  $query = "INSERT INTO token (token, property, value ) VALUES ('$token', 'domain', '$domain');";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_token(domain)');
  $obm_q->query($query);
  $query = "INSERT INTO token (token, property, value ) VALUES ('$token', 'entity', '$entity_type');";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_token(entity)');
  $obm_q->query($query);
  $query = "INSERT INTO token (token, property, value ) VALUES ('$token', 'entityId', '$entity_id');";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_token(entityId)');
  $obm_q->query($query);
  $query = "INSERT INTO token (token, property, value ) VALUES ('$token', 'delegation', '$delegation');";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_token(delegation)');
  $obm_q->query($query);
  $query = "INSERT INTO token (token, property, value ) VALUES ('$token', 'uid', '$uid');";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_token(uid)');
  $obm_q->query($query);
  $query = "INSERT INTO token (token, property, value ) VALUES ('$token', 'readAccess', '$readAcces');";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_token(readAccess)');
  $obm_q->query($query);
  $query = "INSERT INTO token (token, property, value ) VALUES ('$token', 'type', '$type');";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_token(type)');
  $obm_q->query($query);
}

/**
 *  Delete token for all uid
 */
function run_query_calendar_delete_token($entity_id,$entity_type,$type) {
  global $obm, $cdg_sql;

  if($type == 'private'){
    $uid = $obm['uid'];
  } else {
    $uid = 0;
  }
  $obm_q = new DB_OBM();

  $tokens = get_entity_token($entity_id,$entity_type,$type);
  if($uid != 0){
    foreach($tokens as $token){
      $query = "DELETE FROM token WHERE token='$token[$type]';";
      display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_token(delete)');
      $obm_q->query($query);
    }
  } else {
    $token = $tokens[$uid][$type];
    $query = "DELETE FROM token WHERE token='$token';";
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_token(delete)');
    $obm_q->query($query);
  }
}

/**
 * Get all token create for specified entity
 * return array(uid => array(type => token))
 */
function get_entity_token($entity_id,$entity,$type) {
  global $obm, $cdg_sql;

  $domain = $obm['domain_id'];

  $obm_q = new DB_OBM();
  $query = "SELECT DISTINCT(t1.token),
    t3.value as type,
    t4.value as uid
    FROM token as t1
    LEFT JOIN token as t2 ON t1.token = t2.token
    LEFT JOIN token as t3 ON t1.token = t3.token
    LEFT JOIN token as t4 ON t1.token = t4.token
    LEFT JOIN token as t5 ON t1.token = t5.token
    WHERE t1.property='entityId' AND t1.value='$entity_id'
    AND t2.property='entity' AND t2.value='$entity'
    AND t3.property='type'
    AND t4.property='uid'
    AND t5.property='domain' AND t5.value='$domain';";
  display_debug_msg($query, $cdg_sql, 'get_entity_token()');
  $obm_q->query($query);
  while($obm_q->next_record()){
    $type = $obm_q->f('type');
    $uid = $obm_q->f('uid');
    $tokens[$uid][$type]=$obm_q->f('token');
  }
  return $tokens;
}

?>
