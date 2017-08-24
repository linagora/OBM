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
// OBM - File : samba_query.inc                                              //
//     - Desc : Samba Administration query & db File                         //
// 2004-09-15 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Samba Update queries execution
// Parameters:
//   - $samba[] : samba hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_samba_update($samba) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;

  $home = $samba["home"];
  $drive = $samba["drive"];

  $entity_id = of_entity_get('domain', $obm['domain_id']);

  $retour = of_service_property_replace('samba', 'drive', $entity_id, $drive);
  $retour = $retour && of_service_property_replace('samba', 'home', $entity_id, $home);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Samba Form Data checking and formatting
// Parameters:
//   - $samba[]  : values checked
//     keys used : name, desc
///////////////////////////////////////////////////////////////////////////////
function check_data_form($samba) {
  global $err;
  global $php_regexp_dflt_home, $l_err_home, $php_regexp_drive, $l_err_drive;

  $home = stripslashes($samba["home"]);
  $drive = $samba["home_drive"];

  // Samba home correct
  if (($home != "") && (! preg_match($php_regexp_dflt_home, $home))) {
    $err["msg"] = " $home : $l_err_home";
    $err["field"] = "home";
    return false;
  }

  // Samba home drive correct
  if (($drive != "") && (! preg_match($php_regexp_drive, $drive))) {
    $err["msg"] = " $drive : $l_err_drive";
    $err["field"] = "drive";
    return false;
  }

  return true;
}

?>
