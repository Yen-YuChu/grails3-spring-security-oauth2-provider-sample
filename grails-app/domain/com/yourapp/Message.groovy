package com.yourapp

import grails.rest.Resource

@Resource(uri='/message', formats=['json', 'xml'])
class Message {

    String message;
    Long creator_id;
    String user_name;

    static constraints = {
      message nullable: false, blank: false, unique: false
      creator_id nullable: false, blank: true, unique: false
      user_name nullable: false, blank: true, unique: false
    }
}
