///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_code_help.inc                                          //
//     - Desc : Code admin Help File                                         //
// 2002-07-16 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

The Code administration module is a compilation of tools to help checking that
the code respects OBM coding standards and to help keeping the code clean.

Each tool actually check only one aspect of the standard.

It works in txt mode (php from command line) or in html mode (as a module in
OBM administration).

3 actions are currently implemented (all are available in html and txt mode).

===============================================================================
ACTIONS          | args   | Description
===============================================================================
help             |        | Show this help file
===============================================================================
index            |        | Specific to HTML version
                 |        | Display the search screen form
===============================================================================
show_amp         |        | show (line, file) where & could be misuse in URL
                 |        | '& amp;' (in one word) is required in URL, not &
                 |        | Display each occurence of '&' except from a given
                 |        | word list (eg: '& nbsp;')
                 |        | It parse each file (.php, .inc) under the php
                 |        | directory
===============================================================================
func_unused      |        | display each defined functions with infos (file,
                 |        | line number, # used occurence)
                 |        | Usefull to show unused defined functions
===============================================================================
function_uses    |        | display each use of the function, with infos (file,
                 |        | line number)
-------------------------------------------------------------------------------
txt: php admin_code_index.php -a show_amp
===============================================================================

txt mode Usage: php admin_code_index.php [Options]
where Options:
-h, --help help screen
-a action  (avalaible actions)
