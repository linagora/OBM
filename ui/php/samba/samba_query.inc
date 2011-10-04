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

</script>
