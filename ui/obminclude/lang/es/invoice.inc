<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : invoice.inc                                              //
// 2006-02-24 Jaime E. Gomez H.   jgomez@skinatech.com                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// invoice only 
$l_label_start = "Inicio de Referencia";
$l_amount = "Cantidad";
$l_amount_ht = "Cantidad HT";
$l_amount_ttc = "Cantidad TTC";
$l_expiration_date = "Fecha de Expiración";
$l_payment_date = "Fecha de Pago";
$l_expected_date = "Fecha Esperada";
$l_paid = "Pagado";
$l_waiting = "Esperando";
$l_pick_deal = "Escoja un negocio";
$l_inout = "Tipo";
$l_both = "Ambos"; 
$l_tax_rate ="tasa de impuestos";
$l_compute_tax = "calcule total impuestos";
$l_potential = "Potencial";
$l_include_archive = "archivos incluidos";

// payment
$l_no_payment = "no hay pagos relacionados con esta factura";
$l_list_payment = "pagos  relacionados con esta factura";

// Titres
$l_invoice_infos = "Información de la factura";
$l_remaining_amount = "Cantidad no usada";
$l_to_use_amount = "Cantidad para afectar esta factura";
$l_affect = "Afectar";

// Actions 
$l_insert= "Ingresar Factura";
$l_update = "Actualizar Factura";
$l_duplicate = "Duplicar Factura"; 
$l_delete = "Borrar Factura";
$l_delete_confirm = "Confirme la eliminación de esta factura ?";
$l_no_number_confirm = "Esta por crear una factura sin número.\\nPresione OK para confirmar.";
$l_invoice_del_deal = "Borrar Negocio";
$l_invoice_del_paiement = "Borrar pago";
$l_invoice_infos = "Ver Factura";
$l_add_payment = "Adicionar pagos";
$l_add_payment_chosen = "Adicionar pagos escogidos";
$l_check_payment_chosen = "Revisar pagos escogidos";
$l_del_payment = "Borrar pago";
$l_del_payment_chosen = "Borrar pagos escogidos";
$l_payments_todel = "Aqui los pagos retirables de esta factura" ;
$l_invoice_mod = "Modificar Factura";

// Messages 
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener las facturas";
$l_no_deal_display =  "Llene los campos de búsqueda y presione < Buscar > para obtener las negocios";
$l_no_payment_display = "Llene los campos de búsqueda y presione < Buscar > para obtener los pagos";
$l_found="no hay facturas encontradas.";
$l_no_found="No hay facturas correspondientes a su búsqueda";
$l_no_deal_found ="No hay negocios correspondientes a su búsqueda";
$l_no_payment_found ="No hay pagos correspondientes a su búsqueda";
$l_payments_exist = "Hay pagos asociados con esta factura !";
$l_invalid_payment = "La cantidad tomada de un pago es mayor que su totalidad";
$l_too_much_money = "El total de pagos es mayor que la factura. Si continúa, la factura será puesta en estado de 'con problemas'";
$l_error_try_again = "Hay errores !! Por favor reinicie desde el principio";
$l_try_again = "Intente de Nuevo !";

$l_link_payment = "Estos pagos estan conectados con esta factura";
$l_link_payment_no = "La Factura no tiene ninguna pago";
$l_cant_delete = "La Factura NO puede ser borrado !";
$l_can_delete = "La Factura puede ser borrado.";

$l_fill_label = "Debe ingresar la Etiqueta.";
$l_fill_number = "El número debe ser ingresado (para este estado) !";
$l_fill_company = "La compañía debe ser llenada !";
$l_fill_date = "Debe ingresar la Fecha  antes de enviar.";
$l_fill_inout = "Debe seleccionar una clase : $l_client o $l_supplier !";
$l_fill_amount = "La cantidad debe ser ingresada correctamente !";
$l_error_number_exist = "Este número de factura ya esta siendo usada !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_check_number = "\\nEl número de la factura no es válido!";
$l_check_money_ht = "\\nLa cantidad sin cobros no es válido!";
$l_check_money_ttc = "\\nLa cantidad de cobros incluida no es válida!";
$l_check_amounts = "\\nLa cantidad de cobros incluida debe ser mayor que la cantidad sin cobros!";
$l_check_inout = "\\nDebe escoger un Tipo antes de enviar";
$l_changing_inout = "No puede cambiar el tipo de una factura si hay \\n negocios vinculados a ella...";
$l_no_number="\\nFactura debe tener un número!";
$l_check_deal = "Debe escoger un Negocio antes de enviar";
$l_select_deals = "Debe escoger un número de negocios";
$l_rate_nan ="tasa de impuestos no es válida";
$l_ht_nan="la cantidad de impuestos no es válida";

// For the display parameters modification screen
$l_invoice_display = "Opciones de despliegue de listas de facturas"; 

// default invoice numbering policy :
$default_invoice_numbering = "Y"; // we use only year

// Otros Skina 

$l_inout = "Clase";
$l_both = "Ambos"; 
$l_no_deals_related_error = "No puede agregar pagos a esta factura porque no esta vinculado con negocios";
$l_last_deal_remove_error = "Ud no puede remover mas negocios : solo queda uno relacionado con esta factura y quedan aun pagos...<br>Remueva primero los pagos.";
$l_invoice_dealtype_label = "tipo de negocio";
$l_invoice_dealstatus = "estado del negocio";
$l_invoice_deal_datealarm = "fecha de alarma del negocio";
$l_include_archive = "incluir archivos";
$l_compute_tax="cálcule la cantidad total de impuestos";
$l_tax_rate="tasa de impuestos";
$l_invoice_deal_company="vinculado a la compañía";
$l_invoice_deal_archive="vinculado al archivo";


$l_add_deal = "Adicionar negocio";
$l_add_deal_chosen = "Adicionar negocios escogidos";
$l_del_deal = "Borrar negocio";
$l_del_deal_chosen = "Borrar negocios escogidos";
$l_deals_todel = "Aqui los negocios retirables de esta factura" ;
$l_select_nb_deals = "Cuantos negocios estarán vinculados a esta factura ?";
$l_select_deals = "escoja el número de negocios";
$l_error_which = "Error : La variable which NO ES CORRECTAMENTE ASIGNADA... Por favor revise con su administrador" ;
$l_payment_exist = "El Pago existe !";
$l_fill_date = "\\n Debe ingresar la Fecha antes de enviar.";
$l_fill_label = "\\n Debe ingresar la Referencia antes de enviar.";
$l_invalid_date="\\nFormato de Fecha Invalida (DD/MM/AAAA o AAAA-MM-DD)";
