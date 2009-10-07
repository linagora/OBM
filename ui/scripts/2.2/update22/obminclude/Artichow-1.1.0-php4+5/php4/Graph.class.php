<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once dirname(__FILE__)."/Image.class.php";
 
/* <php4> */

define("GRAPH_DRAW_RETURN", 1);
define("GRAPH_DRAW_DISPLAY", 2);

/* </php4> */

/**
 * A graph 
 *
 * @package Artichow
 */
class awGraph extends awImage {

	/**
	 * Graph name
	 *
	 * @var string
	 */
	var $name;

	/**
	 * Cache timeout
	 *
	 * @var int
	 */
	var $timeout = 0;
	
	/**
	 * Graph timing ?
	 *
	 * @var bool
	 */
	var $timing;
	
	/**
	 * Components
	 *
	 * @var array
	 */
	var $components = array();
	
	/**
	 * Some labels to add to the component
	 *
	 * @var array
	 */
	var $labels = array();
	
	/**
	 * Graph title
	 *
	 * @var Label
	 */
	var $title;
	
	/**
	 * File cache location
	 *
	 * @var string
	 */
	var $fileCache;
	
	/**
	 * Time file cache location
	 *
	 * @var string
	 */
	var $fileCacheTime;
	
	/** 
	 * Drawing mode to return the graph
	 *
	 * @var int
	 */
	
	
	/** 
	 * Drawing mode to display the graph
	 *
	 * @var int
	 */
	
	
	/**
	 * Construct a new graph
	 *
	 * @param int $width Graph width
	 * @param int $height Graph height
	 * @param string $name Graph name for the cache (must be unique). Let it null to not use the cache.
	 * @param int $timeout Cache timeout (unix timestamp)
	 */
	 function awGraph($width = NULL, $height = NULL, $name = NULL, $timeout = 0) {
		
		parent::awImage();
	
		$this->setSize($width, $height);

		if(ARTICHOW_CACHE) {
	
			$this->name = $name;
			$this->timeout = $timeout;
			
			// Clean sometimes all the cache
			if(mt_rand(0, 5000) ===  0) {
				awGraph::cleanCache();
			}
		
			// Take the graph from the cache if possible
			if($this->name !== NULL) {
			
				$this->fileCache = ARTICHOW_CACHE_DIRECTORY."/".$this->name;
				$this->fileCacheTime = $this->fileCache."-time";
				
				if(is_file($this->fileCache)) {
				
					$type = awGraph::cleanGraphCache($this->fileCacheTime);
					
					if($type === NULL) {
						awGraph::deleteFromCache($this->name);
					} else {
						header("Content-Type: image/".$type);
						echo file_get_contents($this->fileCache);
						exit;
					}
					
				}
			
			}
		
		}
		
		$this->title = new awLabel(
			NULL,
			new awTuffy(16),
			NULL,
			0
		);
		$this->title->setAlign(LABEL_CENTER, LABEL_BOTTOM);
	
	}
	
	/**
	 * Delete a graph from the cache
	 *
	 * @param string $name Graph name
	 * @return bool TRUE on success, FALSE on failure
	 */
	  function deleteFromCache($name) {

		if(ARTICHOW_CACHE) {
		
			if(is_file(ARTICHOW_CACHE_DIRECTORY."/".$name."-time")) {
				unlink(ARTICHOW_CACHE_DIRECTORY."/".$name."");
				unlink(ARTICHOW_CACHE_DIRECTORY."/".$name."-time");
			}
			
		}
		
	}
	
	/**
	 * Delete all graphs from the cache
	 */
	  function deleteAllCache() {

		if(ARTICHOW_CACHE) {
	
			$dp = opendir(ARTICHOW_CACHE_DIRECTORY);
			
			while($file = readdir($dp)) {
				if($file !== '.' and $file != '..') {
					unlink(ARTICHOW_CACHE_DIRECTORY."/".$file);
				}
			}
			
		}
	
	}
	
	/**
	 * Clean cache
	 */
	  function cleanCache() {

		if(ARTICHOW_CACHE) {
	
			$glob = glob(ARTICHOW_CACHE_DIRECTORY."/*-time");
			
			foreach($glob as $file) {
				
				$type = awGraph::cleanGraphCache($file);
				
				if($type === NULL) {
					$name = ereg_replace(".*/(.*)\-time", "\\1", $file);
					awGraph::deleteFromCache($name);
				}
			
			}
			
		}
		
	}
	
	/**
	 * Enable/Disable Graph timing
	 *
	 * @param bool $timing
	 */
	 function setTiming($timing) {
		$this->timing = (bool)$timing;
	}
	 
	/**
	 * Add a component to the graph
	 *
	 * @param &$component
	 */
	 function add(&$component) {
	
		$this->components[] = $component;
	
	}
	
	/**
	 * Add a label to the component
	 *
	 * @param &$label
	 * @param int $x Position on X axis of the center of the text
	 * @param int $y Position on Y axis of the center of the text
	 */
	 function addLabel(&$label, $x, $y) {
	
		$this->labels[] = array(
			$label, $x, $y
		);
		
	}
	
	/**
	 * Add a label to the component with absolute position
	 *
	 * @param &$label
	 * @param $point Text position
	 */
	 function addAbsLabel(&$label, $point) {
	
		$this->labels[] = array(
			$label, $point
		);
		
	}
	
	/**
	 * Build the graph and draw component on it
	 *
	 * @param string $mode Display mode (can be a file name)
	 */
	 function draw($mode = GRAPH_DRAW_DISPLAY) {
		
		if($this->timing) {
			$time = microtimeFloat();
		}
	
		$this->create();
		
		foreach($this->components as $component) {
		
			$this->drawComponent($component);
		
		}
		
		$this->drawTitle();
		$this->drawShadow();
		$this->drawLabels();
		
		if($this->timing) {
			$this->drawTiming(microtimeFloat() - $time);
		}
		
		// Create graph
		$data = $this->get();
				
		// Put the graph in the cache if needed
		$this->cache($data);
		
		switch($mode) {
		
			case GRAPH_DRAW_DISPLAY :
				$this->sendHeaders();
				echo $data;
				break;
		
			case GRAPH_DRAW_RETURN :
				return $data;
			
			default :
				if(is_string($mode)) {
					file_put_contents($mode, $data);
				} else {
					awImage::drawError("Class Graph: Unable to draw the graph.");
				}
		
		}

	}
	
	 function drawLabels() {
	
		$driver = $this->getDriver();
	
		foreach($this->labels as $array) {
		
			if(count($array) === 3) {
			
				// Text in relative position
				list($label, $x, $y) = $array;
				
				$point = new awPoint(
					$x * $this->width,
					$y * $this->height
				);
				
			} else {
			
				// Text in absolute position
				list($label, $point) = $array;
			
			}
				
			$label->draw($driver, $point);
		
		}
		
	}
		
	 function drawTitle() {
	
		$driver = $this->getDriver();
	
		$point = new awPoint(
			$this->width / 2,
			10
		);
		
		$this->title->draw($driver, $point);
	
	}
	
	 function drawTiming($time) {
	
		$driver = $this->getDriver();
		
		$label = new awLabel;
		$label->set("(".sprintf("%.3f", $time)." s)");
		$label->setAlign(LABEL_LEFT, LABEL_TOP);
		$label->border->show();
		$label->setPadding(1, 0, 0, 0);
		$label->setBackgroundColor(new awColor(230, 230, 230, 25));
		
		$label->draw($driver, new awPoint(5, $driver->imageHeight - 5));
	
	}
	
	 function cache($data) {
		if(ARTICHOW_CACHE and $this->name !== NULL) {
			
			if(is_writable(ARTICHOW_CACHE_DIRECTORY) === FALSE) {
				awImage::drawError("Class Graph: Cache directory is not writable.");
			}
		
			file_put_contents($this->fileCache, $data);
			file_put_contents($this->fileCacheTime, $this->timeout."\n".$this->getFormatString());
			
		}
	}
	
	  function cleanGraphCache($file) {
	
		list(
			$time,
			$type
		) = explode("\n", file_get_contents($file));
		
		$time = (int)$time;
		
		if($time !== 0 and $time < time()) {
			return NULL;
		} else {
			return $type;
		}
		
		
	}

}

registerClass('Graph');

/*
 * To preserve PHP 4 compatibility
 */
function microtimeFloat() { 
	list($usec, $sec) = explode(" ", microtime()); 
	return (float)$usec + (float)$sec; 
}
?>
