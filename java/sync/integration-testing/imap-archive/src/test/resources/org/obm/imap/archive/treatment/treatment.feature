Feature: Mail archive - Treatment

  Scenario: archive treatment on INBOX containing 5 mails one month old
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on INBOX containing 4 mails older one month old and 1 mail one day old 
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 4 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And this user has 1 mail at "2014-12-09T10:00:00Z" in this folder with subject "subject not archived"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 4 mails should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on INBOX containing 5 mails in 5 different years 
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 1 mail at "2010-11-08T10:00:00Z" in this folder with subject "subject 2010"
    And this user has 1 mail at "2011-11-08T10:00:00Z" in this folder with subject "subject 2011"
    And this user has 1 mail at "2012-11-08T10:00:00Z" in this folder with subject "subject 2012"
    And this user has 1 mail at "2013-11-08T10:00:00Z" in this folder with subject "subject 2013"
    And this user has 1 mail at "2014-11-08T10:00:00Z" in this folder with subject "subject 2014"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2010/INBOX@mydomain.org" imap folder with subject "subject 2010"
    And 1 mail should be archived in the "user/usera/arChive/2011/INBOX@mydomain.org" imap folder with subject "subject 2011"
    And 1 mail should be archived in the "user/usera/arChive/2012/INBOX@mydomain.org" imap folder with subject "subject 2012"
    And 1 mail should be archived in the "user/usera/arChive/2013/INBOX@mydomain.org" imap folder with subject "subject 2013"
    And 1 mail should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject 2014"

  Scenario: archive treatment on INBOX containing 5 mails in 5 different years with UIDs not ordered by date
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 1 mail at "2013-11-08T10:00:00Z" in this folder with subject "subject 2013"
    And this user has 1 mail at "2011-11-08T10:00:00Z" in this folder with subject "subject 2011"
    And this user has 1 mail at "2012-11-08T10:00:00Z" in this folder with subject "subject 2012"
    And this user has 1 mail at "2014-11-08T10:00:00Z" in this folder with subject "subject 2014"
    And this user has 1 mail at "2010-11-08T10:00:00Z" in this folder with subject "subject 2010"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2010/INBOX@mydomain.org" imap folder with subject "subject 2010"
    And 1 mail should be archived in the "user/usera/arChive/2011/INBOX@mydomain.org" imap folder with subject "subject 2011"
    And 1 mail should be archived in the "user/usera/arChive/2012/INBOX@mydomain.org" imap folder with subject "subject 2012"
    And 1 mail should be archived in the "user/usera/arChive/2013/INBOX@mydomain.org" imap folder with subject "subject 2013"
    And 1 mail should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject 2014"

  Scenario: archive treatment on a folder containing 5 mails one month old
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org, user/usera/myFolder@mydomain.org" imap folders
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on a folder containing 4 mails older one month old and 1 mail one day old 
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org, user/usera/myFolder@mydomain.org" imap folders
    And this user has 4 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And this user has 1 mail at "2014-12-09T10:00:00Z" in this folder with subject "subject not archived"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 4 mails should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on a folder containing 5 mails in 5 different years 
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org, user/usera/myFolder@mydomain.org" imap folders
    And this user has 1 mail at "2010-11-08T10:00:00Z" in this folder with subject "subject 2010"
    And this user has 1 mail at "2011-11-08T10:00:00Z" in this folder with subject "subject 2011"
    And this user has 1 mail at "2012-11-08T10:00:00Z" in this folder with subject "subject 2012"
    And this user has 1 mail at "2013-11-08T10:00:00Z" in this folder with subject "subject 2013"
    And this user has 1 mail at "2014-11-08T10:00:00Z" in this folder with subject "subject 2014"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2010/myFolder@mydomain.org" imap folder with subject "subject 2010"
    And 1 mail should be archived in the "user/usera/arChive/2011/myFolder@mydomain.org" imap folder with subject "subject 2011"
    And 1 mail should be archived in the "user/usera/arChive/2012/myFolder@mydomain.org" imap folder with subject "subject 2012"
    And 1 mail should be archived in the "user/usera/arChive/2013/myFolder@mydomain.org" imap folder with subject "subject 2013"
    And 1 mail should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject 2014"

  Scenario: archive treatment on a folder containing 5 mails in 5 different years with UIDs not ordered by date
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org, user/usera/myFolder@mydomain.org" imap folders
    And this user has 1 mail at "2013-11-08T10:00:00Z" in this folder with subject "subject 2013"
    And this user has 1 mail at "2011-11-08T10:00:00Z" in this folder with subject "subject 2011"
    And this user has 1 mail at "2012-11-08T10:00:00Z" in this folder with subject "subject 2012"
    And this user has 1 mail at "2014-11-08T10:00:00Z" in this folder with subject "subject 2014"
    And this user has 1 mail at "2010-11-08T10:00:00Z" in this folder with subject "subject 2010"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2010/myFolder@mydomain.org" imap folder with subject "subject 2010"
    And 1 mail should be archived in the "user/usera/arChive/2011/myFolder@mydomain.org" imap folder with subject "subject 2011"
    And 1 mail should be archived in the "user/usera/arChive/2012/myFolder@mydomain.org" imap folder with subject "subject 2012"
    And 1 mail should be archived in the "user/usera/arChive/2013/myFolder@mydomain.org" imap folder with subject "subject 2013"
    And 1 mail should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject 2014"

  Scenario: archive treatment on a folder containing 1 mail on the epoch date
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org, user/usera/myFolder@mydomain.org" imap folders
    And this user has 1 mail at "1970-01-01T00:00:00Z" in this folder with subject "subject epoch"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/1970/myFolder@mydomain.org" imap folder with subject "subject epoch"

  Scenario: archive treatment on a folder containing 3 mails in the future
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org, user/usera/myFolder@mydomain.org" imap folders
    And this user has 1 mail at "2015-01-01T10:00:00Z" in this folder with subject "subject 2015"
    And this user has 1 mail at "2020-02-01T10:00:00Z" in this folder with subject "subject 2020"
    And this user has 1 mail at "2300-03-01T10:00:00Z" in this folder with subject "subject 2300"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then this user imap folders should contain 3 mails

  Scenario: scheduler process an archive treatment on a folder containing 5 mails one month old
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org, user/usera/myFolder@mydomain.org" imap folders
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T10:28:00Z"
    When current date is "2014-12-10T10:29:58Z"
    Then an archive treatment has been processed by the scheduler after 1 second
    And 5 mails should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on two users one is the prefix of the other
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And a user "usera-test" with "user/usera-test@mydomain.org" imap folder
    And this user has 2 mails at "2014-10-08T10:00:00Z" in this folder with subject "subject-test"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject"
    Then 2 mails should be archived in the "user/usera-test/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject-test"

  Scenario: archive treatment on a user excluded
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And configuration excludes users 
      | userb | 8e30e673-1c47-4ca8-85e8-4609d4228c10 |
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And a user "userb" with "user/userb@mydomain.org" imap folder
    And this user has 2 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject"
    Then imap folder "user/userb/arChive/2014/INBOX@mydomain.org" doesn't exists

  Scenario: archive treatment on  all users excluded
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And configuration excludes users 
      | usera | 08607f19-05a4-42a2-9b02-6f11f3ceff3b |
      | userb | 8e30e673-1c47-4ca8-85e8-4609d4228c10 |
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And a user "userb" with "user/userb@mydomain.org" imap folder
    And this user has 2 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then imap folder "user/usera/arChive/2014/INBOX@mydomain.org" doesn't exists
    Then imap folder "user/userb/arChive/2014/INBOX@mydomain.org" doesn't exists

  Scenario: archive treatment on a user included
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And configuration includes users 
      | usera | 08607f19-05a4-42a2-9b02-6f11f3ceff3b |
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And a user "userb" with "user/userb@mydomain.org" imap folder
    And this user has 2 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject2"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject"
    Then imap folder "user/userb/arChive/2014/INBOX@mydomain.org" doesn't exists

  Scenario: archive treatment on all users included
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And configuration includes users 
      | usera | 08607f19-05a4-42a2-9b02-6f11f3ceff3b |
      | userb | 8e30e673-1c47-4ca8-85e8-4609d4228c10 |
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And a user "userb" with "user/userb@mydomain.org" imap folder
    And this user has 2 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject2"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject"
    Then 2 mails should be archived in the "user/userb/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject2"

  Scenario: archive treatment should archive one mail in the year when first second
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 1 mail at "2012-01-01T00:00:01.000Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2012/INBOX@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment should archive one mail in the year when first second and higher time zone
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 1 mail at "2012-01-01T02:00:01.000+0200" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2012/INBOX@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment should archive one mail in the year when last second
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 1 mail at "2012-12-31T23:59:59.000Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2012/INBOX@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment should archive one mail in the year when last second and lower time zone
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 1 mail at "2012-12-31T21:59:59.000-0200" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2012/INBOX@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on shared mailbox containing 5 mails one month old
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a shared mailbox "shared" without imap folder
    And this shared mailbox has 5 mails at "2014-11-08T10:00:00Z" with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "shared/arChive/2014@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on shared mailbox folder containing 5 mails one month old
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a shared mailbox "shared" with "shared/myFolder@mydomain.org" imap folders
    And this shared mailbox has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "shared/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject"
    Then imap folder "shared/arChive/2014@mydomain.org" doesn't exists

  Scenario: archive treatment on  all shared mailboxes excluded
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And configuration excludes shared mailboxes 
      | shared | 1 |
      | shared2 | 2 |
    And a shared mailbox "shared" without imap folder
    And this shared mailbox has 5 mails at "2014-11-08T10:00:00Z" with subject "subject"
    And a shared mailbox "shared2" with "shared2/myFolder@mydomain.org" imap folders
    And this shared mailbox has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then imap folder "shared/arChive/2014/myFolder@mydomain.org" doesn't exists
    Then imap folder "shared/arChive/2014/myFolder@mydomain.org" doesn't exists

  Scenario: archive treatment on all shared mailboxes included
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And configuration includes shared mailboxes 
      | shared | 1 |
      | shared2 | 2 |
    And a shared mailbox "shared" without imap folder
    And this shared mailbox has 5 mails at "2014-11-08T10:00:00Z" with subject "subject"
    And a shared mailbox "shared2" with "shared2/myFolder@mydomain.org" imap folders
    And this shared mailbox has 2 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject2"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "shared/arChive/2014@mydomain.org" imap folder with subject "subject"
    Then 2 mails should be archived in the "shared2/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject2"

  Scenario: archive treatment on one shared mailboxes and one user included
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And configuration includes shared mailboxes 
      | shared2 | 2 |
    And configuration includes users 
      | usera | 08607f19-05a4-42a2-9b02-6f11f3ceff3b |
    And a shared mailbox "shared" without imap folder
    And this shared mailbox has 5 mails at "2014-11-08T10:00:00Z" with subject "subject"
    And a shared mailbox "shared2" with "shared2/myFolder@mydomain.org" imap folders
    And this shared mailbox has 2 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject2"
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And a user "userb" with "user/userb@mydomain.org" imap folder
    And this user has 2 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject2"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then imap folder "shared/arChive/2014@mydomain.org" doesn't exists
    Then 2 mails should be archived in the "shared2/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject2"
    Then 5 mails should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject"
    Then imap folder "user/userb/arChive/2014/INBOX@mydomain.org" doesn't exists
    