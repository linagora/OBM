<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/

require_once 'report/command.php';

class ReportTest extends OBM_Database_TestCase {

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
//    Stato_I18n::setLocale('fr');
    $GLOBALS['obm']['uid'] = 3;
    Stato_Mailer::setDefaultTransport(new Stato_StaticTransport());
  }

  public function testStaticReport() {
    $report = new Report();
    $report->addRecord(new Element('1', 'John', 'Doe', 'M')); 
    $report->addRecord(new Element('2', 'Janne', 'Doe', 'F')); 
    $report->addRecord(new Element('3', 'Julia', 'Doe', 'F')); 
    $formater = new GenericFormater();
    $formater->addField('name');
    $formater->addField('surname');
    $formater->addField('gender');
    $output = $report->format($formater);
    $this->assertEquals($output, "name\tsurname\tgender\t\nJohn\tDoe\tM\t\nJanne\tDoe\tF\t\nJulia\tDoe\tF\t\n");
  }

  public function testStandardGroupReport() {
    $report = ReportFactory::getReport(array(),'group');
    $formater = new GenericFormater();
    $formater->addField('id');
    $formater->addField('name');
    $formater->addField('domain_name');
    $formater->addField('nb_user');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tname\tdomain_name\tnb_user\t\n"
                                ."1\tAdmin\tglobal.virt\t0\t\n"
                                ."2\tCommercial\tglobal.virt\t0\t\n"
                                ."3\tProduction\tglobal.virt\t0\t\n"
                                ."4\tDÃ©veloppeur\tzz.com\t1\t\n");
    unset($report);
    $filter1 = new GenericFilter('nb_user','>','0');
    $report = ReportFactory::getReport(array($filter1),'group');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tname\tdomain_name\tnb_user\t\n"
                                ."4\tDÃ©veloppeur\tzz.com\t1\t\n");
    unset($report);
    $filter2 = new GenericFilter('domain_name','=','global.virt');
    $report = ReportFactory::getReport(array($filter1,$filter2),'group');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tname\tdomain_name\tnb_user\t\n");
  }

  public function testStandardUserReport() {
    $report = ReportFactory::getReport(array(),'user');
    $formater = new GenericFormater();
    $formater->addField('id');
    $formater->addField('login');
    $formater->addField('lastname');
    $formater->addField('firstname');
    $formater->addField('domain_name');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tlogin\tlastname\tfirstname\tdomain_name\t\n"
                                ."1\tadmin0\tAdmin Lastname\tFirstname\tglobal.virt\t\n"
                                ."2\tadmin1\tAdmin\tdomainezz.com\tzz.com\t\n"
                                ."3\tuser1\tDoe\tJohn\tzz.com\t\n"
                                ."4\tediteur1\tDoe\tJane\tzz.com\t\n");
    unset($report);
    $filter1 = new GenericFilter('domain_id','!=','1');
    $report = ReportFactory::getReport(array($filter1),'user');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tlogin\tlastname\tfirstname\tdomain_name\t\n"
                                ."2\tadmin1\tAdmin\tdomainezz.com\tzz.com\t\n"
                                ."3\tuser1\tDoe\tJohn\tzz.com\t\n"
                                ."4\tediteur1\tDoe\tJane\tzz.com\t\n");
    unset($report);
    $filter2 = new GenericFilter('domain_name','=','global.virt');
    $report = ReportFactory::getReport(array($filter1,$filter2),'user');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tlogin\tlastname\tfirstname\tdomain_name\t\n");
  }

  public function testStandardMailshareReport() {
    $report = ReportFactory::getReport(array(),'mailshare');
    $formater = new GenericFormater();
    $formater->addField('id');
    $formater->addField('name');
    $formater->addField('mail_server_name');
    $formater->addField('domain_name');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tname\tmail_server_name\tdomain_name\t\n"
                                ."1\tmailshare-test\tmail-server\tzz.com\t\n");
    unset($report);
    $filter1 = new GenericFilter('domain_name','=','global.virt');
    $report = ReportFactory::getReport(array($filter1),'mailshare');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tname\tmail_server_name\tdomain_name\t\n");
  }

  public function testReportGenericFilter() {
    $item = new Element(10, 'toto', '', '');
    $filter = new GenericFilter('id','=','10');
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('id','==','10');
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('id','===','10');
    $this->assertEquals(false, $filter->filter($item));
    $filter = new GenericFilter('id','===',10);
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('id','!=','10');
    $this->assertEquals(false, $filter->filter($item));
    $filter = new GenericFilter('id','=',0);
    $this->assertEquals(false, $filter->filter($item));
    $filter = new GenericFilter('id','!=',0);
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('id','>',5);
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('id','>=',10);
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('id','<',11);
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('id','<=',10);
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('name','=','toto');
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('name','=','titi');
    $this->assertEquals(false, $filter->filter($item));
    $filter = new GenericFilter('name','!=','toto');
    $this->assertEquals(false, $filter->filter($item));
    $filter = new GenericFilter('name','!=','titi');
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('name','>','titi');
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('name','<','titi');
    $this->assertEquals(false, $filter->filter($item));
    $filter = new GenericFilter('name','>=','toto');
    $this->assertEquals(true, $filter->filter($item));
    $filter = new GenericFilter('name','<=','toto');
    $this->assertEquals(true, $filter->filter($item));
  }

  public function testReportSender() {
    $this->assertEquals('test', Sender::currentContext());
  }

  public function testReportReportMailer() {
    $mailer = new ReportMailer;
    $mailer->addRecipient('domainezz.com Admin <admin1@zz.com>');
    $mailer->sendReportMail('toto','titi');
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : titi',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation de la commande \"titi\" :\n"
                         ."\n"
                         ."toto",$mailData[0]['content']);
  }

  public function testReportMailSender() {
    $sender = new MailSender;
    $sender->addRecipient('domainezz.com Admin <admin1@zz.com>');
    $sender->send('toto','titi');
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : titi',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation de la commande \"titi\" :\n"
                         ."\n"
                         ."toto",$mailData[0]['content']);
  }

  public function testReportCommand() {
    $cmd = new TestCommand();
    $cmd->doIt();
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Rapport d\'exploitation : Les utilisateurs qui ne sont pas dans global.virt',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Rapport d'exploitation de la commande \"Les utilisateurs qui ne sont pas dans global.virt\" :\n"
                         ."\n"
                         ."id\tlogin\tlastname\tfirstname\tdomain_name\t\n"
                         ."2\tadmin1\tAdmin\tdomainezz.com\tzz.com\t\n"
                         ."3\tuser1\tDoe\tJohn\tzz.com\t\n"
                         ."4\tediteur1\tDoe\tJane\tzz.com\t\n",$mailData[0]['content']);
  }

  public function testContextualOutput() {

  }

  public function testStructureUserReport() {

  }
  //[...]
}

class Element {
  public $id;
  public $name;
  public $surname;
  public $gender;

  public function __construct($id, $name, $surname, $gender) {
    $this->id = $id;
    $this->name = $name;
    $this->surname = $surname;
    $this->gender = $gender;
  }
}

class TestCommand extends Command {
  protected $name = 'Les utilisateurs qui ne sont pas dans global.virt';

  /**
   * Define default values (like sender to use).
   *
   * @access protected
   * @return void
   */
  protected function setup() {
    $this->kind = 'user';
    $this->sender = new MailSender;
    $this->formater = new GenericFormater;
  }

  /**
   * Main function of the programm.
   *
   * @access private
   * @return void
   */
  protected function execute() {
    $this->filters[] = new GenericFilter('domain_name','!=','global.virt');
    $this->sender->addRecipient('domainezz.com Admin <admin1@zz.com>');
    $this->formater->addField('id');
    $this->formater->addField('login');
    $this->formater->addField('lastname');
    $this->formater->addField('firstname');
    $this->formater->addField('domain_name');
  }

}
