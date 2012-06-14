package play.modules.mail


import play.api._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Akka._
import akka.pattern.ask
import akka.util.Duration
import java.util.concurrent.Callable
import org.codemonkey.simplejavamail.{Email, Mailer}
import play.modules.mail.MailBuilder.Mail
import play.modules.mail.MailWorker.Start
import play.api.Play.current

/**
 * User: alabbe
 * Date: 01/03/12
 * Time: 17:19
 */

class MailPlugin(app:Application) extends Plugin {

  lazy val helper = MailHelper(
    app.configuration.getString("smtp.host").getOrElse(MailPlugin.DEFAULT_HOST),
    app.configuration.getInt("smtp.port").getOrElse(MailPlugin.DEFAULT_PORT).toInt,
    app.configuration.getString("smtp.username").getOrElse(""),
    app.configuration.getString("smtp.password").getOrElse("")
  )
}

object MailPlugin {
   private[mail] val DEFAULT_HOST = "localhost"
   private[mail] val DEFAULT_PORT = 25

  lazy val worker = {
    Logger.info("Mail plugin starting...")
    MailWorker.ref ! Start(helper.mailer)
    Logger.info("Mail plugin successfully started with smtp server on %s:%s".format(helper.host, helper.port))
    MailWorker.ref
  }

   def send(email:Mail)(implicit app:Application):Promise[Boolean] = {
      app.configuration.getString("mail.smtp") match {
         case Some(s) if (s.equalsIgnoreCase("dev")) => Mock.send()
         case _ => {
            sendMessage(email.toEmail)
         }
      }
   }

   private def sendMessage(msg:Email)(implicit  app:Application):Promise[Boolean] = {
      import akka.util.Timeout
      implicit val timeout = Timeout(Duration(5, "seconds"))
      (worker ? (msg)).mapTo[Boolean].asPromise   //FIX-ME, switch to fire and forget
   }
   
   private def helper(implicit app:Application):MailHelper = app.plugin[MailPlugin] match {
      case Some(plugin) => plugin.helper
      case _ => throw PlayException("MailPugin Error", "The MailPlugin is not initialized, Please edit your conf/play.plugins file and add the following line: '400:play.modules.mailb.MailPlugin' (400 is an arbitrary priority and may be changed to match your needs).")
   }
}

private[mail] case class MailHelper(host:String=MailPlugin.DEFAULT_HOST, port:Int=MailPlugin.DEFAULT_PORT, username:String ="", password:String ="") {
   def mailer = new Mailer(host, port, username, password)
}

object Mock {
   def send():Promise[Boolean] = {
      Akka.future[Boolean](true)
   }
}

