<?PHP

class unread extends rcube_plugin {

  function init() {
    $this->add_hook('ready', array($this, 'ready'));
  }

  function ready($args) {
    error_log("unread PLUGIN: ready method starts");
    if ( $args["task"] != "mail" || $args["action"] != "unread_plugin" ) {
      return ;
    }
    error_log("unread PLUGIN: I'm called");
    $RCMAIL = rcmail::get_instance();
    $storage = $RCMAIL->get_storage();
    $unseen = $storage->count('INBOX', 'UNSEEN', null);
    error_log("unread PLUGIN: unseen = ".$unseen);
    echo $unseen;
    exit();
  }

}
