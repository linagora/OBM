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

require(dirname(__file__)."/../lib/update_object.inc");

class HashEventExtId extends UpdateObject {
    public function main() {
        $con = $this->get_con();
        $con->begin();
        try {
            $con->lock_table_for_writing('opush_event_mapping');

	    $con->query('DELETE FROM opush_event_mapping WHERE id NOT IN (SELECT MAX(id) FROM opush_event_mapping GROUP BY device_id, event_ext_id)');

            $con->query('ALTER TABLE opush_event_mapping '.
                'ADD COLUMN event_ext_id_hash BYTEA');
            $con->query('SELECT DISTINCT event_ext_id FROM opush_event_mapping');
            $event_ext_ids = array();
            while ($con->next_record()) {
                $event_ext_ids[]= $con->f('event_ext_id');
            }
            foreach ($event_ext_ids as $event_ext_id) {
                $event_ext_id_hash = sha1($event_ext_id, true);
                $update_query = "UPDATE opush_event_mapping ".
                    "SET event_ext_id_hash='".pg_escape_bytea(
                        $event_ext_id_hash)."' ".
                    "WHERE event_ext_id='".pg_escape_string($event_ext_id)."'";
                $con->query($update_query);
            }

            $con->query('ALTER TABLE opush_event_mapping '.
                'ALTER COLUMN event_ext_id_hash SET NOT NULL');

            $con->query('CREATE UNIQUE INDEX '.
                'opush_event_mapping_device_id_event_ext_id_fkey '.
                'ON opush_event_mapping (device_id, event_ext_id_hash)');

            $con->unlock_tables();
            $con->commit();
        }
        catch(Exception $ex) {
            fwrite(STDERR, "An error happened during the update, a rollback ".
                "will be attempted.");
            $con->unlock_tables();
            $con->rollback();
            throw $ex;
        }
    }
}

$update = new HashEventExtId();
$update->main();
?>
