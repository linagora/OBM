<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : list.inc                                                 //
// 2004-02-18 Jaime E. Gomez H.   kasandra@skina.com.co                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_subject = "Asunto";
$l_email = "E-mail";
$l_query = "Pregunta";
$l_creator = "Creador";
$l_date_creation = "Creación";
$l_updater = "Último en actualizar";
$l_date_last_update = "Última actualización";
$l_static = "Estáticos";

// Labels and texts
$l_list_contact = "Tiene un contacto";
$l_list_display = "Opciones de despliegue para listas de Listas";
$l_contact_display = "Opciones de despliegue para listas de Contactos";
$l_list_set = "Configurar Lista";
$l_export_format = "Formatos disponibles para exportar";
$l_export_intro = "Se necesita seleccionar un formato de exportación";
$l_select_list = "Seleccione la lista a exportar";

// Contact
$l_lastname = "Apellido";
$l_firstname = "Nombre";
$l_function = "Puesto";
$l_hphone = "Teléfono Casa";

// Actions
$l_insert = "Ingresar lista";
$l_update = "Actualizar lista";
$l_delete = "Borrar lista";
$l_add = "Adicionar estas listas";

// Messages
$l_insert_ok = "La lista ha sido ingresada exitosamente.";
$l_insert_error = "El ingreso de la lista ha fallado !";
$l_update_ok = "La lista ha sido actualizada exitosamente.";
$l_update_error = "La actualización de la lista ha fallado !";
$l_delete_ok = "La lista ha sido borrada exitosamente.";
$l_delete_error = "La eliminación de la lista ha fallado !";
$l_name_list_nomatch = "Nombre de la lista no es válido";
$l_check_samelist = "Estas listas lucen similar a la nueva entrada !";
$l_list_exists = "Lo siento pero esta lista ya existe";
$l_j_check_email = "Dirección de correo electrónico no es válido.";
$l_warn_delete = "contatos registrados en esta lista";

$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener las listas";
$l_found = "listas encontradas.";
$l_no_found = "No hay listas correspondientes a su búsqueda";
$l_add_contact = "Adicionar contactos" ;
$l_del_contact_sel = "Remover estos contactos";
$l_contact_added = "adicionar Contacto(s)";
$l_contact_removed = "Contacto(s) removidos";
$l_static_con_found = "contactos estáticos registrados";
$l_dyn_con_found = "contactos registrados dinámicamente";
$l_no_static_contact = "No hay contactos estáticos registrados";
$l_no_dyn_contact = "No hay contactos registrados dinámicamente";

$l_fill_name = "Debe ingresar el nombre de la lista";
$l_fill_subject = "El asunto debe ser llenado !";
$l_invalid_query = "La búsqueda es inválida !";
$l_banned_word = "Palabra prohibida";

$l_warning_exp_to_nor = "Warning, You will lose your current query. Continue ?";
$l_warning_nor_to_exp = "Warning, You will preserve the criteria in the query but lose these if you return to normal mode. Continue ?";

//------------------------------------//
// Export Format informations         //
//------------------------------------//
// Format 0 : HTML Stickers
$c_export_format[0]["name"] = "HTML Stickers";
$c_export_format[0]["info"] = "HTML Preview for stickers";
// Format 1 :
$c_export_format[1]["name"] = "HTML Table";
$c_export_format[1]["info"] = "HTML Preview for table";
// Format 2 : RTF Stickers
$c_export_format[2]["name"] = "RTF Stickers";
$c_export_format[2]["info"] = "RTF Sticker creation";
// Format 3 : Excel data
$c_export_format[3]["name"] = "Excel Table";
$c_export_format[3]["info"] = "Excel table data";
