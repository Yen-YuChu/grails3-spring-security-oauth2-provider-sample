package oauth2.test

import com.yourapp.Client
import com.yourapp.Role
import com.yourapp.User
import com.yourapp.UserRole

class BootStrap {

    def init = { servletContext ->

      Role roleUser = new Role(authority: 'ROLE_USER').save(flush: true)

      User user = new User(
              username: 'my-user',
              password: 'my-password',
              enabled: true,
              accountExpired: false,
              accountLocked: false,
              passwordExpired: false
      ).save(flush: true)

      UserRole.create(user, roleUser, true)

      new Client(
              clientId: 'my-client',
              clientSecret: 'my-secret',
              authorizedGrantTypes: ['authorization_code', 'refresh_token', 'implicit', 'password', 'client_credentials'],
              authorities: ['ROLE_CLIENT'],
              scopes: ['read', 'write'],
              redirectUris: ['http://localhost:8080/oauth2-test']
      ).save(flush: true)

    }
    def destroy = {
    }
}
