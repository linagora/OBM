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
// OBM - File : admin_pref_query.inc                                         //
//     - Desc : Pref admin Query File                                        //
// 2002-08-10 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get the default user preferences
// Returns:
//   - $obm_q : DB object of default User Preferences 
///////////////////////////////////////////////////////////////////////////////
function get_admin_pref_default_user_pref() {
  global $cdg_sql;

  $query = "SELECT *
            FROM UserObmPref
            WHERE userobmpref_user_id IS NULL
            ORDER BY userobmpref_option";

  display_debug_msg($query, $cdg_sql, "get_admin_pref_default_user_pref()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the user preference value of the option given
// Parameters:
//   - $option : preference to get
// Returns:
//   - $value : of the preference
///////////////////////////////////////////////////////////////////////////////
function get_admin_pref_userpref_value($option) {
  global $obm, $cdg_sql;

  $uid = sql_parse_id( $obm["uid"], true);
  $query = "SELECT userobmpref_value
            FROM UserObmPref
            WHERE userobmpref_user_id  $uid 
              AND userobmpref_option = '$option'";

  display_debug_msg($query, $cdg_sql, "get_admin_pref_userpref_value()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  return $obm_q->f("userobmpref_value");
}


///////////////////////////////////////////////////////////////////////////////
// Admin Preferences Form Data checking and formatting
// Parameters:
//   - $pref[]   : values checked
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_admin_pref_data_form($pref) {

  // Future use to check if user parameter valid ? XXXXX

  return true; 
}

?>
