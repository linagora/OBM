<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : contact.inc                                              //
// 2004-02-18 Jaime E. Gomez H.   kasandra@skina.com.co                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_company = "Compañía";
$l_lastname = "Apellido";
$l_firstname = "Nombre";
$l_kind = "Tipo";
$l_market = "Persona a cargo";
$l_function = "Cargo";
$l_title = "Título del cargo";
$l_hphone = "Teléfono Casa";
$l_minihphone = "(Casa)";
$l_mphone = "Teléfono Celular";
$l_minimphone = "(Celular)";
$l_email = "E-Mail";
$l_mailing_ok = "Correo habilitado";
$l_fax = "Fax";
$l_country = "Pais";
$l_comment = "Comentarios";
$l_coord = "Detalles";
$l_contact_name = "Nombre contacto";
$l_from_company = "De la Compañía";
$l_telother = "Otro teléfono";
$l_contact_select_company = "Seleccione la compañía para el contacto.";
$l_func_manage = "Manejo de funciones de Contactos";
$l_func_exist = "Funciones Existentes";
$l_func_no = "No hay funciones definidas";
$l_func_new = "Nueva Función";
$l_kind_manage = "Manejo del tipo de Contacto";
$l_kind_exist = "Tipos Existentes";
$l_kind_new = "Nuevo Tipo";
$l_header = "Encabezado";

// Titles
$l_contact = "Contacto";

// Actions
$l_insert = "Ingresar el Contacto";
$l_update = "Actualizar el Contacto";
$l_checkdelete = "Revisar o Borrar el Contacto";
$l_delete = "Borrar el Contacto";
$l_func_update = "Actualizar Función";
$l_func_checkdelete = "Seleccionar o Borrar Función";
$l_func_delete = "Borrar Función";
$l_func_insert = "Ingresar Función";
$l_kind_update = "Actualizar Tipo";
$l_kind_checkdelete = "Seleccionar o Borrar  Tipo";
$l_kind_delete = "Borrar Tipo";
$l_kind_insert = "Ingresar Tipo";
$l_add = "Adicionar estos contactos";

// Messages
$l_check_samecontact = "Estos contactos lucen similares a la nueva entrada !";
$l_link_deal = "El contacto esta involucrado en estos negocios";
$l_link_deal_no = "El contacto no esta involucrado en ningún negocio";
$l_link_contract = "El contacto esta involucrado en estos contractos";
$l_link_contract_no = "El contacto no esta involucrado en ningún contracto";
$l_link_list = "El contacto esta registrado en estas listas";
$l_link_list_no = "El contacto no esta registrado en ninguna lista ";
$l_cant_delete = "El contacto no puede ser borrado !";
$l_can_delete = "El contacto puede ser borrado.";
$l_no_display = "Llene el campo de búsqueda y presione < Buscar > para obtener contactos";
$l_found = "contactos encontrados.";
$l_no_found = "No hay contactos que coincidan con al búsqueda !";
$l_delete_ok = "El contacto ha sido borrado exitosamente.";
$l_delete_error = "La eliminación del contacto ha fallado !";
$l_insert_ok = "El contacto ha sido ingresado exitosamente.";
$l_insert_error = "El ingreso del contacto ha fallado !";
$l_update_ok = "El contacto ha sido actualizado exitosamente.";
$l_update_error = "La actualización del contacto ha fallado !";
// Function Messages
$l_func_delete_ok = "La función ha sido borrada exitosamente.";
$l_func_delete_error = "La eliminación de la función ha fallado !";
$l_func_insert_ok = "La función ha sido ingresada exitosamente.";
$l_func_insert_error = "El ingreso de la función ha fallado  !";
$l_func_update_ok = "La función ha sido actualizada exitosamente.";
$l_func_update_error = "La actualización de la función ha fallado !";
$l_func_link_contact = "Estos contactos son referenciados como";
$l_func_link_contact_no = "No hay contactos referenciados como";
$l_func_cant_delete = "La Función no puede ser borrada !";
$l_func_can_delete = "La Función ha sido borrada.";
// Kind Messages
$l_kind_delete_ok = "El tipo ha sido borrado exitosamente.";
$l_kind_delete_error = "La eliminación del tipo ha fallado  !";
$l_kind_insert_ok = "El tipo ha sido ingresado exitosamente.";
$l_kind_insert_error = "El ingreso del tipo ha fallado !";
$l_kind_update_ok = "El tipo ha sido actualizado exitosamente.";
$l_kind_update_error = "La actualización del tipo ha fallado !";
$l_kind_link_company = "Estas compañías son referenciadas como";
$l_kind_link_company_no = "No hay compañías referenciadas como";
$l_kind_cant_delete = "El Tipo NO puede ser borrado !";
$l_kind_can_delete = "El Tipo ha sido borrado.";


// JavaScript Messages (specials charcaters must be escaped whith \ -> \\)
$l_fill_lastname = "Debe ingresar el Apellido antes de enviar.";
$l_fill_company = "Debe seleccionar una compañía antes de enviar.";
$l_fill_phone = "Debe ingresar el teléfono antes de enviar.";
$l_delete_confirm = "Realmente desea borrar el contacto ?";
$l_j_select_company =  "Debe seleccionar una compañía !";
$l_j_check_phone = "El número de teléfono no es válido.";
$l_j_check_hphone = "El número de teléfono personal no es válido.";
$l_j_check_mphone = "El número de teléfono celular no es válido.";
$l_j_check_fax = "El número de fax no es válido.";
$l_j_check_email = "El correo electrónico no es válido.";
// JavaScript messages - Function
$l_j_func_delete = "Debe seleccionar una función a seleccionar o borrar !";
$l_j_func_update = "Debe seleccionar una función e ingresar un nuevo nombre para actualizarla !";
$l_j_func_create = "Debe ingresar una nueva referencia de funcion para crearla !";
// JavaScript messages - Kind
$l_j_kind_delete = "Debe seleccionar un tipo a seleccionar o borrar !";
$l_j_kind_update = "Debe seleccionar un tipo e ingresar un nuevo nombre para actualizarlo !";
$l_j_kind_create = "Debe ingresar toda la información del tipo para crearlo  !";

// For the display parameters modification screen
$l_contact_display = "Opciones de despliegue de lista de contactos"; 
$l_kind_no = "No hay Tipos definidos";
$l_j_select_company = "Debe seleccionar una compañía !";
