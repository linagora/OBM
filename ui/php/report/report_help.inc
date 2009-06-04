Usage : /usr/bin/php report_index.php [-a action][-s]

- a     : Action to execute, if not specified, the *execute* will be executed.

Action list with options
  *help*        :       Show this help file
  *list*        :       List all available reportings
      -v verbose          :       More verbose mode (show report details).  
  *execute*     :       Execute the  report
      -r report         :       Report name to execute.
      -s service        :       Filter report result with the given service (
                                only available for user kind report)
      -v verbose          :       More verbose mode (show the report result
                                on the output).
Exemple :
php report_index.php -a execute -r user -v
php report_index.php -a execute -r vacation -s myservice > /tmp/report.result
