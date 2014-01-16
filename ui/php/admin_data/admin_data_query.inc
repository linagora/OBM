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
// OBM - File : admin_data_query.inc                                         //
//     - Desc : Data admin Query File                                        //
// 2003-05-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Query execution - company list                                            //
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_company_list() {
  global $cdg_sql;

  $query = "SELECT company_id,
      company_name,
      company_aka,
      company_sound,
      company_contact_number,
      company_deal_number,
      company_deal_total
    FROM Company";

  display_debug_msg($query, $cdg_sql);

  $c_q = new DB_OBM;
  $c_q->query($query);
  return $c_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company contact number
// Parameters:
//   - $id : company id
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_company_contact_number($id) {
  global $cdg_sql;
  $id = sql_parse_id( $id);
  $query = "SELECT count(contact_id) as nb
    FROM Contact
    WHERE contact_company_id=$id
      AND contact_archive=0";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('nb');
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company active (non archived) deal number
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_company_active_deal_number($id) {
  global $cdg_sql;

  $id = sql_parse_id( $id);

  $query = "SELECT count(deal_id) as nb
    FROM Deal
    WHERE deal_company_id=$id AND deal_archive=0";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('nb');
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company total deal number
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_company_total_deal_number($id) {
  global $cdg_sql;

  $id = sql_parse_id( $id);
  $query = "SELECT count(deal_id) as nb
    FROM Deal
    WHERE deal_company_id=$id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('nb');
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Company Update
// Parametres:
//   - $id         : company id
//   - $con_num    : contact number
//   - $deal_num   : active deal number
//   - $deal_total : total deal number
///////////////////////////////////////////////////////////////////////////////
function update_admin_data_one_company($id, $con_num, $deal_num, $deal_total) {
  global $cdg_sql;
  $con_num = sql_parse_int( $con_num);
  $deal_num = sql_parse_int( $deal_num);
  $deal_total = sql_parse_int( $deal_total);
  $id = sql_parse_id( $id);

  $query = "UPDATE Company SET
      company_contact_number=$con_num,
      company_deal_number=$deal_num,
      company_deal_total=$deal_total
    WHERE company_id=$id";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Deal list
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_deal_list() {
  global $cdg_sql;

  $query = "SELECT deal_id,
      deal_hitrate,
      deal_status_id,
      deal_archive,
      dealstatus_label,
      dealstatus_hitrate
    FROM Deal
      LEFT JOIN DealStatus ON deal_status_id = dealstatus_id";

  display_debug_msg($query, $cdg_sql);

  $d_q = new DB_OBM;
  $d_q->query($query);
  return $d_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Deal hitrate Update
// Parametres:
//   - $id      : deal id
//   - $hitrate : deal new hitrate
///////////////////////////////////////////////////////////////////////////////
function update_admin_data_one_deal($id, $hitrate) {
  global $cdg_sql;
  
  $id = sql_parse_id( $id);
  $hitrate = sql_parse_int( $hitrate);
  $query = "UPDATE Deal SET
      deal_hitrate=$hitrate
    WHERE deal_id=$id";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - List list
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_list_list() {
  global $cdg_sql;

  $query = "SELECT list_id,
      list_name,
      list_subject,
      list_email,
      list_mailing_ok,
      list_static_nb,
      list_query_nb,
      list_query,
      list_structure
    FROM List
    ORDER BY list_name";

  display_debug_msg($query, $cdg_sql);

  $l_q = new DB_OBM;
  $l_q->query($query);
  return $l_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - List Update
// Parametres:
//   - $id        : list id
//   - $sta_nb    : static contact number
//   - $dyn_nb    : dynamic contact number
//   - $new_query : query
///////////////////////////////////////////////////////////////////////////////
function update_admin_data_one_list($id, $sta_nb, $dyn_nb, $new_query) {
  global $cdg_sql;
  
  $sta_nb = sql_parse_int( $sta_nb);
  $dyn_nb = sql_parse_int( $dyn_nb);
  $id = sql_parse_id( $id);
  
  $query = "UPDATE List SET
      list_static_nb=$sta_nb,
      list_query_nb=$dyn_nb,
      list_query='$new_query'
    WHERE list_id=$id";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - document list
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_document_list() {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $ctt[0]['type'] = 'field';
  $ctt[0]['value'] = 'document_path';
  $ctt[1]['type'] = 'field';
  $ctt[1]['value'] = 'document_name';
  $concat = sql_string_concat($db_type, $ctt);

  $query = "SELECT document_id,
      document_path,
      document_name,
      document_kind
    FROM Document
    ORDER BY $concat";

  display_debug_msg($query, $cdg_sql);

  $c_q = new DB_OBM;
  $c_q->query($query);
  return $c_q;
}


///////////////////////////////////////////////////////////////////////////////
// Calculate the relative disk document path (from document_path)
// Parameters:
//   - $id   : document id
// Returns:
//   real disk path
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_document_disk_relative_path($id) {

  // The document disk path set in the subdirectory named 'last id number'
  // Get the last number from Id
  $rel = substr($id, -1, 1);

  $disk_path = '/' . $rel . '/' . $id;
  
  return $disk_path; 
}


///////////////////////////////////////////////////////////////////////////////
// Get file list from Filesystem. includes Files and optionnally directories
// Parameters:
//   - $root     : root directory to list (complete path)
//   - $with_dir : if true, returns directories too
// Returns:
// Array( [$fullname] => array([0]=>$path [1]=>$name), ..)
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_file_list($root, $with_dir=false) {
  global $cdocument_root;

  $tree = array();

  $root_handler = opendir($root);
  $relative_path = substr($root, strlen($cdocument_root));
  while ( ($file = readdir($root_handler)) !== false ) {
    $path = "$root$file";
    if (($file != '..') && ($file != '.')) {
      $ret[$path] = $file;
    }
  }
  closedir($root_handler);
  if ($ret) {
    ksort($ret);
    foreach($ret as $path=>$file) {
      $tree[$relative_path.$file] = array($relative_path,$file);

      // If entry is a directory
      if (is_dir($path)) {
	// We drop the entry if directory not te be returned
	if (! $with_dir) {
	  unset($tree[$relative_path.$file]);
	}
	// We recurse into it
	$result =  get_admin_data_file_list("$path/");
	if ($result) {
	  $tree = array_merge($tree,$result);
	}
      }
    }
  }
  return $tree;
}


/**
 * Get the public group list
**/
function get_admin_data_group_list() {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $query = "SELECT group_id,
      group_name
    FROM UGroup";

  display_debug_msg($query, $cdg_sql, 'get_admin_data_group_list()');

  $g_q = new DB_OBM;
  $g_q->query($query);

  return $g_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - contact list                                            //
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_contact_list() {
  global $cdg_sql;

  $query = "SELECT contact_id,
      contact_lastname,
      contact_aka,
      contact_sound
    FROM Contact";

  display_debug_msg($query, $cdg_sql);

  $c_q = new DB_OBM;
  $c_q->query($query);
  return $c_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Company Sound Update
// Parametres:
//   - $id    : company id
//   - $aka   : aka to insert
//   - $sound : sound key to insert
///////////////////////////////////////////////////////////////////////////////
function update_admin_data_sound_aka_one_company($id, $aka, $sound) {
  global $cdg_sql;
  $u_q = new DB_OBM;

  $aka_s = $u_q->escape($aka);
  $id = sql_parse_id( $id, true);
  $query = "UPDATE Company SET
      company_aka='$aka_s',
      company_sound='".$u_q->escape($sound)."'
    WHERE company_id $id"; 

  display_debug_msg($query, $cdg_sql);
  $retour = $u_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Contact Sound Update
// Parametres:
//   - $id    : contact id
//   - $aka   : aka to insert
//   - $sound : sound key to insert
///////////////////////////////////////////////////////////////////////////////
function update_admin_data_sound_aka_one_contact($id, $aka, $sound) {
  global $cdg_sql;
  $u_q = new DB_OBM;

  $aka_s = $u_q->escape($aka);
  $id = sql_parse_id( $id, true);
  $query = "UPDATE Contact SET
      contact_aka='$aka_s',
      contact_sound='".$u_q->escape($sound)."'
    WHERE contact_id $id"; 

  display_debug_msg($query, $cdg_sql);
  $retour = $u_q->query($query);

  return $retour;
}

?>
