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
  <div align="center">
    <img src="images/accueil_www.png">
    <br />
  </div>

  <div align="center">
    <strong>OBM is a free business management application (crm ? :))</strong>
  </div>
    <h2 align="left">News</h2>

    <ul class="detailList">

      <li>
        <div align="left"><b>2004-08-16</b> : version <b>0.8.5</b> released (fixes, functionality and architecture enhancements)
        <br />As Postgres, Privacy handling and Search queries (with limit flag) have been dealt with, it remains only one point to do (allow data from one module to be displayed in an other module) on the architectural side before 0.9 starts. 
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

    </ul>
    </div>

<?php include("footer.html"); ?>

</body>
</html>
