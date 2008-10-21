///////////////////////////////////////////////////////////////////////////////
// OBM - File  : user_help.inc                                         //
//     - Desc  : User Help File                                        //
// 2008-10-14 Vincent Bernard                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id: user_help.inc 51 2002-08-29 13:09:56Z mehdi $ //
///////////////////////////////////////////////////////////////////////////////

The User module is a tool to perform operations on OBM users.

It works in txt mode (php from commande line) or in html mode (module Users).

2 actions are currently implemented in txt mode.

Args marked by (bool) can take a boolean value : (0 , false, off) or (1, true, on).
Args marked by (int) can take an integer value : Args value interval of type integer depends of argument.
Args marked by (str) can take a string : Args value of type string containing spaces must be enclosed by '"'.

===============================================================================
ACTIONS          | args   		  		| Description
===============================================================================
help             |        		  		| Show this help file
===============================================================================
batch_processing | -u (int[, int...])	| Perform batch processing for users id
				 |		  		  		| passed with -u argument.
				 |(Optional args)		|
				 | -A (bool)	  		| Archive state
				 | -P (str)		  		| Profile
				 | -M (bool)	  		| Mail perms
				 | -Mq (int)	  		| Mail quota
				 | -N (bool)	  		| Nomade perms
				 | -Ne (bool)	  		| Nomade enable
				 | -Nc (bool)	  		| Nomade local copy
				 | -phone (str)	  		| Phone number
				 | -phone2 (str)  		| Second phone number
				 | -mobile (str)  		| Mobile phone number
				 | -fax (str)	  		| Fax number
				 | -fax2 (str)	  		| Second fax number
				 | -company (str)		| Company name
				 | -direction (str) 	| Direction name
				 | -service (str)		| Service name
				 | -address (str)		| First Address
				 | -address2 (str)		| Second Address
				 | -address3 (str)		| Third Address
				 | -zipcode (str)		| Zip code
				 | -town (str)			| Town name
				 | -expresspostal (str)	| Express postal code
-------------------------------------------------------------------------------
txt: php user_index.php -a batch_processing -u 4,25, 7,8 -A off -P a_profile -M true -Mq 512 -N 1 -Ne false -Nc 0 -phone 0504030201 -company "My company"
===============================================================================



txt mode Usage: php user_index.php [Options] args [optional args]
where Options:
-h, --help help screen
-a action  (avalaible actions)
where args:
-u int[, int...] (Users id list)
where optional args:
Take a look on actions table on top of this help




