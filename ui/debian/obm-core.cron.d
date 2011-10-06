#
# Regular cron jobs for the obm-ui package, manage all cron for OBM
#
* *	* * *	root /usr/bin/php -d memory_limit=128M /usr/share/obm/www/cron/cron.php
