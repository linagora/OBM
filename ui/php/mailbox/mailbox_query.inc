<script language="php">
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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
// OBM - File : mailbox_query.inc                                            //
//     - Desc : mailbox query File                                           //
// 2007-03-28 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

function update_mailbox_acl($u_uid, $u_domain_id) {
    global $cmd_update_bal_acl;

    $cmd = $cmd_update_bal_acl." --type mailbox --name '".$u_uid."' --domain ".$u_domain_id;

    display_debug_msg($cmd, $cdg_exe, "update_mailbox_acl()");
    exec($cmd);

    return true;
}

///////////////////////////////////////////////////////////////////////////////
// Mailbox specific rights checking (eg : delegation)
// Parameters:
//   - $params[] : user values
// Returns:
//   - true if OK, else false
///////////////////////////////////////////////////////////////////////////////
function check_mailbox_update_rights($params) {
  global $err, $cgp_use, $l_error_delegation;

  $entity_id = $params['entity_id'];
  if ($entity_id > 0) {
    // Check if delegation is used
    if ($cgp_use['property']['delegation']) { 
      $obm_q = new DB_OBM;
      $query = "SELECT userobm_delegation FROM UserObm
        INNER JOIN MailboxEntity on mailboxentity_mailbox_id=userobm_id WHERE mailboxentity_mailbox_id='$entity_id'";

      display_debug_msg($query, $cdg_sql, "check_mailbox_update_rights");
      $obm_q->query($query);
      if($obm_q->num_rows()>0) {
        $obm_q->next_record();
        $mailbox_delegation = $obm_q->f("userobm_delegation");
  
        // Check delegation update right
        if (empty($mailbox_delegation)) {
          return false;
        }

        if (! of_delegation_check_update_rights($params, $mailbox_delegation, $mailbox_delegation)) {
	        $err['msg'] .= $l_error_delegation;
	        return false;
        } else {
          return true;
        }
      }
    }
  }
  return false;
}


function get_user_login($id) {
  global $cdg_sql;

  $id = sql_parse_id($id, true);
  $query = "SELECT userobm_login FROM UserObm WHERE userobm_id $id ";
  display_debug_msg($query, $cdg_sql, 'get_user_login()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f('userobm_login');
}
</script>
