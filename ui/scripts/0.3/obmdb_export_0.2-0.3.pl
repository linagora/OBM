#!/usr/bin/perl  
#//////////////////////////////////////////////////////////////////////////////
#// OBM - File  : obmexport_0.2-0.3.pl                                       //
#//     - Desc  : Database Export script from OBM 0.2.X towards OBM 0.3.X    //
#// 2000-07-19 - Pierre Baudracco - Last Update : 2000-07-23                 //
#//////////////////////////////////////////////////////////////////////////////

use DBI;                # generic database interface
use Time::localtime;

#//////////////////////////////////////////////////////////////////////////////
#/ Specific parameters                                                       //
#//////////////////////////////////////////////////////////////////////////////
my $debug=0;
my $driver = "mysql";
my $host_old = "localhost";
my $host_new = "localhost";
my $port = "3306";
my $db_old = "obm02";
my $db_new = "obm";
my $user = "web";
my $password = "web";
my $dsn_old = "DBI:$driver:database=$db_old;host=$host_old;port=$port";
my $dsn_new = "DBI:$driver:database=$db_new;host=$host_new;port=$port";
my $obm_importuser = "import";


#//////////////////////////////////////////////////////////////////////////////
#/ Variables                                                                 //
#//////////////////////////////////////////////////////////////////////////////
my $rc;
my $curdate;
my ($dbh_old, $dbh_new);
my ($companytype, @l_companytype, %h_companytype);
my (@company, @company_new, %h_company);
my ($kind, @l_kind, %h_kind);
my (@contact, @contact_new, %h_contact);
my ($dealtype, @l_dealtype, %h_dealtype);
my ($dealstatus, @l_dealstatus, %h_dealstatus);
my ($dealcategory, @l_dealcategory, %h_dealcategory);
my $dealorigin;
my (@deal, @deal_new);


#//////////////////////////////////////////////////////////////////////////////
#/ Main Program                                                              //
#//////////////////////////////////////////////////////////////////////////////
connect_obmdb();
create_importuser();

import_companytype();
import_company();
import_kind();
import_contact();
import_dealtype();
import_dealstatus();
import_dealcategory();
import_dealorigin();
import_deal();
end_script();


#//////////////////////////////////////////////////////////////////////////////
#/ Databases connections                                                     //
#//////////////////////////////////////////////////////////////////////////////
sub connect_obmdb () {

  $dbh_old = DBI->connect($dsn_old, $user, $password);
  if ( !defined($dbh_old)) {
    print "Export : Can't connect to database $db_old ! Exiting...\n";
    exit 0;
  }
  
  $dbh_new = DBI->connect($dsn_new, $user, $password);
  if ( !defined($dbh_new)) {
    print "Export : Can't connect to database $db_new ! Exiting...\n";
    exit 0;
  }
}

#//////////////////////////////////////////////////////////////////////////////
#/ Creation of the user "import"                                             //
#//////////////////////////////////////////////////////////////////////////////
sub create_importuser () {
  print "Creating User '$obm_importuser'\n";

  # Search if the import user already exists
  $req = "select userobm_id from Userobm where userobm_username='$obm_importuser'";
  $sth = $dbh_new->prepare($req);
  $sth->execute();
  if ($sth->rows() < 1) {
    $curdate = iso_date();
    $req = "INSERT INTO Userobm (userobm_timecreate,userobm_username,userobm_password,userobm_perms) VALUES ('$curdate','$obm_importuser','a','admin')";
    $dbh_new->do($req);
  } else {
    print "user $obm_importuser already exists \n";
  }
  
  # Search of the import user ID
  $req = "select userobm_id from Userobm where userobm_username='$obm_importuser'";
  $sth = $dbh_new->prepare($req);
  $sth->execute();
  
  # This should returns only one row
  while ( @l_userobm = $sth->fetchrow_array()) {
    $userobm = $l_userobm[0];
  }
  $sth->finish();
}

#//////////////////////////////////////////////////////////////////////////////
#/ Import : Table CompanyType                                                //
#//////////////////////////////////////////////////////////////////////////////
sub import_companytype () {

  print "Table CompanyType\n";

  $companytype =  $dbh_old->selectall_arrayref("select * from TypeSociete");
  $curdate = iso_date();
  
  $dbh_new->do("delete from CompanyType");
  
  for ($i=0; $i <= $#$companytype; $i++) {
    $req = "insert into CompanyType (companytype_timeupdate,companytype_timecreate,companytype_userupdate,companytype_usercreate,companytype_label) values (null,'" . $curdate . "', null, '" . $userobm . "','" . $$companytype[$i][1] . "')";
    
    $dbh_new->do($req);
    
    $req = "select companytype_id from CompanyType where companytype_label='" . $$companytype[$i][1] . "'";
    $sth = $dbh_new->prepare($req);
    $sth->execute();
    
    # This should returns only one row
    while ( @l_companytype = $sth->fetchrow_array()) {
      $old_id = $$companytype[$i][0];
      $h_companytype{$old_id} = $l_companytype[0];
      if ($debug > 0) {
	print $$companytype[$i][0] . " - " . $h_companytype{$old_id} . "\n";
      }
    }
    $sth->finish();
  }
  print " - $i records inserted successfully\n";
}


#//////////////////////////////////////////////////////////////////////////////
#/ Import : Table Company                                                    //
#//////////////////////////////////////////////////////////////////////////////
sub import_company () {

  print "Table Company\n";

  $sth = $dbh_old->prepare("select * from Societe");
  $sth->execute();
  
  $dbh_new->do("delete from Company");
  
  # Main Loop : each old company
  $i = 0;
  
  while (@company = $sth->fetchrow_array()) {
    $i++;
    
    # Double the single quotes which could appear in some fields
    $company[1] =~ s/\'/\'\'/g;  # Company Name
    $company[3] =~ s/\'/\'\'/g;  # Company Ad1
    $company[4] =~ s/\'/\'\'/g;  # Company Ad2
    $company[6] =~ s/\'/\'\'/g;  # Company Town
    $company[14] =~ s/\'/\'\'/g; # Company Comment
    
    $curdate = iso_date();
    $req = "INSERT INTO Company (company_timeupdate,company_timecreate,company_userupdate,company_usercreate,company_name,company_type_id,company_address1,company_address2,company_zipcode,company_town,company_expresspostal,company_country,company_phone,company_fax,company_web,company_email,company_mailing,company_comment) values (null,'$curdate',null,'$userobm','".$company[1]."','". $h_companytype{$company[2]} ."','". $company[3] ."','". $company[4] ."','". $company[5] ."','". $company[6] ."','". $company[7] ."','". $company[8]. "','". $company[9] ."','". $company[10] ."','". $company[11] ."','". $company[12] ."','" . $company[13]. "','" .$company[14]."')";
    $res = $dbh_new->do($req);

    # If the insert fails, debug info and exit
    if ($res ne 1) {
      print "oldid=" . $company[0] . "\n";
      print $req . "\n";
      end_script();
    }
    
    # Building the company ID hash table h{id_old}=id_new
    
    $req = "select company_id from Company where company_name='" . $company[1] . "' and company_phone='". $company[9] . "'";
    $sth_id = $dbh_new->prepare($req);
    $sth_id->execute();
    
    # This should returns only one row
    while ( @company_new = $sth_id->fetchrow_array()) {
      $old_id = $company[0];
      $h_company{$old_id} = $company_new[0];
      if ($debug > 0) {
	print $old_id . " - " . $h_company{$old_id} . "\n";
      }
    }
    $sth_id->finish();
  }
  $sth->finish();
  
  print " - $i records inserted successfully\n";
}


#//////////////////////////////////////////////////////////////////////////////
#/ Import : Table Kind                                                       //
#//////////////////////////////////////////////////////////////////////////////
sub import_kind () {

  print "Table Kind\n";

  $kind =  $dbh_old->selectall_arrayref("select * from Genre");
  $curdate = iso_date();
  
  $dbh_new->do("delete from Kind");
  
  for ($i=0; $i <= $#$kind; $i++) {
    $req = "insert into Kind (kind_timeupdate,kind_timecreate,kind_userupdate,kind_usercreate,kind_minilabel,kind_label) values (null,'" . $curdate . "', null, '" . $userobm . "','" . $$kind[$i][1] . "','" . $$kind[$i][2] . "')";
    
    $dbh_new->do($req);
    
    $req = "select kind_id from Kind where kind_label='" . $$kind[$i][2] . "'";
    $sth = $dbh_new->prepare($req);
    $sth->execute();
    
    # This should returns only one row
    while ( @l_kind = $sth->fetchrow_array()) {
      $old_id = $$kind[$i][0];
      $h_kind{$old_id} = $l_kind[0];
      if ($debug > 0) {
	print $$kind[$i][0] . " - " . $h_kind{$old_id} . "\n";
      }
    }
    $sth->finish();
  }
  print " - $i records inserted successfully\n";
}


#//////////////////////////////////////////////////////////////////////////////
#/ Import : Table Contact                                                    //
#//////////////////////////////////////////////////////////////////////////////
sub import_contact () {

  print "Table Contact\n";

  $sth = $dbh_old->prepare("select * from Contact");
  $sth->execute();
  
  $dbh_new->do("delete from Contact");
  
  # Main Loop : each old contact
  $i = 0;
  
  while (@contact = $sth->fetchrow_array()) {
    $i++;
    
    # Double the single quotes which could appear in some fields
    $contact[3] =~ s/\'/\'\'/g;  # Contact Name
    $contact[4] =~ s/\'/\'\'/g;  # Contact FirstName
    $contact[5] =~ s/\'/\'\'/g;  # Contact Function
    $contact[10] =~ s/\'/\'\'/g; # Contact Comment
  
    $curdate = iso_date();
    $req = "INSERT INTO Contact (contact_timeupdate,contact_timecreate,contact_userupdate,contact_usercreate,contact_company_id,contact_kind_id,contact_lastname,contact_firstname,contact_address1,contact_address2,contact_zipcode,contact_town,contact_expresspostal,contact_country,contact_function,contact_phone,contact_homephone,contact_mobilephone,contact_fax,contact_email,contact_comment) values (null,'$curdate',null,'$userobm','" . $h_company{$contact[1]} . "','" . $h_kind{$contact[2]} . "','" . $contact[3]."','". $contact[4] ."',null,null,null,null,null,null,'". $contact[5] ."','". $contact[6] ."','". $contact[7] ."',null,'". $contact[8] ."','". $contact[9]. "','". $contact[10] ."')";
    $res = $dbh_new->do($req);

    # If the insert fails, debug info and exit
    if ($res ne 1) {
      print "oldid=" . $contact[0] . "\n";
      print $req . "\n";
      end_script();
    }
    
    # Building the contact ID hash table h{id_old}=id_new
    
    $req = "select contact_id from Contact where contact_lastname='" . $contact[3] . "' and contact_company_id='". $h_company{$contact[1]} . "'";
    $sth_id = $dbh_new->prepare($req);
    $sth_id->execute();
    
    # This should returns only one row
    while ( @contact_new = $sth_id->fetchrow_array()) {
      $old_id = $contact[0];
      $h_contact{$old_id} = $contact_new[0];
      if ($debug > 0) {
	print $old_id . " - " . $h_contact{$old_id} . "\n";
      }
    }
    $sth_id->finish();
  }
  $sth->finish();
  
  print " - $i records inserted successfully\n";
}


#//////////////////////////////////////////////////////////////////////////////
#/ Import : Table DealType                                                   //
#//////////////////////////////////////////////////////////////////////////////
sub import_dealtype () {

  print "Table DealType\n";

  $dealtype =  $dbh_old->selectall_arrayref("select * from TypeAffaire");
  $curdate = iso_date();

  $dbh_new->do("delete from DealType");
  
  for ($i=0; $i <= $#$dealtype; $i++) {
    $req = "insert into DealType (dealtype_timeupdate,dealtype_timecreate,dealtype_userupdate,dealtype_usercreate,dealtype_label) values (null,'" . $curdate . "', null, '" . $userobm . "','" . $$dealtype[$i][1] . "')";
    
    $dbh_new->do($req);
    
    $req = "select dealtype_id from DealType where dealtype_label='" . $$dealtype[$i][1] . "'";
    $sth = $dbh_new->prepare($req);
    $sth->execute();

    # This should returns only one row
    while ( @l_dealtype = $sth->fetchrow_array()) {
      $old_id = $$dealtype[$i][0];
      $h_dealtype{$old_id} = $l_dealtype[0];
      if ($debug > 0) {
	print $$dealtype[$i][0] . " - " . $h_dealtype{$old_id} . "\n";
      }
    }
    $sth->finish();
  }
  print " - $i records inserted successfully\n";
}


#//////////////////////////////////////////////////////////////////////////////
#/ Import : Table DealStatus                                                 //
#//////////////////////////////////////////////////////////////////////////////
sub import_dealstatus () {

  print "Table DealStatus\n";

  $dealstatus =  $dbh_old->selectall_arrayref("select * from EtatAffaire");
  $curdate = iso_date();
  
  $dbh_new->do("delete from DealStatus");
  
  for ($i=0; $i <= $#$dealstatus; $i++) {
    $req = "insert into DealStatus (dealstatus_timeupdate,dealstatus_timecreate,dealstatus_userupdate,dealstatus_usercreate,dealstatus_label) values (null,'" . $curdate . "', null, '" . $userobm . "','" . $$dealstatus[$i][1] . "')";
    
    $dbh_new->do($req);
    
    $req = "select dealstatus_id from DealStatus where dealstatus_label='" . $$dealstatus[$i][1] . "'";
    $sth = $dbh_new->prepare($req);
    $sth->execute();
    
    # This should returns only one row
    while ( @l_dealstatus = $sth->fetchrow_array()) {
      $old_id = $$dealstatus[$i][0];
      $h_dealstatus{$old_id} = $l_dealstatus[0];
      if ($debug > 0) {
	print $$dealstatus[$i][0] . " - " . $h_dealstatus{$old_id} . "\n";
      }
    }
    $sth->finish();
  }
  print " - $i records inserted successfully\n";
}


#//////////////////////////////////////////////////////////////////////////////
#/ Import : Table DealCategory                                               //
#//////////////////////////////////////////////////////////////////////////////
sub import_dealcategory () {

  print "Table DealCategory\n";

  $dealcategory =  $dbh_old->selectall_arrayref("select * from CategorieAffaire");
  $curdate = iso_date();
  
  $dbh_new->do("delete from DealCategory");
  
  for ($i=0; $i <= $#$dealcategory; $i++) {
    $req = "insert into DealCategory (dealcategory_timeupdate,dealcategory_timecreate,dealcategory_userupdate,dealcategory_usercreate,dealcategory_minilabel,dealcategory_label) values (null,'" . $curdate . "', null, '" . $userobm . "','" . $$dealcategory[$i][1] . "','" . $$dealcategory[$i][2]. "')";
    
    $dbh_new->do($req);
    
    $req = "select dealcategory_id from DealCategory where dealcategory_label='" . $$dealcategory[$i][2] . "'";
    $sth = $dbh_new->prepare($req);
    $sth->execute();
    
    # This should returns only one row
    while ( @l_dealcategory = $sth->fetchrow_array()) {
      $old_id = $$dealcategory[$i][0];
      $h_dealcategory{$old_id} = $l_dealcategory[0];
      if ($debug > 0) {
	print $$dealcategory[$i][0] . " - " . $h_dealcategory{$old_id} . "\n";
      }
    }
    $sth->finish();
  }
  print " - $i records inserted successfully\n";
}


#//////////////////////////////////////////////////////////////////////////////
#/ Import : Table DealOrigin (though this table isn't used in OBM)           //
#//////////////////////////////////////////////////////////////////////////////
sub import_dealorigin () {

  print "Table DealOrigin\n";

  $dealorigin =  $dbh_old->selectall_arrayref("select * from OrigineAffaire");
  $curdate = iso_date();
  
  $dbh_new->do("delete from DealOrigin");
  
  for ($i=0; $i <= $#$dealorigin; $i++) {
    $req = "insert into DealOrigin (dealorigin_name,dealorigin_timeupdate,dealorigin_timecreate,dealorigin_userupdate,dealorigin_usercreate) values ('" . $$dealorigin[$i][0] . "'null,'" . $curdate . "', null, '" . $userobm . "')";
    
    if ($debug > 0) {
      print $$dealorigin[$i][0] . "\n";
    }
  }

  print " - $i records inserted successfully\n";
}


#//////////////////////////////////////////////////////////////////////////////
#/ Import : Table Deal                                                       //
#//////////////////////////////////////////////////////////////////////////////
sub import_deal () {

  print "Table Deal\n";

  $sth = $dbh_old->prepare("select * from Affaire");
  $sth->execute();
  
  $dbh_new->do("delete from Deal");
  
  # Main Loop : each old deal
  $i = 0;
  
  while (@deal = $sth->fetchrow_array()) {
    $i++;
    
    # Double the single quotes which could appear in some fields
    $deal[1] =~ s/\'/\'\'/g;  # Deal Label
    $deal[18] =~ s/\'/\'\'/g; # Deal Comment
    
    $curdate = iso_date();
    $req = "INSERT INTO Deal (deal_timeupdate,deal_timecreate,deal_userupdate,deal_usercreate,deal_label,deal_datebegin,deal_type_id,deal_category_id,deal_company_id,deal_contact1_id,deal_contact2_id,deal_manager_id,deal_origin_id,deal_marketingmanager_id,deal_technicalmanager_id,deal_proposal,deal_dateproposal,deal_amount,deal_status_id,deal_datealarm,deal_comment,deal_archive) values ('" .$deal[3] . "','$curdate',null,'$userobm','" . $deal[1] . "','" . $deal[2] . "','" . $h_dealtype{$deal[4]} . "','" . $h_dealcategory{$deal[5]} . "','" . $h_company{$deal[6]} . "','" . $h_contact{$deal[7]} . "','" . $h_contact{$deal[8]} . "',null,null,'" . $h_contact{$deal[11]} . "','" . $h_contact{$deal[12]} . "','" . $deal[13]."','". $deal[14] ."','". $deal[15] ."','". $h_dealstatus{$deal[16]} ."','". $deal[17] ."','". $deal[18] ."','". $deal[19] ."')";
    $res = $dbh_new->do($req);
    
    # If the insert fails, debug info and exit
    if ($res ne 1) {
      print "oldid=" . $deal[0] . "\n";
      print $req . "\n";
      end_script();
    }
    
    # Building the deal ID hash table h{id_old}=id_new
    
    $req = "select deal_id from Deal where deal_label='" . $deal[1] . "' and deal_company_id='". $h_company{$deal[6]} . "'";
    $sth_id = $dbh_new->prepare($req);
    $sth_id->execute();
    
    # This should returns only one row
    while ( @deal_new = $sth_id->fetchrow_array()) {
      $old_id = $deal[0];
      $h_deal{$old_id} = $deal_new[0];
      if ($debug > 0) {
	print $old_id . " - " . $h_deal{$old_id} . "\n";
      }
    }
    $sth_id->finish();
  }
  $sth->finish();

  print " - $i records inserted successfully\n";
}




#//////////////////////////////////////////////////////////////////////////////
#/ Fonction qui termine le programme (deconnexion des bases et exit)         //
#//////////////////////////////////////////////////////////////////////////////
sub end_script () {

  $rc = $dbh_old->disconnect;
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
