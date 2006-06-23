<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : company.inc                                              //
// 2006-02-24 Jaime E. Gomez H.   jgomez@skinatech.com                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Número";
$l_company_name = "Nombre de la Compañía";
$l_type = "Tipo";
$l_activity = "Actividad";
$l_nafcode = "Código";
$l_market = "Gerente de Mercadeo";
$l_web = "Web";
$l_email = "E-Mail";
$l_coord = "Detalles";
$l_vat = "IVA";

// Labels and texts

$l_contact_new = "Nuevo Contacto";
$l_deal_new = "Nuevo negocio";
$l_project_new = "Nuevo Proyecto";
$l_contract_new = "Nuevo Contrato";
$l_cat_tree = "Arbol de Categorías";
$l_internal = "Interno";
$l_naf_manage = "Administración de Códigos";
$l_naf_exist = "Códigos existentes";
$l_naf_new = "Nuevo Código";
$l_naf_label = "Etiqueta";
$l_naf_code = "Código";
$l_naf_istitle = "Es título";

// Actions
$l_add = "Adicionar";
$l_insert = "Insertar Compañía";
$l_update = "Actualice Compañía";
$l_checkdelete = "Revisar o Borrar Compañía";
$l_delete = "Borrar Compañía";
$l_naf_update = "Actualice Código";
$l_naf_checkdelete = "Revisar o Borrar Código";
$l_naf_delete = "Borre Código NAF";
$l_naf_insert = "Inserte Código NAF";
$l_select_company = "Seleccione la Compañía";

// Messages
$l_err_act_empty = "Debe llenar la actividad de la compañía !";

$l_no_display = "Llene el campo de búsqueda y presione < Buscar > para obtener compañías";
$l_found = "compañías encontradas.";
$l_no_found = "ninguna compañía coincide con la búsqueda !";
$l_check_samecompany = "Estas compañías lucen similares a esta nueva entrada !";
$l_link_contact = "Estos contactos son de esta compañía";
$l_link_contact_no = "La compañía no tiene contactos";
$l_link_deal = "La compañía esta involucrada en estos negocios";
$l_link_deal_no = "La compañía no esta involucrada en ningún negocio";
$l_link_contract = "La compañía esta involucrada en estos contratos";
$l_link_contract_no = "La compañía no esta involucrada en ningún contrato";
$l_link_project = "La compañía esta involucrada en estos proyectos";
$l_link_project_no = "La compañía NO esta involucrada en ningun proyectos";
$l_link_invoice = "La compañía esta involucrada en algunas facturas";
$l_link_invoice_no ="La compañía NO esta involucrada en ninguna factura"; 
$l_cant_delete = "La compañía NO puede ser borrada !";
$l_can_delete = "La compañía puede ser borrada.";
$l_naf_no = "Código No definido";
$l_naf_link_company = "Estas compañías son referenciadas como";
$l_naf_link_company_no = "No compañía desde el Código";
$l_naf_cant_delete = "Código NO pude ser borrada !";
$l_naf_can_delete = "Código puede ser borrada.";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_name = "Debe dar un Nombre antes de enviar.";
$l_delete_confirm = "Realmente desea borrar la compañía ?";
$l_j_check_zip = "El Código de Área (ZIP) no es válido.";
$l_j_check_phone = "El número telefónico no es válido.";
$l_j_check_fax = "El número de fax no es válido.";
$l_j_check_email = "La dirección de correo electrónico no es válida.";
$l_j_check_web = "La dirección WWW no es válida.";
$l_j_cat_deep = "Las categoría solo pueden tener tres niveles.";
$l_j_naf_delete = "Seleccione un código para verificar o borrar !";
$l_j_naf_update = "Debe escoger un código NAF e ingresar un nuevo nombre para actualizar !";
$l_j_naf_create = "Debe dar un código NAF para crearlo !";
$l_j_select_company = "Debe seleccionar una compañía !";

// For the display parameters modification screen
$l_company_display = "Opciones de despliegue de listas de compañías"; 


// Otras Skina


$l_j_kind_delete = "Debe escoger un tipo para seleccionar o borrar !";
$l_j_kind_update = "Debe escoger un tipo e ingresar un nuevo nombre para actualizar !";
$l_j_kind_create = "Debe escoger un nombre para el tipo a crear !";

$l_j_cat_delete = "Debe escoger una categoría para seleccionar o borrar  !";
$l_j_cat_update = "Debe escoger una categoría e ingresar el un nuevo nombre para actualizar  !";
$l_j_cat_create = "Debe escoger un nombre para la categoría a crear !";

$l_j_act_delete = "Debe escoger una actividad para seleccionar o borrar !";
$l_j_act_update = "Debe escoger una actividad e ingresar el un nuevo nombre para actualizar !";
$l_j_act_create = "Debe escoger un nombre para la actividad a crear !";


$l_kind_manage = "Manejo Tipo de Compañía";
$l_kind_exist = "Tipos Existentes";
$l_kind_new = "Nuevo Tipo";
$l_cat_manage = "Manejo Actividad de Compañía";
$l_cat_exist = "Actividades Existentes";
$l_cat_new = "Nueva Actividad";
$l_cat_label = "Referencia";
$l_cat_code = "Codigo";
$l_act_manage = "Manejo de Actividades de la Compañía";
$l_act_exist = "Actividades Existentes";
$l_act_new = "Nueva Actividad";

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


$l_cat_no = "No hay categorías definidas";
$l_query_error = "Problema crítico en la ejecución de la búsqueda.";

$l_kind_link_company = "Estas compañías son referenciadas como";
$l_kind_link_company_no = "No existe compañía referenciada como";
$l_kind_cant_delete = "El tipo no puede ser borrado !";
$l_kind_can_delete = "El tipo puede ser borrado.";

$l_cat_link_company = "Estas compañías son referenciadas como";
$l_cat_link_company_no = "No compañía referenciada como";
$l_cat_cant_delete = "La categoría no puede ser borrada !";
$l_cat_can_delete = "La categoría puede ser borrada.";

$l_act_link_company = "Estas compañías son referenciadas como";
$l_act_link_company_no = "No compañía desde la actividad";
$l_act_cant_delete = "Actividad NO pude ser borrada !";
$l_act_can_delete = "Actividad puede ser borrada.";
