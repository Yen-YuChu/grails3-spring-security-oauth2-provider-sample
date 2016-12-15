package oauth2.test

import com.yourapp.Message
import com.yourapp.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import grails.web.http.HttpHeaders


import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

class MessageController extends RestfulController<Message>
{

  MessageController()
  {
    super(Message)
  }

  def springSecurityService;

  def index()
  {
    List<Message> msgList = Message.findAll();
    def map = [message: msgList]
    render map as JSON;
  }

  @Override
  protected Message createResource()
  {
    Message instance = Message.newInstance()

    bindData instance, getObjectToBind()
    instance

  }

  @Override
  protected Message queryForResource(Serializable id)
  {
    if (id && Message.exists(id))
    {
      Message.where {
        id == id
      }.find()
    }
    else
    {
      println "message : $id not found";
    }
  }

  @Secured("#oauth2.isUser()")
  def show() {
    respond queryForResource(params.id)
  }

  @Secured("#oauth2.isUser()")
  @Override
  def save()
  {
    def instance = createResource()

    instance.validate()

    User u = springSecurityService.currentUser;

    println "user id : " + u.id;

    instance.creator_id = u.id //replace creator_id using access_token user id
    instance.user_name = u.username;
    saveResource instance

    request.withFormat {
      form multipartForm {
        flash.message = message(code: 'default.created.message', args: [classMessageArg, instance.id])
        redirect instance
      }
      '*' {
        response.addHeader(HttpHeaders.LOCATION,
                           grailsLinkGenerator.link(resource: this.controllerName, action: 'show', id: instance.id,
                                                    absolute: true,
                                                    namespace: hasProperty('namespace') ? this.namespace : null))
        respond instance, [status: CREATED, view: 'show']
      }
    }
  }

  @Secured("#oauth2.isUser()")
  def update() {
    if(handleReadOnly()) {
      return
    }

    def instance = queryForResource(params.id)
    if (instance == null) {
      transactionStatus.setRollbackOnly()
      notFound()
      return
    }

    println "instance22 id : " + instance.creator_id;
    User user = springSecurityService.currentUser;
    if(instance.creator_id == user.id)
    {
      instance.properties = getObjectToBind()
    }else
    {
      respond instance, [status: 403]
    }

    if (instance.hasErrors()) {
      transactionStatus.setRollbackOnly()
      respond instance.errors, view:'edit' // STATUS CODE 422
      return
    }

      updateResource instance
      request.withFormat {
        form multipartForm {
          flash.message = message(code: 'default.updated.message', args: [classMessageArg, instance.id])
          redirect instance
        }
        '*' {
          response.addHeader(HttpHeaders.LOCATION,
                             grailsLinkGenerator.link(resource: this.controllerName, action: 'show', id: instance.id,
                                                      absolute: true,
                                                      namespace: hasProperty('namespace') ? this.namespace : null))
          respond instance, [status: OK]
        }
      }

  }


  @Secured("#oauth2.isUser()")
  def delete()
  {
    if (handleReadOnly())
    {
      return
    }

    def instance = queryForResource(params.id)
    if (instance == null)
    {
      transactionStatus.setRollbackOnly()
      notFound()
      return
    }
    User user = springSecurityService.currentUser;
    if(instance.creator_id == user.id)
    {
      deleteResource instance

      request.withFormat {
        form multipartForm {
          flash.message = message(code: 'default.deleted.message', args: [classMessageArg, instance.id])
          redirect action: "index", method: "GET"
        }
        '*' {render status: NO_CONTENT} // NO CONTENT STATUS CODE
      }
    }else
    {
      respond instance, [status: 403]
    }



  }





}
