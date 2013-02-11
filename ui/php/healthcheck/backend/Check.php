<?php

interface Check {
  
  public function getName();
  
  public function getDescription();
  
  public function getDocUrl();
  
  public function getParentId();
  
  public function execute();
  
}