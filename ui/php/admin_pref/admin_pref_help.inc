///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_pref_help.inc                                          //
//     - Desc : Pref admin Help File                                         //
// 2002-07-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

The prefs administration module is a tool to manage OBM Users preferences.

The user_pref_update action allow to update all OBM users preferences in one
shot.
It is useful when updating OBM to a new version or when the default preferences
are updated and must be spread to the existing users.
(default preferences are set in the obmdb_default_values_version.sql file which
must be reloaded then in OBM database).

It works in txt mode (php from command line) or in html mode (as a module in
OBM administration).

4 actions are currently implemented (all are available in html and txt mode).

===============================================================================
ACTIONS              | args   | Description
===============================================================================
help                 |        | Show this help file
===============================================================================
index                |        | Specific to HTML version
                     |        | Display the search screen form
===============================================================================
user_pref_update     |        | Update Users preferences (default values)
-------------------------------------------------------------------------------
user_pref_update_one | -o -v  | Update one User preference (default values)
-------------------------------------------------------------------------------
txt: php admin_pref_index.php -a user_pref_update
txt: php admin_pref_index.php -a user_pref_update_one -o last_account -v 0
===============================================================================

txt mode Usage: php admin_pref_index.php [Options]
where Options:
-h, --help help screen
-a action  (avalaible actions)
-o option, --option option
-v value, --value value