<?php

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
    self::allowConsumer('user', $userId, $entityType, $entityId, $action);
  }
  
  /**
   * Allows action for a group of users on a specific entity
   * 
   * @param mixed $groupId the group ID (UGroup table primary key)
   * @return void
   */
  public static function allowGroup($groupId, $entityType, $entityId, $action) {
    self::allowConsumer('group', $groupId, $entityType, $entityId, $action);
  }
  
  /**
   * Checks if the user is authorized to perform a specific action on a
   * specific entity
   * 
   * @return bool
   */
  public static function isAllowed($userId, $entityType, $entityId, $action) {
    $query = self::getAclQuery('1', $entityType, $entityId, $userId, $action);
    self::$db->query($query);
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
  
  public static function setAsPublic($entityType, $entityId, $access = true, $read = false, $write = false) {
    $rights = array(self::ACCESS => $access, self::READ => $read, self::WRITE => $write);
    self::setConsumerRights('user', null, $entityType, $entityId, $rights);
  }
  
  public static function getRights($userId, $entityType, $entityId) {
    $columns = self::getRightColumns();
    $rights = self::getDefaultRights();
    $query = self::getAclQuery($columns, $entityType, $entityId, $userId, $action);
    self::$db->query($query);
    while (self::$db->next_record()) {
      foreach (self::$actions as $action) {
        $rights[$action] |= self::$db->f("entityright_{$action}");
      }
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
    $query = self::getAclQuery($columns, $entityType, $entityId, null, null, '', '', false);
    self::$db->query($query);
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
  
  public function getAllowedEntities($userId, $entityType, $action, $labelColumn = 'name') {
    $entityTable = ucfirst($entityType);
    if (in_array($entityType, self::$specialEntityTypes)) {
      $columns = array('u2.userobm_id AS id', self::getUsernameColumns('u2').' AS label');
      $additionalJoins = "LEFT JOIN UserObm u2 ON {$entityType}entity_{$entityType}_id = u2.userobm_id";
      $unions = "UNION SELECT userobm_id AS id, ".self::getUsernameColumns()." AS label FROM UserObm WHERE userobm_id = {$userId}";
    } else {
      $columns = array("{$entityType}_id AS id, {$entityType}_{$labelColumn} AS label");
      $additionalJoins = "LEFT JOIN {$entityTable} ON {$entityType}entity_{$entityType}_id = {$entityType}_id";
      $unions = '';
    }
    $query = self::getAclQuery($columns, $entityType, null, $userId, $action, $additionalJoins, $unions);
    $entities = array();
    self::$db->query($query);
    while (self::$db->next_record()) {
      $entities[self::$db->f('id')] = self::$db->f('label');
    }
    return $entities;
  }
  
  private static function allowConsumer($consumerType, $consumerId, $entityType, $entityId, $action) {
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
                 ) VALUES ('{$realEntityId}', '{$consumerEntityId}', 1)";
      self::$db->query($insert);
    } else {
      $update = "UPDATE EntityRight 
                 SET entityright_{$action} = 1 
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
  
  private static function getAclQuery($columns, $entityType, $entityId = null, $userId = null, $action = null, 
                                      $additionalJoins = '', $unions = '', $includePublicEntities = true) {
    $entityTable = ucfirst($entityType);
    $where = self::getAclQueryWhere($entityType, $entityId, $userId, $action);
    if (is_array($columns)) {
      $columns = implode(',', $columns);
    }
    
    // Public entities UNION SELECT
    if ($includePublicEntities) {
      $publicWhere = self::getAclQueryWhere($entityType, $entityId, null, $action, true);
      $unions .= " UNION 
                   SELECT {$columns}
                   FROM EntityRight
                   LEFT JOIN {$entityTable}Entity ON {$entityType}entity_entity_id = entityright_entity_id 
                   {$additionalJoins} {$publicWhere}";
    }
    
    $query = "SELECT {$columns} FROM UserObm u1 
              LEFT JOIN UserEntity ON u1.userobm_id = userentity_user_id 
              LEFT JOIN UserObmGroup ON userobm_id = userobmgroup_userobm_id
              LEFT JOIN GroupEntity ON userobmgroup_group_id = groupentity_group_id 
              LEFT JOIN EntityRight ON (groupentity_entity_id = entityright_consumer_id OR userentity_entity_id = entityright_consumer_id)
              LEFT JOIN {$entityTable}Entity ON {$entityType}entity_entity_id = entityright_entity_id 
              {$additionalJoins} {$where} {$unions}";
              
      return $query;
  }
  
  private static function getAclQueryWhere($entityType, $entityId = null, $userId = null, $action = null, $public = false) {
    $clauses = array();
    if ($entityId !== null) {
      $clauses['entity'] = "{$entityType}entity_{$entityType}_id = '{$entityId}'";
    }
    if ($userId !== null) {
      $clauses['user'] = "u1.userobm_id = '{$userId}'";
    }
    if ($public) {
      $clauses['public'] = "entityright_consumer_id IS NULL";
    }
    if ($action !== null) {
      if (!in_array($action, self::$actions)) {
        throw new Exception("Unknown action: $action");
      }
      $clauses['action'] = "entityright_{$action} = 1";
    }
    if (count($clauses) != 0) {
      return 'WHERE '.implode(' AND ', $clauses);
    }
    return '';
  }
  
  private static function getEntityId($entityType, $id) {
    $entityTable = ucfirst($entityType);
    $query = "SELECT {$entityType}entity_entity_id FROM {$entityTable}Entity WHERE {$entityType}entity_{$entityType}_id = '{$id}'";
    self::$db->query($query);
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
      if (is_bool($value)) {
        $value = ($value) ? 1 : 0;
      } elseif ($value != 1 && $value != 0) {
        throw new Exception("Forbidden value for $action action: $value");
      }
      $rights[$action] = $value;
    }
    return $rights;
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
}
