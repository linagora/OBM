<?PHP

class unread extends rcube_plugin {

  function init() {
    $this->add_hook('ready', array($this, 'ready'));
  }

  function ready($args) {
    if ( $args["task"] != "mail" || $args["action"] != "unread_plugin" ) {
      return ;
    }
    $RCMAIL = rcmail::get_instance();
    $storage = $RCMAIL->get_storage();
    $unseen = $storage->count('INBOX', 'UNSEEN', null);
    echo $unseen;
    exit();
  }

}
