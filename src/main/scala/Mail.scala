
package play.modules.mail

import play.api._
import play.api.libs.concurrent._
import templates.Html
import play.libs.Akka
import akka.actor.{Props, Actor}
import akka.pattern.ask
import akka.util.duration._
import org.codemonkey.simplejavamail.{MailException, Email, Mailer}
import java.util.concurrent.Callable
import play.modules.mail.MailBuilder.Mail


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

   override def onStart() {
      Logger.info("Mail plugin starting...")
      Logger.info("Mail plugin successfully started with smtp server on %s:%s".format(helper.host, helper.port))
   }
}

object MailPlugin {
   private[mail] val DEFAULT_HOST = "localhost"
   private[mail] val DEFAULT_PORT = 25

   def send(email:Mail)(implicit app:Application):Promise[Boolean] = {
      app.configuration.getString("mail.smtp") match {
         case Some(s) if (s.equalsIgnoreCase("dev")) => Mock.send()
         case _ => {
            sendMessage(email.toEmail)
         }
      }
   }

   private def sendMessage(msg:Email)(implicit  app:Application):Promise[Boolean] = {
      val mailer = helper.mailer
      import akka.util.Timeout
      implicit val timeout = Timeout(5 second)
      (MailWorker.ref ? (msg,mailer)).mapTo[Boolean].asPromise
   }
   
   private def helper(implicit app:Application):MailHelper = app.plugin[MailPlugin] match {
      case Some(plugin) => plugin.helper
      case _ => throw PlayException("MailPugin Error", "The MailPlugin is not initialized, Please edit your conf/play.plugins file and add the following line: '400:play.modules.mailb.MailPlugin' (400 is an arbitrary priority and may be changed to match your needs).")
   }
}

object MailBuilder {


   import javax.mail.Message.RecipientType
   sealed case class Recipient(t:RecipientType)
   case class To() extends Recipient(RecipientType.TO)
   case class Bcc() extends Recipient(RecipientType.BCC)
   case class CC() extends Recipient(RecipientType.CC)

   object Mail {
      def apply():Mail = new Mail()
   }

   case class Mail(_from:Option[Tuple2[String, String]] = None, _subject:Option[String] = None, _to:List[Tuple3[String, String, Recipient]] = Nil, _text:Option[String] = None, _html:Option[Html] = None) {
      def from(f:Tuple2[String, String]):Mail = this.copy(_from = Some(f))
      def subject(s:String):Mail = this.copy(_subject = Some(s))
      def to(t:List[Tuple3[String, String, Recipient]]):Mail = this.copy(_to = t)
      def text(t:String):Mail = this.copy(_text = Some(t))
      def html(h:Html):Mail = this.copy(_html = Some(h))

      def toEmail:Email = {
         val email = new Email();
         this._from.map(f => email.setFromAddress(f._1,f._2))
         this._subject.map(s => email.setSubject(s))
         this._to.foreach(t => email.addRecipient(t._1,t._2,t._3.t))
         this._text.map(s => email.setText(s))
         this._html.map(h => email.setTextHTML(h.toString))
         email
      }
   }
}

class MailWorker extends Actor {
   def receive = {
      case (email:Email,mailer:Mailer) => {
         try {
            mailer.sendMail(email)
            Logger.info("MailPlugin: email sent")
            sender ! true
         } catch {
            case e:MailException => {
               Logger.error("MailPlugin error:"+e.getMessage)
               sender ! false
            }
         }
      }
   }
}

object MailWorker {
   val ref = Akka.system.actorOf(Props[MailWorker])
}

private[mail] case class MailHelper(host:String=MailPlugin.DEFAULT_HOST, port:Int=MailPlugin.DEFAULT_PORT, username:String ="", password:String ="") {
   def mailer = new Mailer(host, port, username, password)
}

object Mock {
   def send():Promise[Boolean] = {
      Akka.future(new Callable[Boolean]() {def call() = true} ).getWrappedPromise
   }
}

