<script language="php">
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
  global $cdg_sql;

  $obm_q = new DB_OBM;

  $home_def = $samba["home"];
  $home_drive_def = $samba["home_drive"];

  $query = "UPDATE Samba SET
      samba_value='$home_def'
    WHERE samba_name='samba_home_def'";
  display_debug_msg($query, $cdg_sql, "run_query_samba_update(1)");
  $retour = $obm_q->query($query);

  $query = "UPDATE Samba SET
      samba_value='$home_drive_def'
    WHERE samba_name='samba_home_drive_def'";
  display_debug_msg($query, $cdg_sql, "run_query_samba_update(2)");
  $retour = $obm_q->query($query);

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

</script>
