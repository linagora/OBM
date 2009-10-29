<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : contract.inc                                             //
// 2006-02-24 Jaime E. Gomez H.   jgomez@skinatech.com                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_archive = "Archivo";
$l_contract_contracttype = "Tipo de contrato";
$l_after = "Vencimiento antes de";
$l_before = "Vencimiento despues de";
$l_incident_list = "Incidentes";
$l_incident_new = "Nuevo";
$l_ccf_period = "Periodo";
$l_ccf_ticket = "Tiquetes";
$l_ccf_duration = "Duración";
$l_signature = "Firma";
$l_exp = "Expiración";
$l_renew = "Renovación";
$l_date_cancel = "Cancelación";
$l_autorenewal =" Renovación Automática";
$l_priority = "Prioridad";
$l_type = "Tipo";
$l_pri_manage = "Administración de Prioridad de Contratos";
$l_pri_exist = "Prioridades Existentes";
$l_pri_new = "Nueva Prioridad";
$l_sta_manage = "Administración de Estado de Contrato";
$l_sta_exist = "Estado Existente";
$l_sta_new = "Nuevo Estado";

$l_ticket_nb = "# Tiquetes";
$l_total_ticket ="Tiquetes usados";
$l_disp_ticket = "Tiquetes disponibles";
$l_ticket_final = "Último"; 
$l_duration_used ="Duración usada (h)";
$l_duration = "Duración Soporte (h)";
$l_cck_customer = "Cliente";
$l_cck_supplier = "Proveedor";
$l_contract_kind = "Tipo de Contrato";
$l_contract_select_deal = "Seleccione el Negocio.";
$l_label_libelle = "Palabra clave de la etiqueta";
$l_renewalkind = "Tipo de Renovación";
$l_marketing_manager = "Gerente Mercadeo";
$l_technical_manager = "Gerente Técnico";
$l_client_manager = "Gerente de Cuenta";
$l_clause = "Cláusula";
$l_company_town = "ciudad";
$l_company_zipcode = "código de área";
$l_company_expresspostalcode = "código postal express";
$l_company_phonenumber = "número telefónico";
$l_company_fax = "número fax";
$l_company_web = "dirección web";
$l_company_email = "correo electrónico";

// Actions
$l_pri_update = "Actualizar Prioridad";
$l_pri_checkdelete = "Revisar o Borrar Prioridad";
$l_pri_delete = "Borrar Prioridad";
$l_pri_insert = "Ingresar Prioridad";
$l_sta_update = "Actualizar Estado";
$l_sta_checkdelete = "Revisar o Borrar Estado";
$l_sta_delete = "Borrar estado";
$l_sta_insert = "Ingresar estado";
$l_insert = "Ingresar contrato";
$l_update = "Actualizar contrato";
$l_delete = "Borrar contrato";
$l_select_company = "Seleccione compañía";
$l_select_contract = "Seleccione el Contrato.";

// Messages
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener los contratos";
$l_found = "contratos encontrados.";
$l_no_found = "No hay contratos que coincidan con la búsqueda !";
$l_pri_can_delete = "La Prioridad puede ser borrada";
$l_pri_cant_delete = "La Prioridad NO puede ser borrada";
$l_pri_link_contract = "La Prioridad esta referenciado por estos incidentes";
$l_pri_link_contract_no = "La Prioridad NO esta referenciado";
$l_sta_can_delete = "El Estado puede ser borrada";
$l_sta_cant_delete = "El Estado NO puede ser borrada";
$l_sta_link_incident = "El Estado esta referenciado por estos incidentes";
$l_sta_link_incident_no = "El Estado NO esta referenciado";
$l_link_incident = "El contrato tiene estos incidentes";
$l_link_incident_no = "El contrato no tiene incidentes";
$l_cant_delete = "El contrato no puede ser borrado !";
$l_can_delete = "El contrato puede ser borrado.";
$l_type_manage = "Manejo de tipo de contrato de soporte";
$l_type_exist = "Tipos existentes";
$l_type_new = "Nuevo tipo";
$l_type_checkdelete = "Revisar o Borrar Tipo"; 
$l_type_delete = "Borrar Tipo"; 
$l_type_update = "Actualizar Tipo";
$l_type_insert = "Crear Tipo";
$l_type_can_delete = "EL tipo puede ser borrado.";
$l_type_cant_delete = "El Tipo NO puede ser borrado !";
$l_type_link_contract = "El tipo esta involucrado en estos contratos";
$l_err_label_empty = "Tiene que llenar el nombre !";
$l_err_duration_empty = "Ud. debe llenar la duración del Soporte !";
$l_err_ticket_empty = "Ud. debe llenar el número del Tiquete!";
$l_check_samecontract = "Estos contratos lucen similares a esta nueva entrada !";
$l_err_daterenew = "Fecha de renovación no es válida !";
$l_err_datecancel = "Fecha de cancelación no es válida !";
$l_err_date = "Fecha inicial es posterior a la fecha de expiración !";


// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_j_select_contract = "Debe generar un contrato !";
$l_j_type_delete = "Debe seleccionar el tipo a borrar !";
$l_j_type_update = "Debe seleccionar un Tipo e ingresar la modificación a enviar !"; 
$l_j_type_create = "Debe ingresar el nombre del nuevo tipo a crear !";
$l_delete_confirm = "Realmente desea borrar el Contrato ?";

// For the display preferences modification screen 
$l_contract_display = "Opciones de despliegue de Contratos de Soporte";
$l_j_pri_create = "Debe ingresar una prioridad para crearla !";
$l_j_pri_update = "Debe seleccionar una prioridad e ingrese un nuevo nombre para crearla !";
$l_j_pri_delete = "Debe seleccionar una prioridad para borrarla !";
$l_j_sta_create = "Debe ingresar un estado para crearlo !";
$l_j_sta_update = "Debe seleccionar un estado e ingrese un nuevo nombre para crearlo !";
$l_j_sta_delete = "Debe seleccionar un estado para borrarlo !";

// Others Skina

$l_ticket_final_coupon = "tiquetes";
$l_ticket = "Tipo de Contracto";
$l_yes = "Si";
$l_no = "No";

