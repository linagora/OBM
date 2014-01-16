<?php
/*
# Copyright (C) 2011-2014 Linagora
# 
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
*/
?>
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-2.0-2.1.php                                           //
//     - Desc : Upgrade data from 2.0 to 2.1                                 //
// 2007-06-14 - AliaSource : PB                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$path = "../../php";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("../../obminclude/global.inc");
include("$path/admin_data/admin_data_display.inc");
include("$path/admin_data/admin_data_query.inc");

echo "**** OBM : data migration 2.0 -> 2.1 : DB $obmdb_db ($obmdb_host)\n";

// Get domain list
$d = get_domain_list();
$dp = get_domainproperty_list();
process_domain_list($d, $dp);
update_domain_groups();

dis_admin_data_group('data_update', 'txt');

check_insert_admin0();

///////////////////////////////////////////////////////////////////////////////
// Process the domain list
///////////////////////////////////////////////////////////////////////////////
function process_domain_list($d, $dp) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  // Loop through Domains
  foreach ($d as $d_id) {

    echo "\nDomain $d_id :";

    // Loop through Properties
    foreach ($dp as $key => $prop) {

      $query = "SELECT * FROM DomainPropertyValue
         WHERE domainpropertyvalue_domain_id='$d_id'
           AND domainpropertyvalue_property_key='$key'";
      $obm_q->query($query);

      // If property not found in the domain, insert it with default valuevalue
      if ($obm_q->num_rows() == 0) {
	$default = $prop['default'];
	$type = $prop['type'];
	if ($type == 'integer') {
	  $val = "$default";
	} else {
	  $val = "'$default'";
	}
	echo " $key($val)";
	$q2 = "INSERT INTO DomainPropertyValue (
            domainpropertyvalue_domain_id,
            domainpropertyvalue_property_key,
            domainpropertyvalue_value)
          VALUES ('$d_id','$key', $val)";
	$obm_q->query($q2);
      }
    }
  }

  echo "\n";
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get the domain list
///////////////////////////////////////////////////////////////////////////////
function get_domain_list() {
  global $cdg_sql;

  $d = array();
  $query = "SELECT * FROM Domain";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $id = $obm_q->f("domain_id");
    $d[$id] = $id;
  }

  return $d;
}


///////////////////////////////////////////////////////////////////////////////
// Get the domain propertieslist
///////////////////////////////////////////////////////////////////////////////
function get_domainproperty_list() {
  global $cdg_sql;

  $d = array();
  $query = "SELECT * FROM DomainProperty";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $key = $obm_q->f("domainproperty_key");
    $type = $obm_q->f("domainproperty_type");
    $default = $obm_q->f("domainproperty_default");
    $d[$key] = array ('type' => $type, 'default' => $default);
  }

  return $d;
}


///////////////////////////////////////////////////////////////////////////////
// Update the groups domain associations
///////////////////////////////////////////////////////////////////////////////
function update_domain_groups() {
  global $cdg_sql, $cg_adm, $cg_com, $cg_prod;

  $obm_q = new DB_OBM;

  // Admin group
  if (! ($cg_adm > 0)) { $cg_adm = 1; }
  $q = "UPDATE DomainPropertyValue
    SET domainpropertyvalue_value=$cg_adm
    WHERE domainpropertyvalue_domain_id=1
      AND domainpropertyvalue_property_key='group_admin'";
  $obm_q->query($q);
  echo "\nDomain group_admin : $cg_adm";

  // Commercial group
  if (! ($cg_com > 0)) { $cg_com = 2; }
  $q = "UPDATE DomainPropertyValue
    SET domainpropertyvalue_value=$cg_com
    WHERE domainpropertyvalue_domain_id=1
      AND domainpropertyvalue_property_key='group_com'";
  $obm_q->query($q);
  echo "\nDomain group_com : $cg_com";
 
 // Production group
  if (! ($cg_prod > 0)) { $cg_prod = 3; }
  $q = "UPDATE DomainPropertyValue
    SET domainpropertyvalue_value=$cg_prod
    WHERE domainpropertyvalue_domain_id=1
      AND domainpropertyvalue_property_key='group_prod'";
  $obm_q->query($q);
  echo "\nDomain group_prod : $cg_prod";
}


///////////////////////////////////////////////////////////////////////////////
// Insert an admin0 if not present
///////////////////////////////////////////////////////////////////////////////
function check_insert_admin0() {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  // Check if adomain 0 admin is present
  $q = "Select userobm_id FROM UserObm WHERE userobm_domain_id=0";
  $obm_q->query($q);
  if ($obm_q->num_rows() > 0) {
    echo "\nDomain 0 admin : yes\n";
  } else {
    echo "\nDomain 0 admin : no : insertion...";

    $q = "INSERT INTO UserObm (
        userobm_domain_id,
        userobm_login,
        userobm_password,
        userobm_password_type,
        userobm_perms,
        userobm_lastname,
        userobm_firstname,
        userobm_uid,
        userobm_gid)
      VALUES (
        0,
       'admin0',
       'admin',
       'PLAIN',
       'admin',
       'Admin Lastname',
       'Firstname',
       '1000',
       '512')";

    $obm_q->query($q);
    echo "OK\n";
  }

}


</script>
