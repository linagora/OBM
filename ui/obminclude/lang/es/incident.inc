<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : incident.inc                                             //
// 2006-02-24 Jaime E. Gomez H.   jgomez@skinatech.com                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Fields Referencia
$l_date = "Fecha";
$l_logger = "Gerente Técnico";
$l_owner = "Responsable";
$l_duration = "Tiempo tomado para solución";
$l_solution = "Solución encontrada";
$l_client_manager = "Gerente de Cliente";
$l_date_before ="Fecha Anterior";
$l_date_after ="Fecha Posterior";
$l_archive_new = "Para archivar";
$l_res_duration = "Tiempo para resolver";
$l_pri_manage = "Manejo de Prioridad de Incidente";
$l_pri_exist = "Prioridades Existentes";
$l_pri_new = "Nueva Prioridad";
$l_sta_manage = "Manejo del estado del Incidente";
$l_sta_exist = "Estados Existentes";
$l_sta_new = "Nuevo Estado";
$l_cat1_manage = "Administración de Categorías de Incidentes";
$l_cat1_exist = "Categorías Existentes ";
$l_cat1_new = "Nueva Categoría";

// Title 
$l_redhot = "MUY Alta";
$l_hot = "Alta";
$l_normal = "Normal";
$l_low = "Baja";
$l_open = "Abierto";
$l_call = "Llamar";
$l_waitcall = "Esperar llamada";
$l_paused = "Pausado";
$l_closed = "Cerrado";
$l_lastupdate = "Fecha de la última actualización";

// Action
$l_select_contract = "Seleccione Contrato";
$l_insert = "Registrar Incidente";
$l_update = "Modificar Incidente";
$l_delete = "Borrar Incidente";
$l_pri_update = "Actualizar Prioridad";
$l_pri_checkdelete = "Revisar o Borrar Prioridad";
$l_pri_delete = "Borrar Prioridad";
$l_pri_insert = "Ingresar Prioridad";
$l_sta_update = "Actualizar Estado";
$l_sta_checkdelete = "Revisar o Borrar Estado";
$l_sta_delete = "Borrar Estado";
$l_sta_insert = "Ingresar Estado";
$l_cat1_update = "Actualizar tipo de Categoría";
$l_cat1_checkdelete = "Revisar o Borrar Categoría";
$l_cat1_delete = "Borrar Categoría";
$l_cat1_insert = "Ingresar Categoría";

// Mail
$l_mail_incident_new_subject = "OBM : Nuevo incidente";

// Message
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener los incidentes";
$l_found = "incidentes encontrados";
$l_no_found = "No hay incidentes correspondientes a su búsqueda !";
$l_insert_ok = "El incidente ha sido ingresado exitosamente.";
$l_update_ok = "El incidente ha sido actualizado exitosamente.";
$l_delete_ok = "El incidente ha sido borrado exitosamente.";
$l_can_delete = "El incidente puede ser borrado";
$l_cant_delete = "El incidente NO puede ser borrado";
$l_pri_insert_ok = "La prioridad ha sido ingresada exitosamente.";
$l_pri_insert_error = "El ingreso de la prioridad ha fallado !";
$l_pri_update_ok = "La prioridad ha sido actualizada exitosamente.";
$l_pri_update_error = "La actualización de la prioridad ha fallado !";
$l_pri_delete_ok = "La prioridad ha sido borrada exitosamente.";
$l_pri_delete_error = "La eliminación de la prioridad ha fallado !";
$l_pri_can_delete = "El Prioridad puede ser borrada";
$l_pri_cant_delete = "El Prioridad NO puede ser borrada";
$l_pri_link_incident = "El Prioridad esta referenciada por estos incidentes";
$l_pri_link_incident_no = "El Prioridad no esta referenciado";
$l_sta_insert_ok = "El estado ha sido ingresado exitosamente.";
$l_sta_insert_error = "El ingreso del estado ha fallado !";
$l_sta_update_ok = "El estado ha sido actualizado exitosamente.";
$l_sta_update_error = "La actualización del estado ha fallado !";
$l_sta_delete_ok = "El estado ha sido borrado exitosamente.";
$l_sta_delete_error = "La eliminación del estado ha fallado !";
$l_sta_can_delete = "El Estado puede ser borrado";
$l_sta_cant_delete = "El Estado NO puede ser borrado";
$l_sta_link_incident = "El Estado esta referenciado por estos incidentes";
$l_sta_link_incident_no = "El Estado NO esta referenciado";

$l_cat1_insert_ok = "La categoría ha sido ingresada exitosamente.";
$l_cat1_insert_error = "El ingreso de the category type ha fallado !";
$l_cat1_update_ok = "La categoría type ha sido actualizada exitosamente.";
$l_cat1_update_error = "La categoría type update ha fallado !";
$l_cat1_delete_ok = "La categoría type ha sido borrado exitosamente.";
$l_cat1_delete_error = "La eliminación de the category type ha fallado !";
$l_cat1_can_delete = "La categoría type puede ser borrado";
$l_cat1_cant_delete = "La categoría type NO puede ser borrado";
$l_cat1_link_incident = "El tipo de categoría esta referenciada por estos incidentes";
$l_cat1_link_incident_no = "El tipo de categoría no esta referenciado";


// For the display preferences modification screen 
$l_incident_display = "Opciones de despligue de incidentes";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_length_label = "La Referencia es demasiado larga";
$l_fill_contract = "Debe asignar un contrato antes de enviar.";
$l_j_select_contract = "Debe escoger un contracto !";
$l_delete_confirm = "Realmente desea borrar el Incidente ?";
$l_date_after_js = "Fecha Posterior";
$l_j_pri_create = "Debe proveer a prioridad para crear !";
$l_j_pri_update = "Debe escoger una prioridad e ingresar un nuevo nombre para actualizar !";
$l_j_pri_delete = "Debe escoger una prioridad para borrar !";
$l_j_sta_create = "Debe proveer a estado para crear!";
$l_j_sta_update = "Debe escoger un estado e ingresar un nuevo nombre para actualizar !";
$l_j_sta_delete = "Debe escoger un estado para borrar !";
$l_j_cat1_create = "Debe ingresar el tipo de categoría para crearlo !";
$l_j_cat1_update = "Debe seleccionar el tipo de categoría e ingrese un nuevo nombre para actualizarla !";
$l_j_cat1_delete = "Debe seleccionar el tipo de categoría para borrarlo !";
