<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : payment.inc                                              //
// 2004-02-18 Jaime E. Gomez H.   kasandra@skina.com.co                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Referencias et textes
// payment only stuff
$l_label = "Referencia";
$l_label_start = "Inicio de Referencia";
$l_number = "Número";
$l_amount = "Cantidad"; 
$l_comment = "Comentario";
$l_date = "Fecha";
$l_expected_date = "Fecha Esperada";
$l_date_apres = "Fecha Posterior";
$l_date_avant = "Fecha Anterior";
$l_account = "Cuenta";
$l_type = "Tipo";
$l_inout = "Tipo";
$l_both = "Ambos"; 
$l_client = "Cliente";
$l_fournisseur = "Proveedor";
$l_pick_paymentkind = "escoja un tipo de pago";
$l_kind = "Tipo";
$l_pick_account = "escoja una cuenta";
$l_payment_account = "Cuenta ";
$l_status = "Estado";
$l_paid = "Cancelado";
$l_unpaid = "No Cancelado";
$l_choose_csv = "Escoja el Archivo";
$l_include_checked = "Incluya los pagos realizados";

// invoice connected :
$l_add_invoices = "Asociar a factura";
$l_invoice_label = "Referencia de la Factura";
$l_invoice_date = "Fecha de la Factura";
$l_invoice_TTC = "Cantidad de la Factura";
$l_invoice_HT = "Cantidada HT";
$l_invoice_number = "Factura Número"; 
$l_invoice_status = "Estado";
$l_invoice_not_paid_yet = "Cantidad no pagada ANTES";
$l_invoice_still_not_paid = "Cantidad no pagada AHORA";
$l_invoice_share = "Porción afectada";
$l_invoice_inout = "Tipo Factura";
$l_invoice_paid = "Cantidad TTC cancelada";
$l_already_used_share = "Parte del pago ya usada ";
$l_invoice_company = "relacionar a la compañía" ;
$l_deal = "Negocio";
$l_company = "Compañía";

// Titres 
$l_payment = "Pago";
$l_payment_infos = "Información acerca de los pagos ";
$l_entrytemp_date = "Fecha";
$l_entrytemp_type = "Tipo";
$l_entrytemp_amount = "Cantidad";
$l_entrytemp_realdate = "Fecha de Vencimiento";
$l_entrytemp_label = "Referencia";
$l_entrytemp_comment = "Comentario";

// Actions 
$l_insert= "Ingresar Pago";
$l_update = "Actualizar Pago";
$l_delete = "Borrar Pago";
$l_bank = "Banco";
$l_delete_confirm = "Confirma la eliminación del este pago ?";
$l_error_which = "Error : La variable which NO ES CORRECTAMENTE ASIGNADA... Por favor revise con su administrador" ;
$l_payment_infos = "Ver Pago";
$l_payment_mod = "Modificar Pago";
$l_search_invoice ="Buscar Facturas";
$l_select_invoice = "Use";
$l_affect_invoices = "Afectar Facturas por este Pago";
$l_duplicate = "Duplique este Pago";
$l_break_asso = "Eliminar las asociaciones entre este pago y las facturas";
$l_break_asso_strong_confirm = "Atencion !! Este pago ya esta en caja y ".
"tiene varias factures.<br> Si confirma, este pago y todas ".
"las facturas serán consideradas como NO pagas !";
$l_break_asso_soft_confirm = "Atencion !! Este pago ya esta en caja <br>".
"Si confirma, este pago y todas las facturas asociadas serán consideradas".
"como NO pagas !";
$l_break_asso_easy_confirm = "caso simple";
$l_break_asso_go = "Continuar";
$l_payment_break_asso = "Selección";
$l_refresh = "Calcular";

// Messages 
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener los pagos";
$l_found="pagos encontrados.";
$l_no_found="No hay pagos correspondientes a su búsqueda";
$l_no_invoices_connected = "No hay Facturas conectadas a este Pago";
$l_invoices_connected = "Factura(s) asociadas(s) a este Pago";
$l_no_invoices = "ninguna factura encontrada";
$l_invoices_found = " Factura(s) correspondiente(s) a su búsqueda";
$l_delete_ok = "El pago ha sido borrado exitosamente.";
$l_delete_error = "La eliminación del pago ha fallado !";
$l_insert_ok = "El pago ha sido inserted exitosamente.";
$l_insert_error = "El ingreso del pago ha fallado !";
$l_update_ok = "El pago ha sido actualizado exitosamente.";
$l_update_error = "La actualización del pago ha fallado !";
$l_query_error = "Problemas ejecutando la búsqueda...";
$l_insert_paymentinvoice_ok = "La asociación Pago/Factura ha sido exitosa";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_no_number = "\\nEl número de pago number no es válido!";
$l_check_amount = "\\nLa cantidad no es válida!";
$l_check_inout = "\\nDebe decidir si el pago es de un cliente o de un proveedor antes de enviar";
$l_check_paymentkind = "Debe escoger un tipo de pago antes de enviar!";
$l_check_account = "Debe escoger una cuenta antes de enviar!";
$l_no_amount = "Debe proveer una cantidad para el pago";
$l_invalid_amount = "La cantidad ingresada no es válido";

// For the display parameters modification screen
$l_payment_options = "Opciones de despliegue para lista de pagos"; 
$l_invoice_options = "Opciones de despliegue para listas de facturas";
$l_et_options = "Opciones de despliegue para lista de Operaciones Temporales";
$l_options_dis = "Opciones de despliegue" ;



// banking check messages : 
$l_not_enough = "No ha usado todo el pago !<BR>";
$l_too_much = "Ud. ha usado mas dinero que el que tiene actualmente!<BR>";
$l_trouble_invoice = "Al menos una factura ha recibido demasiado dinero.<BR>
	Su estado será \"problema\".<BR>Esta seguro que desea que haga eso ?<BR>";
$l_bank_anyway = "Consignar este Pago";
$l_bank_again = "Reinicie la consignación desde el principio";


// reconcile messages :
$l_reconcile = "P";
$l_nb_payments_to_reconcile = " Pagos a asociar";
$l_nb_entrytemp_to_reconcile = " Operaciones a asociar";
$l_file_error = "Error en acceso al archivo ";
$l_write_error = "Error de escritura del archivo";
$l_read_error = "Error en la lectura del archivo csv";
$l_remains_entrytemps = "Operaciones restantes en a realizar";
$l_restart_reconcile = "reiniciar la asociación";
$l_restart_import = "reiniciar el importe de archivo csv";
$l_import_ok = "continuar la ascociacion con estos aportes";
$l_reconcile_errors = "Errors en la asociación. ".
"las anterrioes modificaciones serán anuladas";
