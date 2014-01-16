<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/

function handle_share_action($action, $params, $obm, $msg) {
    global $l_share_calendar, $l_share_ok, $l_reinit_ok, $l_mail_ok;
    global $l_rights, $l_of_right_err_user, $l_of_right_err_share_resource;
    global $display;

    $message = null;
    try {
        switch ($action) {
        case 'share':
            handle_share($params, $obm);
            $message = $l_share_ok;
            break;
        case 'share_reinit':
            handle_share_reinit($params, $obm);
            $message = $l_reinit_ok;
            break;
        case 'send_url':
            handle_send_url($params, $obm);
            $message = $l_mail_ok;
            break;
        default:
            return;
        }
    }
    catch (InsufficientRightsException $ex) {
        if ($params['entity_type'] = 'resource') {
          json_error_msg("$l_rights : $l_of_right_err_share_resource");
        } else {
          json_error_msg("$l_rights : $l_of_right_err_user");
        }
    }
    catch (UnknownEntityTypeException $ex) {
        http_response_code(400);
        exit();
    }
    if ($message !== null) {
        json_ok_msg("$l_share_calendar : $message");
    }
    if(is_null($msg)) {
        echo "({".$display['json']."})";
    }
    else {
        echo "({".$display['json'].",$msg})";
    }
    exit();
}

function has_right_to_share($obm, $params) {
    $entity_type = null;
    switch($params['entity_type']) {
    case 'resource':
        $entity_type = 'resource';
        // OBMFULL-5624 When sharing a resource calendar in OBM, $obm['uid'] = 0 by default and we use an externalToken.
        // We have decided to block resource calendar sharing if the resource has not the public read access.
        // Otherwise an error message warns the resource admin of the impossibility to share the resource calendar.
        // We do this because right now of_acl.php is unmanageable.
        return OBM_Acl::areAllowed(0, $entity_type, array($params['entity_id']), 'read');
    case 'user':
    case 'calendar':
        $entity_type = 'calendar';
        return OBM_Acl::areAllowed($obm['uid'], $entity_type, array($params['entity_id']), 'admin' ) || check_calendar_update_rights($params); 
    default:
        throw new UnknownEntityTypeException();
    }
}

function assert_has_right_to_share($obm, $params) {
    if (!has_right_to_share($obm, $params)) {
        throw new InsufficientRightsException();
    }
}

function handle_share($params, $obm) {
    assert_has_right_to_share($obm, $params);
    $token = get_calendar_entity_share($params['entity_id'],$params['entity_type'],$params['type']);
    $loginAtDomain = $obm['login']."@".$obm['domain_name'];
    dis_calendar_share_public($token, $loginAtDomain);
}


function handle_share_reinit($params, $obm) {
    assert_has_right_to_share($obm, $params);
    run_query_calendar_delete_token($params['entity_id'],$params['entity_type'],$params['type']);
}

function handle_send_url($params, $obm) {
    assert_has_right_to_share($obm, $params);
    $format = $params['format'];
    $params['others_attendees'][]=$params['mail'];
    $entity = get_user_info($params['entity_id']);
    $entity['token'] = get_calendar_entity_share($params['entity_id'],$params['entity_type'],$params['type']);
    run_query_insert_others_attendees($params);
    $sharemail = new shareCalendarMailer();
    $sharemail->addRecipient($params['mail']);
    $sharemail->send("userShare$format",array($entity));
}

class UnknownEntityTypeException extends Exception {
}

class InsufficientRightsException extends Exception {
}
?>
