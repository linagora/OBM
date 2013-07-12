<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language :Chinese                                                  //
//     - File     : company.inc                                              //
// 1999-01-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-09T15:20:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T14:02:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T14:20:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T14:49:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T14:50:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T14:58:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T15:01:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T15:04:37+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T15:12:07+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T15:14:07+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T15:23:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T15:34:10+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T15:43:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T15:44:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T16:32:41+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:53:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:00:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:01:06+02:00 using OBMtranslation (0.4.1)

$l_number = "编号";  //"Number"
$l_company_name = "公司名称";  //"Company Name"
$l_type = "类型";  //"Type"
$l_activity = "活动";  //"Activity"
$l_nafcode = "NAF代码";  //"NAF Code"
$l_market = "市场部经理";  //"Marketing manager"
$l_web = "网络";  //"Web"
$l_email = "电子邮件";  //"E-Mail"
$l_coord = "细节";  //"Details"
$l_vat = "EU VAT";  //"EU VAT"
$l_siret = "SIRET";  //"SIRET"
$l_contact_new = "新建联系人";  //"New Contact"
$l_deal_new = "新建交易";  //"New Deal"
$l_project_new = "新建项目";  //"New Projet"
$l_contract_new = "新建合约";  //"New Contract"
$l_internal = "内部";  //"Internal"
$l_naf_manage = "NAF代码管理";  //"NAF Code Management"
$l_naf_exist = "存在NAF代码";  //"Existing NAF codes"
$l_naf_new = "NAF新代码";  //"New NAF Code"
$l_naf_label = "标签";  //"Label"
$l_naf_code = "代码";  //"Code"
$l_naf_istitle = "这是个标题"; //"It is a title"
$l_add = "添加"; //"Add"
$l_insert = "添加公司";  //"Insert Company"
$l_update = "更新公司";  //"Update Company"
$l_checkdelete = "检查或删除公司";  //"Check or Delete Company"
$l_delete = "删除公司";  //"Delete Company"
$l_naf_update = "更新NAF代码";  //"Update NAF Code"
$l_naf_checkdelete = "检查或删除NAF代码";  //"Check or Delete NAF Code"
$l_naf_delete = "删除NAF代码";  //"Delete NAF Code"
$l_naf_insert = "添加NAF代码";  //"Insert NAF Code"
$l_select_company = "选择公司";  //"Select Company"
$l_err_act_empty = "您必须填写公司活动！";  //"You must fill in the company activity !"
$l_cat_no = "没有已定义的类别";  //"No categories defined"
$l_no_display = "在搜索区域输入关键字，并点击<查找>来搜索公司";  //"Fill in the search fields and press < Find > to retrieve the companies"
$l_found = "个公司已找到。";  //"companies found."
$l_no_found = "未发现与查询匹配的公司";  //"No company matching the query has been found!"
$l_check_samecompany = "这些公司与输入类似";  //"These companies seem similar to this new entry !"
$l_link_contact = "该公司有联系人";  //"The company has contacts"
$l_link_contact_no = "该公司无联系人";  //"The company has no contacts"
$l_link_deal = "该公司参与了一些交易";  //"The company is involved in some deals"
$l_link_deal_no = "该公司没有参与任何交易";   //"The company is not involved in any deal"
$l_link_contract = "该公司参与了一些合约";  //"The company is involved in some contracts"
$l_link_contract_no = "该公司没有参与任何合约"; //"The company is not involved in any contract"
$l_link_project = "该公司参与了一些项目";  //"The company is involved in some projects"
$l_link_project_no = "该公司没有参与任何项目";  //"The company is not involved in any projects"
$l_link_invoice = "该公司与一些发票有关联";  //"The company is involved in some invoices"
$l_link_invoice_no = "该公司没有关联任何发票"; //"The company is not involved in any invoices"
$l_cant_delete = "该公司无法被删除！";  // "The company cannot be deleted !"
$l_can_delete = "该公司可以被删除。";  //"The company can be deleted."
$l_naf_no = "无已定义的NAF代码";  //"No NAF Code defined"
$l_naf_link_company = "这些公司与此NAF代码有关联";  //"These companies are referenced by the NAF code"
$l_naf_link_company_no = "没有公司关联此NAF代码";  //"No company linked to NAF code"
$l_naf_cant_delete = "此NAF代码无法被删除！";  //"The NAF code cannot be deleted !"
$l_naf_can_delete = "此NAF代码可以被删除。";  //"NAF Code can be deleted."
$l_delete_confirm = "确认删除此公司？";   //"Confirm company deletion  ?"
$l_fill_name = "您必须在提交之前输入名称";  //"You have to enter the Name before submitting."
$l_j_check_zip = "邮编无效。";  //"The zip code is not valid."
$l_j_check_phone = "电话号码无效。"; // "The phone number is not valid."
$l_j_check_fax = "传真号码无效。";  //"The fax number is not valid."
$l_j_check_email = "邮箱地址无效。请验证域名。";  //"The email address is not valid. Verify domain"
$l_j_check_web = "网络地址无效。";  //"The web address is not valid."
$l_j_select_company = "您必须选择一个公司！";  //"You have to select a company !"
$l_j_naf_create = "您必须输入一个NAF代码来创建它！";  //"You have to enter a NAF code to create it !"
$l_company_display = "显示公司列表选项";  //"Display options for companies lists"
$l_phone_labels = array ('WORK_VOICE' => '电话', 'CELL_VOICE' => '手机', 'WORK_FAX' => '传真', 'OTHER' => '其他联系方式');  //'WORK_VOICE' => 'Phone', 'CELL_VOICE' => 'Mobile phone', 'WORK_FAX' => 'Fax', 'OTHER' => 'Other phone')
$l_email_labels = array ('INTERNET' => '电子邮件', 'OTHER' => '其他');  //('INTERNET' => 'email', 'OTHER' => 'Other')
$l_address_labels = array ('HQ' => '地址', 'OTHER' => '其他');  //('HQ' => 'Headquarters', 'OTHER' => 'Other')
$l_website_labels = array ('URL' => '网站', 'OTHER' => '其他');  //('URL' => 'Website', 'OTHER' => 'Other')
$l_j_naf_update = "验证之前，您必须选择一个NAF代码并更新。";  //"You have to select a NAF code and to update before validating."
$l_j_cat_deep = "此类别不能超过三个深度级别";  //"The categories cannot have more than three depth levels"
$l_j_naf_delete = "请选择NAF代码来验证或删除！";  //"Select a NAF code to validate or to delete !"
$l_comment = "评论";  //"Comment"

