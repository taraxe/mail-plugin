package play.modules.mail

import play.api.Logger
import play.libs.Akka

import akka.actor.{Actor, Props}

import org.codemonkey.simplejavamail.{MailException, Email, Mailer}

object MailWorker {
   val ref = Akka.system.actorOf(Props[MailWorker])
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


// vim: set ts=2 sw=2 ft=scala et:
