<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : deal.inc                                                 //
// 1999-01-26 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-09T15:20:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:49:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:49:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:54:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:57:07+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:59:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:02:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:03:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:03:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:04:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:06:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:08:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:11:19+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:19:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:22:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:23:05+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:23:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:24:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:25:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:26:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:26:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:27:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:27:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:27:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:29:37+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:30:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:31:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:32:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:33:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:34:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:37:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:38:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:40:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:40:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:40:58+02:00 using OBMtranslation (0.4.1)
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
// Modified on 2009-09-11T16:51:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T16:51:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T16:53:47+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T16:54:25+02:00 using OBMtranslation (0.4.1)

$l_date_init = "初始时间"; //"Initial Date"
$l_parentdeal = "父交易"; //"Parent Deal"
$l_marketing_manager = "市场负责人"; //"Marketing manager"
$l_technical_manager = "技术负责人"; //"Technical manager"
$l_amount = "数量"; //"Amount"
$l_margin = "幅度"; //"Margin"
$l_hitrate = "点击率"; //"Hit Rate"
$l_commission = "佣金"; //"Commission"
$l_commission_p = "% 佣金"; //"% Commission"
$l_commission_balanced = "Balanced Commission "; //"Balanced Commission "
$l_expected = "预期"; //"Expected"
$l_quarter = "Q"; //"Q"
$l_sale_delay = "销售延时"; //"Sale delay"
$l_date_comment = "评论日期"; //"Comment Date"
$l_todo = "待办事项"; //"To Do"
$l_category = "类别"; //"Category"
$l_code = "代码"; //"Code"
$l_role = "角色"; //"Role"
$l_other_relation = "其他关系"; //"Other relations"
$l_infos = "信息"; //"Information"
$l_minus = "-";  //"-"
$l_plus = "+"; //"+"
$l_order = "命令"; //"Order"
$l_amount_in = "输入金额"; //"Imputs amount"
$l_amount_out = "输出金额";  //"Outputs amount"
$l_potential_balanced = "加权潜力"; //"Weighted Potential"
$l_label_start = "标签开始"; //"Label start"
$l_proposal = "建议"; //"Proposal"
$l_deal_select_company = "选择已填写交易的公司"; //"Select the company with whom the deal must be filled in."
$l_deal_select_parentdeal = "为被指定的交易选择父交易"; //"Select the parent deal to which the deal must be affected"
$l_affect_deal= "更改父交易"; //"Change Parent Deal"
$l_no_invoice = "没有发票被连接"; //"No invoice connected"
$l_type_manage = "交易类型管理"; //"Deal Type Management"
$l_type_exist = "现有类型"; //"Existing Types"
$l_type_new = "新类型"; //"New Type"
$l_status_manage = "交易状态管理"; //"Deal Status Management"
$l_status_exist = "现有状态"; //"Existing Status"
$l_status_new = "新状态"; //"New Status" 
$l_parentdeal_deals = "属于该父交易的交易"; //"Deals belonging to this parent deal"
$l_forecast = "预测"; //"Forecast"
$l_deal_panel = "交易面板"; //"Deal panel"
$l_select_deal = "选择交易"; //"Select Deal"
$l_insert = "添加交易"; //"Insert Deal"
$l_update = "更新交易"; //"Update Deal"
$l_delete = "删除交易"; //"Delete Deal"
$l_select_company = "选择公司"; //"Select Company"
$l_affect = "影响"; //"Affect"
$l_insert_parent = "添加父交易"; //"Insert the Parent Deal"
$l_update_parent = "更新父交易"; //"Update the Parent Deal"
$l_delete_parent = "删除父交易"; //"Delete the Parent Deal"
$l_type_checkdelete = "检查或删除类型"; //"Check or Delete Type"
$l_type_delete = "删除类型"; //"Delete Type"
$l_type_update = "更新类型"; //"Update Type"
$l_type_insert = "添加类型"; //"Insert Type"
$l_status_update = "更新状态"; //"Update Status"
$l_status_checkdelete = "检查或删除状态"; //"Check or Delete Status"
$l_status_delete = "删除状态"; //"Delete Status"/
$l_status_insert = "添加状态"; //"Insert Status"
$l_add_cats = "添加这些类别"; //"Add these categories"
$l_link_contract = "该交易与一些合约有关联";  //"The deal is involved in some contracts"
$l_link_contract_no = "该交易与任何合约都无关联"; //"The deal is not involved in any contracts"
$l_link_project = "该交易与一些项目有关联"; //"The deal is involved in some projects"
$l_link_project_no = "该交易与任何项目都无关联"; //"The deal is not involved in any projects"
$l_link_invoice = "该交易与一些发票有关联"; //"The deal is involved in some invoices"
$l_link_invoice_no = "该交易与任何发票都无关联"; //"The deal is not involved in any invoices"
$l_can_delete = "该交易可以被删除。"; //"The deal can be deleted."
$l_cant_delete = "该交易无法被删除！"; //"The deal cannot be deleted !"
$l_link_deal = "一些交易依附于该父交易"; //"Deals are attached to this ParenDeal"
$l_cant_delete_parent = "该父交易无法被删除！"; //"The parent deal cannot be deleted !"
$l_err_label_empty = "您必须填写父交易标签！"; //"You must fill in the Parent Deal label !"
$l_err_type_empty = "您必须填写交易类型！"; //"You must fill in the deal type !"
$l_err_task_empty = "您必须填写交易类别！"; //"You must fill in the deal category !"
$l_err_market_empty = "您必须填写市场负责人！"; //"You must fill in the marketing manager !"
$l_err_tech_empty = "您必须填写技术负责人！"; //"You must fill in the technical manager !"
$l_err_hitrate = "点击率必须在0-100的范围之间！"; //"Hit rate must be in the range 0 - 100 !"
$l_updateaffect_ok = "被指定给一个父交易的交易已更新成功"; //"The deal has been successfully affected to a parent deal"
$l_no_display = "填写搜索字段并点击<查找>搜索交易"; //"Fill in the search fields and press < Find > to retrieve the deals"
$l_found = "个交易已找到。"; //"deals found."
$l_no_found = "未找到与查询匹配的交易！"; //"No deal matching the query has been found !"
$l_found_parent = "个父交易已找到"; //"Parent Deals found"
$l_no_found_parent = "未找到与查询匹配的父交易！"; //"No parent deal matching the query has been found !"
$l_no_display_parent ="填写搜索字段并点击<查找>搜索父交易"; //"Fill the search fields and press < Find > to retrieve the parentdeals"
$l_type_can_delete = "该类型可以被删除！"; //"The Type can be deleted !"
$l_type_cant_delete = "该类型无法被删除！"; //"The Type cannot be deleted !"
$l_type_link_deal = "该类型被这些交易引用"; //"The Type is involved in these deals"
$l_type_link_deal_no = "该类型没有与任何交易都无关联"; //"The Type is not involved in any deal"
$l_status_can_delete = "该状态可以被删除"; //"The Status can be deleted"
$l_status_cant_delete = "该状态无法被删除"; //"The Status cannot be deleted"
$l_status_link_deal = "该状态与这些交易有关联"; //"The Status is involved in these deals"
$l_status_link_deal_no = "该状态与任何交易都无关联"; //"The Status is not involved in any deal"
$l_help_deal_forecast = "预测交易的标准: 未处于存档状态的交易，估计值和成功率在0-100之间"; //"Forecast takes into account deals with these criteria : deal not in an archived state, with an estimated amount and with a hit rate > 0 and < 100."
$l_fill_parentdeal = "提交前，您必须选择一个父交易。"; //"You have to select a parent deal before submitting."
$l_fill_label = "提交前，您必须填写标签。"; //"You have to fill in the Label before submitting."
$l_fill_number = "提交前，您必须填写编号。"; //"You have to fill in the Number before submitting."
$l_fill_datealarm = "提交前，您必须填写提醒日期。"; //"You have to fill in the Alarm date before submitting."
$l_fill_datebegin = "提交前，您必须填写开始日期。"; //"You have to fill in the Beginning date before submitting."
$l_confirm_datealarm = "提醒日期是在过去！确定此操作？"; //"Date Alarm is in the past ! Confirm the action ?"
$l_invalid_date = "无效的日期格式(MM/JJ/AAAA或AAAA-MM-JJ)."; //"Invalid Date Format (MM/JJ/AAAA or AAAA-MM-JJ).";
$l_delete_confirm = "确定删除交易？"; //"Confirm deal deletion ?"
$l_j_type_create = "您必须输入一个新类型名称来创建它！"; //"You have to enter a new type name to create it !"
$l_j_type_update = "您必须选择一个类型并输入一个新名称来更新它！"; //"You have to select a type and enter a new name to update it !"
$l_j_type_delete = "您必须选择一个类型来检查或者删除！"; //"You have to select a type to check or delete!"
$l_j_status_create = "您必须输入一个新状态名称来创建它！"; //"You have to enter a new status name to create it !"
$l_j_status_update = "您必须选择一个状态并输入一个新名称来更新它！"; //"You have to select a status and enter a new name to update it !"
$l_j_status_delete = "您必须选择一个状态来检查或者删除！"; //"You have to select a status to check or delete !"
$l_deal_display = "显示交易列表选项"; //"Display options for deals list"
$l_parentdeal_display = "显示父交易列表选项"; //"Display options for parent deals list"
$l_invoice_display = "显示发票列表选项"; //"Display options for invoice list"
$l_invoice_ttc = "含税金额"; //"Tax included amount"
$l_invoice_number = "编号"; //"Number"
$l_category1 = "类别"; //"Category"
$l_number = "号码"; //"Number"
$l_invoice_date = "日期"; //"Date"/
$l_type_label = "类型"; //"Type"
$l_label = "标签"; //"Label"/
$l_invoice_ht = "完税金额"; //"Tax exlusion amount"/
$l_invoice_status = "状态"; //"Status"
$l_invoice_label = "标签"; //"Label"

