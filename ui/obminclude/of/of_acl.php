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

/**
 * OBM ACL Class
 * 
 * Note : for special entity types that does not have a related table 
 * (calendar for example), you must pass a user ID to methods that 
 * require a $entityId parameter.
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
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

  /**
   * These entity types have no related table (i.e. one entity per user)
   */
  private static $specialEntityTypes = array('calendar', 'mailbox');
  
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
    self::$cache = array();
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
    if (self::isSpecialEntity($entityType) && $userId == $entityId) {
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
    if (self::isSpecialEntity($entityType) && in_array($userId, $entityIds)) {
      $count = 1;
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
    self::log($query, 'hasAllowedEntities[peer to peer]');
    if(self::$db->nf() <= 0) {
      $query = self::getPublicAclQuery(1, $entityType, null, $action, '') . " LIMIT 1"; 
      self::$db->query($query);
      self::log($query, 'hasAllowedEntities[public]');
    }
    if(self::$db->nf() <= 0) {
      $query = self::getGroupAclQuery(1, $entityType, null, $userId, $action, '') . " LIMIT 1";
      self::$db->query($query);
      self::log($query, 'hasAllowedEntities[groups]');
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
    if (self::isSpecialEntity($entityType) && in_array($userId, $entityIds)) {
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
    if (self::isSpecialEntity($entityType)) {
      $columns = array('u2.userobm_id AS id', self::getUsernameColumns('u2').' AS label');
      $additionalJoins = "INNER JOIN UserObm u2 ON {$entityType}entity_{$entityType}_id = u2.userobm_id";
      $unions = "UNION SELECT userobm_id AS id, ".self::getUsernameColumns()." AS label FROM UserObm WHERE userobm_id = {$userId}";
    } else {
      $entityTable = self::getEntityTable($entityType);
      if ($entityType=='contact') {
        $columns = array("{$entityType}_id AS id", self::getContactnameColumns()." AS label");
      } else {
        $columns = array("{$entityType}_id AS id, {$entityType}_{$labelColumn} AS label");
      }
      $additionalJoins = "INNER JOIN {$entityTable} ON {$entityType}entity_{$entityType}_id = {$entityType}_id";
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
  
  private static function setConsumerRights($consumerType, $consumerId, $entityType, $entityId, $rights) {
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
    $publicWhere = self::getAclQueryWhere($entityType, $entityId, null, $action, true);
    if (is_array($columns)) {
      $columns = implode(',', $columns);
    }
    return "SELECT {$columns}
            FROM EntityRight
            INNER JOIN {$entityJoinTable} ON {$entityType}entity_entity_id = entityright_entity_id 
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
              INNER JOIN UserEntity ON u1.userobm_id = userentity_user_id 
              INNER JOIN of_usergroup ON userobm_id = of_usergroup_user_id
              INNER JOIN GroupEntity ON of_usergroup_group_id = groupentity_group_id 
              INNER JOIN EntityRight ON groupentity_entity_id = entityright_consumer_id              
              INNER JOIN {$entityJoinTable} ON {$entityType}entity_entity_id = entityright_entity_id 
              {$additionalJoins} {$where}";
              
    return $query;    
  }

  private static function getAclQueryWhere($entityType, $entityId = null, $userId = null, $action = null, $public = false) {
    $clauses = array();
    if ($entityId !== null) {
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
    if ($action !== null) {
      $clauses['action'] = self::getActionClause($action);
    }
    if (count($clauses) != 0) {
      return 'WHERE '.implode(' AND ', $clauses);
    }
    return '';
  }
  
  private static function getActionClause($action) {
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
      } elseif ($value != 1 && $value != 0) {
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
