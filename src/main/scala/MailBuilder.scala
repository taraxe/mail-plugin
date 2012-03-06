package play.modules.mail

import play.api.templates.Html


object MailBuilder {
   import javax.mail.Message.RecipientType
   sealed case class Recipient(t:RecipientType)
   case class To() extends Recipient(RecipientType.TO)
   case class Bcc() extends Recipient(RecipientType.BCC)
   case class CC() extends Recipient(RecipientType.CC)

   object Mail {
      def apply():Mail = new Mail()
   }

   case class Mail(_from:Option[(String, String)] = None, _subject:Option[String] = None, _to:List[(String, String, Recipient)] = Nil, _text:Option[String] = None, _html:Option[Html] = None) {
      def from(f:(String, String)):Mail = this.copy(_from = Some(f))
      def subject(s:String):Mail = this.copy(_subject = Some(s))
      def to(t:List[(String, String, Recipient)]):Mail = this.copy(_to = t)
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

// vim: set ts=2 sw=2 ft=scala et:
