///////////////////////////////////////////////////////////////////////////////
// OBM - File  : admin_data_help.inc                                         //
//     - Desc  : Data admin Help File                                        //
// 2002-07-01 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

The data administration module is a tool to check OBM database consistency.
Especially some fields thar are stored instead of being calculated for
performance (eg: in company module, company_contact_number, company_deal_number).

It works in txt mode (php from command line) or in html mode (as a module in
OBM administration).

4 actions are currently implemented (all are available in html and txt mode).

modules valid values are automaticaly retrieved from the OBM sources.

For now, only the company module is subject to data inconsistencies.

===============================================================================
ACTIONS          | args   | Description
===============================================================================
help             |        | Show this help file
===============================================================================
index            |        | Specific to HTML version
                 |        | Display the search screen form
===============================================================================
data_show        | module | Show Data inconsistency from module
-------------------------------------------------------------------------------
txt: php admin_data_index.php -a data_show -m company
===============================================================================
data_update      | module | Show and update Data inconsistency from module
-------------------------------------------------------------------------------
txt: php admin_data_index.php -a data_update -m company
===============================================================================



txt mode Usage: php admin_data_index.php [Options]
where Options:
-h, --help help screen
-a action  (avalaible actions)
-m module  (avalaible modules)




