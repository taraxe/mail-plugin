A Play 2.0 plugin providing a scala wrapper to simple-java-mail.

Add a play.plugins file in your conf directory with :
400:play.modules.mail.MailPlugin

Then in your controller, you can do :

import play.modules.mail._
import play.modules.mail.MailBuilder._

def sendMail = Action { request =>
    val m = Mail()
                .from("Bibi","no-reply@bibi.com")
                .to(List(("Toto","toto@bibi.com",To())))
                .subject("A subject")
                .html(views.html.mail())
    MailPlugin.send(m)
    Ok("It works")
}

