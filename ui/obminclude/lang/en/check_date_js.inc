<?php
///////////////////////////////////////////////////////////////////////////////
// OBM   - Language : English                                                //
//       - File     : check_date_js.inc                                      //
// 1999-01-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// JAVASCRIPT function that check the validity of a given date               //
// Formats accepted are MM/JJ/AAAA and the ISO format ISO AAAA-MM-JJ         //
// A correct date return true                                                //
// An incorrect or empty date returns false and displays an alert            //
// The field given as argument receive the correct date in the ISO format    //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
function check_date(champ_date, nom_date) {
  var day = \"\";
  var month = \"\";
  var year = \"\";        
  var ind1=0;   
  var ind2=0;   

  date = champ_date.value;

  if (date == \"\") {
    alert( nom_date + \" : $l_fill_date\");
    return false;
  } 

  ind1=date.indexOf(\"/\");       

  // MM/JJ/AAA format case

  if (ind1 > 0) {

    month = date.substring(0,ind1); 
    ind2=date.indexOf(\"/\",ind1+1);        
    if (ind2 == -1) {
      alert( nom_date + \" : $l_invalid_date\");
      return false;
    } 
    day = date.substring(ind1+1,ind2); 
    year = date.substring(ind2+1,date.length);
  }

  // ISO AAAA-MM-JJ format case

  else { 

    ind1=date.indexOf(\"-\");       
    if (ind1 == -1) {
      alert( nom_date + \" : $l_invalid_date\");
      return false;
    }

    year = date.substring(0,ind1);
    ind2=date.indexOf(\"-\",ind1+1);        
    if (ind2 == -1) {
      alert( nom_date + \" : $l_invalid_date\");
      return false;
    } 
    month = date.substring(ind1+1,ind2); 
    day = date.substring(ind2+1,date.length);
  }

  // Days, monthes and years verifications

  if ((day.length != 1 && day.length != 2) || (month.length != 1 && month.length != 2) || (year.length != 2 && year.length != 4)) {
    alert( nom_date + \" : $l_invalid_date\");
    return false;
  }

  if (!((day > 0) && (day < 32) && (month > 0) && (month < 13) && (year > 1990) && (year < 2100))) {
    alert( nom_date + \" : $l_invalid_date\");
    return false;
  }

  // We write the date in the ISO format

  champ_date.value = year + \"-\" + month + \"-\" + day;

  return true;  
}  
";
