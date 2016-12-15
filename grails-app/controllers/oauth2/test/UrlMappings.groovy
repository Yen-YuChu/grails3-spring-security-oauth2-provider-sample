package oauth2.test

import com.yourapp.User

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/sample"(controller: "sample", action:"index", method: "GET")

        "/auth/auth_facebook"(controller: "auth", action:"authFacebook", method: "GET")
        "/auth/auth_google"(controller: "auth", action:"authGoogle", method: "GET")
        "/auth/logout"(controller: "auth", action:"logout", method: "GET")
        "/auth/savephoto/$id"(controller: "auth",action: "uploadPhoto",method:"POST")
        "/auth/loadphoto/$id"(controller: "auth",action: "downloadPhoto",method:"GET");

        "/auth/forgetPassword"(controller: "auth", action:"forgotPassword", method: "GET")

        "/message/"(controller: "message", action: "create", method: "POST")
        "/message/$id"(controller: "message", action:"update", method: "PUT")
        "/message/$id"(controller: "message", action:"show", method: "GET")
        "/message/$id"(controller: "message", action:"delete", method: "DELETE")
        "/message/all"(controller: "message", action:"index", method: "GET")
        //"/auth/savephoto"(controller: "auth", action: "uploadPhoto", method:"POST")
        //"/auth/loadphoto"(controller: "auth", action: "downloadPhoto", method:"GET")

        "/register"(controller: 'register',action: "save", method:"POST")

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
