<?PHP
/******************************************************************************
Copyright (C) 2013-2014 Linagora

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

class obm_unread extends rcube_plugin {

  function init() {
    $this->add_hook('ready', array($this, 'ready'));
  }

  function ready($args) {
    $RCMAIL = rcmail::get_instance();
    if ($args["task"] == "mail" && $args["action"] == "unread_plugin") {
      // action to get the unread count
      echo $RCMAIL->get_storage()->count('INBOX', 'UNSEEN', null);
      exit();
    }

    $filter = get_input_value('_filter', RCUBE_INPUT_GET);
    if ($args["task"] == "mail" && $filter) {
      // request to filter the view for a specific type of messages
      $_SESSION['search_filter'] = $filter;
    }
  }
}
