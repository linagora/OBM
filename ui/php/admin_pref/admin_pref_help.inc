///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_pref_help.inc                                          //
//     - Desc : Pref admin Help File                                         //
// 2002-07-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

The prefs administration module is a tool to manage OBM preferences.
There are two sorts of preferences.
- Global preferences, with global OBM scope like sessions lifetime value.
- User preferences, specific to each user.

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
ACTIONS            | args   | Description
===============================================================================
help               |        | Show this help file
===============================================================================
index              |        | Specific to HTML version
                   |        | Display the search screen form
===============================================================================
user_pref_update   |        | Update Users preferences (default values)
-------------------------------------------------------------------------------
global_pref_update | $param | Update Global preferences (values as params)
-------------------------------------------------------------------------------
txt: php admin_pref_index.php -a user_pref_update
txt: php4 admin_pref_index.php -a global_pref_update -lf 3600
===============================================================================

txt mode Usage: php admin_pref_index.php [Options]
where Options:
-h, --help help screen
-a action  (avalaible actions)
-lifetime lifetime, -lf lifetime : session lifetime (seconds)
