<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : deal.inc                                                 //
// 2004-02-18 Jaime E. Gomez H.   kasandra@skina.com.co                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_date_init = "Fecha de inicio";
$l_parentdeal="Negocio original (Padre)";
$l_kind = "Tipo";
$l_marketing_manager = "Gerente de Mercadeo";
$l_technical_manager = "Gerente Técnico";
$l_date_proposal = "Fecha de Propuesta";
$l_amount = "Cantidad";
$l_hitrate = "Rata de Exito";
$l_state = "Estado";
$l_date_alarm = "Fecha de Alarma";
$l_date_comment = "Fecha de Comentario";

// Labels and texts
$l_infos = "Información";
$l_minus = "-";
$l_plus = "+";
$l_order = "Ordenar";
$l_amount_in = "Cantidad de ingresos";
$l_amount_out = "Cantidad de salidas";
$l_potential = "Potencial";
$l_label_start = "Referencia de Inicio";
$l_proposition = "Propuesta";
$l_show_archive = "Mostrar archivos";
$l_date_alarm_after = "Fecha de Alarma posterior";
$l_date_alarm_before = "Fecha de Alarma anterior";
$l_after = "Después";
$l_before = "Antes";
$l_last_update = "Última actualización en";
$l_at = "a las";
$l_deal_select_company = "Seleccione la compañía a quien el negocio debe ser asignado";
$l_deal_select_parentdeal = "Seleccione el negocio original (Padre) a quien este negocio debe afectar.";
$l_affect_deal= "Cambie Negocio Original(Padre)";
$l_no_invoice = "No hay facturas relacionadas";
$l_see_archived_invoices = "Ver facturas archivadas tambien";
$l_kind_manage = "Manejo del Tipo de Negocio";
$l_kind_exist = "Tipos Existentes";
$l_kind_new = "Nuevo Tipo";
$l_status_manage = "Manejo del Estado del Negocio";
$l_status_exist = "Estados Existentes";
$l_status_new = "Nuevo Estado";
$l_category_manage = "Manejo de la Categoría del Negocio";
$l_category_exist = "Categorías Existentes";
$l_category_new = "Nueva Categoría";
$l_category_label="Referencia";
$l_category_minilabel="Mini Referencia";
$l_invoice_number = "Número";
$l_invoice_label = "Referencia";
$l_invoice_ht = "Montant HT";
$l_invoice_ttc = "Montant TTC";
$l_invoice_status = "Estado";
$l_invoice_date = "Fecha";

$l_parentdeal_deals = "Negocios pertenecientes a este Negocio Original(Padre)";

// Actions
$l_select_deal = "Seleccionar Negocio";
$l_insert = "Ingresar el Negocio";
$l_update = "Actualizar el  Negocio";
$l_delete = "Borrar el Negocio";
$l_archive_update = "Actualizar Archivos";
$l_select_company = "Seleccione una Compañía";
$l_affect = "Afectar";

$l_insert_parent = "Ingresar el negocio Original(Padre)";
$l_update_parent = "Actualizar el negocio Original(Padre)";
$l_delete_parent = "Borrar negocio Original(Padre)";

$l_kind_checkdelete = "Revisar or Borrar Tipo";
$l_kind_delete = "Borrar Tipo";
$l_kind_update = "Actualizar Tipo";
$l_kind_insert = "Ingresar Tipo";
$l_status_checkdelete = "Revisar or Borrar Estado";
$l_status_delete = "Borrar Estado";
$l_status_update = "Actualizar Estado";
$l_status_insert = "Ingresar Estado";
$l_cat_checkdelete = "Revisar or Borrar Categoria";
$l_cat_delete = "Borrar Categoría";
$l_cat_update = "Actualizar Categoria";
$l_cat_insert = "Ingresar Categoría";

// Messages
$l_link_contract = "El negocio esta involucrado en estos contratos";
$l_link_contract_no = "El negocio NO esta involucrado en ningún contrato";
$l_link_project = "El negocio esta involucrado con estos proyectos";
$l_link_project_no = "El negocio NO esta involucrado en ningún proyecto";
$l_cant_delete = "El negocio NO puede ser borrado !";
$l_can_delete = "El negocio puede ser borrado.";

$l_err_parent_has_deal = "Negocios estan vinculados a este negocio Original(Padre), NO pueden ser borrados !";
$l_err_label_empty = "Debe ingresar Referencia de negocio Original(Padre) !";
$l_err_kind_empty = "Debe ingresar el tipo del negocio !";
$l_err_task_empty = "Debe ingresar la categoría del negocio !";
$l_err_market_empty = "Debe ingresar el Gerente de Mercadeo !";
$l_err_tech_empty = "Debe ingresar el Gerente Técnico !";
$l_err_hitrate = "La posibilidad de exito (hit rate) debe estar entre 0 - 100 !";
$l_updateaffect_ok = "El negocio ha sido exitosamente afectado al negocio original(padre)";
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener los  negocios";
$l_found = "negocios encontrados.";
$l_no_found = "No hay negocios correspondientes a su búsqueda!";
$l_found_parent = "negocios originales(padres) encontrados";
$l_no_found_parent ="No hay negocios originales(padres) correspondientes a su búsqueda!";

$l_delete_ok = "El negocio ha sido borrado exitosamente.";
$l_delete_error = "La eliminación of el negocio ha fallado !";
$l_insert_ok = "El negocio ha sido ingresado exitosamente.";
$l_insert_error = "El ingreso del negocio ha fallado !";
$l_update_ok = "El negocio ha sido actualizado exitosamente.";
$l_update_error = "La actualización del negocio ha fallado !";
$l_query_error = "Problemas ejecutando la búsqueda...";
$l_archive_ok = "Los archivos han sido actualizados exitosamente.";
$l_archive_error = "La actualización e los archivos ha fallado !";
$l_archive_number = "negocios ingresados en los archivos.";
$l_kind_insert_ok = "El tipo ha sido ingresado exitosamente.";
$l_kind_insert_error = "El ingreso del tipo ha fallado !";
$l_kind_update_ok = "El tipo ha sido actualizado exitosamente.";
$l_kind_update_error = "La actualización del tipo del negocio ha fallado !";
$l_kind_delete_error = "La eliminación del tipo ha fallado !";
$l_kind_delete_ok = "El tipo ha sido borrado exitosamente.";
$l_kind_can_delete = "El Tipo puede ser borrado !";
$l_kind_cant_delete = "El Tipo no puede ser borrado!";
$l_kind_link_deal = "El Tipo esta involucrado en estos negocios";
$l_kind_link_deal_no = "El Tipo NO esta involucrado en ningún negocio";
$l_status_insert_ok = "El estado ha sido ingresado exitosamente.";
$l_status_insert_error = "El ingreso del estado ha fallado !";
$l_status_update_ok = "El Estado ha sido actualizado exitosamente.";
$l_status_update_error = "El estado del negocio ha fallado !";
$l_status_delete_error = "La eliminación del estado ha fallado !";
$l_status_delete_ok = "El estado ha sido borrado exitosamente.";
$l_status_can_delete = "El Estado puede ser borrado";
$l_status_cant_delete = "El Estado no puede ser borrado";
$l_status_link_deal = "El Estado esta involucrado en estos negocios";
$l_status_link_deal_no = "El Estado NO esta involucrado en ningún negocio";
$l_cat_insert_ok = "La categoría ha sido ingresada exitosamente.";
$l_cat_insert_error = "El ingreso de la categoría ha fallado !";
$l_cat_update_ok = "La categoría ha sido actualizado exitosamente.";
$l_cat_update_error = "La actualización de la categoría ha fallado !";
$l_cat_delete_ok = "La categoría ha sido borrada exitosamente.";
$l_cat_delete_error = "El eliminación de la categoría ha fallado !";
$l_cat_can_delete = "La Categoría puede ser borrada";
$l_cat_cant_delete = "La Categoría NO puede ser borrada";
$l_cat_link_deal = "La Categoría esta involucrada en estos negocios";
$l_cat_link_deal_no = "La Categoría NO esta involucrada en ningún negocio";
$l_cat_link_project = "La Categoría esta involucrada en estos proyectos";
$l_cat_link_project_no = "La Categoría NO esta involucrada en ningún proyecto";
$l_cat_link_timetask = "La Categoría es usada en el manejo de tiempos";
$l_cat_link_timetask_no = "La Categoría NO es usada en el manejo de tiempos";

// JavaScript Messages (characters like \ have to be backslashed -> \\)
$l_fill_parentdeal = "Debe seleccionar un negocio original(padre) antes de enviar.";
$l_fill_label = "Debe ingresar la Referencia antes de enviar.";
$l_fill_number = "Debe ingresar el Número antes de enviar.";
$l_fill_datealarm = "Debe ingresar la Fecha de Alarma date antes de enviar.";
$l_fill_datebegin = "Debe ingresar Fecha de Inicio antes de enviar.";
$l_confirm_datealarm = "Fecha de Alarma esta en el pasado ! Confirma la acción ?";
$l_j_select_company = "Debe seleccionar una compañía!";
$l_invalid_date = "Formato de fecha inválido (MM/JJ/AAAA or AAAA-MM-JJ).";
$l_delete_confirm = "Confirma eliminación del negocio ?";

$l_j_kind_delete = "Debe escoger un tipo para revisar o borrar !";
$l_j_status_delete = "Debe escoger un estado para revisar o borrar !";
$l_j_category_delete = "Debe escoger una categoría para revisar o borrar !";
$l_j_kind_update = "Debe escoger un tipo e ingresar un nuevo nombre para actualizar !";
$l_j_status_update = "Debe escoger un estado  e ingresar un nuevo nombre para actualizar !";
$l_j_category_update = "Debe escoger un category e ingresar un nuevo nombre para actualizar !";
$l_j_kind_create = "Debe ingresar un nuevo nombre para crear el tipo !";
$l_j_status_create = "Debe ingresar un nuevo nombre para crear el estado !";
$l_j_category_create = "Debe ingresar un nuevo nombre para crear la categoría !";


// For the display parameters modification screen
$l_deal_display = "Opciones de despliegue de lista de negocios";
$l_parentdeal_display = "Opciones de despliegue de lista de facturas negocios Originales(Padre)";
$l_invoice_display = "Opciones de despliegue de lista de facturas";
$l_no_display_parent ="Llene los campos de búsqueda y presione < Buscar > para obtener negocios originales(padres)"; 
