#!/usr/bin/perl  
#//////////////////////////////////////////////////////////////////////////////
#// This script inserts preferences for all obm's users.
#// The obm database have to be in 0.3.3 format
#// It means you should have update the database by using the sql script "update_obm_db_.0.3.3-0.4.mysql.sql" 
#//////////////////////////////////////////////////////////////////////////////

use DBI;                # generic database interface
use Time::localtime;


#//////////////////////////////////////////////////////////////////////////////
#/ Specific parameters                                                       //
#//////////////////////////////////////////////////////////////////////////////
my $driver = "mysql";
my $host_new = "localhost";
my $port = "3306";
my $db_new="obm_fictif";
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





#////////////////////////////////////////////////////////////////////////////////////////
#//  Each user must have his preferences profile in UserObmPref and the xxxDisplay tables
#////////////////////////////////////////////////////////////////////////////////////////
sub import_preferences () {

    print "Importing preferences profile for each obm user \n";

    $query ="select userobm_id from UserObm";
    $sth=$dbh_new->prepare($query);
    $sth->execute();

    while ( @l_userobm = $sth->fetchrow_array()) {

      #---- for the User's prefrences 
      $query="insert into UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'set_lang','$set_lang_default')";
      $dbh_new->do($query);
      $query="insert into UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'set_theme','$set_theme_default')";
      $dbh_new->do($query);
      $query="insert into UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'set_debug','$set_debug_default')";
      $dbh_new->do($query);
      $query="insert into UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'set_rows','$set_rows_default')";
      $dbh_new->do($query);
      $query="insert into UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'set_display','$set_display_default')";
      $dbh_new->do($query);
      $query="insert into UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'last_company','$last_company_default')";
      $dbh_new->do($query);
      $query="insert into UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'last_contact','$last_contact_default')";
      $dbh_new->do($query);
      $query="insert into UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'last_deal','$last_deal_default')";
      $dbh_new->do($query);
      $query="insert into UserObmPref(userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'order_contactslist','$order_contactslist_default')";
      $dbh_new->do($query);
      $query="insert into UserObmPref(userobmpref_id,userobmpref_option,userobmpref_choice) values ($l_userobm[0],'order_servicecomputer','$order_servicecomputer_default')";
      $dbh_new->do($query);
      
      
      #---- for the Deal display preferences tables : 
      $query="insert into DealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'deal_label',1,2)";
      $dbh_new->do($query);
      $query="insert into DealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'deal_company_name',2,2)";
      $dbh_new->do($query);
      $query="insert into DealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'dealtype_label',3,1)";
      $dbh_new->do($query);
      $query="insert into DealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'dealcategory_minilabel',4,1)";
      $dbh_new->do($query);
      $query="insert into DealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'dealstatus_label',5,1)";
      $dbh_new->do($query);
      $query="insert into DealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'deal_todo',6,1)";
      $dbh_new->do($query);
      $query="insert into DealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'deal_datealarm',7,2)";
      $dbh_new->do($query);


      #---- for the ParentDeal display preferences tables : 
      $query="insert into ParentDealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'parentdeal_label',1,2)";
      $dbh_new->do($query);
      $query="insert into ParentDealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'parentdeal_marketing_lastname',2,1)";
      $dbh_new->do($query);
      $query="insert into ParentDealDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'parentdeal_technical_lastname',3,1)";
      $dbh_new->do($query);


      #---- for the List display preferences tables : 
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_subject',1,2)" ;  
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_nb_contact',2,2)" ;  
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_usercreate',3,1)" ;    
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_timecreate',4,1)" ;  
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_userupdate',5,1)" ;   
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_timeupdate',6,1)" ;
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_contact_lastname',1,2)" ;   
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_contact_firstname',2,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_contact_function',3,1)" ;   
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values($l_userobm[0],'list_contact_company_id',4,2)" ;   
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_contact_town',5,1)" ;  
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_contact_phone',6,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_contact_mobilephone',7,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'list_contact_email',8,1)" ; 
      $dbh_new->do($query);

      
      #---- for the Computer display preferences tables :
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'computer_ip',1,2)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'computer_user',2,1)" ;  
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'computer_usercreate',3,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'computer_timecreate',4,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'computer_userupdate',5,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'computer_timeupdate',6,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'computer_auth_scan',7,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'computer_date_lastscan',8,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'computer_comments',9,1)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'service_name',1,2)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'service_port',2,2)" ; 
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'service_proto',3,2)" ;    
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'service_desc',4,1)" ;  
      $dbh_new->do($query);
      $query = "insert into ComputerDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0],'service_status',5,2)" ;    
      $dbh_new->do($query);

      #---- for the Company display preferences tables :
      $query = "insert into CompanyDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'company_name',1,2)" ;
      $dbh_new->do($query);   
      $query = "insert into CompanyDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'company_contacts',2,1)" ;
      $dbh_new->do($query);
      $query = "insert into CompanyDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'company_new_contact',3,1)" ;
      $dbh_new->do($query);
      $query = "insert into CompanyDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'companytype_label',4,1)" ;
      $dbh_new->do($query);
      $query = "insert into CompanyDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'company_address1',5,1)" ;
      $dbh_new->do($query);
      $query = "insert into CompanyDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'company_phone',6,1)" ;
      $dbh_new->do($query);
      $query = "insert into CompanyDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'company_fax',7,1)" ;
      $dbh_new->do($query);
      $query = "insert into CompanyDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'company_email',8,1)" ;
      $dbh_new->do($query);
      $query = "insert into CompanyDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'company_web',9,1)" ;
      $dbh_new->do($query);


      #---- for the Contact display preferences tables :
      $query = "insert into ContactDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'contact_lastname',1,2)" ;
      $dbh_new->do($query);
      $query = "insert into ContactDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'contact_firstname',2,1)" ;
      $dbh_new->do($query);
      $query = "insert into ContactDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'contact_function',3,1)" ;
      $dbh_new->do($query);
      $query = "insert into ContactDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'contact_company_name',4,2)" ;
      $dbh_new->do($query);
      $query = "insert into ContactDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'contact_phone',5,1)" ;
      $dbh_new->do($query);
      $query = "insert into ContactDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'contact_homephone',6,1)" ;
      $dbh_new->do($query);
      $query = "insert into ContactDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'contact_mobilephone',7,1)" ;
      $dbh_new->do($query);
      $query = "insert into ContactDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) values ($l_userobm[0], 'contact_email',8,1)" ;
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
