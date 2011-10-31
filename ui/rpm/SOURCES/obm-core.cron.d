#
# Regular cron jobs for the obm-core package, manage all cron for OBM
#
* *	* * *	root /usr/bin/php -d memory_limit=128M /usr/share/obm/cron/cron.php
