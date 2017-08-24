<?php
///////////////////////////////////////////////////////////////////////////////
// OBM   - Language : French                                                 //
//       - File     : check_date_js.inc                                      //
// 1999-01-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// Fonction JAVASCRIPT qui verifie la validite d'une date                    //
// Les formats acceptes sont JJ/MM/AAAA et le format ISO AAAA-MM-JJ          //
// Une date correcte renvoie true                                            //
// Une date incorrecte ou vide "" renvoie false et affiche un message        //
// Le champ passe en parametre recoit la date correcte au format ISO         //
///////////////////////////////////////////////////////////////////////////////
// $Id$
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

  // Cas du format JJ/MM/AAA

  if (ind1 > 0) {

    day = date.substring(0,ind1); 
    ind2=date.indexOf(\"/\",ind1+1);        
    if (ind2 == -1) {
      alert( nom_date + \" : $l_invalid_date\");
      return false;
    } 
    month = date.substring(ind1+1,ind2); 
    year = date.substring(ind2+1,date.length);
  }

  // Cas du format ISO AAAA-MM-JJ

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

  // Verification des jours, mois et annees

  if ((day.length != 1 && day.length != 2) || (month.length != 1 && month.length != 2) || (year.length != 2 && year.length != 4)) {
    alert( nom_date + \" : $l_invalid_date\");
    return false;
  }

  if (!((day > 0) && (day < 32) && (month > 0) && (month < 13) && (year > 1990) && (year < 2100))) {
    alert( nom_date + \" : $l_invalid_date\");
    return false;
  }

  // On ecrit la date au format ISO
  champ_date.value = year + \"-\" + month + \"-\" + day;

  return true;  
}  
";
