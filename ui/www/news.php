<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- $Id$ -->
<html>
<head>
<title>OBM obm.aliacom.fr</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="style01.css" rel="stylesheet" type="text/css">
</head>

<body>
<?php include("menu.html"); ?>

<div class="center">

  <h1>NEWS</h1>
  <div align="center">
    <h2 align="left">News</h2>

    <ul class="detailList">

      <li>
        <div align="left"><b>2004-08-16</b> : version <b>0.8.5</b> released (fixes, functionality and architecture enhancements)
        <br />As Postgres, Privacy handling and Search queries (with limit flag) have been dealt with, on the architectural side it remains only one point to do (allow data from one module to be displayed in an other module) before 0.9 starts. 
        <ol>
          <li> Many Postgres improvement (Document, Invoice, incident, Project,...), near on par with mysql support now
          <li> New functionalities in Deal (quick update), Contact (Vcard export), List, Time (user entry date)
          <li> Enhancements in Document, Todo, Contact, List
          <li> Architecture enhancements for privacy handling, Todo, SQL Limit clause can be toggled on / off
          <li> Performance enhancements : SQL Limit clause can be toggled on / of for all searches, List and Todo
          <li> Fixes in Export CSV, Contact, Deal, Contract, Incident, Invoice, Project, Time, Admin,...
          <li> Many fixes and Clean up preparing 0.9
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2004-08-02</b> : version <b>0.8.4</b> released (<b>Guêpe Release</b>) (fixes and minors enhancements)
        <ol>
          <li> Many enhancements to the List (Mail merge) module
          <li> Comments now automatically stamped in Company, Contact
          <li> Group membership management from User screen
          <li> Fixes and enhancements in column availability (addresses, order)
          <li> Search enhancements for Company, Contact, Deal
          <li> Enhancements in Company, List, Contact, Contract, User
          <li> Fixes in Install, Company, Deal, Group, Contract
          <li> Clean up in external modules calls
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2004-07-13</b> : version <b>0.8.3</b> released (fixes and minors enhancements)
        <ol>
          <li> New multiple categories for deals
          <li> A Contract can now be archived
          <li> More fields in List export to allow better mail merge
          <li> Fixes in Company, Contact, Project, Incident
          <li> Enhancements in Company, List, Contact, Incident, Contract
          <li> French Company Naf code data provided
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2004-06-14</b> :  New FAQ section on obm community site</div>
        <br />
      </li>

      <li>
        <div align="left"><b>2004-06-14</b> : version <b>0.8.2</b> released (fixes and minors enhancments)
        <ol>
          <li> New field NAF code on Company, can be hidden
          <li> A project task can now be updated
          <li> Lists can now be private
          <li> Reporting ans statistics on #companies (by categories, countries and managers)
          <li> Fixes on List (graphical construction), Deal, Document, Contact
          <li> Enhancements in Company, Project, CSV export, List, Calendar
          <li> Optimizations (hidden data no more fetched from DB)
          <li> Install more mandrake friendly
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2004-04-22</b> : version <b>0.8.1</b> released (fixes)
        <ol>
          <li> Corrected some action rights on deal, document and user modules
          <li> Corrected problem that prevent inserting Category with IE 6
          <li> Calendar event users can now be updated again
          <li> Fixes on Todo, Contact, Calendar, graphical Lists, sort order
          <li> Postgres Sorts now case insensitive too
          <li> Enhancements in Company, Contact, Subscriptions, site config, www links...
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2004-03-19</b> : version <b>0.8.0</b> released (<b>Clément Release</b>) (architectural and functionnality enhancments, fixes)
        <ol>
          <li> PostreSQL support (except time and document)
          <li> Spanish translation added
          <li> Many Company, Contact enhancements
          <li> Better configuration (allowing specific site configuration keeping compatibility with generic branch)
          <li> New module : Publication with subscription management
          <li> New Access right and profile models (with module granularity)
          <li> A directory can now be removed in Document
          <li> Graphical query construction in List
          <li> Many Fixes and enhancements in Project, Todo, Calendar,...
          <li> ...
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2003-12-24</b> : version <b>0.7.5</b> released (enhancement)
        <ol>
          <li> PostreSQL support is back (db scripts, modules Company, Contact,Deal and User for now)
          <li> Company enhancements (Phonetics and approximate searches, Categories, datasource...)
          <li> Contact enhancements (Categories, marketing manager, kind, functions, datasource,...)
          <li> New module : Admin Referential to handle global entities
    <li> New tables : Country and DataSource (
          <li> Fixes and enhancements in Project, Deal,...
          <li> Updates on install script and doc
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2003-12-05</b> : version <b>0.7.4</b> released (minor fixes)
        <ol>
          <li> Project module fixes and improvement
          <li> Fixes on install script (typo ';' !)
     <li> Fixes in Incident, Agenda (better export for Outlook), Lang, User,...
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2003-11-24</b> : version <b>0.7.3</b> released (many fixes)
        <ol>
          <li> Project module rewrite
          <li> CSS + Javascript enhancement improve Mozilla 1.0 and IE display
          <li> Specific site configuration now in a separated file
          <li> Fixes on install script (Contact address 3,...)
     <li> Fixes in Project, Time management, Contact, User,...
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2003-10-13</b> : version <b>0.7.2</b> released (major fixes)
        <ol>
          <li> Major fixes on install script (Todo table)
          <li> Minor fixes in Contract, Incident, Project and Todo
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2003-09-29</b> : version <b>0.7.1</b> released (fixes and enhancement)
        <ol>
          <li> New module : todo
          <li> New theme : grey (finally we have more than 1 real theme)
          <li> CVS Export enhancement (delimiter in user preference, default to save with module.csv filename, export all pages...)
	  <li> Document tree now viewed directly from database
	  <li> Time optimizations and enhancement (project list refresh)
	  <li> fixes in Company (deletion), Deal (User comments), Contract, User (deletion)
        </ol>
        </div>
        <br />
      </li>

      <li> 
        <div align="left"><b>2003-09-09</b> : version <b>0.7.0</b> released (major enhancement, functionnality update)</b> !
        <ol>
          <li> New module : <b>Project management</b>. Tasks, members and progress tracking.
          <li> <b>Time management</b> total rewrite to interact with new Project management. 
	  <li> New module : <b>Document</b>, with dual view (DB and filesystem). Documents can be linked to entities (deal,...)
	  <li> New module : <b>User groups</b>. A group consist of users and/or groups.
          <li> CSV export (OBM_DISPLAY) : each dataset or search results can be exported to csv files,
	  <li> Dynamic lists. Contacts lists can now be based on a query (and keeping hard linked members too).
	  <li> Global display reworked. Only one function handle the display, with all display attributes in CSS.
	  <li> Many fixes and enhancements !
        </ol>
        </div>
        <br />
      </li>

      <li> 
        <div align="left"><b>2003-07-18</b> : version <b>0.6.6</b> released (fixes and enhancement) and <b>mailing List is back</b> !
        <ol>
          <li> Company, Contact, Deal, Agenda, Settings and Install fixes and enhancement
	  <li> New pref to allow deal comment sorting (chronological or reverse)
	  <li> Companies can be archived
	  <li> global form parameters handling polishment
	  <li> Install now works flawlessly on RedHat
        </ol>
        </div>
        <br />
      </li>

      <li> 
        <div align="left"><b>2003-06-12</b> : version <b>0.6.5</b> released (fixes)
        <ol>
          <li> Incident, Company and searches fixes
          <li> Install script corrected (table CalendarRight was missing !)
          <li> OBM works with PHP safe_mode On
        </ol>
        </div>
        <br />
      </li>
      <li> 
        <div align="left"><b>2003-05-28</b> : version <b>0.6.4</b> released (fixes)
	<ol>
	  <li>Calendar : private events, emails, rights management and gui enhancements
	  <li>Calendar : vcalendar export (works with Evolution)
	  <li>Incident : fixes, search by company
	  <li>Global users lists now don't display archived users
	  <li>New tools : one user preference propagation, and deals consistencies
	  <li>Deal, Company and Contact fixes
	</ol>
	</div>
	<br />
      </li>
      <li> 
        <div align="left"><b>2003-04-18</b> : version <b>0.6.3</b> released (fixes, polishment and enhancement)
	<ol>
	  <li>Calendar : added Meeting planning, per user selectable display interval
	  <li>Calendar : User selectable display interval
	  <li>Calendar : Rights management (who can read, write your own calendar)
	  <li>Calendar and Incident admin screens
	  <li>Many deal, incident, calendar fixes and enhancement
	</ol>
	</div>
	<br />
      </li>
      <li> 
        <div align="left"><b>2003-03-02</b> : version <b>0.6.2</b> released (calendar work & minor fixes)
	<ol>
	  <li>Major Calendar enhancements,
	  <li>Contact, Deal, documentations fixes
	  <li>Debug flag to show session content
	</ol>
	</div>
	<br />
      </li>
      <li> 
        <div align="left"><b>2003-01-28</b> : version <b>0.6.1</b> released (minor fixes)
	<ol>
	  <li>Calendar, bookmark fixes and enhancements
	  <li>New admin code tool to display function uses and used functions
	  <li>Contacts can be archived
	  <li>Cosmetics and usability improvements (deal, calendar,...)
	  <li>clean up, fixes, polishment
	</ol>
	</div>
	<br />
      </li>
      <li> 
        <div align="left"><b>2003-01-07</b> : version <b>0.6.0</b> released (Major release)
	<ol>
	  <li>New design and theme (with real design now !)
	  <li>XHTML / CSS compliance
	  <li>modules are grouped in sections (tabs in the default theme)
	  <li>New (cleaner, better, nicer) shared calendar (complete rewrite by Mehdi)
	  <li>Better genericity (themes, menus, actions,...)
	  <li>New access right model not code intrusive,
	  <li>New actions model (theme don't need now some hardcoded info)
	  <li>Code quality improvment : Rules defined and applied everywhere for date, url,...
	  <li>Lots, lots of clean up, fixes, polishment
	  <li>Lots of minor functionalities added
	  <li>New web site, documentation, install scripts,...
	</ol>
	</div>
	<br />
      </li>
      <li> 
        <div align="left"><b>2002-08-23</b> : version 0.5.3 released (Fixes and architecture)</div>
      </li>
      <li> 
        <div align="left">
          2002-07-30 : version 0.5.2 released (Big architecture improvements)
        </div>
      </li>
      <li> 
        <div align="left"> 2002-06-10 : version 0.5.1 released </div>
      </li>
      <li> 
        <div align="left">2002-02-01 : version 0.5.0 released </div>
      </li>
      <li> 
        <div align="left">2002-02-01 : OBM bugzilla is open at bugzilla.aliacom.fr 
        </div>
      </li>
      <li> 
        <div align="left">2001-07-29 : OBM mailing list is open </div>
      </li>
      <li> 
        <div align="left">2001-07-28 : version 0.4.1 released </div>
      </li>
      <li> 
        <div align="left">2001-06-13 : version 0.4.0 released </div>
      </li>
      <li> 
        <div align="left">2001-02-15 : version 0.3.3 released with the 
          list module. (minor bug fixes release)</div>
      </li>
      <li> 
        <div align="left"> 2000-10-10 : version 0.3.2 released.</div>
      </li>
    </ul>
    </div>

<?php include("footer.html"); ?>

</body>
</html>
