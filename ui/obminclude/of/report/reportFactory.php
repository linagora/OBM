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



require_once('obminclude/of/report/user.php');
require_once('obminclude/of/report/group.php');
require_once('obminclude/of/report/mailshare.php');
require_once('obminclude/of/report/report.php');
require_once('obminclude/of/report/filter.php');
/**
 * Public class used to instantiate a new Report object. This class fill the
 * Report internal list, with data from the database, depending on the data 
 * type desired (user, mailshare, group).
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 */
class ReportFactory {

  /**
   * Return the Report instance from filters list and data type.
   * 
   * @param mixed $filters array of IFilter used to filter data before adding to Report
   * @param mixed $type data type the report refers to (user, mailshare, group). The equivalent class must be defined to access data (e.g. class User for $type='user').
   * @access public
   * @return void
   */
  public static function getReport($filters, $type='user') {

    if (($type!='mailshare') && ($type!='group')) {
      $type = 'user';
    }

    $report = new Report;
    $obm_q = new DB_OBM;
    $query_builder = 'build_'.$type.'_query';
    $entity_builder = 'build_'.$type.'_entity';

    // query database to get entity data
    $query = ReportFactory::$query_builder($obm_q);
    display_debug_msg($query, $cdg_sql, 'getReport()');
    $obm_q->query($query);
    while ($obm_q->next_record()) {

      // build object from any result row
      $record = ReportFactory::$entity_builder($obm_q);

      // filter the results with filters list
      $included = true;
      foreach($filters as $filter) {
        $included &= $filter->filter($record);
      }
      if ($included) $report->addRecord($record);
    }

    return $report;
  }

  /**
   * build and return the SQL query to get mailshares data
   * 
   * @param DB_OBM $obm_q
   * @access private
   * @return string
   */
  private static function build_mailshare_query($obm_q) {
    global $obm;

    $domain_id = $obm['domain_id'];

    $query =  "SELECT
    mailshare_id,
    mailshare_domain_id,
    domain_name AS mailshare_domain_name,
    mailshare_timecreate,
    mailshare_timeupdate,
    mailshare_usercreate AS mailshare_usercreate_id,
    A.userobm_login AS mailshare_usercreate_login,
    mailshare_userupdate AS mailshare_userupdate_id,
    B.userobm_login AS mailshare_userupdate_login,
    mailshare_name,
    mailshare_archive,
    mailshare_quota,
    mailshare_mail_server_id,
    host_name AS mailshare_mail_server_name,
    mailshare_delegation,
    mailshare_description,
    mailshare_email
    FROM MailShare
    INNER JOIN Domain ON domain_id = mailshare_domain_id
    LEFT JOIN Host ON mailshare_mail_server_id=host_id
    LEFT JOIN UserObm as A ON MailShare.mailshare_usercreate=A.userobm_id
    LEFT JOIN UserObm as B ON MailShare.mailshare_userupdate=B.userobm_id
    WHERE mailshare_domain_id = '$domain_id'";

    return $query;
  }

  /**
   * build and return a Mailshare instance from a SQL query response row
   * 
   * @param DB_OBM $obm_q
   * @access private
   * @return Mailshare
   */
  private static function build_mailshare_entity($obm_q) {
    $record = new Mailshare;
    $record->id               = $obm_q->f('mailshare_id');
    $record->domain_id        = $obm_q->f('mailshare_domain_id');
    $record->domain_name      = $obm_q->f('mailshare_domain_name');
    $record->timecreate       = $obm_q->f('mailshare_timecreate');
    $record->timeupdate       = $obm_q->f('mailshare_timeupdate');
    $record->usercreate_id    = $obm_q->f('mailshare_usercreate_id');
    $record->usercreate_login = $obm_q->f('mailshare_usercreate_login');
    $record->userupdate_id    = $obm_q->f('mailshare_userupdate_id');
    $record->userupdate_login = $obm_q->f('mailshare_userupdate_login');
    $record->name             = $obm_q->f('mailshare_name');
    $record->archive          = $obm_q->f('mailshare_archive');
    $record->quota            = $obm_q->f('mailshare_quota');
    $record->mail_server_id   = $obm_q->f('mailshare_mail_server_id');
    $record->mail_server_name = $obm_q->f('mailshare_mail_server_name');
    $record->delegation       = $obm_q->f('mailshare_delegation');
    $record->description      = $obm_q->f('mailshare_description');
    $record->email            = $obm_q->f('mailshare_email');
    return $record;
  }

  /**
   * build and return the SQL query to get groups data
   * 
   * @param DB_OBM $obm_q
   * @access private
   * @return string
   */
  private static function build_group_query($obm_q) {
    global $obm;

    $domain_id = $obm['domain_id'];

    $query =  "SELECT
    group_id,
    group_domain_id,
    domain_name AS group_domain_name,
    group_timecreate,
    group_timeupdate,
    group_usercreate AS group_usercreate_id,
    A.userobm_login AS group_usercreate_login,
    group_userupdate AS group_userupdate_id,
    B.userobm_login AS group_userupdate_login,
    group_system,
    group_archive,
    group_privacy,
    group_local,
    group_ext_id,
    group_samba,
    group_gid,
    group_mailing,
    group_delegation,
    group_manager_id,
    group_name,
    group_desc,
    group_email,
    count(usergroup.of_usergroup_user_id) AS group_nb_user
    FROM UGroup
    INNER JOIN Domain ON domain_id = group_domain_id
    LEFT JOIN of_usergroup usergroup ON of_usergroup_group_id=group_id
    LEFT JOIN UserObm as A ON UGroup.group_usercreate=A.userobm_id
    LEFT JOIN UserObm as B ON UGroup.group_userupdate=B.userobm_id
    WHERE group_domain_id = '$domain_id'
    GROUP BY 
    group_id,
    group_domain_id,
    group_domain_name,
    group_timecreate,
    group_timeupdate,
    group_usercreate_id,
    group_usercreate_login,
    group_userupdate_id,
    group_userupdate_login,
    group_system,
    group_archive,
    group_privacy,
    group_local,
    group_ext_id,
    group_samba,
    group_gid,
    group_mailing,
    group_delegation,
    group_manager_id,
    group_name,
    group_desc,
    group_email";

    return $query;
  }

  /**
   * build and return a Group instance from a SQL query response row
   * 
   * @param DB_OBM $obm_q
   * @access private
   * @return Group
   */
  private static function build_group_entity($obm_q) {
    $record = new Group;
    $record->id               = $obm_q->f('group_id');
    $record->domain_id        = $obm_q->f('group_domain_id');
    $record->domain_name      = $obm_q->f('group_domain_name');
    $record->timecreate       = $obm_q->f('group_timecreate');
    $record->timeupdate       = $obm_q->f('group_timeupdate');
    $record->usercreate_id    = $obm_q->f('group_usercreate_id');
    $record->usercreate_login = $obm_q->f('group_usercreate_login');
    $record->userupdate_id    = $obm_q->f('group_userupdate_id');
    $record->userupdate_login = $obm_q->f('group_userupdate_login');
    $record->system           = $obm_q->f('group_system');
    $record->archive          = $obm_q->f('group_archive');
    $record->privacy          = $obm_q->f('group_privacy');
    $record->local            = $obm_q->f('group_local');
    $record->ext_id           = $obm_q->f('group_ext_id');
    $record->samba            = $obm_q->f('group_samba');
    $record->gid              = $obm_q->f('group_gid');
    $record->mailing          = $obm_q->f('group_mailing');
    $record->delegation       = $obm_q->f('group_delegation');
    $record->manager_id       = $obm_q->f('group_manager_id');
    $record->name             = $obm_q->f('group_name');
    $record->desc             = $obm_q->f('group_desc');
    $record->email            = $obm_q->f('group_email');
    $record->nb_user          = $obm_q->f('group_nb_user');
    return $record;
  }

  /**
   * build and return the SQL query to get users data
   * 
   * @param DB_OBM $obm_q
   * @access private
   * @return string
   */
  private static function build_user_query($obm_q) {
    global $obm;

    $date = new Of_Date();
    $date->subMonth(3);
    $domain_id = $obm['domain_id'];

    $query =  "SELECT
    U.userobm_id,
    U.userobm_domain_id,
    domain_name AS userobm_domain_name,
    U.userobm_timecreate,
    U.userobm_timeupdate,
    U.userobm_usercreate AS userobm_usercreate_id,
    A.userobm_login AS userobm_usercreate_login,
    U.userobm_userupdate AS userobm_userupdate_id,
    B.userobm_login AS userobm_userupdate_login,
    U.userobm_local,
    U.userobm_ext_id,
    U.userobm_system,
    U.userobm_archive,
    U.userobm_status,
    U.userobm_timelastaccess,
    U.userobm_login,
    U.userobm_nb_login_failed,
    U.userobm_password_type,
    U.userobm_password_dateexp,
    U.userobm_account_dateexp,
    U.userobm_perms,
    U.userobm_delegation_target,
    U.userobm_delegation,
    U.userobm_calendar_version,
    U.userobm_uid,
    U.userobm_gid,
    U.userobm_datebegin,
    U.userobm_hidden,
    U.userobm_kind,
    U.userobm_lastname,
    U.userobm_firstname,
    U.userobm_title,
    U.userobm_sound,
    U.userobm_company,
    U.userobm_direction,
    U.userobm_service,
    U.userobm_address1,
    U.userobm_address2,
    U.userobm_address3,
    U.userobm_zipcode,
    U.userobm_town,
    U.userobm_expresspostal,
    U.userobm_country_iso3166,
    U.userobm_phone,
    U.userobm_phone2,
    U.userobm_mobile,
    U.userobm_fax,
    U.userobm_fax2,
    U.userobm_web_perms,
    U.userobm_web_list,
    U.userobm_web_all,
    U.userobm_mail_perms,
    U.userobm_mail_ext_perms,
    U.userobm_email,
    U.userobm_mail_server_id,
    host_name AS userobm_mail_server_name,
    U.userobm_mail_quota,
    U.userobm_mail_quota_use,
    U.userobm_mail_login_date,
    U.userobm_nomade_perms,
    U.userobm_nomade_enable,
    U.userobm_nomade_local_copy,
    U.userobm_email_nomade,
    U.userobm_vacation_enable,
    U.userobm_vacation_datebegin,
    U.userobm_vacation_dateend,
    U.userobm_vacation_message,
    U.userobm_samba_perms,
    U.userobm_samba_home,
    U.userobm_samba_home_drive,
    U.userobm_samba_logon_script,
    U.userobm_host_id,
    U.userobm_description,
    U.userobm_location,
    U.userobm_education,
    (U.userobm_photo_id IS NOT NULL) AS userobm_has_photo,
    count(EventLink.eventlink_event_id) as groupware_usage
    FROM UserObm as U
    INNER JOIN Domain ON domain_id = U.userobm_domain_id
    INNER JOIN UserEntity ON userentity_user_id = userobm_id
    LEFT JOIN Host ON U.userobm_mail_server_id=host_id
    LEFT JOIN UserObm as A ON U.userobm_usercreate=A.userobm_id
    LEFT JOIN EventLink ON (eventlink_entity_id = userentity_entity_id AND eventlink_state != 'NEEDS-ACTION' AND (eventlink_timeupdate > '$date' OR eventlink_timecreate > '$date'))
      OR (eventlink_userupdate = U.userobm_id AND eventlink_timeupdate > '$date') OR (eventlink_usercreate = U.userobm_id AND eventlink_timecreate > '$date')
    LEFT JOIN UserObm as B ON U.userobm_userupdate=B.userobm_id 
    WHERE U.userobm_domain_id = '$domain_id'
    GROUP BY  
    U.userobm_id,
    U.userobm_domain_id,
    userobm_domain_name,
    U.userobm_timecreate,
    U.userobm_timeupdate,
    userobm_usercreate_id,
    userobm_usercreate_login,
    userobm_userupdate_id,
    userobm_userupdate_login,
    U.userobm_local,
    U.userobm_ext_id,
    U.userobm_system,
    U.userobm_archive,
    U.userobm_status,
    U.userobm_timelastaccess,
    U.userobm_login,
    U.userobm_nb_login_failed,
    U.userobm_password_type,
    U.userobm_password_dateexp,
    U.userobm_account_dateexp,
    U.userobm_perms,
    U.userobm_delegation_target,
    U.userobm_delegation,
    U.userobm_calendar_version,
    U.userobm_uid,
    U.userobm_gid,
    U.userobm_datebegin,
    U.userobm_hidden,
    U.userobm_kind,
    U.userobm_lastname,
    U.userobm_firstname,
    U.userobm_title,
    U.userobm_sound,
    U.userobm_company,
    U.userobm_direction,
    U.userobm_service,
    U.userobm_address1,
    U.userobm_address2,
    U.userobm_address3,
    U.userobm_zipcode,
    U.userobm_town,
    U.userobm_expresspostal,
    U.userobm_country_iso3166,
    U.userobm_phone,
    U.userobm_phone2,
    U.userobm_mobile,
    U.userobm_fax,
    U.userobm_fax2,
    U.userobm_web_perms,
    U.userobm_web_list,
    U.userobm_web_all,
    U.userobm_mail_perms,
    U.userobm_mail_ext_perms,
    U.userobm_email,
    U.userobm_mail_server_id,
    userobm_mail_server_name,
    U.userobm_mail_quota,
    U.userobm_mail_quota_use,
    U.userobm_mail_login_date,
    U.userobm_nomade_perms,
    U.userobm_nomade_enable,
    U.userobm_nomade_local_copy,
    U.userobm_email_nomade,
    U.userobm_vacation_enable,
    U.userobm_vacation_datebegin,
    U.userobm_vacation_dateend,
    U.userobm_vacation_message,
    U.userobm_samba_perms,
    U.userobm_samba_home,
    U.userobm_samba_home_drive,
    U.userobm_samba_logon_script,
    U.userobm_host_id,
    U.userobm_description,
    U.userobm_location,
    U.userobm_education,
    userobm_has_photo    
    ORDER BY 
    U.userobm_lastname,
    U.userobm_firstname
    ";

    return $query;
  }

  /**
   * build and return a Group instance from a SQL query response row
   * 
   * @param DB_OBM $obm_q
   * @access private
   * @return User
   */
  private static function build_user_entity($obm_q) {
    $record = new User;
    $record->id                  = $obm_q->f('userobm_id');
    $record->domain_id           = $obm_q->f('userobm_domain_id');
    $record->domain_name         = $obm_q->f('userobm_domain_name');
    $record->timecreate          = $obm_q->f('userobm_timecreate');
    $record->timeupdate          = $obm_q->f('userobm_timeupdate');
    $record->usercreate_id       = $obm_q->f('userobm_usercreate_id');
    $record->usercreate_login    = $obm_q->f('userobm_usercreate_login');
    $record->userupdate_id       = $obm_q->f('userobm_userupdate_id');
    $record->userupdate_login    = $obm_q->f('userobm_userupdate_login');
    $record->local               = $obm_q->f('userobm_local');
    $record->ext_id              = $obm_q->f('userobm_ext_id');
    $record->system              = $obm_q->f('userobm_system');
    $record->archive             = $obm_q->f('userobm_archive');
    $record->status              = $obm_q->f('userobm_status');
    $record->timelastaccess      = $obm_q->f('userobm_timelastaccess');
    $record->login               = $obm_q->f('userobm_login');
    $record->nb_login_failed     = $obm_q->f('userobm_nb_login_failed');
    $record->password_type       = $obm_q->f('userobm_password_type');
    $record->password_dateexp    = $obm_q->f('userobm_password_dateexp');
    $record->account_dateexp     = $obm_q->f('userobm_account_dateexp');
    $record->perms               = $obm_q->f('userobm_perms');
    $record->delegation_target   = $obm_q->f('userobm_delegation_target');
    $record->delegation          = $obm_q->f('userobm_delegation');
    $record->calendar_version    = $obm_q->f('userobm_calendar_version');
    $record->uid                 = $obm_q->f('userobm_uid');
    $record->gid                 = $obm_q->f('userobm_gid');
    $record->datebegin           = $obm_q->f('userobm_datebegin');
    $record->hidden              = $obm_q->f('userobm_hidden');
    $record->kind                = $obm_q->f('userobm_kind');
    $record->lastname            = $obm_q->f('userobm_lastname');
    $record->firstname           = $obm_q->f('userobm_firstname');
    $record->title               = $obm_q->f('userobm_title');
    $record->sound               = $obm_q->f('userobm_sound');
    $record->company             = $obm_q->f('userobm_company');
    $record->direction           = $obm_q->f('userobm_direction');
    $record->service             = $obm_q->f('userobm_service');
    $record->address1            = $obm_q->f('userobm_address1');
    $record->address2            = $obm_q->f('userobm_address2');
    $record->address3            = $obm_q->f('userobm_address3');
    $record->zipcode             = $obm_q->f('userobm_zipcode');
    $record->town                = $obm_q->f('userobm_town');
    $record->expresspostal       = $obm_q->f('userobm_expresspostal');
    $record->country_iso3166     = $obm_q->f('userobm_country_iso3166');
    $record->phone               = $obm_q->f('userobm_phone');
    $record->phone2              = $obm_q->f('userobm_phone2');
    $record->mobile              = $obm_q->f('userobm_mobile');
    $record->fax                 = $obm_q->f('userobm_fax');
    $record->fax2                = $obm_q->f('userobm_fax2');
    $record->web_perms           = $obm_q->f('userobm_web_perms');
    $record->web_list            = $obm_q->f('userobm_web_list');
    $record->web_all             = $obm_q->f('userobm_web_all');
    $record->mail_perms          = $obm_q->f('userobm_mail_perms');
    $record->mail_ext_perms      = $obm_q->f('userobm_mail_ext_perms');
    $record->email               = $obm_q->f('userobm_email');
    $record->mail_server_id      = $obm_q->f('userobm_mail_server_id');
    $record->mail_server_name    = $obm_q->f('userobm_mail_server_name');
    $record->mail_quota          = $obm_q->f('userobm_mail_quota');
    $record->mail_quota_use      = $obm_q->f('userobm_mail_quota_use');
    $record->mail_login_date     = $obm_q->f('userobm_mail_login_date');
    $record->nomade_perms        = $obm_q->f('userobm_nomade_perms');
    $record->nomade_enable       = $obm_q->f('userobm_nomade_enable');
    $record->nomade_local_copy   = $obm_q->f('userobm_nomade_local_copy');
    $record->email_nomade        = $obm_q->f('userobm_email_nomade');
    $record->vacation_enable     = $obm_q->f('userobm_vacation_enable');
    $record->vacation_datebegin  = $obm_q->f('userobm_vacation_datebegin');
    $record->vacation_dateend    = $obm_q->f('userobm_vacation_dateend');
    $record->vacation_message    = $obm_q->f('userobm_vacation_message');
    $record->samba_perms         = $obm_q->f('userobm_samba_perms');
    $record->samba_home          = $obm_q->f('userobm_samba_home');
    $record->samba_home_drive    = $obm_q->f('userobm_samba_home_drive');
    $record->samba_logon_script  = $obm_q->f('userobm_samba_logon_script');
    $record->host_id             = $obm_q->f('userobm_host_id');
    $record->description         = $obm_q->f('userobm_description');
    $record->location            = $obm_q->f('userobm_location');
    $record->education           = $obm_q->f('userobm_education');
    $record->has_photo           = $obm_q->f('userobm_has_photo');
    $record->groupware_usage     = $obm_q->f('groupware_usage');
    return $record;
  }

}
