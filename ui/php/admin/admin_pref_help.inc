///////////////////////////////////////////////////////////////////////////////
// OBM - File  : admin_pref_help.inc                                         //
//     - Desc  : Pref admin Help File                                        //
// 2002-07-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

The prefs administration module is a tool to update all OBM users preferences
in one shot.
It is useful when updating OBM to a new version or when the default preferences
are updated and must be spread to the existing users.

It works in txt mode (php from command line) or in html mode (as a module in
OBM administration).

3 actions are currently implemented (all are available in html and txt mode).

For now, only the company module is subject to data inconsistencies.

===============================================================================
ACTIONS          | args   | Description
===============================================================================
help             |        | Show this help file
===============================================================================
index            |        | Specific to HTML version
                 |        | Display the search screen form
===============================================================================
pref_update      |        | Update Users preferences (default values)
-------------------------------------------------------------------------------
txt: php admin_pref_index.php -a pref_update
===============================================================================

txt mode Usage: php admin_pref_index.php [Options]
where Options:
-h, --help help screen
-a action  (avalaible actions)
