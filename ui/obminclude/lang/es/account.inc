<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : account.inc                                              //
// 2006-02-24 Jaime E. Gomez H.   jgomez@skinatech.com                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_label_start = "Etiqueta de Inicio";
$l_bank = "Banco"; 
$l_number = "Número";
$l_balance = "Balance";
$l_initial_balance = "Balance de Creación";
$l_choose_balance_date = "Fecha de Cómputo de Balance";
$l_expected_date = "Fecha Esperada";
$l_amount = "Cantidad";

$l_include_expected = "Incluir operaciones esperadas ?";
$l_in_total ="Total Entrada";
$l_out_total = "Total Salida";
$l_total ="Balance Estimado";
$l_old_balance = "Balance Hoy";
$l_nb_expected_used = " operaciones esperadas usadas durante el cómputo.";
$l_expected = "esperado";
$l_used_payments = "Pagos usados para calcular el balance ";
$l_used_expected_payments = "Pagos estimados usados para calcular el balance";
$l_expected_payments = "Pagos estimados";
$l_payments = "Pagos";
$l_totals = "Totales";

// Actions
$l_insert = "Insertar Cuenta";
$l_update = "Actualizar Cuenta";
$l_delete = "Borrar Cuenta";
$l_delete_confirm = "Confirme eliminar esta cuenta?";
$l_compute_balance = "Calcular Balance";

// Messages 
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener las cuentas";
$l_found="cuentas encontradas.";
$l_no_found="No hay cuentas correspondientes a su búsqueda";
$l_delete_ok = "La cuenta ha sido borrada correctamente.";
$l_delete_error = "La cuenta NO ha sido borrada !";
$l_insert_ok = "La cuenta ha sido ingresada correctamente.";
$l_insert_error = "El ingreso de la cuenta ha fallado !";
$l_update_ok = "La cuenta ha sido actualizada correctamente.";
$l_update_error = "La actualización ha fallado !";
$l_impossible_deletion = " existen pagos asociados con esta cuenta. Eliminarla no es posible"; // debe ser usada con los pagos iniciando por...

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_check_number = "\\nEl número de la cuenta no es válido!";
$l_fill_number = "Es necesario proveer el número antes de enviar!";
$l_fill_bank = "Tiene que llenar el campo del banco antes de enviar!";
$l_confirm_no_bank="Esta cuenta no tiene banco asociado. Presiona OK para confirmar.";

// For the display parameters modification screen
$l_account_options = "Opciones de despliegue para lista de cuentas"; 
$l_payment_options = "Opciones de despliegue para lista de pagos"; 
