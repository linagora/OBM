<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Romanian                                                   //
//     - File     : payment.inc                                              //
// 2001-08-02 AliaSource                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id: payment.inc 3261 2008-10-28 17:08:39Z vincentb $ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-18T11:12:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T11:14:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T11:14:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T11:20:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T11:22:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T11:38:45+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T11:42:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:02:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:03:41+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:11:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:14:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:18:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:20:12+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:20:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:22:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:31:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T12:34:12+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:12:33+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:16:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:19:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:26:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:26:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:30:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:33:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:41:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:44:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:50:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T14:51:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T15:13:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T15:23:25+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T15:26:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T15:38:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T15:47:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T15:47:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T15:55:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T16:03:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T16:35:25+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T16:41:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T16:44:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T16:57:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T17:18:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T17:27:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T17:29:45+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T17:41:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T17:59:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-18T17:59:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T09:54:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T09:54:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T09:55:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T09:57:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T09:58:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T09:58:45+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T09:59:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T09:59:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:00:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:02:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:04:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:05:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:06:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:08:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:11:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:12:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:13:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:14:32+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:15:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:15:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:17:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:18:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:18:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:18:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:18:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:19:32+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:19:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:21:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:24:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:24:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:24:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:25:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:26:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:27:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:28:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:29:25+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:29:56+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:30:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:32:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:32:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:32:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:39:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:39:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:40:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:41:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:54:47+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:56:32+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:58:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:59:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T10:59:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:03:07+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:04:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:05:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:06:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:14:37+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:19:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:20:12+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:21:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:22:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:22:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:23:10+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:23:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:25:24+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:26:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:26:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:27:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:27:24+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:29:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:30:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:35:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:35:37+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:37:12+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:37:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:38:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:40:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:40:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:42:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:42:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:43:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:43:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:44:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:48:33+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:50:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:50:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:57:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:57:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:02:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:02:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:05:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:05:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:09:25+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:16:45+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:18:12+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:18:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:19:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:19:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:22:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:23:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:25:07+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:26:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:30:05+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:32:33+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:31:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:32:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:35:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:37:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:37:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T15:01:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T15:01:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T15:05:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T15:05:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T15:06:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T15:17:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T15:20:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T15:21:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T15:21:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:01:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:10:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:13:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:20:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:22:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:29:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:29:24+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:31:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:34:12+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:34:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:41:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:44:25+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:44:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:46:34+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:46:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:46:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:46:56+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:48:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:48:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:48:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:48:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:49:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:51:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T16:58:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:10:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:18:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:19:25+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:19:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:19:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:20:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:27:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:27:34+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:30:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:30:40+02:00 using OBMtranslation (0.4.1)

$l_label_start = "Începutul Etichetei";
$l_amount = "Suma";
$l_expected_date = "Dată Prevăzută";
$l_type = "Mod de Plată";
$l_inout = "Tip";
$l_both = "Amândouă";
$l_credit_memo = "Credit";
$l_pick_paymentkind = "Alegeți un mod de Plată";
$l_kind = "Mod";
$l_pick_account = "Alegeți un Cont";
$l_payment_account = "Cont";
$l_paid = "Plătit";
$l_unpaid = "Neplătit";
$l_already_paid = "Deja plătit";
$l_choose_csv = "Alegeți un fișier";
$l_include_checked = "Includeți Plățile verificate";
$l_kind_manage = "Gestiunea modurilor de plată";
$l_kind_label = "Nume";
$l_kind_short_label = "Nume scurt";
$l_no_invoice = "Nicio Factură nu este asociată acestei plăți"; 
$l_invoice_list = "Facturi asociate acestei plăți";
$l_insert = "Introduceți Plata";
$l_update = "Actualizați Plata";
$l_delete = "Ștergeți Plata";
$l_delete_confirm = "Doriți să ștergeți aceasta Plată?";
$l_duplicate = "Copiați această Plată";
$l_delete_invoice = "Ștergeți";
$l_kind_delete = "Ștergeți modul de plată";
$l_no_display = "Completați spațiile și apăsați butonul  <Caută > pentru a lansa căutarea.";
$l_found = "Plăți găsite.";
$l_no_found = "Nicio Plată nu corespunde criteriilor introduse";
$l_no_invoice = "Aucune Facture liée à ce paiment";
$l_cant_delete = "Plata nu poate fi ștearsă!";
$l_can_delete = "Plata poate di ștearsă.";
$l_invoice_added = "Asocierea Plați/Facturi nu a fost corect creată";
$l_no_invoice_added = "Asocierea Plăti/Facturi a eșuat!";
$l_fill_company = "Introduceți firma!";
$l_fill_amount = "Introduceți corect suma!";
$l_fill_inout = "Tipul trebuie selectat : (+) $l_received sau (-) $l_emitted !";
$l_fill_kind = "Modul de plată trebuie selectat!";
$l_invalid_amount = "Suma introdusă nu este valabilă!";
$l_err_total_payment_affected = "Suma totală înscrisă este mai mare decât plata!";
$l_err_affected_invoice = "Suma înscrisă pe factură este superioară sumei restante.";
$l_err_kind = "Modul de plată nu a fost adăugat!";
$l_err_del_kind = "Modul de plată nu a fost șters!";
$l_fill_kind_label = "Introduceți numele modului de plată!";
$l_fill_kind_short_label = "Introduceți numele scurt al modului de plată!";
$l_no_number = "Numărul Plătii nu este valabil!";
$l_check_amount = "Suma nu este valabilă!";
$l_check_inout = "Trebuie să alegeți între Plăți Client sau Plăți Furnizor!";
$l_check_paymentkind = "Introduceți un mod de Plată!";
$l_check_account = "Trebuie să alegeți un Cont!";
$l_no_amount = "Introduceți o sumă pentru această Plată";
$l_j_fill_kind_label = "Introduceți numele modului de plată!";
$l_j_fill_kind_short_label = "Introduceți numele scurt al modului de plată!";
$l_payment_options = "Opțiuni de vizualizare a listelor de Plăți";
$l_invoice_options = "Opțiuni de vizualizare a listelor de Facturi";

