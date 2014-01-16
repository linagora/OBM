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



require_once 'report/filter.php';
//require_once 'report/filter/mailquotaFilter.php';
require_once 'report/format/AlertExpirationFormater.php';
require_once 'report/format/AlertQuotaFormater.php';
require_once 'report/sender/downloadSender.php';
require_once 'report/command/UserReportCommand.php';
require_once 'report/command/UserAliasReportCommand.php';
require_once 'report/command/UserActiveReportCommand.php';
require_once 'report/command/UserArchiveReportCommand.php';
require_once 'report/command/UserRedirectionReportCommand.php';
require_once 'report/command/UserMailQuotaReportCommand.php';
require_once 'report/command/UserExpirationReportCommand.php';
require_once 'report/command/MailShareReportCommand.php';
require_once 'report/command/VacationReportCommand.php';
require_once 'report/command/GroupReportCommand.php';
require_once 'PHPUnit/Extensions/OutputTestCase.php';


class UserReportCommandTest extends OBM_Database_TestCase {

  protected function getDataSet() {
    $csvDataSet = new OBM_Database_CsvDataSet(';');
    $csvDataSet->addEntityTable('Domain', 'domain', dirname(__FILE__).'/db_data/Domain.csv');
    $csvDataSet->addEntityTable('UserObm', 'user', dirname(__FILE__).'/db_data/UserObm.csv');
    $csvDataSet->addEntityTable('UGroup', 'group', dirname(__FILE__).'/db_data/UGroup.csv');
    $csvDataSet->addEntityTable('Host', 'host', dirname(__FILE__).'/db_data/Host.csv');
    $csvDataSet->addEntityTable('MailShare', 'mailshare', dirname(__FILE__).'/db_data/MailShare.csv');
    $csvDataSet->addTable('of_usergroup', dirname(__FILE__).'/db_data/of_usergroup.csv');
    return $csvDataSet;
  }

  public function setup() {
    parent::setup();
    $_SESSION['set_lang'] = 'fr';
    $GLOBALS['params']['report_email'] = array('domainezz.com Admin <admin1@zz.com>');
//    Stato_I18n::setLocale('fr');
    $GLOBALS['obm']['uid'] = 3;
    $GLOBALS['context'] = 'console';
    Stato_Mailer::setDefaultTransport(new Stato_StaticTransport());
  }

 public function testUserReportCommand() {
    global $l_id, $l_login, $l_lastname, $l_firstname, $l_domain_name;
    ob_start();
    $cmd = new UserReportCommand();
    $cmd->doIt();
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Tous les utilisateurs',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Tous les utilisateurs\" :\n"
                         ."\n"
                         ."$l_id\t$l_login\t$l_lastname\t$l_firstname\t$l_domain_name\t\n"
                         ."1\tadmin0\tAdmin Lastname\tFirstname\tglobal.virt\t\n"
                         ."5\tadmin2\tAdmin Lastname\tFirstname\tglobal.virt\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }

  public function testUserActiveReportCommand() {
    global $l_id, $l_login, $l_lastname, $l_firstname, $l_domain_name;
    ob_start();
    $cmd = new UserActiveReportCommand();
    $cmd->doIt();
    PHPUnit_Extensions_OutputTestCase::expectOutputString($rep);
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Les utilisateurs actifs',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Les utilisateurs actifs\" :\n"
                         ."\n"
                         ."$l_id\t$l_login\t$l_lastname\t$l_firstname\t$l_domain_name\t\n"
                         ."2\tadmin1\tAdmin\tdomainezz.com\tzz.com\t\n"
                         ."4\tediteur1\tDoe\tJane\tzz.com\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }

  public function testUserArchiveReportCommand() {
    global $l_id, $l_login, $l_lastname, $l_firstname, $l_archive, $l_timeupdate;
    ob_start();
    $cmd = new UserArchiveReportCommand();
    $cmd->doIt();
    PHPUnit_Extensions_OutputTestCase::expectOutputString($rep);
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Liste des utilisateurs en archive',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Liste des utilisateurs en archive\" :\n"
                         ."\n"
                         ."$l_id\t$l_login\t$l_lastname\t$l_firstname\t$l_archive\t$l_timeupdate\t\n"
                         ."3\tuser1\tDoe\tJohn\t1\t2026-11-08 13:45:00\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }

  public function testMailShareReportCommand() {
    global $l_id, $l_name, $l_desc, $l_email;
    ob_start();
    $cmd = new MailShareReportCommand();
    $cmd->doIt();
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Liste des comptes fonctionnels',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Liste des comptes fonctionnels\" :\n"
                         ."\n"
                         ."$l_id\t$l_name\t$l_desc\t$l_email\t\n"
                         ."1\tmailshare-test\tmailshare-test-desc-1\tmailshare-test-email-1\t\n"
                         ."2\tmailshare-test-name-2\tmailshare-test-desc-2\tmailshare-test-email-2\t\n"
                         ."3\tmailshare-test-name-3\tmailshare-test-desc-3\tmailshare-test-email-3\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }
  public function testVacationReportCommand() {
    global $l_id, $l_login, $l_lastname, $l_firstname, $l_vacation_enable, $l_vacation_message;
    ob_start();
    $cmd = new VacationReportCommand();
    $cmd->doIt();
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Etat du message d\'absence des utilisateurs',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Etat du message d'absence des utilisateurs\" :\n"
                         ."\n"
                         ."$l_id\t$l_login\t$l_lastname\t$l_firstname\t$l_vacation_enable\t$l_vacation_message\t\n"
                         ."1\tadmin0\tAdmin Lastname\tFirstname\t1\tmessage de vacance admin1\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }
  public function testGroupReportCommand() {
    global $l_id, $l_name, $l_desc, $l_email, $l_nb_user;
    ob_start();
    $cmd = new GroupReportCommand();
    $cmd->doIt();
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Groupe de la structure',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Groupe de la structure\" :\n"
                         ."\n"
                         ."$l_id\t$l_name\t$l_desc\t$l_email\t$l_nb_user\t\n"
                         ."1\tAdmin\tgroupe systeme Administration\tadmin\t0\t\n"
                         ."2\tCommercial\tgroupe systeme Commercial\t\t0\t\n"
                         ."3\tProduction\tgroupe systeme Production\t\t0\t\n"
                         ."4\tDeveloppeur\t\tdev@zz.com\t1\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }
  
  public function testUserAliasReportCommand() {
    global $l_id, $l_login, $l_lastname, $l_firstname, $l_email;
    ob_start();
    $cmd = new UserAliasReportCommand();
    $cmd->doIt();
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Les alias de tous les utilisateurs',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Les alias de tous les utilisateurs\" :\n"
                         ."\n"
                         ."$l_id\t$l_login\t$l_lastname\t$l_firstname\t$l_email\t\n"
                         ."1\tadmin0\tAdmin Lastname\tFirstname\t\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }
  
  public function testUserRedirectionReportCommand() {
    global $l_id, $l_login, $l_lastname, $l_firstname, $l_nomade_enable, $l_email_nomade;
    ob_start();
    $cmd = new UserRedirectionReportCommand();
    $cmd->doIt();
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Etat de redirection des mails utilisateurs',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Etat de redirection des mails utilisateurs\" :\n"
                         ."\n"
                         ."$l_id\t$l_login\t$l_lastname\t$l_firstname\t$l_nomade_enable\t$l_email_nomade\t\n"
                         ."1\tadmin0\tAdmin Lastname\tFirstname\t0\t\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }
  
  public function testuserExpirationReportCommand() {
    global $l_id, $l_login, $l_lastname, $l_firstname, $l_account_dateexp, $l_warn;
    ob_start();
    $cmd = new UserExpirationReportCommand();
    $cmd->doIt();
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Date d\'expiration des utilisateurs',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Date d'expiration des utilisateurs\" :\n"
                         ."\n"
                         ."$l_id\t$l_login\t$l_lastname\t$l_firstname\t$l_account_dateexp\t$l_warn\t\n"
                         ."1\tadmin0\tAdmin Lastname\tFirstname\t0000-00-05\tAttention le compte arrive bientôt à expiration\t\n"
                         ."5\tadmin2\tAdmin Lastname\tFirstname\t0000-00-05\tAttention le compte arrive bientôt à expiration\t\n"
                         ."6\tadmin2\tAdmin Lastname\tFirstname\t2009-05-20\t\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }
  
  public function testuserMailQuotaReportCommand() {
    global $l_id, $l_login, $l_lastname, $l_firstname, $l_mail_quota_use, $l_used_percent, $l_warn;
    ob_start();
    $cmd = new UserMailQuotaReportCommand();
    $cmd->doIt();
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Utilisation espace mail',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation du traitement \"Utilisation espace mail\" :\n"
                         ."\n"
                         ."$l_id\t$l_login\t$l_lastname\t$l_firstname\t$l_mail_quota_use\t$l_used_percent\t$l_warn\t\n"
                         ."1\tadmin0\tAdmin Lastname\tFirstname\t5\t50%\t\t\n"
                         ."5\tadmin2\tAdmin Lastname\tFirstname\t19\t95%\tAttention le quota est bientôt atteind\t\n"
                         ,$mailData[0]['content']);
    ob_clean();
    ob_end_flush();
  }

}
