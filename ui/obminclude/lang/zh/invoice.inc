<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : invoice.inc                                              //
// 2001-08-02 - AliaSource - Pierre Baudracco                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-09T15:20:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:22:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:31:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:35:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:41:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:48:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:50:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:55:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:05:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:25:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:26:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:32:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:33:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:33:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:34:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:35:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:36:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:40:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:42:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:52:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:56:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:56:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:57:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:59:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:59:34+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T11:07:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T11:11:16+02:00 using OBMtranslation (0.4.1)

$l_label_start = "标签开始"; //"Label start"
$l_amount = "金额"; //"Amount"
$l_amount_ht = "HT金额"; //"HT amount"
$l_amount_ttc = "TTC金额"; //"TTC Amount"
$l_expiration_date = "过期日"; //"Expiration date"
$l_payment_date = "支付日"; //"Payment date"
$l_expiration = "过期"; //"Expiration"
$l_credit_memo = "信用凭证"; //"Credit memo"
$l_paid = "已支付"; //"Paid"
$l_waiting = "等待"; //"Waiting"
$l_pick_deal = "选择一笔交易"; //"Choose a deal"
$l_inout = "类型"; //"Type"
$l_both = "所有"; //"All"
$l_tax_rate = "税率"; //"Tax rate"
$l_compute_tax = "计算纳税金额"; //"Calculate the taxed amount"
$l_include_archive = "包括存档"; //"Include archives"
$l_amount_remaining = "剩余金额";  //"Remaining amount"
$l_select_invoice = "选择发票";  //"Select invoice"
$l_invoicestatus_tocreate = '待创建'; //'To create'
$l_invoicestatus_sent = '已发送'; //'Sent' 
$l_invoicestatus_received = '已接收'; //'Received'
$l_invoicestatus_ppaid = '支付部分费用'; //'Partially paid'
$l_invoicestatus_paid = '已支付'; //'Paid'
$l_invoicestatus_conflict = '有冲突'; //'Conflict'
$l_invoicestatus_cancelled = '已取消'; //'Cancelled'
$l_invoicestatus_loss = '丢失'; //'Lost'
$l_invoicestatus_factor = '因素'; //'Factor'
$l_no_payment = "无支付款项关联此发票"; //"no payments related to this invoice"
$l_list_payment = "支付款项关联此发票"; //"payments related to this invoice"
$l_invoice_infos = "发票信息";  //"Invoice information"
$l_affect = "影响"; //"Affect"
$l_year_to_year = "年对比"; //"Years comparison"
$l_insert= "添加发票"; //"Insert Invoice"
$l_update = "更新发票"; //"Update Invoice"
$l_delete = "删除发票"; //"Delete Invoice"
$l_delete_confirm = "确认删除此发票？"; //"Confirm the deletion of this invoice ?"
$l_no_display = "填写搜索字段，并点击<查找>来搜索发票";  //"Fill in the search fields and press < Find > to retrieve the invoices"
$l_no_payment_display = "填写搜索字段，并点击<查找>来搜索支付款项"; //"Fill the search fields and press < Find > to retrieve the payments"
$l_found = "个已找到的支票"; //"invoices found"
$l_no_found="没有与查询对应的发票"; //"No invoice corresponding to your query"
$l_no_payment_found ="没有与查询对应的支付款项"; //"No payment corresponding to your query"
$l_link_payment = "这些支付款项与该发票有关联"; //"These payments are connected to this invoice"
$l_link_payment_no = "该发票无支付款项"; //"The invoice has no payment"
$l_cant_delete = "该发票无法被删除！";   //"The invoice cannot be deleted !"
$l_can_delete = "该发票可以被删除。"; //"The invoice can be deleted."
$l_fill_label = "您必须填写标签！";  //"The label must be filled in !"
$l_fill_number = "您必须填写编号（在此状态下）！";  //"The number must be filled in (for this status) !"
$l_fill_company = "您必须填写公司！";  //"The company must be filled in !"
$l_fill_inout = "您必须选择类型: $l_client或$l_supplier ！"; //"You must select the kind : $l_client or $l_supplier !"
$l_fill_date = "您必须填写日期（在此状态下）！";  //"The date must be filled in (for this status) !"
$l_fill_amount = "您必须填写金额！";  //"Tha amounts must be correctly filled in !"
$l_error_number_exist = "该发票的编号已经被使用！"; //"This invoice number is already used !"
$l_err_paid_status = "该发票款项已支付，状态必须是'$l_invoicestatus_paid' !"; //"The invoice has been paid, status must be '$l_invoicestatus_paid' !"
$l_err_ppaid_status = "该发票款项已经被部分支付，状态必须是'$l_invoicestatus_ppaid', '$l_invoicestatus_received'或'$l_invoicestatus_conflict' !"; //"The invoice has been partially paid, status must be '$l_invoicestatus_ppaid', '$l_invoicestatus_received' or '$l_invoicestatus_conflict' !"
$l_err_no_payment_status = "该发票无支付款项， 状态必须是'$l_invoicestatus_paid'或'$l_invoicestatus_ppaid' !"; //"The invoice has no payments, status cannot be '$l_invoicestatus_paid' or '$l_invoicestatus_ppaid' !"
$l_err_status_received_customer = " 对于$l_client发票来说，该发票状态$l_invoicestatus_received是无效的！"; //"The invoice status $l_invoicestatus_received is not valid for $l_client invoices !"
$lj_check_number = "\\n该发票编号无效！"; //"\\nThe invoice number is not valid!"
$lj_check_money_ht = "\\n未支付的金额无效！"; //"\\nThe amount without charges is not valid!"
$lj_check_money_ttc = "\\n已支付的金额无效！"; //"\\nThe amount charges included is not valid!"
$lj_check_amounts = "\\n包括支付金额的"; //"\\nThe amount charges included should be greater than the amount without charges!"
$lj_check_inout = "\\n提交前，您必须选择一个种类"; //"\\nYou have to choose a kind before submitting"
$lj_changing_inout = "如果一些交易与该发票有关联，该发票类型则无法被更改..."; //"You cannot change an invoice kind if there are \\n deals linked to it..."
$lj_no_number="\\n发票必须有一个编号！"; //"\\nInvoice must have a number!"
$lj_fill_date = "\\n 提交前，您必须填写日期。"; //"\\n You have to fill the Date before submitting."
$lj_fill_label = "\\n 提交前，您必须填写标签。"; //"\\n You have to fill the Label before submitting."
$l_invalid_date="\\n无效的日期格式(MM/JJ/AAAA或AAAA-MM-JJ)"; //"\\nInvalid Date Format (MM/JJ/AAAA or AAAA-MM-JJ)"
$lj_check_deal = "提交前，您必须选择一笔交易。"; //"You have to select a Deal before submitting"
$lj_select_deals = "您必须选择交易的一个编号"; //"You have to select a number of deals"
$l_invoice_display = "显示发票列表选项"; //"Display options for invoices list"
$default_invoice_numbering = "Y"; //"Y"
$l_check_money_ttc = "该发票的含税金额不正确！";  //"The tax included amount of the invoice is incorrect !"
$l_check_amounts = "含税金额必须高于完税金额！";  //"The tax included amount has to be higher than the tax exclusion amount !"
$l_check_inout = "您必须选择一个类型！";  //"You have to choose a  type !"
$l_ht_nan = "无效的完税金额！";  //"Invalid tax exclusion amount !"
$l_check_payment_chosen = "检查已选中的支付款项";  //"Check chosen payments "
$l_j_select_invoice = "您必须选择一个发票！";  //"You have to select an invoice !"
$l_remaining_amount = "剩余金额";  //"Remaining amount"
$l_add_payment_chosen = "添加已选中的支付款项"; //"Add chosen paiements"
$l_rate_nan = "无效的VAT号码！"; //"Invamid VAT number !"
$l_no_number_confirm = "您将要创建一个无编号的发票。请点击“确认“继续。"; //"You are about to create an invoice without Number. Press OK to continue."
$l_payments_exist = "一些支付款项与该发票相关联！"; //"There are payments linked to this invoice !"
$l_to_use_amount = "发票金额"; //"Invoice amount"
$l_del_payment_chosen = "删除已选中的支付款项"; //"Delete chosen payments"
$l_invoice_mod = "修改发票"; //"Modify Invoice"
$l_invalid_payment = "取自支付款项的金额大于总金额"; //"The amount taken from a payment is bigger than its total"
$l_check_money_ht = "该发票的完税金额不正确！";  //"The tax exclusion amount of the invoice is incorrect !"
$l_changing_inout = "如果有一些交易与该发票有关联，您就不能修改该发票"; //"You cannot change an invoice kind if there are Deals linked to it"
$l_invoice_del_paiement = "删除发票付款 "; //"Delete invoice payment "
$l_invoice_del_deal = "删除发票交易"; //"Delete invoice Deal"
$l_del_payment = "删除支付款项"; //"Delete payment"
$l_payments_todel = "与该发票有关的支付款项"; //"Payments linked to this invoice "
$l_no_number = "发票必须有编号！ "; //"An invoice must have a number ! "
$l_add_payment = "添加支付款项"; //"Add payment"
$l_check_number = "无效的发票编号！"; //"Invalid Invoice Number! "
$l_duplicate = "复制发票"; //"Duplicate Invoice "

