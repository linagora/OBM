<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : agenda.inc                                               //
// 2003-11-18 Jaime E. Gomez H.   kasandra@skina.com.co                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_agenda = "Agenda";
$l_users = "Usuarios";
$l_groups = "Grupos";
$l_title = "Título";
$l_priority = "Prioridad";
$l_private_description = "Este evento es privado y no puede ver mas información";
$l_state = "Estado";
$l_high = "Alta";
$l_low = "Baja";
$l_medium = "Media";

$l_timebegin = "Hora Inicio";
$l_timebegin2 = "Hora Inicio";
$l_timeend = "Hora Final";
$l_timeend2 = "Hora Final";
$l_occupied_day = "Día Entero";
$l_event_duration = "Duración de la Reunión";
$l_format = "(AAAA-MM-DD)";
$l_repeat = "Repetición";
$l_periodicity = "Fecuencia";
$l_repeatkind = "Tipo de Repetición";
$l_repeatdays = "Dias de Repetición (Tipo semanal)";
$l_repeatdays2 = "Dias de Repetición";

$l_use_endrepeat = "Fecha Final de repetición";
$l_carac = "Propiedades";
$l_force = "Forzar la inserción del evento";

$l_link_events = "eventos vinculados con esta categoría.";
$l_waiting_events = "Reunión en espera";
$l_free = "Libre con restricción";
$l_possible = "Libre";
$l_occupied = "No disponible";
$l_meeting_legend = "Legenda";

$l_accepted = "Aceptar";
$l_rejected = "Rechazar";
$l_waiting  = "Esperando confirmación";
$l_short_accepted = "A";
$l_short_rejected = "R";
$l_short_waiting  = "Espere";
$l_wait = "Esperando";

$l_daily = "diario";
$l_weekly = "semanal";
$l_monthlybydate = "mensual (fecha)";
$l_monthlybyday = "mensual (dia)";
$l_yearly = "anual";

$l_from = "de";
$l_to = "para";

$l_conflicts = "Conflictos(s) encontrados";
$l_date = "Fecha";
$l_user = "Usuario";
$l_back_mono = "Vuelva a su vista propia";
// actions 
$l_meeting_perform = "Busque rangos libres";
$l_insert = "Ingresar el evento";
$l_update = "Actualizar el evento";
$l_accept = "Aceptar";
$l_reject = "Rechazar";
$l_select = "Seleccionar";
$l_repeat_update = "Actualizar todas las ocurrencias de esta cita ";
$l_go_to = "Ir a";
$l_refuse = "Rechazar";
$l_change_state = "Su partición en el evento";
$l_force_all = "Forzar Todo";
$l_replace_all = "Reemplace Todo";
$l_cancel_all = "Cancelar Todo";
$l_force_event = "Forzar";
$l_cancel_event = "Cancelar";
$l_replace_event = "Reemplace";
$l_too_much_user = "No mas de seis usuarios";
$l_insert_ok = "Inserción exitosa";
$l_update_ok = "Actualización exitosa";
$l_update_warning = "Alerta : algunos conflictos han sido detectados";
$l_insert_warning = "Alerta : algunos conflictos han sido detectados";
$l_update_error = "Conflicts han sido detectados, actualización cancelada";
$l_insert_error = "Conflicts han sido detectados, ingreso cancelado";
$l_sendamail = "Forzar notificación por correo.";
$l_category_update = "Actualizar Categoría";
$l_category_checkdelete = "Revisar o Borra Categoría";
$l_category_delete = "Borrar Categoría";
$l_category_insert = "Insertar Categoría";

// Messages
$l_category_delete_ok = "La categoría ha sido borrada exitosamente.";
$l_category_delete_error = "La eliminación de la categoria ha fallado !";
$l_category_insert_ok = "La categoría ha sido incluida exitosamente.";
$l_category_insert_error = "El ingreso de la categoria ha fallado !";
$l_category_update_ok = "La categoría ha sido actualizada exitosamente.";
$l_category_update_error = "La actualización de la categoria ha fallado !";
$l_category_link_company = "Estas son las compañías referenciadas como";
$l_category_link_company_no = "No existe compañía referenciada como";
$l_category_cant_delete = "La categoría NO puede ser borrada !";
$l_category_can_delete = "La categoría puede ser borrada";
$l_select_contacts = "Debe seleccionar un usuario o grupo.";
$l_select_repeat_day = "Debe seleccionar los días de repetición.";
$l_confirm_delete ="Esta operación borrará todas las ocurrencias de estos eventos para todos los usuarios, esta seguro que desea continuar?";
$l_conflict_events = "Las horas escogidas están ocupadas por"; 
$l_fill_title = "Tiene que llenar el campo [ $l_title ] ";
$l_fill_dateend = "Tiene que llenar el campo [ $l_dateend ]";
$l_fill_datebegin = "Tiene que llenar el campo [ $l_datebegin ]";
$l_err_datebegin = "[ $l_datebegin ] formato inválido !";
$l_err_dateend = "[ $l_dateend ] formato inválido !";
$l_err_repeat = "[ $l_use_endrepeat ] formato inválido !";
$l_err_begin_end = "Fecha final debe ser posterior a la de inicio !";
$l_err_end_repeat = "El final de la repetición debe ser posterior a la fecha de final.";
$l_err_end_repeat2 = "The duración máxima de una repetición es un año";
$l_err_end_repeat3 = "La máxima duración de una repetición anual es de 10 años.";
$l_err_days_repeat = "Debe elegir uno o mas días de repetición.";
$l_err_weekly_repeat = "Para repetición semanal, el dia final debe estar al menos siete dias despues del evento.";

$l_category_manage = "Manejo de Categorías de Agenda";
$l_category_exist = "Categorías Existentes";
$l_header_meeting = "Nueva reunión";
$l_category_new = "Nueva Categoría";
$l_header_right = "Manejo de permisos";
$l_header_export = "Exportar";
$l_header_admin = "Administración";
$l_authorize_list = "Personas autorizadas";
$l_denie_list = "Personas no autorizadas";
$l_read_permission = "Manejo de permisos de lectura";
$l_write_permission = "Manejo de permisos de escritura";
$l_everyone = "Todos autorizados";
$l_noone = "Nadie autorizado";
$l_list = "Lista de personas autorizadas";
$l_authorize = "&lt;&lt;&lt;&lt;";
$l_deny = "&gt;&gt;&gt;&gt;";

//MAIL
$l_add_event_mail_head = "Nuevo evento en OBM :";
$l_update_event_mail_head = "Evento actualizado en OBM :";
$l_delete_event_mail_head = "Evento borrado en OBM :";

$l_add_event_mail_subject = "Nuevo evento en OBM";
$l_update_event_mail_subject = "Actualizado un evento en OBM";
$l_delete_event_mail_subject = "Eliminado un evento en OBM";

$l_event_mail_body = "Fecha : ";
$l_update_event_mail_body = "A cambio de : ";
$l_delete_event_mail_body = "Fecha : ";
$l_confirm_mail = "Confirmar+Forzar notificación";

$l_unknown="Desconocido";
$l_calendar_visible = "Select calendars";
$l_calendar_displayed = "Calendars displayed";
// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_interval_null = "Duración no puede ser nula";
$mj_fill_title_event = "\\n Debe ingresar un título al evento.";
$mj_fill_time_begin = "\\n Debe ingresar una hora de inicio válida.";
$mj_fill_time_end = "\\n Debe ingresar una hora final.";
$mj_fill_valid_time_begin = "\\n Debe ingresar una hora de inicio válida.";
$mj_fill_valid_time_end = "\\n Debe ingresar una hora final válida.";
$mj_delete_confirm = "Confirme la eliminación del evento?";
$mj_invalid_day = "El día escogido no existe para el mes seleccionado.";
$mj_date_incompatibility = "La date de final o puede ser anterior a la fecha de inicio.";
$mj_repeat_date_incompatibility = "La fecha del fin de la repetición no puede estar antes de la fecha de inicio."; 
$mj_event_delete_confirm = "Confirme la eliminación del evento ?"; 
$mj_event_delete_all_confirm = "Confirme la eliminación del evento  y todas sus repeticiones ?"; 
$l_j_category_create = "Por Favor especifique un nombre para la nueva categoría";
$l_j_category_update = "Por Favor especifique un nuevo nombre, y seleccione una categoría para la actualización";
$l_j_category_delete = "Por Favor seleccione una categoria para borrar";
