<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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

require(dirname(__file__)."/../lib/update_object.inc");

class HashEventExtId extends UpdateObject {
    public function main() {
        $con = $this->get_con();
        if ($this->was_wrong_2_4_1_alpha6_update_applied($con)) {
            $this->fix_wrong_2_4_1_alpha6_update($con);
        }
        else {
            $this->upgrade_to_alpha9($con);
        }
    }

    private function was_wrong_2_4_1_alpha6_update_applied($con) {
        $con->query("SELECT COUNT(*) AS count_event_ext_id_hash ".
            "FROM information_schema.columns ".
            "WHERE table_name='opush_event_mapping' AND column_name='event_ext_id_hash' AND table_schema = '".$this->database."'");
        $con->next_record();
        return $con->f('count_event_ext_id_hash') != 0;
    }

    private function fix_wrong_2_4_1_alpha6_update($con) {
        $con->query("SELECT COUNT(*) AS count_create_triggers ".
            "FROM information_schema.triggers ".
            "WHERE trigger_name='opush_event_mapping_event_ext_id_hash_create_trigger' AND table_schema = '".$this->database."'");
        $con->next_record();
        if ($con->f('count_create_triggers') != 0) {
            try {
                $con->query('DROP TRIGGER '.
                    'opush_event_mapping_event_ext_id_hash_create_trigger');
            }
            catch(MySQLQueryException $ex) {
                fwrite(STDERR, "Unable to remove the trigger ".
                    "opush_event_mapping_event_ext_id_hash_create_trigger, ".
                    "please login as MySQL administrator and drop by hand");
            }
        }
        $con->query("SELECT COUNT(*) AS count_update_triggers ".
            "FROM information_schema.triggers ".
            "WHERE trigger_name='opush_event_mapping_event_ext_id_hash_update_trigger' AND table_schema = '".$this->database."'");
        $con->next_record();
        if ($con->f('count_update_triggers') != 0) {
            try {
                $con->query('DROP TRIGGER '.
                    'opush_event_mapping_event_ext_id_hash_update_trigger');
            }
            catch(MySQLQueryException $ex) {
                fwrite(STDERR, "Unable to remove the trigger ".
                    "opush_event_mapping_event_ext_id_hash_update_trigger, ".
                    "please login as MySQL administrator and drop by hand");
            }
        }
    }

    private function upgrade_to_alpha9($con) {
        $con->begin();
        try {
            $con->query("CREATE TABLE opush_event_mappingTmp (id INT(11) NOT NULL DEFAULT 0, device_id INT(11) NOT NULL, event_uid VARCHAR(300) NOT NULL, event_ext_id VARCHAR(300) NOT NULL, unique_id INT(11))");
            $con->lock_table_for_writing(
              array(
                'opush_event_mapping',
                'opush_event_mapping as e1',
                'opush_event_mappingTmp'
              )
            );
            $con->query("INSERT INTO opush_event_mappingTmp SELECT * FROM opush_event_mapping e1 JOIN (SELECT MAX(id) unique_id FROM opush_event_mapping GROUP BY device_id, event_ext_id) e2 ON e1.id=e2.unique_id");
            $con->query('DELETE FROM opush_event_mapping');
            $con->query('INSERT INTO opush_event_mapping (id, device_id, event_uid, event_ext_id) SELECT id, device_id, event_uid, event_ext_id FROM opush_event_mappingTmp;');
            $con->query('DROP TABLE opush_event_mappingTmp;');

            $con->query('ALTER TABLE opush_event_mapping '.
                'ADD COLUMN event_ext_id_hash BINARY(20)');

            # MySQL implicitly releases locks after an ALTER TABLE
            $con->lock_table_for_writing('opush_event_mapping');
            $con->query('SELECT DISTINCT event_ext_id FROM opush_event_mapping');
            $event_ext_ids = array();
            while ($con->next_record()) {
                $event_ext_ids[]= $con->f('event_ext_id');
            }
            foreach ($event_ext_ids as $event_ext_id) {
                $event_ext_id_hash = sha1($event_ext_id, true);
                $update_query = "UPDATE opush_event_mapping ".
                    "SET event_ext_id_hash='".mysql_real_escape_string(
                        $event_ext_id_hash)."' ".
                    "WHERE event_ext_id='".mysql_escape_string($event_ext_id)."'";
                $con->query($update_query);
            }

            $con->query('ALTER TABLE opush_event_mapping '.
                'MODIFY event_ext_id_hash BINARY(20) NOT NULL');
            # MySQL implicitly releases locks after an ALTER TABLE
            $con->lock_table_for_writing('opush_event_mapping');

            $con->query('CREATE UNIQUE INDEX '.
                    'opush_event_mapping_device_id_event_ext_id_fkey '.
                'ON opush_event_mapping (device_id, event_ext_id_hash)');
            $con->unlock_tables();
            $con->commit();
        }
        catch(Exception $ex) {
            fwrite(STDERR, "An error happened during the update, a rollback ".
                "will be attempted. This will NOT rollback schema changes!");
            $con->unlock_tables();
            $con->rollback();
            throw $ex;
        }
    }
}

$update = new HashEventExtId();
$update->main();
?>
