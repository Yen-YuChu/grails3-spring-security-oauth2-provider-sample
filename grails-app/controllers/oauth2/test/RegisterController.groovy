package oauth2.test

import com.yourapp.Role
import com.yourapp.User
import com.yourapp.UserRole
import grails.converters.JSON
import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.*

class RegisterController {

  /** $ curl http://localhost:8080/register -H "Authorization: Bearer c2f95787-9c6d-4116-a408-0f6994fd6cd1" -H "Content-Type: application/json" -X POST -d '{"username":"bobbywarner", "password":"xyz", "email":"bobbywarner@gmail.com"}'
   {"id":3,"accountExpired":false,"accountLocked":false,"email":"bobbywarner@gmail.com","enabled":true,"password":"$2a$10$qxRBP8dx2FiFq1Rr50ZDuePM6udQZ66RmluT0S0Skj3ncPs3MqWiy","passwordExpired":false,"username":"bobbywarner"}
   */

  static allowedMethods = [save: "POST"]

  @Transactional
  def save(RegisterCommand signup) {
    log.debug("Content-Type: " + request.getHeader("Content-Type"))
    log.debug("Accept: " + request.getHeader("Accept"))
    if (!signup.hasErrors()) {
      User person = User.findByUsername(signup.username)
      if (!person) {
        person = User.findByEmail(signup.email)
        if (!person) {
          person = new User(username: signup.username, password: signup.password, email: signup.email).save(flush: true)
          def userRole = Role.findByAuthority('ROLE_USER')
          UserRole.create(person, userRole)
          def map = [suceed: true]
          render map as JSON;
          return
        } else {
          response.status = 400
          def error = [
                  [
                          field: 'email',
                          'rejected-value': signup.email,
                          message: 'A user already exists with this email address'
                  ]
          ]
          def results = [errors: error]
          render results as JSON
          return
        }
      } else {
        response.status = 400
        def error = [
                [
                        field: 'username',
                        'rejected-value': signup.username,
                        message: 'A user already exists with this username'
                ]
        ]
        def results = [errors: error]
        render results as JSON
        return
      }
      respond person, [status: CREATED]
    } else {
      response.status = 400
      render signup.errors as JSON
    }
  }
}

class RegisterCommand {
  String username
  String email
  String password

  static constraints = {
    username(blank: false, unique: true)
    email(blank: false, email: true) //remove unique:true
    password(blank: false)
  }
}
