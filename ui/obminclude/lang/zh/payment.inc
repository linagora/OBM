<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : payment.inc                                              //
// 2001-08-02 Nicolas Roman                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-09T15:20:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T10:49:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T10:51:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T10:55:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T10:57:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:00:19+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:07:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:09:10+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:09:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:16:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:17:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:17:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:18:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:19:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:22:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:22:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:23:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:24:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:27:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:28:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:30:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:41:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:50:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:52:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:54:32+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:54:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:10:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:11:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:12:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:12:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:17:45+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:20:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:42:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:43:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:43:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:45:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:45:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:47:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:53:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:00:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:01:06+02:00 using OBMtranslation (0.4.1)

$l_label_start = "标签开始"; //"Label start"
$l_amount = "金额"; //"Amount"
$l_expected_date = "预期"; //"Expected Date"
$l_type = "支付类型"; //"Payment Type"
$l_inout = "类型"; //"Type"
$l_both = "都是"; //"Both"
$l_credit_memo = "信用凭证"; //"Credit memo"
$l_pick_paymentkind = "选择一个支付类型"; //"Choose a Payment type"
$l_kind = "种类"; //"Kind"
$l_pick_account = "选择一个账户"; //"Choose an Account"
$l_payment_account = "账户"; //"Account "
$l_paid = "支付"; //"Paid"
$l_unpaid = "未支付"; //"Not Paid"
$l_already_paid = "已支付"; //"Already paid"
$l_kind_manage = "支付类型管理"; //"Payment types management"
$l_kind_label = "标签"; // "Label"
$l_kind_short_label = "短标签"; //"Short label"
$l_no_invoice = "没有发票与该支付款项相关联"; //"No invoices related to this payment"
$l_invoice_list = "有发票与该支付款项相关联"; //"Invoices related to this payment"
$l_already_used_share = "部分支付已被使用"; //"Part of the Payment already used "
$l_payment_infos = "支付信息"; //"Informations about the payment "
$l_insert= "添加支付"; //"Insert Payment"
$l_update = "更新支付"; //"Update Payment"
$l_delete = "删除支付"; //"Delete Payment"
$l_delete_confirm = "确定删除该支付款项？"; //"Confirm the deletion of this payment ?"
$l_delete_invoice = "删除"; //"Delete"
$l_kind_delete = "删除支付类型"; //"Delete payment type"
$l_payment_infos = "查看支付"; //"View Payment"
$l_payment_mod = "修改支付"; //"Modify Payment"
$l_search_invoice ="搜索发票"; //"Search Invoices"
$l_select_invoice = "使用"; //"Use"
$l_affect_invoices = "为该支付款项分配发票"; //"Affect Invoices to that Payment"
$l_duplicate = "重复该支付款项"; //"Duplicate that Payment"
$l_no_display = "填写搜索字段并点击<查找>搜索支付款项"; //"Fill in the search fields and press < Find > to retrieve the payments";
$l_found = "个支付款项已找到。"; //"Payments found."
$l_no_found = "未找到与查询匹配的支付款项"; //"No Payment match your query"
$l_no_invoices_connected = "没有发票与该支付款项相关联"; //"No Invoices connected to that Payment"
$l_cant_delete = "该支付无法被删除！"; //"The payment cannot be deleted !"
$l_can_delete = "该支付可以被删除。"; //"The payment can be deleted."
$l_invoice_added = "支付与发票之间的连接已建立"; //"The connection Payment/Invoice has been created"
$l_no_invoice_added = "支付与发票连接失败！"; //"The connection Payment/Invoice has failed !"
$l_fill_company = "您必须选择一个公司！"; //"A company must be selected !"
$l_err_kind = "还未添加支付类型！"; //"Payment type has not been added !"
$l_err_del_kind = "还未删除支付类型！"; //"Payment type has not been deleted !"
$l_fill_kind = "还未选择支付类型！"; //"The payment type must be selected !"
$l_fill_kind_short_label = "T您必须填写该支付类型的短标签！"; //"The payment type short label has to be filled in !"
$lj_no_number = "\\n该支付编号无效！"; //"\\nThe payment number is not valid!"
$lj_check_amount = "\\n该金额无效！"; //"\\nThe amount is not valid!"
$lj_check_inout = "\\n提交前，您必须选择是客户支付还是供应商支付！"; //"\\nYou have to decide wether it's a payment from a customer or to a supplier before submitting"
$lj_check_paymentkind = "提交前，您必须选择一个支付种类！"; //"You have to choose a payment kind before submitting!"
$lj_check_account = "提交前，您必须选择一个账户！"; //"You have to choose an account before submitting!"
$l_no_amount = "您必须输入该支付金额"; //"You have to enter an amount for the payment"
$l_invalid_amount = "您最后输入的金额无效！";  //"The last amount you have entered is not valid !"
$l_payment_options = "显示支付列表选项"; // "Display options for Payment lists";
$l_not_enough = "您还没有使用所有的支付金额！<BR>"; //"You have not used all the payment amount !<BR>"
$l_too_much = "您的使用金额已超过您的实际金额！<BR>"; //"You have used more money than you actually have !<BR>"
$l_trouble_invoice = "至少有一张发票已经得到太多的钱。<BR>它的状态将变成\"trouble\".<BR>您确定要这样做？<BR>"; //"At least one invoice has received too much money.<BR>Its status will become \"trouble\".<BR>Are you sure you want to do that ?<BR>"
$l_bank_anyway = "Bank that Payment"; // "Bank that Payment"
$l_bank_again = "Restart that banking from the beginning"; //"Restart that banking from the beginning"
$l_check_account = "您必须选择一个账户！"; //"You must choose an Account !"
$l_check_inout = "您必须在客户支付和供应商支付之间选择！"; //"You must choose between Client Payment and Supplier Payment !"
$l_j_fill_kind_label = "您必须填写支付类型名称！"; //"The payment type name must be filled in !"
$l_fill_kind_label = "您必须填写支付类型名称！"; //"The payment type name must be filled in !"
$l_fill_amount = "请正确输入支付金额！"; //"The payment must be entered correctly !"
$l_choose_csv = "选择文件"; //"Choose file"
$l_j_fill_kind_short_label = "您必须填写支付类型短标签！"; //"The payment type short label must be filled in !"
$l_err_affected_invoice = "发票上填写的金额要高于需支付的剩余金额"; //"The amount written on the invoice is higher than the remained amount to be paid"
$l_err_total_payment_affected = "要支付的总金额高于该支付款项！"; //"The total amount to be paid is higher than the payment !"
$l_fill_inout = "您必须选择类型: (+) $l_received或(-) $l_emitted !"; //"The type must be selected : (+) $l_received ou (-) $l_emitted !"
$l_no_number = "无效的支付账号！"; //"Invalid Payment number !"
$l_invoice_options = "显示发票列表选项"; //"Display options for Invoice lists"
$l_check_amount = "无效金额！"; //"Invalid amount !"
$l_include_checked = "添加支付款项";  //"Insert Payments"
$l_check_paymentkind = "您必须选择一个支付类型！"; //"You have to choose a Payment type !"

