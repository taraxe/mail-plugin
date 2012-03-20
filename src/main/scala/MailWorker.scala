package play.modules.mail

import play.api.Logger
import play.libs.Akka

import akka.actor.{Actor, Props}

import org.codemonkey.simplejavamail.{MailException, Email, Mailer}
import play.modules.mail.MailWorker.Start

object MailWorker {
   sealed trait Event
   case class Start(mailer:Mailer) extends Event
   val ref = Akka.system.actorOf(Props[MailWorker])
}

class MailWorker extends Actor {
   var mailer:Mailer = null
   def receive = {
      case Start(mailer:Mailer) => this.mailer = mailer
      case email:Email => {
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


// vim: set ts=2 sw=2 ft=scala et:
