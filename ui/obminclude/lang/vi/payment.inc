<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Tiếng Việt                                               //
//     - File     : payment.inc                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_label_start = "Nhãn bắt đầu";
$l_amount = "Số tiền";
$l_expected_date = "Ngày dự kiến";
$l_type = "Loại hình thanh toán";
$l_inout = "Loại";
$l_both = "Cả hai";
$l_credit_memo = "Giấy báo thanh toán Credit memo";
$l_pick_paymentkind = "Chọn loại hình thanh toán";
$l_kind = "Kiểu";
$l_pick_account = "Chọn một tài khoản";
$l_payment_account = "Tài khoản ";
$l_paid = "Thanh toán";
$l_unpaid = "Không thanh toán";
$l_already_paid = "Đã thanh toán";
$l_kind_manage = "Quản lý loại hình thanh toán";
$l_kind_label = "Nhãn";
$l_kind_short_label = "Nhãn ngắn";
$l_no_invoice = "Không có hóa đơn nào liên quan đến thanh toán này";
$l_invoice_list = "Các hóa đơn liên quan đến thanh toán này";
$l_already_used_share = "Một phần của Thanh toán đã được sử dụng";
$l_payment_infos = "Thông tin ve thanh toán";
$l_insert= "Chèn thêm thanh toán";
$l_update = "Cập nhật thanh toán";
$l_delete = "Xóa thanh toán";
$l_delete_confirm = "Xác nhận việc xóa thanh toán?";
$l_delete_invoice = "Xóa";
$l_kind_delete = "Xóa loại thanh toán";
$l_payment_infos = "Xem thanh toán";
$l_payment_mod = "Sửa thanh toán";
$l_search_invoice ="Tìm kiếm hóa đơn";
$l_select_invoice = "Sử dụng";
$l_affect_invoices = "Gán hóa đơn cho thanh toán này";
$l_duplicate = "Bản sao thanh toán";
$l_no_display = "Điền vào các trường tìm kiếm và nhấn <Tìm> để lấy các khoản thanh toán";
$l_found = "Các thanh toán đã tìm thấy.";
$l_no_found = "Không có thanh toán nào phù hợp với truy vấn của bạn";
$l_no_invoices_connected = "Không có hóa đơn kết nối đến việc chi trả đó";
$l_cant_delete = "Thanh toán không thể xóa!";
$l_can_delete = "Thanh toán có thể xóa.";
$l_invoice_added = "Kết nối thanh toán/hóa đơn đã được tạo";
$l_no_invoice_added = "Kết nối thanh toán / hóa đơn đã thất bại";
$l_fill_company = "Phải chọn công ty!";
$l_err_kind = "Loại hình thanh toán không được thêm vào!";
$l_err_del_kind = "Loại hình thanh toán không được xóa!";
$l_fill_kind = "Phải chọn loại thanh toán!";
$l_fill_kind_short_label = "Loại thanh toán nhãn ngắn phải được điền!";
$lj_no_number = "\\nSố lượng thanh toán không hợp lệ!";
$lj_check_amount = "\\nSố tiền này không hợp lệ!";
$lj_check_inout = "\\nBạn phải quyết định đó là một khoản thanh toán từ khách hàng hoặc nhà cung cấp trước khi gửi";
$lj_check_paymentkind = "Bạn phải chọn một loại thanh toán trước khi gửi!";
$lj_check_account = "Bạn phải chọn một tài khoản trước khi gửi!";
$l_no_amount = "Bạn phải nhập một số tiền để thanh toán";
$l_invalid_amount = "Số tiền cuối cùng bạn nhập không hợp lệ!"; 
$l_payment_options = "Tùy chọn hiển thị danh sách Thanh toán";
$l_not_enough = "Bạn không sử dụng tất cả số tiền thanh toán!<BR>";
$l_too_much = "Bạn đã sử dụng nhiều tiền hơn bạn thực sự có!<BR>";
$l_trouble_invoice = "Ít nhất một hóa đơn đã nhận được quá nhiều tiền.<BR>Trạng thái của nó sẽ trở thành \"trouble\".<BR>Bạn có chắc bạn muốn làm điều đó?<BR>";
$l_bank_anyway = "Ngân hàng Thanh toán";
$l_bank_again = "Khởi động lại phiên giao dịch với ngân hàng từ đầu";
$l_check_account = "Bạn phải chọn một tài khoản!";
$l_check_inout = "Bạn phải lựa chọn giữa trả Thanh toán khách hàng hoặc Nhà cung cấp!";
$l_j_fill_kind_label = "Tên kiểu thanh toán phải được điền!";
$l_fill_kind_label = "Tên kiểu thanh toán phải được điền!";
$l_fill_amount = "Việc thanh toán phải được nhập chính xác!";
$l_choose_csv = "Chọn file";
$l_j_fill_kind_short_label = "Nhãn ngắn của loại thanh toán phải được điền vào!";
$l_err_affected_invoice = "Số lượng ghi trên hoá đơn cao hơn số tiền còn lại cần thanh toán";
$l_err_total_payment_affected = "Tổng số tiền phải trả cao hơn so với thanh toán!";
$l_fill_inout = "Phải chọn loại : (+) $l_received ou (-) $l_emitted !";
$l_no_number = "Số thanh toán không hợp lệ!";
$l_invoice_options = "Tùy chọn để hiển thị danh sách hóa đơn";
$l_check_amount = "Số tiền không hợp lệ!";
$l_include_checked = "Chèn Thanh toán"; 
$l_check_paymentkind = "Bạn phải chọn một loại thanh toán!";

