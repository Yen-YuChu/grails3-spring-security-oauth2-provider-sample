package oauth2.test

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

class SampleController {
  @Secured("#oauth2.isUser()")
  def index() {
    def map = [suceed: true]
    render map as JSON;

  }
}
