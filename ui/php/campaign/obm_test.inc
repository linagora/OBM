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



abstract class OBM_Test {
  /**
   * @param String $code
   * @param String $action_name
   * @return String extracted code
   */
  public static function extract_action ($code, $action_name) {
    $search = "if (\$action == '$action_name') {";
    $start = strpos($code, $search) + strlen($search);
    
    return self::extract_block($code, $start);
  }
  
  private static function extract_block ($code, $start) {
    $tab = 1;
    
    $i = $start;
    
    while ($i < strlen($code)) {
      if ($code[$i] == '{') {
        $tab += 1;
      } else if ($code[$i] == '}')
        $tab -= 1;
      
      if ($tab == 0)
        break;
        
      $i += 1;
    }
    
    return substr($code, $start, $i - $start);
  }
  
  public static function extract_piece_of_code ($code, $tag_name) {
    $start = strpos($code, "// BEGIN OBM TEST $tag_name");
    $end = strpos($code, "// END OBM TEST $tag_name");
    
    $code = substr($code, $start, $end - $start);
    
    $res_code = array();
    foreach (explode("\n", $code) as $line) {
      if (!preg_match("/\/\/\ +OBM +TEST +delete/i", $line))
        $res_code[] = $line;
    }
    
    return join("\n", $res_code);
  }
  
  public static function get_body_function ($function_name) {
    $func = new ReflectionFunction($function_name);
    
    $lines = explode("\n", file_get_contents($func->getFileName()));
    $res = array();
    for ($i = $func->getStartLine(); $i <= $func->getEndLine(); $i += 1) {
      $res[] = $lines[$i];
    }
    
    return join("\n", $res);
  }
  
  /**
   * @param String $code
   * @param OBM_TestContext $context
   * @return OBM_TestResult
   */
  public static function exec ($code, $context) {
    $result = new OBM_TestResult;
    
    $result->source = $code;
    
    foreach ($context->data as $var_name => $value) {
      $$var_name = &$context->data[$var_name];
    }
    
    $code = preg_replace('/\$GLOBALS\[.display.\]/', '$display', $code);
    $code = preg_replace('/\$display\[.msg.\] +.?= +display_ok_msg/', '$oks[] =', $code);
    $code = preg_replace('/\$display\[.msg.\] +.?= +display_err_msg/', '$errors[] =', $code);
    $code = str_replace('$display', '$exec_return', $code);
    $code = preg_replace('/\$(l_[a-zA-Z0-9_]+)/', '$GLOBALS[\'\1\']', $code);
    
    if (is_a($context, 'OBM_TestContext_Function')) {
      $return = &$context->return;
      $code = str_replace('return', '$return = ', $code);
    }
    
    $oks = array();
    $errors = array();
    
    ob_start();
    eval($code);
    $stdout = ob_get_clean();
    
    $result->display = $exec_return;
    $result->stdout = $stdout;
    $result->oks = $oks;
    $result->errors = $errors;
    
    return $result;
  }
}

class OBM_Test_Instanciable {
  protected $code;
  protected $context;
  
  public function setCode ($code) {
    $this->code = $code;
  }
  
  public function setContext ($context) {
    $this->context = $context;
  }
  
  public function getContext () {
    return $this->context;
  }
}

/**
 * Class instanciator of OBM_Test specialized in code extraction
 *
 */
class OBM_Test_Extractor extends OBM_Test_Instanciable {
  public function __construct ($code) {
    $this->setCode($code);
  }
  
  public function extract_action ($action_name) {
    OBM_Test::extract_action($this->code, $action_name);
  }
}

/**
 * Class instanciator of OBM_Test specialized in code execution
 *
 */
class OBM_Test_Executor extends OBM_Test_Instanciable {
  public function __construct ($context) {
    $this->setContext($context);
  }
  
  public function exec ($code) {
    OBM_Test::exec($code, $this->context);
  }
}

/**
 * Class instanciator of OBM_Test specialized in action execution
 *
 */
class OBM_Test_ActionExecutor extends OBM_Test_Instanciable {
  public function __construct ($code = null, $context = null) {
    $this->setCode($code);
    $this->setContext($context);
  }
  
  /**
   * @param String $action_name
   * @return OBM_TestResult
   */
  public function exec ($action_name) {
    $code = OBM_Test::extract_action($this->code, $action_name);
    return OBM_Test::exec($code, $this->context);
  }
}



class OBM_TestContext {
  public $data;
  
  public function __construct ($data = null) {
    $this->data = $data;
  }
}

class OBM_TestContext_Function extends OBM_TestContext {
  public $return;
}

class OBM_TestContext_Action extends OBM_TestContext {
}


class OBM_Test_DB extends DB_OBM {
  function query_rand ($query, $primary_key, $count) {
    list($select, $from) = explode(' FROM ', $query);
    
    $keys = array();
    
    while (count($keys) < $count) {
      $this->query("SELECT $primary_key as pkey FROM $from");
      
      if (count($keys) >= $this->num_rows())
        break;
      
      while ($this->next_record()) {
        if (count($keys) >= $count)
          break;
          
        if (rand(0, 50) == 25) {
          $keys[] = $this->f('pkey');
        }
      }
    }
    
    list($select, $where) = explode(' WHERE ', $query);
    
    $query = $select;
    
    if (!empty($keys)) {
      $where_in = "WHERE $primary_key IN ('". join("','", $keys). "')";
    }
    
    $query = "$query $where_in";
    
    return $this->query($query);
  }
}

class OBM_Test_HTML_Field {
   private $fieldReader;
   private $obm_q;
   private $html;
   
   function __construct ($html) {
     $this->setHTML($html);
     $this->fieldReader = array(
       'html' => '<tr><th>{label}</th><td>{value}</td></tr>',
       'label' => '{label}',
       'value' => '{value}',
     );
   }
   
   function setFieldReader ($field) {
     $this->fieldReader = $fieldReader;
   }
   
   function setHTML ($html) {
     $this->html = $html;
   }
   
   function assert_equals ($field_label, $field_value) {
     $html = str_replace("\n", '', $this->html);
     $html = preg_replace('/> +</', '><', $html);
     
     $find_target = $this->fieldReader['html'];
     $find_target = str_replace($this->fieldReader['label'], $field_label, $find_target);
     $find_target = str_replace($this->fieldReader['value'], $field_value, $find_target);
     $find_target = str_replace('/', '\/', $find_target);
     $find_target = "/$find_target/";
     
     return preg_match($find_target, $html);
   }
   
   function assert_empty ($field_label) {
     $html = str_replace("\n", '', $this->html);
     $html = preg_replace('/> +</', '><', $html);
     
     $find_target = $this->fieldReader['html'];
     $find_target = str_replace($this->fieldReader['label'], $field_label, $find_target);
     $find_target = str_replace($this->fieldReader['value'], '', $find_target);
     $find_target = str_replace('/', '\/', $find_target);
     $find_target = "/$find_target/";
     
     return preg_match($find_target, $html);
   }
}



/*
class OBM_Test_HTMLDB_FieldReader {
   var $fieldreader;
   var $label_tag;
   var $value_tag;
   
   function OBM_Test_HTMLDB_FieldReader ($html, $label_tag, $value_tag) {
     $this->html = $html;
     $this->label_tag = $label_tag;
     $this->value_tag = $value_tag;
   }
}
*/

class OBM_TestResult {
  public $display;
  public $stdout;
  public $source;
  public $context;
  public $oks;
  public $errors;
  
  function toHTML ($label) {
    $state = 'info'; 
    if (!empty($this->oks)) {
      $state = 'ok';
    }
    if (!empty($this->stdout)) {
      $state = 'warning';
    }
    if (!empty($this->errors)) {
      $state = 'error';
    }
    
    $res .= "<span class=\"message $state source\">$label ";
    
    if (!empty($this->stdout)) {
      $str = $this->stdout;
      $str = str_replace('<', '&lt;', $str);
      $str = str_replace('>', '&gt;', $str);
      
      $res .= ("<span class=\"foldbox\">
        <span class=\"label\">[content flush on stdout]</span>
        <span class=\"content\">$str</span>
      </span>");
    }
    
    if (!empty($this->oks)) {
      $res2 .= '';
      foreach ($this->oks as $e) {
        $res2 .= display_ok_msg($e);
      }
      
      $res .= ("<span class=\"foldbox\">
        <span class=\"label\">[". count($this->oks). " ok msg]</span>
        <span class=\"content\">$res2</span>
      </span>"); 
    }
    
    if (!empty($this->errors)) {
      $res2 .= '';
      foreach ($this->errors as $e) {
        $res2 .= display_err_msg($e);
      }
      
      $res .= ("<span class=\"foldbox\">
        <span class=\"label\">[". count($this->errors). " err msg]</span>
        <span class=\"content\">$res2</span>
      </span>"); 
    }
    
    $res .= ("<span class=\"foldbox source\">
      <span class=\"label\">[Source]</span>
      <span class=\"content\"><pre>$this->source</pre></span>
    </span>");
    
    $res .= "</span>";
    
    return $res;
  }
  
  static function getStyleAndJS () {
    return "
      <style>
      .message {
        text-align:left;
        padding-left:50px;
      }
      
      .foldbox .label:hover {
        font-weight:bold;
      }
      
      .foldbox .hide {
        display:none;
      }
      
      </style>
      
      
      <script type=\"text/javascript\">
        \$each(\$\$('.content'), function (e) {
          e.toggleClass('hide');
          
          e.getParent().getChildren('.label').addEvent('click', function () {
            \$each(this.getParent().getChildren(), function (e) {
              if (e.hasClass('content'))
                e.toggleClass('hide');
            });
          });
        });
      </script>";
  }
}

?>