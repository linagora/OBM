3 actions are currently implemented (all are available in html and txt mode).

This page show some informations about the user connected to OBM and about the
database.
===============================================================================
ACTIONS          | args   | Description
===============================================================================
help             |        | Show this help file
===============================================================================
index            |        | Specific to HTML version
===============================================================================
download_report  | report | show the return of the report. 
-------------------------------------------------------------------------------
txt: php report_index.php -a download_report -r UserReportCommand 
===============================================================================


txt mode Usage: php report_index.php [Options]
where Options:
-h, --help help screen
-a action  (avalaible actions)
-r report  (avalaible reports)
-s service (available services only user)
