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
        <div align="left"><b>2004-11-03</b> : version <b>0.8.8</b> released (fixes)
        <ol>
          <li> ACCESSKEY support, better focus handling
          <li> More site config parameters (allow * in searches, allow auto-format some fields, visiblue fields...)
          <li> Reworked Contact category assignments (better ergonomics)
          <li> Enhancement in Company category search, contact email search, contact address handling
          <li> Company Statistics can be based on a given List
          <li> Fixes in modules Company, Contact, Calendar List, Time, Admin data
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2004-09-21</b> : version <b>0.8.7</b> released (fixes)
        <ol>
          <li> Many fixes in Country handling, sql limit tuning parameter
          <li> Fixes in Mail handling and new email notification from Incident
          <li> Many small fixes in modules Company, Deal, List, Calendar, Stats, Incident, Invoice, ExportCSV
        </ol>
        </div>
        <br />
      </li>

      <li>
        <div align="left"><b>2004-09-02</b> : version <b>0.8.6</b> released (new functionality and fixes)
        <ol>
          <li> Invoice module total rewrite
          <li> Postgres Install fixes
          <li> Countries handling enhancements (now referenced by iso code)
          <li> New Company European VAT field
          <li> New links to Document from Project, Invoice
          <li> Many enahancement and fixes in Company, Deal, List, Document
        </ol>
        </div>
        <br />
      </li>

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

    </ul>
    </div>

<?php include("footer.html"); ?>

</body>
</html>
