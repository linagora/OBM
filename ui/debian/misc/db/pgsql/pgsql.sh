#/bin/bash
set -e

create_plpgsql_language() {
        local dbname=$1
        local lang_exists=`su - postgres -c "psql ${dbname} -At -c \"SELECT COUNT(*) FROM pg_language WHERE lanname='plpgsql'\""`

        if [ $lang_exists -eq 0 ]; then
            su - postgres -c "psql ${dbname} -c \"CREATE LANGUAGE plpgsql\""
        fi
}

set_db_to_gmt() {
        local dbname=$1

        su - postgres -c "psql ${dbname} -c \"ALTER DATABASE ${dbname} SET TIMEZONE='GMT'\""
}

create_obm_schema() {
        cd /usr/share/obm-storage/update-install/scripts/creation
        bash ./install_obmdb.sh filldata
        bash ./install_roundcubedb_2.4.sh
        cd -
}

. /etc/dbconfig-common/$1.conf

create_plpgsql_language $dbc_dbname
set_db_to_gmt $dbc_dbname
create_obm_schema

exit 0

