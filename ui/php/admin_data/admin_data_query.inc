<SCRIPT language="php">
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
function get_company_list() {
  global $cdg_sql;

  $query = "select company_id,
      company_contact_number,
      company_deal_number,
      company_deal_total
    from Company";

  display_debug_msg($query, $cdg_sql);

  $c_q = new DB_OBM;
  $c_q->query($query);
  return $c_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Company Update
// Parametres:
//   - $id         : company id
//   - $con_num    : contact number
//   - $deal_num   : active deal number
//   - $deal_total : total deal number
///////////////////////////////////////////////////////////////////////////////
function update_one_company($id, $con_num, $deal_num, $deal_total) {
  global $cdg_sql;

  $query = "update Company set
      company_contact_number='$con_num',
      company_deal_number='$deal_num',
      company_deal_total='$deal_total'
    where company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Deal list                                               //
///////////////////////////////////////////////////////////////////////////////
function get_deal_list() {
  global $cdg_sql;

  $query = "select deal_id,
      deal_hitrate,
      deal_status_id,
      deal_archive,
      dealstatus_label,
      dealstatus_hitrate
    from Deal
      left join DealStatus on deal_status_id = dealstatus_id";

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
function update_one_deal($id, $hitrate) {
  global $cdg_sql;

  $query = "update Deal set
      deal_hitrate='$hitrate'
    where deal_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}
