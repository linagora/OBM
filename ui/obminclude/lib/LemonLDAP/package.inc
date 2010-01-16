<?php

//
// $Id$
//
// Copyright (c) 2009, Thomas Chemineau - thomas.chemineau<at>gmail.com
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//   * Redistributions of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//   * Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//   * Neither the name of the LINAGORA GROUP nor the names of its contributors
//     may be used to endorse or promote products derived from this software
//     without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
//

// -----------------------------------------------------------------------------
//  Load files in this package
// -----------------------------------------------------------------------------

$_files = Array(
    'LemonLDAP_Constants.php',
    'LemonLDAP_Logger.php',
    'LemonLDAP_Engine.php',
    'LemonLDAP_Sync.php',
    'LemonLDAP_Auth.php',
  );

foreach ($_files as $index => $file)
  require_once dirname(__FILE__) . '/' . $file;

// -----------------------------------------------------------------------------
//  Dependencies
// -----------------------------------------------------------------------------

// Provides tools to synchronize OBM users.
require_once dirname(__FILE__) . '/../../../php/user/user_query.inc';

// Provides tools to synchronize OBM groups.
require_once dirname(__FILE__) . '/../../../php/group/group_query.inc';

// Provides tools to update internal LDAP directory.
require_once dirname(__FILE__) . '/../../../php/tools/tools_query.inc';

// -----------------------------------------------------------------------------
//  Hack
// -----------------------------------------------------------------------------

// Set $entities, so that we could use incremental updates on OBM LDAP directory
// grep -R -A70 "entities" php/tools/tools_index.php
$entities = array(
  'user' => array(
    'table'   => 'UserObm',
    'link'    => array(
      'table' => 'EntityRight',
      'join' => array (
        'table' => 'MailboxEntity',
        'id' => 'mailboxentity_entity_id',
        'joinId' => 'entityright_entity_id'
      ),
      'id' => 'mailboxentity_mailbox_id'
    ),
    'prefix'  => 'userobm',
    'exclude' => array('domain_id' => 1, 'timeupdate' => 1, 'timecreate' => 1, 'usercreate' => 1, 'userupdate' => 1,
                       'local' => 1, 'timelastaccess' => 1, 'nb_login_failed' => 1, 'delegation_target' => 1,
                       'calendar_version' => 1, 'nomade_datebegin' => 1, 'nomade_dateend' => 1, 'vacation_datebegin' => 1,
                       'vacation_dateend' => 1),
    'rules'   => array("status" => "VALID"),
    'display' => array('firstname', 'lastname', 'email'),
    'display_format' => '%s %s <%s>'
  ),
  'group' => array(
    'table'   => 'UGroup',
    'link'    => array(
      'table' => 'of_usergroup',
      'id' => 'of_usergroup_group_id'
    ),
    'prefix'  => 'group',
    'exclude' => array('domain_id' => 1, 'timecreate' => 1, 'usercreate' => 1, 'timeupdate' => 1,
                        'privacy' => 1,'usercreate' => 1),
    'rules'   => array('privacy' => '0'),
    'display' => array('name', 'email'),
    'display_format' => '%s <%s>'
  ),
  'host' => array(
    'table'   => 'Host',
    'link'    => array(
      'table' => 'Service',
      'join' => array (
        'table' => 'HostEntity',
        'id' => 'hostentity_entity_id',
        'joinId' => 'service_entity_id'
      ),
      'id' => 'hostentity_host_id',
      'rules' => array('service_service' => array('smtp_in', 'smtp_out', 'imap', 'samba'))
    ),
    'prefix'  => 'host',
    'exclude' => array('timeupdate' => 1, 'timecreate' => 1, 'usercreate' => 1, 'userupdate' => 1),
    'rules'   => array(),
    'display' => array('name', 'ip'),
    'display_format' => '%s (%s)'
  ),
  'mailshare' => array(
    'table'   => 'MailShare',
    'link'    => array(
      'table' => 'EntityRight',
      'join' => array (
        'table' => 'MailshareEntity',
        'id' => 'mailshareentity_entity_id',
        'joinId' => 'entityright_entity_id'
      ),
      'id' => 'mailshareentity_mailshare_id'
    ),
    'prefix'  => 'mailshare',
    'exclude' => array('timeupdate' => 1, 'timecreate' => 1, 'usercreate' => 1, 'userupdate' => 1),
    'rules'   => array(),
    'display' => array('name', 'email'),
    'display_format' => '%s <%s>'
  )
);

?>
