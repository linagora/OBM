#!/usr/bin/perl  
#//////////////////////////////////////////////////////////////////////////////
#// This script new preferences for all obm's users.
#// The obm database have to be in 0.5 format
#// It means you should have update the database by using the sql script "update_obm_db_.0.4.x-0.5.mysql.sql" 
#//////////////////////////////////////////////////////////////////////////////

use DBI;                # generic database interface
use Time::localtime;


#//////////////////////////////////////////////////////////////////////////////
#/ Specific parameters                                                       //
#//////////////////////////////////////////////////////////////////////////////
my $driver = "mysql";
my $host_new = "localhost";
my $port = "3306";
my $db_new="obm";
my $user = "web";
my $password = "web";

my $dsn_new = "DBI:$driver:$db_new:$host_new:$port";
my $obm_importuser = "import";



#//////////////////////////////////////////////////////////////////////////////
#/ Default preferences variables                                               
#//////////////////////////////////////////////////////////////////////////////
my $set_lang_default="en";
my $set_theme_default="standard";
my $set_rows_default=10;
my $set_display_default="no";
my $set_debug_default=0;
my $last_deal_default=0;
my $last_company_default=0;
my $last_contact_default=0;
my $order_servicecomputer_default="service_port";
my $order_contactslist_default="contact_lastname";
my $set_day_weekstart_default="monday";

#//////////////////////////////////////////////////////////////////////////////
#/ Variables                                                                 //
#//////////////////////////////////////////////////////////////////////////////
my $rc;
my $curdate;
my ($dbh_old, $dbh_new);
my @l_userobm;



#//////////////////////////////////////////////////////////////////////////////
#/ Main Program                                                              //
#//////////////////////////////////////////////////////////////////////////////
connect_obmdb();
import_preferences();
transfert_contactdisplay();
transfert_companydisplay();
transfert_dealdisplay();
transfert_parentdealdisplay();
transfert_listdisplay();
transfert_computerdisplay();
transfert_invoicedisplay ();
transfert_paymentdisplay ();
transfert_accountdisplay ();
end_script();


#//////////////////////////////////////////////////////////////////////////////
#/ Databases connections                                                     //
#//////////////////////////////////////////////////////////////////////////////
sub connect_obmdb () {

  $dbh_new = DBI->connect($dsn_new, $user, $password);
  if ( !defined($dbh_new)) {
    print "Export : Can't connect to database $db_new ! Exiting...\n";
    exit 0;
  }
}


#//////////////////////////////////////////////////////////////////////////////
# Each user must have his preferences profile in UserObmPref and the
#  xxxDisplay tables
#//////////////////////////////////////////////////////////////////////////////
sub import_preferences () {

    print "Importing preferences profile for each obm user \n";

    $query ="select userobm_id from UserObm";
    $sth=$dbh_new->prepare($query);
    $sth->execute();

    while ( @l_userobm = $sth->fetchrow_array()) {

      #---- for the User's prefrences 
      $query="insert into UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) values ($l_userobm[0],'set_day_weekstart','$set_day_weekstart_default')";
      $dbh_new->do($query);
      
      #---- for the List display preferences tables :
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_name',1,2)" ;  
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_email',7,1)" ;  
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_state',8,2)" ;  
      $dbh_new->do($query);

    }

    $sth->finish();

}


#//////////////////////////////////////////////////////////////////////////////
# Transfert ContactDisplay in DisplayPref
#//////////////////////////////////////////////////////////////////////////////
sub transfert_contactdisplay () {

  print "\nTransferring ContactDisplay to DisplayPref \n";
  $entity = "contact";  

  $query = "select * from ContactDisplay";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while ( @l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();
}    


#//////////////////////////////////////////////////////////////////////////////
# Transfert CompanyDisplay in DisplayPref
#//////////////////////////////////////////////////////////////////////////////
sub transfert_companydisplay () {

  print "\nTransferring CompanyDisplay to DisplayPref \n";
  $entity = "company";  

  $query = "select * from CompanyDisplay";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while (@l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();
}    


#//////////////////////////////////////////////////////////////////////////////
# Transfert DealDisplay in DisplayPref
#//////////////////////////////////////////////////////////////////////////////
sub transfert_dealdisplay () {

  print "\nTransferring DealDisplay to DisplayPref \n";
  $entity = "deal";  

  $query = "select * from DealDisplay";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while (@l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();
}    


#//////////////////////////////////////////////////////////////////////////////
# Transfert ParentDealDisplay in DisplayPref
#//////////////////////////////////////////////////////////////////////////////
sub transfert_parentdealdisplay () {

  print "\nTransferring ParentDealDisplay to DisplayPref \n";
  $entity = "parentdeal";

  $query = "select * from ParentDealDisplay";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while (@l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();
}    


#//////////////////////////////////////////////////////////////////////////////
# Transfert ListDisplay (list and list_contact) in DisplayPref
#//////////////////////////////////////////////////////////////////////////////
sub transfert_listdisplay () {

  print "\nTransferring ListDisplay (list) to DisplayPref \n";
  $entity = "list";  

  $query = "select * from ListDisplay where display_fieldname not like 'list_contact_%'";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while (@l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();


  print "\nTransferring ListDisplay (list_contact) to DisplayPref \n";
  $entity = "list_contact";  

  $query = "select * from ListDisplay where display_fieldname like 'list_contact_%'";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while (@l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();
}    



#//////////////////////////////////////////////////////////////////////////////
# Transfert ComputerDisplay in DisplayPref
#//////////////////////////////////////////////////////////////////////////////
sub transfert_computerdisplay () {

  print "\nTransferring ComputerDisplay to DisplayPref \n";
  $entity = "computer";  

  $query = "select * from ComputerDisplay";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while (@l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();
}    
    
#//////////////////////////////////////////////////////////////////////////////
# Transfert InvoiceDisplay in DisplayPref
#//////////////////////////////////////////////////////////////////////////////
sub transfert_invoicedisplay () {

  print "\nTransferring InvoiceDisplay to DisplayPref \n";
  $entity = "invoice";  

  $query = "select * from InvoiceDisplay".
      " where display_fieldname not like 'invoice_deal_%'".
      " and display_fieldname not like 'invoice_pay_%'";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while (@l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();
}    
    
#//////////////////////////////////////////////////////////////////////////////
# Transfert PaymentDisplay in DisplayPref
#//////////////////////////////////////////////////////////////////////////////
sub transfert_paymentdisplay () {

  print "\nTransferring PaymentDisplay to DisplayPref \n";
  $entity = "payment";  

  $query = "select * from PaymentDisplay".
      " where display_fieldname not like 'pay_inv_%'";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while (@l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();
}    
    
#//////////////////////////////////////////////////////////////////////////////
# Transfert AccountDisplay in DisplayPref
#//////////////////////////////////////////////////////////////////////////////
sub transfert_accountdisplay () {

  print "\nTransferring AccountDisplay to DisplayPref \n";
  $entity = "payment";  

  $query = "select * from AccountDisplay ".
      "where display_fieldname not like 'account_pay_%'";
  $sth=$dbh_new->prepare($query);
  $sth->execute();
  
  while (@l_line = $sth->fetchrow_array()) {
    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$l_line[0]', '$entity', '$l_line[1]', '$l_line[2]', '$l_line[3]')";
    $dbh_new->do($query);
  }

  $sth->finish();
}    
    

#//////////////////////////////////////////////////////////////////////////////
#/ Fonction qui termine le programme (deconnexion des bases et exit)         //
#//////////////////////////////////////////////////////////////////////////////
sub end_script () {

#  $rc = $dbh_old->disconnect;
  $rc = $dbh_new->disconnect;

  exit 0;
}


#//////////////////////////////////////////////////////////////////////////////
#/ Fonction qui retourne la date actuelle au format ISO                      //
#//////////////////////////////////////////////////////////////////////////////
sub iso_date () {
    my ($tm, $curdate);
    
    $tm = localtime;
    $curdate = $tm->year+1900 . "-";
    $curdate .= $tm->mon+1 . "-";
    $curdate .= $tm->mday . " ";
    $curdate .= $tm->hour . ":";
    $curdate .= $tm->min . ":";
    $curdate .= $tm->sec;
    
    return $curdate;
}
