# grails3-spring-security-oauth2-provider-sample

Create your grails 3 application(with grails 3 SDK)
I think use an IDE create is a good way and naming oauth2-test

Add maven repositories and dependencies in bulid.gradle
	
```
repositories {
    mavenLocal()
    maven { url "https://repo.grails.org/grails/core" }
   //add by me
    maven { url "http://dl.bintray.com/bluesliverx/grails-plugins" }
}
dependencies {
    //add by me
    compile 'org.grails.plugins:spring-security-oauth2-provider:3.0.0-RC2'
}
```
Fix RC2 can’t found
https://github.com/bluesliverx/grails-spring-security-oauth2-provider/issues/126

Using s2-init-oauth2-provider with grails to create your domain class
Client, AuthorizationCode, AccessToken and RefreshToken.
com.yourapp you can change it for your package name
```
# grails s2-init-oauth2-provider com.yourapp Client AuthorizationCode AccessToken RefreshToken
```
Using s2-quickstart [com.yourapp] User Role to create domain class User, Role, and UserRole for spring security
```
# grails s2-quickstart com.yourapp User Role
```
Run application for configuring Spring Security Core and OAuth2 Provider
```
# grails run-app
```

Open /grails-app/conf/application.groovy config
You can see as below

```
// Added by the Spring Security OAuth2 Provider plugin:
grails.plugin.springsecurity.oauthProvider.clientLookup.className = 'com.yourapp.Client'
grails.plugin.springsecurity.oauthProvider.authorizationCodeLookup.className = 'com.yourapp.AuthorizationCode'
grails.plugin.springsecurity.oauthProvider.accessTokenLookup.className = 'com.yourapp.AccessToken'
grails.plugin.springsecurity.oauthProvider.refreshTokenLookup.className = 'com.yourapp.RefreshToke'


// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.yourapp.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = ' com.yourapp.UserRole'
grails.plugin.springsecurity.authority.className = ' com.yourapp.Role'
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
 [pattern: '/',               access: ['permitAll']],
 [pattern: '/error',          access: ['permitAll']],
 [pattern: '/index',          access: ['permitAll']],
 [pattern: '/index.gsp',      access: ['permitAll']],
 [pattern: '/shutdown',       access: ['permitAll']],
 [pattern: '/assets/**',      access: ['permitAll']],
 [pattern: '/**/js/**',       access: ['permitAll']],
 [pattern: '/**/css/**',      access: ['permitAll']],
 [pattern: '/**/images/**',   access: ['permitAll']],
 [pattern: '/**/favicon.ico', access: ['permitAll']],
]
```
Please check they are correct ClassPath.
And two lines as below
```
grails.plugin.springsecurity.rejectIfNoRule = false
grails.plugin.springsecurity.fii.rejectPublicInvocations = false
```
If you set rejectIfNoRule or rejectPublicInvocations to true, we’ll need to configure the staticRules map to include URLs that can’t otherwise be guarded.

And add pattern for filterChain
```
grails.plugin.springsecurity.filterChain.chainMap = [
 [pattern: '/assets/**',      filters: 'none'],
 [pattern: '/**/js/**',       filters: 'none'],
 [pattern: '/**/css/**',      filters: 'none'],
 [pattern: '/**/images/**',   filters: 'none'],
 [pattern: '/**/favicon.ico', filters: 'none'],
 [pattern: '/**',             filters: 'JOINED_FILTERS'],
        //add by me
  [pattern: '/securedOAuth2Resources/**', filters: 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-rememberMeAuthenticationFilter,-oauth2BasicAuthenticationFilter,-exceptionTranslationFilter'],
  [pattern: '/**',                        filters: 'JOINED_FILTERS,-statelessSecurityContextPersistenceFilter,-oauth2ProviderFilter,-clientCredentialsTokenEndpointFilter,-oauth2BasicAuthenticationFilter,-oauth2ExceptionTranslationFilter']

]

```

Add oauth url mapping in UrlMappings, it can be skip for new version
```
"/oauth/authorize" (uri:"/oauth/authorize.dispatch")
"/oauth/token" (uri:"/oauth/token.dispatch")
```

For now, we don’t have a user and role for spring security, so we add them in BootStrap.groovy
```
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
```

For now, we should be done for an Oauth2 Provider Server (Authorization Server).
You can test it with curl command.
```
curl -X POST -u my-client -d "grant_type=password" -d "username=my-user" -d "password=my-password" -d "scope=read" http://localhost:8080/oauth/token
 

curl -X POST -u my-client -d "grant_type=client_credentials" -d "scope=read" http://localhost:8080/oauth/token
``` 

They are different from grant_type, and the second command don’t pass username and password for user. 
And we can use POSTMAN tool to get client_credentirals.
 
OK, we succeed to build an OAuth2 Provider, now we want to use the token to request our REST API basic on Spring Security.

In theory, an Authorization Server and Resource Server (our functional methods)
should be divided, but we can put them together for test.
http://security.stackexchange.com/questions/83450/why-would-you-decouple-your-resource-and-login-servers

So, continue to your project. 

Create your controller
```
# grails create-controller sample
```

Put sample code in index method
```
import grails.converters.JSON
class SampleController {
    def index() {
      def map = [suceed: true]
      render map as JSON;
    }
}
```

Add config in UrlMappings.groovy
```
"/sample"(controller: "sample", action:"index", method: "GET")
```
Test it first.

Add Spring Security @annotation in index() method
```
	package oauth2.test
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
class SampleController {
  @Secured("#oauth2.isClient()")
  def index() {
    def map = [suceed: true]
    render map as JSON;
  }
}
```
And test it again.
 
Because of “unauthorized” occurs, using POSTMAN to get access_token for Spring Security.
 
Because we @Secured(“#oauth2.isClient()”) and according to POSTMAN is a client for our application, so POSTMAN can use this method.

In the other side, if we change it to @Secured(“#oauth2.isUser()”)
And using POSTMAN test it
We got the error
 
This is because of our access_token is for client (from postman), not user.
So, we must get a user token to query this method.
```
curl -X POST -u my-client -d "grant_type=password" -d "username=my-user" -d "password=my-password" -d "scope=read" http://localhost:8080/oauth/token
```
After enter host password, we got the new access_token 
And test it again, you can be succeeded again.

If you want to know more spring security for SecuredOAuth2Resource, please reference https://bluesliverx.github.io/grails-spring-security-oauth2-provider/v3/manual/guide/single.html#installPlugin.
A more concept you can reference http://blog.nbostech.com/2015/09/grails-sample-oauth2/

