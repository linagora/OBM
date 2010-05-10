<?php

class ODF {
  private $filename;
  private $files = array();
  private $bins = array();
  private $to_replace = array();
  private $to_replace_bin = array();

  //////////////////////////////////////////////////////////////////////////
  // Class constructor
  // params :
  //   $filename    : the template file to use
  //////////////////////////////////////////////////////////////////////////
  function __construct($filename) {
    $this->filename = $filename;
  }

  //////////////////////////////////////////////////////////////////////////
  // Loads the template and gets the content of the usefull files
  // within this template.
  //////////////////////////////////////////////////////////////////////////
  function loadFile()  {
    global $l_odf_load_err;

    $zip = new ZipArchive();

    if ($zip->open($this->filename) !== true) {
      return $l_odf_load_err;
    }

    $this->files['content'] = $zip->getFromName('content.xml');
    $this->files['styles'] = $zip->getFromName('styles.xml');
    $this->files['calc'] = $zip->getFromName('Object 1/content.xml');
    $this->bins['calc'] = $zip->getFromName('ObjectReplacements/Object 1');
    $zip->close();
    return true;
  }

  //////////////////////////////////////////////////////////////////////////
  // Does the replacement of the place-holders by their values
  //////////////////////////////////////////////////////////////////////////
  function parse() {
	foreach ($this->to_replace as $k => $v)
		$this->to_replace[$k] = utf8_encode($v);

    foreach ($this->files as $file => $content) {
      $this->files[$file] = str_replace(array_keys($this->to_replace),
                                        array_values($this->to_replace),
                                        $content);
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // Does the replacement of the binary place-holders by their values
  //////////////////////////////////////////////////////////////////////////
  function parseBin() {
    foreach ($this->bins as $file => $content) {
      $this->bins[$file] = str_replace(array_keys($this->to_replace_bin),
                                       array_values($this->to_replace_bin),
                                       $content);

      foreach ($this->to_replace_bin as $to_replace => $replace_by)  {
        $patt = '#'.implode('.', str_split($to_replace)).'#';
        $repl = implode("\000", str_split($replace_by));
        $this->bins[$file] = preg_replace($patt, $repl, $content);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // Saves the modified document.
  // params :
  //   $newfile     : the path of the new file
  //////////////////////////////////////////////////////////////////////////
  function save($newfile) {
    global $l_odf_save_err;
    $newfile = str_replace('/','-',$newfile) ;

    $zip = new ZipArchive();

    $temp = tempnam("/tmp", "obm");
    copy($this->filename, $temp);
    $this->filename = $newfile;

    if ($zip->open($temp, ZIPARCHIVE::CREATE) !== true) {
      return $l_odf_save_err;
    }
    if (!$zip->addFromString('content.xml', $this->files['content']) ||
        !$zip->addFromString('styles.xml', $this->files['styles']) ||
        !$zip->addFromString('Object 1/content.xml', $this->files['calc']) ||
        !$zip->addFromString('ObjectReplacements/Object 1', $this->bins['calc'])) {
      return $l_odf_save_err;
    }

    $zip->close();

    $file = fopen($temp, 'r');
    header('Content-Type: application/vnd.oasis.opendocument.text') ;
    header("Content-Disposition: attachment; filename=\"$newfile\";") ;
    header('Cache-Control: maxage=3600');
    header('Pragma: public');
    fpassthru($file) ;
    fclose($file) ;

    // Delete tmp file
    unlink($temp) ; 
    exit();
  }

  //////////////////////////////////////////////////////////////////////////
  // Creates the binding between place-holders and values
  // params :
  //   $infos       : the database query result containing values
  //////////////////////////////////////////////////////////////////////////
  function setFields($infos) {
    $prop = strtolower($infos->f('company_name') . '_' . $infos->f('dealtype_label'))
          . '_' . $infos->f('project_shortname');

    $client_addr = $this->getFromField('{}', $infos->f('invoice_comment'));
	$client_addr=utf8_decode($client_addr);
    if ($client_addr == "")
      $client_addr = $this->getContactInfos($infos);

    // Get account numbers
	if(!ereg("([0-9]{5})([0-9]{5})([0-9]{10})([0-9]{2})",$infos->f('account_number'),$bban))
	$bban = array("","","",$infos->f('account_number'),"");

    $this->to_replace['{prop}']              = $prop;
    $this->to_replace['{client_ref}']        = $this->getFromField('[]', $infos->f('invoice_comment'));
    $this->to_replace['{client_addr}']       = $client_addr;
    $this->to_replace['{inv_number}']        = $infos->f('invoice_number');
    $this->to_replace['{inv_date}']          = $this->convertDate($infos->f('invoice_date'));
    $this->to_replace['{dt_label}']          = $infos->f('dealtype_label');
    $this->to_replace['{inv_amount_ht}']     = $infos->f('invoice_amount_ht');
    $this->to_replace['{bban_bank_code}']    = $bban[1];
    $this->to_replace['{bban_code_guichet}'] = $bban[2];
    $this->to_replace['{bban_account_no}']   = $bban[3];
    $this->to_replace['{bban_key}']          = $bban[4];
    $this->to_replace['{bban_bank}']         = $infos->f('account_bank');
    $this->to_replace['00/00/0000']          = $this->convertDate($infos->f('invoice_expiration_date'));
    $this->to_replace['{date}']              = $date=date('d/m/Y');
    $this->to_replace['{date_expiration}']   = $this->convertDate($infos->f('invoice_expiration_date'));
    $this->to_replace['{inv_label}']         = $infos->f('invoice_label');
    $this->to_replace['{inv_ttc}']           = $infos->f('invoice_amount_ttc');
    $this->to_replace_bin['00/00/0000']      = $this->convertDate($infos->f('invoice_expiration_date'));
  }

  //////////////////////////////////////////////////////////////////////////
  // Exports an invoice to the ODF  format
  // params :
  //   $infos       : the database query result containing values
  //////////////////////////////////////////////////////////////////////////
  function export($infos) {
    $save_to = $infos->f('invoice_number')
             . '_' . $infos->f('company_name')
             . '_' . $infos->f('project_shortname')
             . '_' . $this->convertDate($infos->f('invoice_date'), 'd.m.y')
             . '.odt';
    $save_to = str_replace(' ', '', $save_to);

    $save_to = str_replace('/','-',$save_to);

    $res = $this->loadFile();
	
    if ($res !== true) {
      return $res;
    }

    $this->setFields($infos);
    $this->parse();
    $this->parseBin();
    $this->save($save_to);
  }

  function export_reminder($infos) {

	       $nom = substr($this->filename, 0,9);
	
	$save_to = $infos->f('invoice_number')
             . '_' . $infos->f('company_name')
             . '_' . $infos->f('project_shortname')
             . '_' . $this->convertDate($infos->f('invoice_date'), 'd.m.y')
             . '_'.$nom
	     .'.odt';
	
    $save_to = str_replace(' ', '', $save_to);
	
    $save_to = str_replace('/','-',$save_to);

    $res = $this->loadFile();
    if ($res !== true) {
      return $res;
    }

    $this->setFields($infos);
    $this->parse();
    $this->parseBin();
    
    $this->save($save_to);
  }

 function export_reminder_by_company($infos) {

    $nom = substr($this->filename, 0,9);
	$save_to = $infos[0]['company_name']
                  . '_' . $date=date('d-m-Y')
                  . '_'.$nom
	          .'.odt';
	
    $save_to = str_replace(' ', '', $save_to);
    $save_to = str_replace('/','-',$save_to);
    $res = $this->loadFile();
	
    if ($res !== true) {
      return $res;
    }
    $this->setFields_reminder($infos);
    $this->parse();
    $this->parseBin();
    $this->save($save_to);
  }

function setFields_reminder($infos) {
  
    $client_addr = $this->getFromField('{}', $infos[0]['invoice_comment']);
    $client_addr=utf8_decode($client_addr);
    $block="";

    foreach($infos as $cle=>$valeur){
	
     $block.= ' Facture NÂ°: '.$valeur['invoice_number'].'</text:p> 
           <text:p text:style-name="P7"> Date de facture: '.$this->convertDate($valeur['invoice_date']).'</text:p>
           <text:p text:style-name="P7"> Date d\'Ã©chÃ©ance: '.$this->convertDate($valeur['invoice_expiration_date']).'</text:p>                                   <text:p text:style-name="P7"> LibellÃ©: '.$valeur['invoice_label'].'</text:p>
           <text:p text:style-name="P7"> Montant TTC: '.$valeur['invoice_amount_ttc'].' EUR</text:p><text:p text:style-name="P7"></text:p><text:p text:style-name="P7">';
	
}	
	$block=utf8_decode($block);
	
	$this->to_replace['{date}']         = $date=date('d/m/Y');
	$this->to_replace['{detail_block}'] = $block;
	$this->to_replace['{client_addr}']  = $client_addr;

}
  //////////////////////////////////////////////////////////////////////////
  // Converts a date to the user prefered format or to the $format format
  // params :
  //   $date        : the date to convert
  //   $format      : the convertion format (user defined if set to null)
  //////////////////////////////////////////////////////////////////////////
  function convertDate($date, $format = null) {
    if ($format == null)
      $format = $_SESSION['set_date'];
    return date($format, strtotime($date));
  }

  //////////////////////////////////////////////////////////////////////////
  // Retrieves informations contained between $boundaries in $data
  // params :
  //   $boundaries  : an array containing the data boundaries (ex: {}, [])
  //   $data        : the string that contain informations
  //////////////////////////////////////////////////////////////////////////
  function getFromField($boundaries, $data) {
    $pattern = '/\\' . $boundaries[0] . '(.*)\\' . $boundaries[1] . '/Us';
    if (preg_match($pattern, $data, $match))
        return str_replace("\n", "<text:line-break/>", $match[1]);
    else
        return '';
  }
  
  //////////////////////////////////////////////////////////////////////////
  // Retrieves contact's informations
  // params :
  //   $infos       : the database query result containing values
  //////////////////////////////////////////////////////////////////////////
  function getContactInfos($infos)
  {
    $contact_infos = array($infos->f('contact_company'),
                           $infos->f('contact_service'),
                           strtoupper($infos->f('contact_lastname')) . " " . ucfirst($infos->f('contact_firstname')),
                           $infos->f('contact_address1'),
                           $infos->f('contact_address2'),
                           $infos->f('contact_zipcode') . " " . $infos->f('contact_town') . " " . $infos->f('contact_expresspostal')
                     );
    return implode("<text:line-break/>", $contact_infos);
  }
}

?>
