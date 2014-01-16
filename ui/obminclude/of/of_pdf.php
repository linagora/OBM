<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



require_once("$obminclude/lib/Zend/Pdf.php");

/**
 * OBM class to handle pdf files
 * 
 * @uses Zend_Pdf
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
class OBM_Pdf extends Zend_Pdf{ 

  private $fontSize;

  private $interLign;

  private $font;

  private $currentPage;

  private static $templateRoot; 

  private static $subject;

  private static $author;

  public function __construct($source = null, $revision = null, $load = false) {
    $this->font = Zend_Pdf_Font::fontWithName(Zend_Pdf_Font::FONT_HELVETICA);
    $this->fontSize = 6;
    $this->interLign = 1.25;
    parent::__construct($source, $revision,$load);
  }

  /**
   * load 
   * 
   * @param mixed $source 
   * @param mixed $revision 
   * @static
   * @access public
   * @return void
   */
  public static function load($source = null, $revision = null, $module = null) {
    if(!file_exists($source)) {
      $source = self::getTemplatePath($source, $module);
    }
    return new OBM_Pdf($source, $revision, true);
  }
  /**
   * Download PDF
   * 
   * @access public
   * @return void
   */
  public function download($filename) {

    header("Content-Type: application/pdf") ;
    header("Content-Disposition: attachment; filename=\"$filename.pdf\";");
    header("Cache-Control: maxage=3600");
    header('Pragma: public');
    echo $this->render();
  }

  /**
   * setMetadata 
   * 
   * @access public
   * @return void
   */
  public function setMetadata($subject) {
    $title = $GLOBALS['l_'.$GLOBALS['module']];
    $this->pdf->properties['Title'] = $title;
    $this->pdf->properties['Subject'] = $this->subject;
    $this->pdf->properties['Author'] = $this->author;
    $this->pdf->properties['Creator'] = $this->author;
    $this->pdf->properties['Producer'] = "$GLOBALS[l_obm_title] $GLOBALS[obm_version]";
    $this->pdf->properties['CreationDate'] = "D:".date('YmdHis');
  }
  

  /**
   * Return the size of the string in point 
   * 
   * @param mixed $string 
   * @access public
   * @return void
   */
  public function getStringWidth($string) {
    $drawingString = iconv('UTF-8', 'UTF-16BE//IGNORE', $string);
    $characters = array();
    for ($i = 0; $i < strlen($drawingString); $i++) {
      $characters[] = (ord($drawingString[$i++]) << 8) | ord($drawingString[$i]);
    }
    $glyphs = $this->font->glyphNumbersForCharacters($characters);
    $widths = $this->font->widthsForGlyphs($glyphs);
    $stringWidth = (array_sum($widths) / $this->font->getUnitsPerEm()) * $this->font_size;

    return $stringWidth;
  }

   /**
   * Long words killer
   *
   */
  function cutLongWords($string,$length,$separation) {
    return preg_replace('/([^ ]{'.$length.'})/si','\1'.$separation,$string);
  }

  function drawSplittedText(Zend_Pdf_Page $page, 
                            $string,
                            $max_length,
                            $X,
                            &$Y){
    $chunks = str_split($string, $max_length);
    foreach ($chunks as $key=>$line) {
      $page->drawText($line, $X, $Y, 'UTF-8');
      $Y -= $this->getLineHeight();
    }
  }

  /**
   * get font 
   * 
   * @access public
   * @return void
   */
  public function getFont() {
    return $this->font;
  }

  /**
   * get font size 
   * 
   * @access public
   * @return void
   */
  public function getFontSize() {
    return $this->fontSize; 
  }

  /**
   * get font size 
   * 
   * @access public
   * @return void
   */
  public function setFontSize($size) {
    $this->fontSize = $size; 
  }
  /**
   * Return the size of the height in point
   * 
   * @access public
   * @return void
   */
  public function getLineHeight() {
    return $this->fontSize * $this->interLign;
  }

  /**
   * add a new page
   * with header & legend
   * 
   * @param mixed $header 
   * @param mixed $legend 
   * @access public
   * @return void
   */
  public function addPage($header=true, $legend=true) {
    $this->currentPage = $this->newPage();
    $pdf->pages[] = $this->currentPage;
    $this->page->setFont($this->font, $this->fontSize);
  }

  /**
   * getTemplatePath 
   * 
   * @param mixed $templateName 
   * @param mixed $module 
   * @access protected
   * @return void
   */
  protected function getTemplatePath($templateName, $module=null) {
    if(!$module) $module = $GLOBALS['module'];
    $possiblePaths = array(
      dirname(__FILE__)."/../../conf/views/pdf/$module/$_SESSION[set_lang]/$templateName.pdf",
      dirname(__FILE__)."/../../views/pdf/$module/$_SESSION[set_lang]/$templateName.pdf",
      dirname(__FILE__)."/../../conf/views/pdf$module/en/$templateName.pdf",
      dirname(__FILE__)."/../../views/pdf/$module/en/$templateName.pdf"
    );
    foreach ($possiblePaths as $path) {
      if (file_exists($path) && is_readable($path)) {
        return $path;
      }
    }
    return false;
  }  

}
