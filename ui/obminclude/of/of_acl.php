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



/**
 * OBM ACL Class
 * 
 * Note : for special entity types that does not have a related table 
 * (calendar for example), you must pass a user ID to methods that 
 * require a $entityId parameter.
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 */
class OBM_Acl {

  const ACCESS = 'access';

  const READ   = 'read';

  const WRITE  = 'write';

  const ADMIN  = 'admin';

  private static $db;

  private static $log;

  private static $cache;

  private static $actions;

  private static $authorizedRights = array(-2, -1, 0, 1);

  /**
   * These entity types have no related table (i.e. one entity per user)
   */
  private static $specialEntityTypes = array('calendar', 'mailbox', 'addressbook');
  
  /**
   * Initializes the DB connection and the cache
   * 
   * This method must be called at the beginning of the request, prior 
   * to any use of the class.
   * 
   * @return void
   */
  public static function initialize() {
    self::$db = new DB_OBM();
    self::$cache = array('entities' => array(), 'acl' => array());
    self::$actions = array(self::ACCESS, self::READ, self::WRITE, self::ADMIN);
  }

  public static function possibleActions() {
    return self::$actions;
  }
  
  /**
   * Allows action for a user on a specific entity
   * 
   * @param mixed $userId the user ID (UserObm table primary key)
   * @param mixed $entityType lowercased entity type (ex: mailshare, resource, ...)
   * @param mixed $entityId the entity original id  (ex: MailShare table primary key)
   * @param mixed $action possible values : access, read, write, admin
   * @return void
   */
  public static function allow($userId, $entityType, $entityId, $action) {
    self::setRight('user', $userId, $entityType, $entityId, $action, 1);
  }
  
  /**
   * Allows action for a group of users on a specific entity
   * 
   * @param mixed $groupId the group ID (UGroup table primary key)
   * @return void
   */
  public static function allowGroup($groupId, $entityType, $entityId, $action) {
    self::setRight('group', $groupId, $entityType, $entityId, $action, 1);
  }
  
  /**
   * Denies action for a user on a specific entity
   * 
   * @param mixed $userId the user ID (UserObm table primary key)
   * @param mixed $entityType lowercased entity type (ex: mailshare, resource, ...)
   * @param mixed $entityId the entity original id  (ex: MailShare table primary key)
   * @param mixed $action possible values : access, read, write, admin
   * @return void
   */
  public static function deny($userId, $entityType, $entityId, $action) {
    self::setRight('user', $userId, $entityType, $entityId, $action, 0);
  }
  
  /**
   * Denies action for a group of users on a specific entity
   * 
   * @param mixed $groupId the group ID (UGroup table primary key)
   * @return void
   */
  public static function denyGroup($groupId, $entityType, $entityId, $action) {
    self::setRight('group', $groupId, $entityType, $entityId, $action, 0);
  }
  
  /**
   * Denies all actions for all users
   * 
   * @return void
   */
  public static function denyAll($entityType, $entityId) {
    $realEntityId = self::getEntityId($entityType, $entityId);
    $delete = "DELETE FROM EntityRight WHERE entityright_entity_id = '{$realEntityId}'";
    self::$db->query($delete);
  }
  
  /**
   * Checks if the user is authorized to perform a specific action on a
   * specific entity
   * 
   * @return bool
   */
  public static function isAllowed($userId, $entityType, $entityId, $action) {
    if (self::isSpecialEntity($entityType) && count(self::hasSpecialCredential($userId, $entityType, $entityId)) > 0) {
      return true;
    }
    $query = self::getAclQuery('1', $entityType, $entityId, $userId, $action);
    self::$db->query($query);
    self::log($query, 'isAllowed');
    if (!self::$db->next_record()) {
      return false;
    }
    return true;
  }
  
  /**
   * Checks if the user is authorized to perform a specific action on a
   * specific set of entities
   * 
   * @return bool
   */
  public static function areAllowed($userId, $entityType, $entityIds, $action) {
    $query = self::getAclQuery('COUNT(1) as count', $entityType, $entityIds, $userId, $action);
    self::$db->query($query);
    self::log($query, 'areAllowed');
    if (self::isSpecialEntity($entityType)) {
      $count = count(self::hasSpecialCredential($userId, $entityType, $entityIds));
    } else {
      $count = 0;
    }
    while (self::$db->next_record()) {
      $count+= self::$db->f('count');
    }
    return $count >= count($entityIds);
  }
  
  /**
   * Checks if the user is authorized to perform a specific action on ONE
   * OR MORE entity (not including his own entities like his calendar)
   * 
   * @return bool
   */
  public static function hasAllowedEntities($userId, $entityType, $action) {
    $query = self::getAclQuery('1', $entityType, null, $userId, $action, '', '', FALSE, FALSE) . " LIMIT 1";
    self::$db->query($query);
    self::log($query, 'hasAllowedEntity[peer to peer]');
    if(self::$db->nf() <= 0) {
      $query = self::getPublicAclQuery('1', $entityType, null, $action, '') . " LIMIT 1"; 
      self::$db->query($query);
      self::log($query, 'hasAllowedEntity[public]');
    }
    if(self::$db->nf() <= 0) {
      $query = self::getGroupAclQuery('1', $entityType, null, $userId, $action, '') . " LIMIT 1";
      self::$db->query($query);
      self::log($query, 'hasAllowedEntity[groups]');
    }
    return self::$db->nf() > 0;
  }
  
  /**
   * Checks if the user is authorized to perform a specific action on ONE
   * OR MORE entity of a specific entity array
   * 
   * @return bool
   */
  public static function areSomeAllowed($userId, $entityType, $entityIds, $action) {
    if (self::isSpecialEntity($entityType) && count(self::hasSpecialCredential($userId, $entityType, $entityIds)) > 0) {
      return true;
    }
    $query = self::getAclQuery('1', $entityType, $entityIds, $userId, $action);
    self::$db->query($query);
    self::log($query, 'areSomeAllowed');
    if (!self::$db->next_record()) {
      return false;
    }
    return true;
  }
  
  /**
   * Checks if the user is authorized to access a specific entity
   * 
   * @return bool
   */
  public static function canAccess($userId, $entityType, $entityId) {
    return self::isAllowed($userId, $entityType, $entityId, self::ACCESS);
  }
  
  /**
   * Checks if the user is authorized to read a specific entity
   * 
   * @return bool
   */
  public static function canRead($userId, $entityType, $entityId) {
    return self::isAllowed($userId, $entityType, $entityId, self::READ);
  }
  
  /**
   * Checks if the user is authorized to update a specific entity
   * 
   * @return bool
   */
  public static function canWrite($userId, $entityType, $entityId) {
    return self::isAllowed($userId, $entityType, $entityId, self::WRITE);
  }
  
  /**
   * Checks if the user is authorized to admin (delete for example) a 
   * specific entity
   * 
   * @return bool
   */
  public static function canAdmin($userId, $entityType, $entityId) {
    return self::isAllowed($userId, $entityType, $entityId, self::ADMIN);
  }
  
  /**
   * Sets rights for a user on a specific entity
   * 
   * $rights param must be an array whose keys are possible actions and 
   * values are boolean or numeric (0 or 1) values, for example :
   * array('access' => true, 'read' => true, 'write' => false, 'admin' => false)
   * If a possible action is not present in the array, the corresponding 
   * right will be set to false.
   * 
   * @return void
   */
  public static function setRights($userId, $entityType, $entityId, $rights) {
    self::setConsumerRights('user', $userId, $entityType, $entityId, $rights);
  }
  
  public static function setGroupRights($groupId, $entityType, $entityId, $rights) {
    self::setConsumerRights('group', $groupId, $entityType, $entityId, $rights);
  }
  
  public static function setPublicRights($entityType, $entityId, $rights) {
    if (in_array(self::ADMIN, $rights)) {
      unset($rights[self::ADMIN]);
    }
    self::setConsumerRights('user', null, $entityType, $entityId, $rights);
  }
  
  public static function setDefaultPublicRights($entityType, $entityId) {
    self::setConsumerRights('user', null, $entityType, $entityId, OBM_Acl_Utils::getDefaultPublicRights($entityType));
  }
  
  public static function getRights($userId, $entityType, $entityId) {
    $columns = self::getRightColumns();
    $rights = self::getDefaultRights();
    $query = self::getAclQuery($columns, $entityType, $entityId, $userId);
    self::$db->query($query);
    self::log($query, 'getRights');
    while (self::$db->next_record()) {
      foreach (self::$actions as $action) {
        $rights[$action] |= self::$db->f("entityright_{$action}");
      }
    }
    return $rights;
  }

  public static function getPublicRights($entityType, $entityId) {
    $columns = self::getRightColumns();
    $rights = self::getDefaultRights();
    $query = self::getPublicAclQuery($columns, $entityType, $entityId);
    self::$db->query($query);
    self::log($query, 'getPublicRights');
    self::$db->next_record();
    foreach (self::$actions as $action) {
      $rights[$action] |= self::$db->f("entityright_{$action}");
    }
    return $rights;
  }
  
  public static function getEntityReaders($entityType, $entityId) {
    return self::getEntityUsers($entityType, $entityId, self::READ);
  }
  
  public static function getEntityWriters($entityType, $entityId) {
    return self::getEntityUsers($entityType, $entityId, self::WRITE);
  }
  
  public static function getEntityAdmins($entityType, $entityId) {
    return self::getEntityUsers($entityType, $entityId, self::ADMIN);
  }
  
  public static function getEntityUsers($entityType, $entityId, $action = null) {
    $columns = array('userobm_id', 'userobm_firstname', 'userobm_lastname');
    if ($action === null) {
      $columns = array_merge($columns, self::getRightColumns());
    }
    $query = self::getAclQuery($columns, $entityType, $entityId, null, $action, '', '', false);
    self::$db->query($query);
    self::log($query, 'getEntityUsers');
    $users = array();
    while (self::$db->next_record()) {
      $id = self::$db->f('userobm_id');
      if (!isset($users[$id])) {
        $users[$id] = array(
          'id'    => $id,
          'label' => self::$db->f('userobm_lastname').' '.self::$db->f('userobm_firstname')
        );
      }
      if ($action === null) {
        foreach (self::$actions as $a) {
          $users[$id][$a] |= self::$db->f('entityright_'.$a);
        }
      }
    }
    return $users;
  }
  
  public static function getEntityConsumers($entityType, $entityId, $action = null) {
    $entityJoinTable = self::getEntityJoinTable($entityType);
    $columns = array('userobm_id AS id', self::getUsernameColumns().' AS label', "'user' as consumer");
    $unionColumns = array('group_id AS id', 'group_name AS label', "'group' AS consumer");
    if ($action === null) {
      $columns = array_merge($columns, self::getRightColumns());
      $unionColumns = array_merge($unionColumns, self::getRightColumns());
    }
    
    $union = "UNION SELECT ".implode(',', $unionColumns)." FROM UGroup 
              INNER JOIN GroupEntity ON group_id = groupentity_group_id 
              INNER JOIN EntityRight ON groupentity_entity_id = entityright_consumer_id 
              INNER JOIN {$entityJoinTable} ON {$entityType}entity_entity_id = entityright_entity_id "
              .self::getAclQueryWhere($entityType, $entityId, null, $action);
              
    $query = self::getAclQuery($columns, $entityType, $entityId, null, null, '', $union, false, false)
             ." ORDER BY consumer, label";
    self::$db->query($query);
    self::log($query, 'getEntityConsumers');
    
    $consumers = array();
    while (self::$db->next_record()) {
      $consumer = array(
        'id' => self::$db->f('id'),
        'label' => self::$db->f('label'),
        'consumer' => self::$db->f('consumer')
      );
      if ($action === null) {
        foreach (self::$actions as $a) {
          $consumer[$a] |= self::$db->f('entityright_'.$a);
        }
      }
      $consumers[] = $consumer;
    }
    return $consumers;
  }

  /**
   * Return entities on which the user is authorized to perform an action
   * 
   * If an array of entity IDs is provided as $entityId parameter, this method
   * will only return the allowed entities whose IDs where initially present
   * in the $entityId array.
   *
   * @param $action : if 'null', then all actions
   * @return array
   */
  public static function getAllowedEntities($userId, $entityType, $action, $entityId = null, $labelColumn = 'name') {
    $columns = self::getEntityColumns($entityType);
    $additionalJoins = self::getEntityJoin($entityType);
  
    if (self::isSpecialEntity($entityType)) {
      $unions = self::getEntityUnion($userId, $entityType, $entityId);
    } else {
      $unions = '';
    }
    $query = self::getAclQuery($columns, $entityType, $entityId, $userId, $action, $additionalJoins, $unions);
    $entities = array();
    self::$db->query($query);
    self::log($query, 'getAllowedEntities');
    while (self::$db->next_record()) {
      $id = self::$db->f('id');
      if (is_numeric($id)) {
        $entities[$id] = self::$db->f('label');
      }
    }
    return $entities;
  }

  public static function getAclSubselect($columns, $entityType, $entityId = null, $userId = null, $action = null) {
    return self::getAclQuery($columns, $entityType, $entityId, $userId, $action);
  }

  private static function setRight($consumerType, $consumerId, $entityType, $entityId, $action, $right = 1) {
    if (!in_array($action, self::$actions)) {
      throw new Exception("Unknown action: $action");
    }

    $realEntityId = self::getEntityId($entityType, $entityId);
    $consumerEntityId = self::getEntityId($consumerType, $consumerId);

    $query = "SELECT * FROM EntityRight 
              WHERE entityright_consumer_id = '{$consumerEntityId}' 
              AND entityright_entity_id = '{$realEntityId}'";
    self::$db->query($query);
    if (!self::$db->next_record()) {
      $insert = "INSERT INTO EntityRight (
                 entityright_entity_id, entityright_consumer_id, entityright_{$action}
                 ) VALUES ('{$realEntityId}', '{$consumerEntityId}', '{$right}')";
      self::$db->query($insert);
    } else {
      $update = "UPDATE EntityRight 
                 SET entityright_{$action} = '{$right}'  
                 WHERE entityright_consumer_id = '{$consumerEntityId}' 
                 AND entityright_entity_id = '{$realEntityId}'";
      self::$db->query($update);
    }
  }
  
  public static function setConsumerRights($consumerType, $consumerId, $entityType, $entityId, $rights) {
    $rights = self::normalizeRightsArray($rights);
    $realEntityId = self::getEntityId($entityType, $entityId);
    if ($consumerType == 'user' && $consumerId === null) {
      $consumerEntityId = 'NULL';
    } else {
      $consumerEntityId = self::getEntityId($consumerType, $consumerId);
    }
    $delete = "DELETE FROM EntityRight 
               WHERE entityright_consumer_id ".(($consumerId === null) ? "IS NULL" : "= {$consumerEntityId}")." 
               AND entityright_entity_id = '{$realEntityId}'";
    $insert = "INSERT INTO EntityRight (
               entityright_entity_id, entityright_consumer_id, entityright_access,
               entityright_read, entityright_write, entityright_admin
               ) VALUES (
               '{$realEntityId}', {$consumerEntityId}, '{$rights[access]}',
               '{$rights[read]}', '{$rights[write]}', '{$rights[admin]}')";
    self::$db->query($delete);
		if(array_sum($rights) > 0) 
	    self::$db->query($insert);
  }

  public static function getAclQuery($columns, $entityType, $entityId = null, $userId = null, $action = null, 
                                     $additionalJoins = '', $unions = '', $includePublicEntities = true, $includeGroups = true) {
    $entityTable = self::getEntityTable($entityType);
    $entityJoinTable = self::getEntityJoinTable($entityType);
    $where = self::getAclQueryWhere($entityType, $entityId, $userId, $action);
    if (is_array($columns)) {
      $columns = implode(',', $columns);
    }
    if ($includePublicEntities) {
      $unions .= " UNION ALL ".self::getPublicAclQuery($columns, $entityType, $entityId, $action, $additionalJoins);
    }
    if ($includeGroups) {
      $unions .= " UNION ALL ".self::getGroupAclQuery($columns, $entityType, $entityId, $userId, $action, $additionalJoins);
    }
    
    $query = "SELECT {$columns} FROM UserObm u1 
              INNER JOIN UserEntity ON u1.userobm_id = userentity_user_id 
              INNER JOIN EntityRight ON userentity_entity_id = entityright_consumer_id
              INNER JOIN {$entityJoinTable} ON {$entityType}entity_entity_id = entityright_entity_id 
              {$additionalJoins} {$where} {$unions}";
    return $query;
  }
  
  private static function getPublicAclQuery($columns, $entityType, $entityId = null, $action = null, $additionalJoins = '') {
    $entityJoinTable = self::getEntityJoinTable($entityType);
    if($additionalJoins === '') $joinTable = self::getEntityJoin($entityType);
    $publicWhere = self::getAclQueryWhere($entityType, $entityId, null, $action, true);
    $publicWhere .= ' AND '.self::getEntityMultidomain($entityType);
    if (is_array($columns)) {
      $columns = implode(',', $columns);
    }
    return "SELECT {$columns}
            FROM EntityRight
            INNER JOIN {$entityJoinTable} ON {$entityType}entity_entity_id = entityright_entity_id 
	    $joinTable
            {$additionalJoins} {$publicWhere}";
  }
  
  private static function getGroupAclQuery($columns, $entityType, $entityId = null, $userId = null, $action = null, $additionalJoins = '') {
    $entityTable = self::getEntityTable($entityType);
    $entityJoinTable = self::getEntityJoinTable($entityType);
    $where = self::getAclQueryWhere($entityType, $entityId, $userId, $action);    
    if (is_array($columns)) {
      $columns = implode(',', $columns);
    }
    
    $query = "SELECT {$columns} FROM UserObm u1 
              INNER JOIN of_usergroup ON userobm_id = of_usergroup_user_id
              INNER JOIN GroupEntity ON of_usergroup_group_id = groupentity_group_id 
              INNER JOIN EntityRight ON groupentity_entity_id = entityright_consumer_id              
              INNER JOIN {$entityJoinTable} ON {$entityType}entity_entity_id = entityright_entity_id 
              {$additionalJoins} {$where}";
              
    return $query;    
  }

  private static function getAclQueryWhere($entityType, $entityId = null, $userId = null, $action = null, $public = false) {
    $clauses = array();
    if ($entityId != null) {
      if (is_array($entityId)) {
        $entityIds = implode(',', $entityId);
        $clauses['entity'] = "{$entityType}entity_{$entityType}_id IN ({$entityIds})";
      } else {
        $clauses['entity'] = "{$entityType}entity_{$entityType}_id = '{$entityId}'";
      }
    }
    if ($userId !== null) {
      $clauses['user'] = "u1.userobm_id = '{$userId}'";
    }
    if ($public) {
      $clauses['public'] = "entityright_consumer_id IS NULL";
    }
    $clauses['action'] = self::getActionClause($action);
    if (count($clauses) != 0) {
      return 'WHERE '.implode(' AND ', $clauses);
    }
    return '';
  }
  
  private static function getActionClause($action) {
    if($action === null) {
      $clause = array();
      foreach(self::$actions as $action) {
        $clause[] = self::getActionClause($action);
      }
      return '('.implode(' OR ', $clause).')';
    }
    if (!in_array($action, self::$actions)) {
      throw new Exception("Unknown action: $action");
    }
    return "entityright_{$action} = 1";
  }
  
  private static function getEntityTable($entityType) {
    switch (strtolower($entityType)) {
      case 'group':
        return 'UGroup';
      case 'mailshare':
        return 'MailShare';
      case 'cv':
        return 'CV';
      default:
        return ucfirst($entityType);
    }
  }
  
  private static function getEntityJoinTable($entityType) {
    return ucfirst($entityType).'Entity';
  }
  
  public static function getEntityId($entityType, $id) {
    $sql_id = sql_parse_id($id);
    $entityJoinTable = self::getEntityJoinTable($entityType);
    $query = "SELECT {$entityType}entity_entity_id FROM {$entityJoinTable} WHERE {$entityType}entity_{$entityType}_id = {$sql_id}";
    self::$db->query($query);
    self::log($query, 'getEntityId');
    if (!self::$db->next_record()) {
      throw new Exception("Unknown $entityType entity #$id");
    }
    return self::$db->f("{$entityType}entity_entity_id");
  }
  
  private static function normalizeRightsArray($rights) {
    $rights = array_merge(self::getDefaultRights(), $rights);
    foreach ($rights as $action => $value) {
      if (!in_array($action, self::$actions)) {
        unset($rights[$action]);
        continue;
      }
      // Added to prevent Postgres errors
      if ($value == '') $value = 0;

      if (is_bool($value)) {
        $value = ($value) ? 1 : 0;
      } elseif ( !in_array($value, self::$authorizedRights) ) {
	throw new Exception("Forbidden value for $action action: $value");
      }
      $rights[$action] = $value;
    }
    return $rights;
  }
  
  public static function isSpecialEntity($entityType) {
    return in_array($entityType, self::$specialEntityTypes);
  }
  
  private static function getDefaultRights() {
    return array(self::ACCESS => 0, self::READ => 0, 
                 self::WRITE => 0, self::ADMIN => 0);
  }
  
  private static function getRightColumns() {
    $columns = array();
    foreach (self::$actions as $action) {
      $columns[] = "entityright_{$action}";
    }
    return $columns;
  }
  
  private static function getUsernameColumns($prefix = null) {
    if ($prefix !== null) {
      $prefix = $prefix.'.';
    } else {
      $prefix = '';
    }
    $ctt[0]['type'] = 'field';
    $ctt[0]['value'] = "{$prefix}userobm_lastname";
    $ctt[1]['type'] = 'string';
    $ctt[1]['value'] = ' ';
    $ctt[2]['type'] = 'field';
    $ctt[2]['value'] = "{$prefix}userobm_firstname";
    return sql_string_concat(self::$db->type, $ctt);
  }

  private static function getContactnameColumns($prefix = null) {
    if ($prefix !== null) {
      $prefix = $prefix.'.';
    } else {
      $prefix = '';
    }
    $ctt[0]['type'] = 'field';
    $ctt[0]['value'] = "{$prefix}contact_lastname";
    $ctt[1]['type'] = 'string';
    $ctt[1]['value'] = ' ';
    $ctt[2]['type'] = 'field';
    $ctt[2]['value'] = "{$prefix}contact_firstname";
    return sql_string_concat(self::$db->type, $ctt);
  }

  public static function getEntityColumns($entityType) {
    $function = 'get'.ucfirst($entityType).'Columns';
    if(method_exists('OBM_Acl', $function)) {
      return self::$function($entityType); 
    } else {
      return array("AllowedEntity.{$entityType}_id AS id, AllowedEntity.{$entityType}_name AS label");
    }
  }

  public static function getCalendarColumns() {
    return array('AllowedEntity.userobm_id AS id', self::getUsernameColumns('AllowedEntity').' AS label'); 
  }

  public static function getMailboxColumns() {
    return array('AllowedEntity.userobm_id AS id', self::getUsernameColumns('AllowedEntity').' AS label'); 
  }

  public static function getAddressbookColumns() {
    return array('AllowedEntity.id as id', 'AllowedEntity.name as label');
  }


  public static function getEntityMultidomain($entityType) {
    $function = 'get'.ucfirst($entityType).'Multidomain';
    if(method_exists('OBM_Acl', $function)) {
      return self::$function($entityType); 
    } else {
      return "AllowedEntity.{$entityType}_domain_id = ".$GLOBALS['obm']['domain_id'];
    }
  }

  public static function getCalendarMultidomain() {
    return "AllowedEntity.userobm_domain_id = ".$GLOBALS['obm']['domain_id'];
  }

  public static function getAddressbookMultidomain() {
    return "AllowedEntity.domain_id = ".$GLOBALS['obm']['domain_id'];
  }

  public static function getMailboxMultidomain() {
    return "AllowedEntity.userobm_domain_id = ".$GLOBALS['obm']['domain_id'];
  }


  public static function getEntityJoin($entityType) {
    $function = 'get'.ucfirst($entityType).'Join';
    if(method_exists('OBM_Acl', $function)) {
      return self::$function($entityType); 
    } else {
      $entityTable = self::getEntityTable($entityType);
      return "INNER JOIN {$entityTable} as AllowedEntity ON {$entityType}entity_{$entityType}_id = AllowedEntity.{$entityType}_id";
    }
  }

  public static function getCalendarJoin() {
    return "INNER JOIN UserObm as AllowedEntity ON calendarentity_calendar_id = AllowedEntity.userobm_id AND AllowedEntity.userobm_archive = 0";
  }

  public static function getMailboxJoin() {
    return "INNER JOIN UserObm as AllowedEntity ON mailboxentity_mailbox_id = AllowedEntity.userobm_id";
  }

  public static function getAddressbookJoin() {
    return "INNER JOIN AddressBook as AllowedEntity ON addressbookentity_addressbook_id = AllowedEntity.id";
  }

  public static function getEntityUnion($userId, $entityType, $entityId) {
    $function = 'get'.ucfirst($entityType).'Union';
    if(method_exists('OBM_Acl', $function)) {
      return self::$function($userId, $entityId); 
    } else {
      if($entityId) {
        if(!is_array($entityId)) $entityId = array($entityId);
        $subset = 'AND userobm_id IN ('.implode(',', $entityId).')';
      }      
      if($userId !== 0 && $userId !== '0') {
        $implicit = "AND userobm_id = {$userId} ";
      }
      return "UNION SELECT userobm_id AS id, ".self::getUsernameColumns()." AS label FROM UserObm WHERE 1=1 $implicit $subset";
    }
  }

  public static function getAddressbookUnion($userId, $entityId) {
    if($entityId) {
      if(!is_array($entityId)) $entityId = array($entityId);
      $subset = 'AND id IN ('.implode(',', $entityId).')';
    }
    return "UNION SELECT id, name as label FROM AddressBook WHERE owner = $userId $subset";
  }

  public static function hasSpecialCredential($userId, $entityType, $entityId = null) {
    $function = 'has'.ucfirst($entityType).'Credential';
    if(method_exists('OBM_Acl', $function)) {
      return self::$function($userId, $entityId);
    } elseif(!$entityId || $entityId == $userId || $userId === '0' || $userId === 0 || (is_array($entityId) && in_array($userId, $entityId))) {
      return array($userId);
    }
    return array();
  }

  public static function hasAddressbookCredential($userId, $entityId) {
    if($entityId) {
      if(!is_array($entityId)) $entityId = array($entityId);
      $subset = 'AND id IN ('.implode(',', $entityId).')';
    }
    self::$db->query("SELECT id FROM AddressBook WHERE owner = $userId $subset");
    self::log($query, 'hasAddressbookCredential');
    $idSet = array();
    while(self::$db->next_record()) {
      $idSet[] = self::$db->f('id');
    }
    return $idSet;
  }

  /**
   * Get all entityId that if set in the consumer field, give rights to an user 
   * 
   * @static
   * @access public
   * @return void
   */
  public static function getUserEntities($id) {
    if(!isset(self::$cache['entities'][$id])) {
      self::$db->query('SELECT userentity_entity_id as entity_id FROM UserEntity WHERE userentity_user_id = '.$id.' 
        UNION SELECT groupentity_entity_id as entity_id FROM GroupEntity INNER JOIN of_usergroup ON of_usergroup_group_id = groupentity_group_id WHERE of_usergroup_user_id = '.$id);
      while(self::$db->next_record()) {
        self::$cache['entities'][$id][] = self::$db->f('entity_id');
      }
    }
    return self::$cache['entities'][$id];
  }  

  private static function log($sql, $methodCall) {
    global $cdg_sql;
    display_debug_msg($sql, $cdg_sql, "OBM_Acl::$methodCall()");
  }
}

class OBM_Acl_Utils {
  
  private static $consumerRegex = '/^data-(user|group)-([0-9]+)$/';
 
  public static function updateRights($entityType, $entityId, $currentUserId, $params) {

    $rights = OBM_Acl_Utils::parseRightsParams($params);

    if (!(Perm::get_module_rights($entityType) & $GLOBALS['cright_write_admin']) == $GLOBALS['cright_write_admin'] && !OBM_Acl::isAllowed($currentUserId, $entityType, $entityId, 'admin')) {
      return false;
    }

    //current user can't remove himself admin's rights
    if (!empty($currentUserId)) {
      if (empty($rights['user'][$currentUserId]) || !is_array($rights['user'][$currentUserId]))
        $rights['user'][$currentUserId] = array();
      $rights['user'][$currentUserId][OBM_Acl::ADMIN] = 1;
    }

    OBM_Acl::denyAll($entityType, $entityId);
    
    OBM_Acl::setPublicRights($entityType, $entityId, $rights['public']);

    foreach ($rights['user'] as $userId => $userRights) {
      OBM_Acl::setRights($userId, $entityType, $entityId, $userRights);
    }
    foreach ($rights['group'] as $groupId => $groupRights) {
      OBM_Acl::setGroupRights($groupId, $entityType, $entityId, $groupRights);
    }
    return true;
  }
  
  public static function parseRightsParams($params) {
    $rights = array('user' => array(), 'group' => array(), 'public' => array());
    
    foreach (OBM_Acl::possibleActions() as $a) {
      if (isset($params[$a.'_public'])) {
        $rights['public'][$a] = $params[$a.'_public'];
      }
      if (!isset($params['accept_'.$a])) {
        continue;
      }
      foreach ($params['accept_'.$a] as $consumer) {
        if (preg_match(self::$consumerRegex, $consumer, $matches)) {
          if (!isset($rights[$matches[1]][$matches[2]])) {
            $rights[$matches[1]][$matches[2]] = array();
          }
          $rights[$matches[1]][$matches[2]][$a] = 1;
        }
      }
    }
    return $rights;
  }
  
  public static function getDefaultPublicRights($entityType) {
    global $cgp_default_right;
    
    $confRights =& $cgp_default_right[$entityType]['public'];
    
    $rights = array();
    $rights['write']  = ($confRights['write']  == -1) ? 0 : $confRights['write'];
    $rights['read']   = ($confRights['read']   == -1) ? 0 : $confRights['read'];;
    $rights['access'] = ($confRights['access'] == -1) ? 0 : $confRights['access'];
    return $rights;
  }
  
  public static function expandEntitiesArray($entities) {
    $expandedArray = array(
      'ids' => array_keys($entities),
      'entity' => array()
    );
    foreach ($entities as $id => $label) {
      $expandedArray['entity'][$id] = array('id' => $id, 'label' => $label);
    }
    return $expandedArray;
  }

}
