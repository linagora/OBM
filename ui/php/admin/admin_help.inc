3 actions are currently implemented (all are available in html and txt mode).

This page show some informations about the user connected to OBM and about the
database.
===============================================================================
ACTIONS          | args   | Description
===============================================================================
help             |        | Show this help file
===============================================================================
index            |        | Specific to HTML version
                 |        | Show All the Active User And the number Sessions
		 |        | of records in the ActiveUserObm. It also show
		 |        | some data about the database 
===============================================================================
clear_sess       |        | Run the sessions clean up script. 
-------------------------------------------------------------------------------
txt: php admin_index.php -a clear_sess
===============================================================================


txt mode Usage: php admin_index.php [Options]
where Options:
-h, --help help screen
-a action  (avalaible actions)
-m module  (avalaible modules)


