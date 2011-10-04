<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : payment.inc                                              //
// 2006-02-24 Jaime E. Gomez H.   jgomez@skinatech.com                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
// payment only stuff
$l_label_start = "Inicio de Referencia";
$l_amount = "Cantidad"; 
$l_expected_date = "Fecha Esperada";
$l_type = "Tipo";
$l_inout = "Tipo";
$l_both = "Ambos"; 
$l_pick_paymentkind = "escoja un tipo de pago";
$l_kind = "Tipo";
$l_pick_account = "escoja una cuenta";
$l_payment_account = "Cuenta ";
$l_paid = "Cancelado";
$l_unpaid = "No Cancelado";
$l_choose_csv = "Choisir ce fichier";
$l_include_checked = "Inclure les Paiements pointés";

// invoice connected :

$l_invoice_label = "Referencia de la Factura";
$l_invoice_date = "Fecha de la Factura";
$l_invoice_ttc = "Cantidad de la Factura";
$l_invoice_ht = "Cantidad HT";
$l_invoice_number = "Factura Número"; 
$l_invoice_status = "Estado";
$l_invoice_not_paid_yet = "Cantidad no pagada ANTES";
$l_invoice_still_not_paid = "Cantidad no pagada AHORA";
$l_invoice_share = "Porción afectada";
$l_invoice_inout = "Type";
$l_invoice_paid = "Montant déjà payé";
$l_already_used_share = "Parte del pago ya usada ";


// Titres 
$l_payment_infos = "Información acerca de los pagos ";
$l_entrytemp_date = "Fecha";
$l_entrytemp_type = "Tipo";
$l_entrytemp_amount = "Cantidad";
$l_entrytemp_realdate = "Fecha de Pago";
$l_entrytemp_label = "Etiqueta";
$l_entrytemp_comment = "Commentario";

// Actions 
$l_insert= "Ingresar Pago";
$l_update = "Actualizar Pago";
$l_delete = "Borrar Pago";
$l_delete_invoice = "Borrar";
$l_bank = "Llevar el Pago a caja";
$l_delete_confirm = "Confirma la eliminación del este pago ?";
$l_payment_infos = "Ver Pago";
$l_payment_mod = "Modificar Pago";
$l_search_invoice ="Buscar Facturas";
$l_select_invoice = "Usar";
$l_affect_invoices = "Afectar Facturas por este Pago";
$l_duplicate = "Duplique este Pago";
$l_break_asso = "Suprimir las asociaciones entre estos pagos y las facturas";
$l_break_asso_strong_confirm = "Alerta !! !! Este pago esta en caja y  ".
"esta relacionado con varias facturas.<br> Si confirma la acción, este pago y TODAS ".
"las facturas seran consideradas como NO pagadas !";
$l_break_asso_soft_confirm = "Alerta !! !! Este pago esta en caja y  ".
"esta relacionado con varias facturas.<br> Si confirma la acción, este pago y TODAS ".
"las facturas seran consideradas como NO pagadas !";
$l_break_asso_easy_confirm = "cas simple";
$l_break_asso_go = "Continuar";
$l_payment_break_asso = "Selección";
$l_refresh = "Calcular";

// Messages 
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener los pagos";
$l_found="pagos encontrados.";
$l_no_found="No hay pagos correspondientes a su búsqueda";
$l_no_invoices_connected = "No hay Facturas asociadas a este Pago";
$l_invoices_connected = "Facturas asociadas con este Pago";
$l_no_invoices = "No se encuentran facturas";
$l_invoices_found = " Factura(s) correspondiente(s) a su búsqueda";
$l_cant_delete = "El pago NO puede ser borrado !";
$l_can_delete = "El pago puede ser borrado.";
$l_delete_ok = "El pago ha sido borrado exitosamente.";
$l_delete_error = "La eliminación del pago ha fallado !";
$l_insert_ok = "El pago ha sido inserted exitosamente.";
$l_insert_error = "El ingreso del pago ha fallado !";
$l_update_ok = "El pago ha sido actualizado exitosamente.";
$l_update_error = "La actualización del pago ha fallado !";
$l_insert_paymentinvoice_ok = "La asociación entre Pagos y Facturas fue realizada correctamente";

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
$l_invoice_options = "Opciones de despliegue para lista de facturas"; 
$l_et_options = "Opciones de despliegue para lista de Operaciones temporales"; 
$l_options_dis = "Opciones de despliegue" ;


// banking check messages : 
$l_not_enough = "No ha usado todo el pago !<BR>";
$l_too_much = "Ud. ha usado mas dinero que el que tiene actualmente!<BR>";
$l_trouble_invoice = "Al menos una factura ha recibido demasiado dinero.<BR> Su estado será \"problema\".<BR>Esta seguro que desea que haga eso ?<BR>";
$l_bank_anyway = "Consignar este Pago";
$l_bank_again = "Reinicie la consignación desde el principio";

// reconcile messages :
$l_reconcile = "P";
$l_nb_payments_to_reconcile = " Pagos para asociar";
$l_nb_entrytemp_to_reconcile = " Operaciones para asociar";
$l_file_error = "Error abriendo el archivo";
$l_write_error = "Error escribiendo en el archivo";
$l_read_error =  "Error leyendo el archivo csv";
$l_remains_entrytemps = "Aún quedan Operaciones para asociar";
$l_restart_reconcile = "reiniciar la asociación";
$l_restart_import = "reiniciar la importación del archivo csv";
$l_import_ok = "continuar asociaciones con estos datos";
$l_reconcile_errors = "Errores en la asociacion. ".
"Las últimas modificaciones ha sido canceladas";
