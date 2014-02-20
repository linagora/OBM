<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : calendar.inc                                             //
// 2006-02-24 Jaime E. Gomez H.   jgomez@skinatech.com                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_calendar = "Agenda";
$l_users = "Usuarios";
$l_resources = "Recursos";
$l_groups = "Grupos";
$l_title = "Título";
$l_location = "Ubicación";
$l_private_description = "Este evento es privado y no puede ver mas información";
$l_high = "Alta";
$l_low = "Baja";
$l_medium = "Media";
$l_go = "Ejecutar";
$l_hour = "Hora";
$l_occupied_day = "Día Entero";
$l_event_duration = "Duración de la Reunión";
$l_format = "(AAAA-MM-DD)";
$l_repeat = "Repetición";
$l_periodicity = "Fecuencia";
$l_repeatkind = "Tipo de Repetición";
$l_repeatdays = "Dias de Repetición (Tipo semanal)";
$l_repeatdays2 = "Dias a repetir";
$l_user_right = "Administre los permisos de";
$l_date_repeatend = "Fecha Final de repetición";
$l_carac = "Propiedades";
$l_force = "Forzar la inserción del evento";
$l_everybody = "Todos";
$l_everyresources = "Todos los recursos";
$l_all_day = "Todos los dias";
$l_begin = "Iniciando";
$l_end = "Final";
$l_link_events = "eventos vinculados con esta categoría.";
$l_waiting_events = "Reunión en espera";
$l_free = "Libre pero restringido";
$l_possible = "Libre";
$l_occupied = "No disponible";
$l_meeting_legend = "Leyenda";
$l_selection = "Selección";
$l_exception = "Exceptions";

$l_accepted = "Aceptar";
$l_needs_action = "Esperando";
$l_declined = "Rechazar";
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
$l_accept = "Aceptar";
$l_reject = "Rechazar";
$l_select = "Seleccionar";
$l_repeat_update = "Actualizar todas las ocurrencias de esta cita ";
$l_go_to = "Ir a";
$l_refuse = "Rechazar";
$l_change_state = "Su partición en el evento";
$l_force_insert = "Forzar ingreso";
$l_refuse_insert = "Rechazar evento";
$l_cancel_insert = "Espere...";
$l_too_much_user = "No mas de seis usuarios";
$l_too_much_resource = "No mas de seis recursos";
$l_update_warning = "Alerta : algunos conflictos han sido detectados";
$l_insert_warning = "Alerta : algunos conflictos han sido detectados";
$l_update_error = "Conflicts han sido detectados, actualización cancelada";
$l_insert_error = "Conflicts han sido detectados, ingreso cancelado";
$l_sendamail = "Force the notification by mail.";
$l_calendar_select_group = "Seleccione los grupos.";
$l_view_type = "Ver tipo";

// Messages
$l_select_contacts = "Debe seleccionar un usuario o grupo.";
$l_select_repeat_day = "Debe seleccionar los días de repetición.";
$l_confirm_delete ="Esta operación borrará todas las ocurrencias de estos eventos para todos los usuarios, esta seguro que desea continuar?";
$l_conflict_events = "Las horas escogidas están ocupadas por"; 
$l_fill_title = "Tiene que llenar el campo [ $l_title ] ";
$l_fill_dateend = "Tiene que llenar el campo [ $l_dateend ]";
$l_fill_datebegin = "Tiene que llenar el campo [ $l_datebegin ]";
$l_err_no_calendar_selected = "No ha seleccionado calendario";
$l_err_begin_end = "Fecha final debe ser posterior a la de inicio !";
$l_err_end_repeat = "El final de la repetición debe ser posterior a la fecha de final.";
$l_err_end_repeat2 = "The duración máxima de una repetición es un año";
$l_err_end_repeat3 = "La máxima duración de una repetición anual es de 10 años.";
$l_err_days_repeat = "Debe elegir uno o mas días de repetición.";
$l_err_days_repeat_not_weekly = "Solo puede escoger dias de repeticion en repeticiones semanales.";
$l_err_weekly_repeat = "Para repetición semanal, el dia final debe estar al menos siete dias despues del evento.";
$l_event_cant_delete = "Solo puede borrar los eventos propios !";

$l_header_meeting = "Nueva reunión";
$l_header_admin = "Administración";
$l_authorize_list = "Personas autorizadas";
$l_denie_list = "Personas no autorizadas";
$l_read_permission = "Manejo de permisos de lectura";
$l_write_permission = "Manejo de permisos de escritura";
$l_everyone = "Todos autorizados";
$l_noone = "Nadie autorizado";
$l_authorize = "&lt;&lt;&lt;&lt;";
$l_deny = "&gt;&gt;&gt;&gt;";

//Mail
$l_add_event_mail_head = "Nuevo evento en OBM :";
$l_update_event_mail_head = "Evento actualizado en OBM :";
$l_delete_event_mail_head = "Evento borrado en OBM :";

$l_add_event_mail_subject = "Nuevo evento en obm";
$l_update_event_mail_body = "En vez de : ";
$l_delete_event_mail_body = "Fecha : ";

$l_update_event_mail_subject = "Evento actualizado en obm";
$l_delete_event_mail_subject = "Evento borrado de obm";
$l_confirm_mail = "Confirme+Forzar notificación";

$l_unknown = "Desconocido";
$l_calendar_visible = "Escoja calendarios";
$l_calendar_displayed = "Calendarios mostrados";
$l_resource_visible = "Escoja Recursos";
$l_resource_displayed = "Recursos mostrados";
// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_interval_null = "Duración no puede ser nula";
$mj_fill_title_event = "\\n Debe ingresar un título al evento.";
$mj_fill_time_begin = "\\n Debe ingresar una hora de inicio válida.";
$mj_fill_time_end = "\\n Debe ingresar una hora final.";
$mj_fill_valid_time_begin = "\\n Debe ingresar una hora de inicio válida.";
$mj_fill_valid_time_end = "\\n Debe ingresar una hora final válida.";
$mj_delete_confirm = "Confirme la eliminación del evento?";
$mj_invalid_day = "El día escogido no existe para el mes seleccionado.";
$mj_date_incompatibility = "La fecha de final o puede ser anterior a la fecha de inicio.";
$mj_repeat_date_incompatibility = "La fecha del fin de la repetición no puede estar antes de la fecha de inicio."; 
$mj_event_delete_confirm = "Confirme la eliminación del evento ?"; 
$mj_event_delete_all_confirm = "Confirme la eliminación del evento y todas sus repeticiones ?"; 

// Otras Skina
$l_repeatkind = "Tipo de Repetición";
$l_confirm = "Confirmar";
$l_cancel = "Cancelar";
$l_update = "Actualizar el evento";
$l_header_export = "Exportar";

// Edit occurrence
$l_edit_event_occurrence = 'Edit participation of this appointment';
$l_edit_occurrence_message = 'Edit only this event or all events in the series ?';
$l_edit_occurrence = 'Editer this event';
$l_edit_all_occurrences = 'Edit all event in the series';
$l_is_occurrence_of_parent_event = 'This event has part of a series';
$l_exception_already_set = "This event is already set, return to clendar to refresh";

$l_resource_rights_information = "Read right on selected resource(s) is required";