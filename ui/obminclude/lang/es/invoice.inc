<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : invoice.inc                                              //
// 2004-02-18 Jaime E. Gomez H.   kasandra@skina.com.co                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Referencias et textes
// invoice only
$l_label = "Referencia";
$l_label_start = "Inicio de Referencia";
$l_number = "Número";
$l_amount = "Cantidad";
$l_geninvoice = "Características";
$l_amount_HT = "Cantidad HT";
$l_amount_TTC = "Cantidad TTC";
$l_comment = "Comentario";
$l_deal="Negocio";
$l_company = "Compañía";
$l_status="Estado";
$l_date="Fecha";
$l_date_apres = "Despues";
$l_date_avant = "Antes";
$l_paid = "Cantidad TTC pagado";
$l_pick_deal = "Escoja un negocio";
$l_inout = "Tipo";
$l_both = "Ambos"; 
$l_client = "Cliente";
$l_fournisseur = "Proveedor";
$l_tax_rate="tasa de impuestos";
$l_compute_tax="calcule total impuestos";
$l_include_archive="archivos incluidos";
$l_archive_invoices="A";
$l_archive_update="actualizaciones de archivos";
$l_archive_ok="facturas archivadas exitosamente";
$l_archive_number=" facturas archivadas";

// deal
$l_no_deal = "no hay negocios relacionados con esta factura";
$l_list_deal = "negocios relacionados con esta factura";
$l_invoice_deal_company ="vinculado a compañía";
$l_invoice_deal_label = "Referencia del Negocio";
$l_invoice_deal_number = "Negocio Número";
$l_invoice_deal_category_label = "Categoría del Negocio";
$l_invoice_company = "Compañía del Negocio";
$l_invoice_dealtodo = "Tareas del Negocio";
$l_invoice_deal_archive = "Incluir los archivos ?";
$l_invoice_dealtype_label = "tipo de negocio";
$l_invoice_dealstatus = "estado del negocio";
$l_invoice_deal_datealarm = "fecha de alarma del negocio";
$l_no_deals_related_error = "No puede ingresar pagos a esta factura porque no tiene negocios relacionados.";
$l_last_deal_remove_error = "No puede borrar mas negocios: permanece solo uno relacionado a esta factura, y aun existen pagos .. <br>
	Borre primero los pagos.";

//payment
$l_no_payment = "no hay pagos relacionados con esta factura";
$l_list_payment = "pagos  relacionados con esta factura";
$l_invoice_payment_label = "Referencia del Pago";
$l_invoice_payment_number = "Pago Número";
$l_invoice_payment_date = "Fecha del Pago";
$l_invoice_payment_amount = "Monto del Pago";
$l_invoice_payment_expect_date="fecha de pago esperado";

// Titres
$l_invoice = "Factura";
$l_invoice_infos = "Información de la factura";
$l_remaining_amount = "Cantidad no usada";
$l_to_use_amount = "Cantidad para afectar esta factura";
$l_affect = "Afectar";

// Actions 
$l_select_nb_deals = "Cuantos negocios estarán vinculados a esta factura ?";
$l_select_deals = "escoja el número de negocios";
$l_insert= "Ingresar Factura";
$l_update = "Actualizar Factura";
$l_duplicate = "Duplicar esta Factura";
$l_delete = "Borrar Factura";
$l_delete_confirm = "Confirme la eliminación de esta factura ?";
$l_no_number_confirm = "Esta por crear una factura sin número.\\nPresione OK para confirmar.";
$l_error_which = "Error : La variable which NO ES CORRECTAMENTE ASIGNADA... Por favor revise con su administrador" ;
$l_invoice_del_deal = "Borrar";
$l_invoice_del_payment = "Borrar";
$l_invoice_infos = "Ver Factura";
$l_add_deal = "Adicionar negocio";
$l_add_deal_chosen = "Adicionar negocios escogidos";
$l_del_deal = "Borrar negocio";
$l_del_deal_chosen = "Borrar negocios escogidos";
$l_deals_todel = "Aqui los negocios retirables de esta factura" ;
$l_add_payment = "Adicionar pagos";
$l_add_payment_chosen = "Adicionar pagos escogidos";
$l_check_payment_chosen = "Revisar pagos escogidos";
$l_del_payment = "Borrar pago";
$l_del_payment_chosen = "Borrar pagos escogidos";
$l_payments_todel = "Aqui los pagos retirables de esta factura" ;
$l_invoice_mod = "Modificar Factura";

// Messages 
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener las facturas";
$l_no_deal_display = "Llene los campos de búsqueda y presione < Buscar > para obtener las negocios";
$l_no_payment_display = "Llene los campos de búsqueda y presione < Buscar > para obtener los pagos";
$l_found="no hay facturas encontradas.";
$l_no_found="No hay facturas correspondientes a su búsqueda";
$l_no_deal_found ="No hay negocios correspondientes a su búsqueda";
$l_no_payment_found ="No hay pagos correspondientes a su búsqueda";
$l_delete_ok = "La factura ha sido borrado exitosamente.";
$l_delete_error = "La eliminación de la factura ha fallado !";
$l_payments_exist = "Pagos ya existen !";
$l_remove_deal_ok = "La eliminación del negocio(s) se ha hecho exitosamente!";
$l_remove_deal_error = "La eliminación del negocio(s) ha fallado!";
$l_insert_ok = "La factura ha sido ingresada exitosamente.";
$l_insert_error = "El ingreso de la factura ha fallado !";
$l_insert_deal_ok = "El ingreso del negocio(s) se ha hecho exitosamente!";
$l_insert_deal_error = "El ingreso del negocio(s) ha fallado!";
$l_update_ok = "La factura ha sido actualizada exitosamente.";
$l_update_error = "La actualización de la factura ha fallado !";
$l_query_error = "Problemas ejecutando la búsqueda...";
$l_invalid_payment = "La cantidad de pagos es mayor que el total ";
$l_too_much_money = "El total de pagos es mayor que el total de la factura.".
" Si continua, la factura alcanzará el estado de 'probleam'";
$l_error_try_again = "Hay errores !! Por favor comience desde el principio";
$l_try_again = "Intente nuevamente !";

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
$l_rate_nan = "La tasa de IVA es inválida !";
$l_ht_nan = "salida de los impuestos no es válida";

// For the display parameters modification screen
$l_invoice_options = "Opciones de despliegue de listas de facturas"; 
$l_deal_options = "Opciones de despliegue de Negocios"; 
$l_options_dis = "Opciones de despliegue" ;

// default invoice numbering policy :
// this is used as a date format
$default_invoice_numbering = "Y"; // we use only year

// default tax rate 
// used like that : TTC = HT * $default_tax
$default_tax= 1.16;

$l_fill_date = "\\n Debe ingresar la Fecha  antes de enviar.";
$l_fill_label = "\\n Debe ingresar la Referencia antes de enviar.";
$l_invalid_date = "\\nFormato de Fecha Inválido (MM/JJ/AAAA or AAAA-MM-JJ)";
$l_invalid_payment = "La cantidad tomada del pago es mayor que el total";
$l_too_much_money = "La cantidad total de pagos es mayor que el total de la factura. Si continua, la facturar alcanzará el estado de 'Problema'";
$l_error_try_again = "Hay errores !! Por favor reinicie desde el principio";
$l_try_again = "Intente de nuevo !";
