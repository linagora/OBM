<script language="php">
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
        if (! of_delegation_check_update_rights($params, $mailbox_delegation, $mailbox_delegation)) {
	        $err['msg'] .= $l_error_delegation;
	        return false;
        }
      }
    }
  }
  return true;
}


</script>
