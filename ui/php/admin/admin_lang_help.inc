///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_lang_help.inc                                          //
//     - Desc : lang admin Help File                                         //
// 2001-12-27 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

The lang administration module is a tool to help manage, synchronize and
translate OBM language files.

It works in txt mode (php from command line) or in html mode (as a module in
OBM administration).

6 actions are currently implemented (all are available in html mode, only three
in txt mode).

modules, langs and themes valid values are automaticaly retrieved from the OBM
sources.

===============================================================================
ACTIONS          | args   | Description
===============================================================================
help             |        | Show this help file
===============================================================================
index            |        | Specific to HTML version
                 |        | Display the search screen form
===============================================================================
show_src         | module | Show variables referenced in the module source
                 |        | files (module_index.php, module_display.inc
                 |        | module_query.inc, module_js.inc)
                 |        | used vars : vars really used
                 |        | unused vars : vars present only in a global declar.
-------------------------------------------------------------------------------
txt: php admin_lang_index.php -a show_src -m deal
===============================================================================
show_lang        | module | Compares variables referenced in the module source
                 | lang   | with variables defined in the lang for the module
-------------------------------------------------------------------------------
                 | notions used for vars :
                 | - referenced : the variable is present in source files
                 | - used       : referenced and used (countrary to unused)
                 | - unused     : referenced but only in declaration (global)
                 | - defined    : present in language file definition
-------------------------------------------------------------------------------
                 | variables are classified in 5 groups | Suggestion
 used and defined : used in source, defined in lang  -> Great
 used but not def : used in src, not defined in lang -> to add in lang file
 unused and def   : global in src, defined in lang   -> miss, mispelled in src
 unused, not def  : global in src, not def in lang   -> remove from global dec
 def, not referen : in lang but not in source        -> deprecated var in lang
-------------------------------------------------------------------------------
txt: php admin_lang_index.php -a show_lang -m deal -l en
===============================================================================
comp_lang        | module | Compares variables referenced in the module source
                 | lang1  | for both languages.
                 | lang2  | It helps to sync lang files and translation
-------------------------------------------------------------------------------
Not implemented in txt mode 
===============================================================================
comp_header_lang | theme  | Special case for action 'comp_lang' for the special
                 | lang1  | module header. As this module is theme dependant
                 | lang2  | and its files are different from other modules :
                 |        | source : obminclude/theme/$theme/menu.inc
-------------------------------------------------------------------------------
Not implemented in txt mode 
===============================================================================

txt mode Usage: php admin_lang_index.php [Options]
where Options:
-h, --help help screen
-a action  (avalaible actions)
-m module  (avalaible modules)
-l lang    (avalaible langs)
-t theme   (avalaible themes)
