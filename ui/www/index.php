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
    </ul>
    </div>

<?php include("footer.html"); ?>

</body>
</html>
