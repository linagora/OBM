<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : company.inc                                              //
// 2004-02-18 Jaime E. Gomez H.   kasandra@skina.com.co                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Número (NIT)";
$l_aka = "Tambien Conocida como";
$l_company_name = "Nombre de la Compañía";
$l_kind = "Tipo";
$l_category = "Categorías";
$l_activity = "Actividad";
$l_market = "Gerente de Mercadeo";
$l_company_kind = "Tipo de Compañía";
$l_fax = "Fax";
$l_web = "Web";
$l_email = "E-Mail";
$l_coord = "Detalles";

// Labels and texts
$l_comment = "Comentarios";
$l_contact_new = "Nuevo Contacto";
$l_deal_new = "Nuevo negocio";
$l_project_new = "Nuevo Proyecto";
$l_kind_manage = "Manejo Tipo de Compañía";
$l_kind_exist = "Tipos Existentes";
$l_kind_new = "Nuevo Kind";
$l_internal = "Interno";
$l_cat_manage = "Manejo Actividad de Compañía";
$l_cat_exist = "Actividades Existentes";
$l_cat_new = "Nueva Actividad";
$l_cat_label = "Referencia";
$l_cat_code = "Codigo";
$l_act_manage = "Manejo de Actividades de la Compañía";
$l_act_exist = "Actividades Existentes";
$l_act_new = "Nueva Actividad";

// Titres
$l_company = "Compañía";

// Actions
$l_add = "Adicionar";
$l_insert = "Insertar Compañía";
$l_update = "Actualice Compañía";
$l_checkdelete = "Revisar o Borrar Compañía";
$l_delete = "Borrar Compañía";
$l_kind_update = "Actualice Tipo";
$l_kind_checkdelete = "Revisar or Borrar Tipo";
$l_kind_delete = "Borrar Tipo";
$l_kind_insert = "Insertar Tipo";
$l_cat_update = "Actualice Actividad";
$l_cat_checkdelete = "Revisar o Borrar Actividad";
$l_cat_delete = "Borrar Actividad";
$l_cat_insert = "Insertar Actividad";
$l_act_update = "Actualizar Actividad";
$l_act_checkdelete = "Revisar o Borrar Actividad";
$l_act_delete = "Borrar Actividad";
$l_act_insert = "Ingresar Actividad";
$l_select_company = "Seleccione la Compañía";

// Messages
$l_err_act_empty = "Debe llenar la actividad de la compañía !";

$l_cat_no = "No hay categorías definidas";
$l_no_display = "Llene el campo de búsqueda y presione < Buscar > para obtener compañías";
$l_found = "compañías encontradas.";
$l_no_found = "ninguna compañía coincide con la búsqueda !";
$l_delete_ok = "La compañía ha sido borrada exitosamente.";
$l_delete_error = "La eliminación de la compañía ha fallado !";
$l_insert_ok = "La compañía ha sido incluida exitosamente.";
$l_insert_error = "El ingreso de la compañía ha fallado !";
$l_check_samecompany = "Estas compañías lucen similares a esta nueva entrada !";
$l_update_ok = "La compañía ha sido actualizada exitosamente.";
$l_update_error = "La actualización de la compañía ha fallado !";
$l_link_contact = "Estos contactos son de esta compañía";
$l_link_contact_no = "La compañía no tiene contactos";
$l_link_deal = "La compañía esta involucrada en estos negocios";
$l_link_deal_no = "La compañía no esta involucrada en ningún negocio";
$l_link_contract = "La compañía esta involucrada en estos contratos";
$l_link_contract_no = "La compañía no esta involucrada en ningún contrato";
$l_link_project = "La compañía esta involucrada en estos proyectos";
$l_link_project_no = "La compañía no esta involucrada en ningún proyecto";
$l_cant_delete = "La compañía NO puede ser borrada !";
$l_can_delete = "La compañía puede ser borrada.";
$l_kind_delete_ok = "El tipo ha sido borrado exitosamente.";
$l_kind_delete_error = "La eliminación del tipo ha fallado !";
$l_kind_insert_ok = "El tipo ha ha sido incluido exitosamente.";
$l_kind_insert_error = "El ingreso del tipo ha fallado !";
$l_kind_update_ok = "El tipo ha sido actualizado exitosamente.";
$l_kind_update_error = "La actualización del tipo ha fallado !";
$l_kind_link_company = "Estas compañías son referenciadas como";
$l_kind_link_company_no = "No compañía referenciada como";
$l_kind_cant_delete = "El tipo no puede ser borrado !";
$l_kind_can_delete = "El tipo puede ser borrado.";
$l_cat_delete_ok = "La categoría ha sido borrada exitosamente.";
$l_cat_delete_error = "La eliminación de la categoría ha fallado !";
$l_cat_insert_ok = "La categoría ha sido ingresada exitosamente.";
$l_cat_insert_error = "El ingreso de la categoría ha fallado !";
$l_cat_update_ok = "La categoría ha sido actualizado exitosamente.";
$l_cat_update_error = "La actualización de la categoría ha fallado  !";
$l_cat_link_company = "Estas compañías son referenciadas como";
$l_cat_link_company_no = "No compañía referenciada como";
$l_cat_cant_delete = "La categoría no puede ser borrada !";
$l_cat_can_delete = "La categoría puede ser borrada.";
$l_act_delete_ok = "Actividad ha sido borrada exitosamente.";
$l_act_delete_error = "Eliminación de actividad fallado !";
$l_act_insert_ok = "Actividad ha sido incluida exitosamente.";
$l_act_insert_error = "Ingreso de actividad ha fallado !";
$l_act_update_ok = "Actividad ha sido actualizada exitosamente.";
$l_act_update_error = "Actualización de actividad ha fallado !";
$l_act_link_company = "Estas compañías son referenciadas como";
$l_act_link_company_no = "No compañía desde la actividad";
$l_act_cant_delete = "Actividad no pude ser borrada !";
$l_act_can_delete = "Actividad puede ser borrada.";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_delete_confirm = "Realmente desea borrar la compañía ?";
$l_fill_name = "Debe dar un Nombre antes de enviar.";
$l_delete_confirm = "Confirme la eliminación de la compañía ?";
$l_j_check_zip = "El Código de Área (ZIP) no es válido.";
$l_j_check_phone = "El número telefónico no es válido.";
$l_j_check_fax = "El número de fax no es válido.";
$l_j_check_email = "La dirección de correo electrónico no es válida.";
$l_j_check_web = "La dirección WWW no es válida.";
$l_j_kind_delete = "Debe escoger un tipo para seleccionar o borrar !";
$l_j_kind_update = "Debe escoger un tipo e ingresar un nuevo nombre para actualizar !";
$l_j_kind_create = "Debe escoger un nombre para el tipo a crear !";
$l_j_cat_delete = "Debe escoger una categoría para seleccionar o borrar  !";
$l_j_cat_update = "Debe escoger una categoría e ingresar el un nuevo nombre para actualizar  !";
$l_j_cat_create = "Debe escoger un nombre para la categoría a crear !";
$l_j_cat_deep = "Las categoría solo pueden tener tres niveles.";

$l_j_act_delete = "Debe escoger una actividad para seleccionar o borrar !";
$l_j_act_update = "Debe escoger una actividad e ingresar el un nuevo nombre para actualizar !";
$l_j_act_create = "Debe escoger un nombre para la actividad a crear !";
$l_j_select_company = "Debe seleccionar una compañía !";

// For the display parameters modification screen
$l_company_display = "Opciones de despliegue de listas de compañías"; 
$l_query_error = "Problema crítico durante la ejecución de la búsqueda.";
