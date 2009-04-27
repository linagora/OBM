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

  /**
   * GroupReportCommand 
   * 
   * @uses Command
   * @package 
   * @version $id:$
   * @copyright Copyright (c) 1997-2009 LINAGORA GSO
   * @author Benoît Caudesaygues <benoit.caudesaygues@linagora.com> 
   * @license GPL 2.0
   */
  class GroupReportCommand extends Command {
    protected $name = 'Groupe de la structure';
    const kind = 'group';

    /**
     * @see Command::execute 
     */
    protected function execute() {
      $this->kind = 'group';
      
      $this->sender = new stdoutSender;
      $this->sender->setNext(new downloadSender());

      $this->formater = new GenericFormater;
      $this->formater->addField('id');
      $this->formater->addField('name');
      $this->formater->addField('desc');
      $this->formater->addField('email');
      $this->formater->addField('nb_user');
    }

    public function getKind() {
      return self::kind;
    }

  }
?>
