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

</script>
