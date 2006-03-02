<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : publication.inc                                          //
// 2006-02-24 Jaime E. Gomez H.   jgomez@skinatech.com                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_publication_title = "Título de la Publicación";
$l_type = "Tipo";
$l_renew = "Renovacion Automática";
$l_recept = "Modo de Recepción";
$l_reception = "Modo de Recepción"; 
$l_publication_type = "Tipo de Publicación";
$l_year= "Año";
$l_quantity = "Cantidad";
$l_header_new_auto = "Renovación";

// Labels and texts
$l_type_manage = "Administración de tipo de Publicación";
$l_type_exist = "Tipos Existentes";
$l_type_new = "Nuevo Tipo";

$l_renew_manage = "Administración de Modos de renovación";
$l_renew_exist = "Modos de renovación existentes";
$l_renew_new = "Nuevo modo de renovación";

$l_recept_manage = "Administración de modos de recepción";
$l_recept_exist = "Modos de recepción existentes";
$l_recept_new = "Nuevo modo de recepción";
$l_auto_sub = "Suscripción automática";
$l_contact_category1 = "Categoría 1 de contacto";

// Titres
$l_publication_orig = "exportar para publicación";
$l_publication_dest = "a publicar";
$l_subscription = "Suscripción";

// Actions
$l_insert = "Ingresar la Publicación";
$l_update = "Actualizar la Publicación";
$l_checkdelete = "Revisar o Borrar la Publicación";
$l_delete = "Borrar la Publicación";

$l_insert_sub = "Ingresar suscripción";
$l_update_sub = "Actualizar suscripción";
$l_checkdelete_sub = "Verificar o Borrar suscripción";
$l_delete_sub = "borrar suscripción";

$l_type_update = "Actualizar Tipo";
$l_type_checkdelete = "Check or Borrar Tipo";
$l_type_delete = "Borrar Tipo";
$l_type_insert = "Ingresar Tipo";

$l_renew_update = "Actualizar modo de renovación";
$l_renew_checkdelete = "Verificar o Borrar modo de renovación";
$l_renew_delete = "Borrar modo de renovación";
$l_renew_insert = "Ingresar modo de renovación";

$l_recept_update = "Actualizar modo de recepción";
$l_recept_checkdelete = "Verificar o Borrar modo de recepción";
$l_recept_delete = "Borrar modo de recepción";
$l_recept_insert = "Ingresar modo de recepción";

// Messages
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener las publicaciones";
$l_found = "publicaciones encontradas.";
$l_no_found = "No hay publicaciones que coincidan con la búsqueda !";
$l_delete_ok = "La publicación ha sido borrada exitosamente.";
$l_delete_error = "La eliminación de la publicación ha fallado !";
$l_insert_ok = "La publicación ha sido ingresada exitosamente.";
$l_insert_error = "El ingreso de la publicación ha fallado !";
$l_check_samepublication = "Estas publicaciones lucen similares a la nueva entrada!";
$l_update_ok = "La publicación ha sido actualizada exitosamente.";
$l_update_error = "La actualización de la publicación ha fallado !";
$l_link_contact = "Estos contactos tiene suscripciones a esta publicación";
$l_link_contact_no = "No existen suscripciones de contactos a esta publicación";
$l_can_delete = "La publicación puede ser borrada.";
$l_cant_delete = "La publicación NO puede ser borrada !";
$l_type_delete_ok = "El tipo ha sido borrado exitosamente.";
$l_type_delete_error = "La eliminación del tipo ha fallado !";
$l_type_insert_ok = "El tipo ha sido ingresada exitosamente.";
$l_type_insert_error = "El ingreso del tipo ha fallado !";
$l_type_update_ok = "El tipo ha sido actualizado exitosamente.";
$l_type_update_error = "La actualización de el tipo ha fallado !";
$l_type_link_publication = "Estas compañías son referenciadas como";
$l_type_link_publication_no = "No hay publicaciones referenciadas como";
$l_type_cant_delete = "The Tipo NO puede ser borrado !";
$l_type_can_delete = "The Tipo puede ser borrado.";

$l_renew_delete_ok = "Modo de renovación borrado.";
$l_renew_delete_error = "Borrado de modo de renovación cancelado!";
$l_renew_insert_ok = "Ingreso de Modo de renovación realizado.";
$l_renew_insert_error = "Ingreso de modo de renovación cancelado !";
$l_renew_update_ok = "Modo de renovación actualizado";
$l_renew_update_error = "Actualización de modo de renovación cancelado !";
$l_renew_link_publication = "Estas publicaciones son de este modo de renovación";
$l_renew_link_publication_no =  "No hay publicaciones asignadas a este modo de renovación";
$l_renew_cant_delete =  "Este modo de renovación NO puede ser borrado !";
$l_renew_can_delete =  "Este modode renovación  puede ser borrado !";

$l_recept_delete_ok = "Modo de recepción borrado.";
$l_recept_delete_error = "Borrado de modo de recepción cancelado!";
$l_recept_insert_ok = "Ingreso de Modo de recepción realizado.";
$l_recept_insert_error = "Ingreso de modo de recepción cancelado !";
$l_recept_update_ok = "Modo de recepción actualizado";
$l_recept_update_error = "Actualización de modo de recepción cancelado !";
$l_recept_link_publication = "Estas publicaciones son de este modo de recepción ";
$l_recept_link_publication_no = "No hay publicaciones asignadas a este modo de recepción";
$l_recept_cant_delete = "Este modo NO puede ser borrado !";
$l_recept_can_delete = "Este modo puede ser borrado !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_title = "Debe asignar el Título antes de enviar.";
$l_delete_confirm = "Borrar la publicación ?";
$l_j_check_year = "El Año no es válido.";
$l_j_fill_lang = "El Idioma no es válido.";
$l_j_check_lang = "El Idioma no es válido.";
$l_j_type_delete = "Debe seleccionar un tipo para revisar o borrarlo !";
$l_j_type_update = "Debe seleccionar el tipo e ingresar un nuevo nombre para actualizarla !";
$l_j_type_create = "Debe ingresar un nuevo nombre del tipo para crearlo !";
$l_j_renew_delete = "Debe seleccionar un tipo de renovación para revisar o borrarlo !";
$l_j_renew_update = "Debe seleccionar el tipo de renovación e ingresar un nuevo nombre para actualizarla !";
$l_j_renew_create = "Debe ingresar un nuevo nombre del tipo de renovación para crearlo !";
$l_j_recept_delete = "Seleccione un modo de recepción !";
$l_j_recept_update = "Seleccione un modo de recepción !";
$l_j_recept_create = "Ingrese la referencia de el modo de recepción !";
$l_j_select_publication = "Debe seleccionar una publicación !";
$l_j_fill_quantity = "Especifique una cantidad";
$l_j_check_quantity = "Ingrese una cantidad válida";

// For the display parameters modification screen
$l_publication_display = "Mostrar opciones de las listas de publicaciones"; 


// Other Skina

$l_j_check_fax = "El número de fax no es válido.";
$l_market = "Gerente de Mercadeo";
