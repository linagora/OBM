<?php
/******************************************************************************
Copyright (C) 2013 Linagora

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

/**
 * A roundcube plugin that synchronizes OBM identities at login.
 */
class obm_identities extends rcube_plugin {
  public $task = 'settings|login';

  /**
   * Initialize OBM identities plugin
   */
  function init() {
    $this->db = new DB_OBM();

    $this->add_hook('user_create', array($this, 'user_create'));
    $this->add_hook('user2email', array($this, 'user2email'));
    $this->add_hook('login_after', array($this, 'login_after'));

    $this->add_hook('identity_create', array($this, 'identity_create'));
    $this->add_hook('identity_update', array($this, 'hook_abort'));
    $this->add_hook('identity_delete', array($this, 'hook_abort'));
  }

  /**
   * Hook to create an identity. We only allow this during login
   */
  public function identity_create($args) {
    if (!$args['login']) {
      $args['abort'] = true;
    }
    return $args;
  }

  /**
   * Aborts the hook that is calling this function
   */
  public function hook_abort($args) {
    $args['abort'] = true;
    return $args;
  }

  /**
   * Hook called after login. Synchronizes the obm identities to roundcube.
   */
  public function login_after($args) {
    $user = rcmail::get_instance()->user;
    $this->synchronizeIdentities($user);
    return $args;
  }

  /**
   * Hook that translates the username (email, in OBM's case) to the list of
   * identities.
   */
  public function user2email($args) {
    $idents = $this->getOBMIdentities($args['user'], $args['first'],
                                      $args['extended']);
    if ($idents) {
      $args['email'] = $idents;
    } else {
      $args['abort'] = true;
    }

    return $args;
  }

  /**
   * Hook called during user creation. Most settings are taken care of by the
   * other hooks, but be sure to set the language to the one configured in OBM.
   */
  public function user_create($args) {
    $args['language'] = $this->getLanguageForUser($args['user']);
    return $args;
  }

  /**
   * Synchronize the OBM identities to roundcube (one way).
   *
   * @param user    Current user instance
   */
  private function synchronizeIdentities($user) {
    // First of all, create a map of the existing roundcube identities.
    $identMap = array();
    $identities = $user->list_identities();
    foreach ($identities as $ident) {
      $identMap[$ident["email"]] = $ident;
    }

    // Now, synchronize the OBM identities
    $obmidentities = $this->getOBMIdentities($user->data["username"]);
    foreach ($obmidentities as $obmident) {
      $mail = $obmident["email"];
      if (array_key_exists($mail, $identMap)) {
        // If it already exists, update its fields
        $iid = $identMap[$mail]["identity_id"];
        $user->update_identity($iid, $obmident);
      } else {
        // Otherwise, insert it as a new identity
        $user->insert_identity($obmident);
      }

      // In any case, remove it from the map so we know which ones we have
      // already processed.
      unset($identMap[$mail]);
    }

    // Now, go through the remaining identities, they have been deleted in OBM
    foreach ($identMap as $rcident) {
      $user->delete_identity($rcident["identity_id"]);
    }
  }

  /**
   * Retrieves the language for the user by his email address
   *
   * @param fullEmail   The full email address of the user
   */
  private function getLanguageForUser($fullEmail) {
    list($user, $domain) = explode('@', $fullEmail, 2);
    $this->db->query($this->db->query_fmt(
        "   SELECT userobmpref_value
              FROM UserObmPref
         LEFT JOIN UserObm ON userobm_id = userobmpref_user_id
         LEFT JOIN Domain  ON domain_id  = userobm_domain_id
             WHERE userobmpref_option = 'set_lang'
               AND userobm_login = '%s'
               AND domain_name = '%s'
             LIMIT 1",
        $user, $domain
    ));

    return $this->db->next_record() ? $this->db->f("userobmpref_value") : null;
  }

  /**
   * Retrieves all OBM identities as an array.
   *
   * @param fullEmail   The full email address of the user
   * @param first       If true, only the first entry is returned.
   * @param extended    If true, each entry in the array is a
   *                      roundcube-compatible user data object.
   *                      Otherwise, just the email.
   * @return            Array of identities, or null on failure.
   */
  private function getOBMIdentities($fullEmail, $first=false, $extended=true) {
    if (strpos($email, '@') < 0) {
      return null;
    }

    list($user, $domain) = explode('@', $fullEmail, 2);

    $this->db->query($this->db->query_fmt(
      " SELECT userobm_email, domain_name, userobm_commonname, userobm_company
          FROM UserObm
     LEFT JOIN Domain ON domain_id = userobm_domain_id
         WHERE userobm_login = '%s'
           AND domain_name = '%s'",
      $user, $domain
    ));

    if (!$this->db->next_record()) {
      return null;
    }

    $result = array();

    $identities = get_entity_email($this->db->f("userobm_email"),
                                   $this->db->f("domain_name"),
                                   false, null);
    foreach ($identities as $email) {
      if ($extended) {
        $result[] = array(
          'email'        => rcube_utils::idn_to_ascii($email),
          'name'         => $this->db->f("userobm_commonname"),
          'organization' => $this->db->f("userobm_company"),
          'reply-to'     => '',
          'bcc'          => '',
          'signature'    => '',
          'html_signature'    => 0
        );
      } else {
        $result[] = $email;
      }

      if ($first) {
        break;
      }
    }

    return $result;
  }

  /**
   * Helper function to mark all elements with a certain class as disabled.
   *
   * @param content     The content to replace in.
   * @param classid     The class to search for.
   * @return            The replaced content.
   */
  private function _disable_class($content, $classid) {
    return str_replace("class=\"$classid\"",
                       "class=\"$classid\" disabled=\"disabled\"",
                       $content);
  }
}
