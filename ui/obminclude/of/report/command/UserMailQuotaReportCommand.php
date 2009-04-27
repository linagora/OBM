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

  require_once 'obminclude/of/report/command.php';
  require_once 'obminclude/of/report/sender/mailSender.php';
  require_once 'obminclude/of/report/sender/stdoutSender.php';
  require_once 'obminclude/of/report/sender/downloadSender.php';
  require_once 'obminclude/of/report/format/AlertQuotaFormater.php';

  /**
   * UserMailQuotaReportCommand 
   * 
   * @uses Command
   * @package 
   * @version $id:$
   * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
   * @author Benoît Caudesaygues <benoit.caudesaygues@aliasource.fr> 
   * @license GPL 2.0
   */
  class UserMailQuotaReportCommand extends Command {
    protected $name = 'Utilisation espace mail';
    const kind = 'user';

    /**
     * @see Command::execute 
     */
    protected function execute() {
      $this->kind = 'user';

      $this->sender = new stdoutSender;
      $this->sender->setNext(new downloadSender());

      //Workflow Filter
      $this->filters[] = new GenericFilter('archive','==','0');
      $this->filters[] = new GenericFilter('status','==','VALID');

      $this->formater = new AlertQuotaFormater;
      $this->filters[] = new GenericFilter('mail_quota','!=','0');
      $this->formater->addField('id');
      $this->formater->addField('login');
      $this->formater->addField('lastname');
      $this->formater->addField('firstname');
      $this->formater->addField('mail_quota_use');
    }

    public function getKind() {
      return self::kind;
    }
  }
?>
