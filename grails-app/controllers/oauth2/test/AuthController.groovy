package oauth2.test

import com.yourapp.Role
import com.yourapp.User
import com.yourapp.UserRole
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.plugins.rest.client.RestBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices

class AuthController {

  def springSecurityService;

  @Autowired
  private ConsumerTokenServices consumerTokenServices;

  def rest = new RestBuilder()

  def index() { }

  private static HashMap getMapForTokenParameter(String username)
  {
    def map = [:];
    map.put("username",username);
    map.put("password",username);
    map.put("grant_type","password");
    map.put("client_id","my-client");
    map.put("client_secret","my-secret");
    map.put("scope","read");

    return map;

  }

  @Secured("#oauth2.isUser()")
  def downloadPhoto(params)
  {
    def content = loadPhoto(params);
    if(content)
    {
      println "if content";
      response.outputStream << content;
      response.status = 200;
    }

  }

  def loadPhoto(params)
  {
    println "downloadPhoto";
    User u = User.get(params.id as Long)
    if(!u)
    {
      throw new IllegalArgumentException("User with ID ${params.id} not found");
    }
    if(u?.photo)
    {
      println "user has photo";
      return u.photo;
    }

  }

  @Secured("#oauth2.isUser()")
  def uploadPhoto(params)
  {
    println "id : $params.id";

    if (params.id)
    {
      def file = request.getFile('file');
      if(file)
      {
        savePhoto(params,file.getBytes());
      }
    }
    User u = springSecurityService.currentUser
    println "user id : " + u.id;

    def map = [uploadPhoto_suceed: true]
    render map as JSON;

  }

  public void savePhoto(params, byte[] content)
  {
    println "SavePhoto";
    User u = User.get(params.id as Long)
    if(u)
    {
      u.photo = content;
      u.save(failOnError : true);
      println "save photo done"
    }

  }

  def logout(params)
  {
    String access_token = params.token ? params.token : null;
    println access_token;

    try
    {
      /** reference http://stackoverflow.com/questions/21987589/spring-security-how-to-log-out-user-revoke-oauth2-token*/
      def b = consumerTokenServices.revokeToken(access_token);
      println b;
    }catch (Exception e)
    {
      println "error";
    }

    def map = [logout_suceed: true]
    render map as JSON;

  }

  @Secured("#oauth2.isClient()")
  def authFacebook(params)
  {
    //println params.token;
    String fb_access_token = params.token ? params.token : null;
    println fb_access_token;

    //client token should be on Header

    /** Step 1. Using token to FB for getting userProfile*/
    if(fb_access_token)
    {
      def resp = rest.get("https://graph.facebook.com/me?access_token=$fb_access_token")
      println resp.status;
      def json = resp.json;
      println json;
      println json.id;
      String username = json.id;
      String email = json.email;

      def count = User.executeQuery("select count(*) from User where username = '$username'")
      println count[0];

      if(count[0] != 1)
      {
        /** register it for FB User*/
        def user = new User();
        user.username = username;
        user.password = username;
        user.email = email
        user.save(flush: true, failOnError: true);

        def userRole = Role.findByAuthority('ROLE_USER');
        UserRole.create(user, userRole, true);
      }

      def map = getMapForTokenParameter(username);
      def resp2 = rest.post("http://localhost:8080/oauth/token?username={username}&password={password}&grant_type={grant_type}&client_id={client_id}&client_secret={client_secret}&scope={scope}"){
        urlVariables map
      }

      def json2 = resp2.json;
      println "$username : " + json2;

      render json2 as JSON;

    }
  }

  @Secured("#oauth2.isClient()")
  def authGoogle(params)
  {
    String id_token = params.token ? params.token : null;
    println id_token;

    if(id_token)
    {
      def resp = rest.get("https://www.googleapis.com/oauth2/v1/tokeninfo?id_token=$id_token")
      println resp.status;
      def json = resp.json;
      println json;
      println json.user_id;
      String username = json.user_id;
      String email = json.email;

      def count = User.executeQuery("select count(*) from User where username = '$username'")
      println count[0];

      if(count[0] != 1)
      {
        /** register it for FB User*/
        def user = new User();
        user.username = json?.user_id;
        user.password = json?.user_id;
        user.email = email;
        user.save(flush: true, failOnError: true);
      }

      def map = getMapForTokenParameter(username);
      def resp2 = rest.post("http://localhost:8080/oauth/token?username={username}&password={password}&grant_type={grant_type}&client_id={client_id}&client_secret={client_secret}&scope={scope}"){
        urlVariables map
      }


      def json2 = resp2.json;
      println "$username : " + json2;

      render json2 as JSON;


    }


  }
}

