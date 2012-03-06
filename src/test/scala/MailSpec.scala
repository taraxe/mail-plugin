import javax.mail.Message.RecipientType
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.modules.mail._
import play.api.libs.json._
import org.junit.runner.RunWith
import org.codemonkey.simplejavamail._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MailSpec extends Specification {
   "Mail" should {
      "send dummy email using mock" in {
         running(FakeApplication(
            additionalConfiguration= Map(
               "mail.smtp" -> "dev"/*,
               "smtp.host" -> "localhost")*/
            ))) {
            import play.api.Play.current

            val email = new Email();

            email.setFromAddress("lollypop", "lolly.pop@somemail.com");
            email.setSubject("hey");
            email.addRecipient("C. Cane", "candycane@candyshop.org", RecipientType.TO);
            email.addRecipient("C. Bo", "chocobo@candyshop.org", RecipientType.BCC);
            email.setText("We should meet up! ;)");
            email.setTextHTML("<img src='cid:wink1'><b>We should meet up!</b><img src='cid:wink2'>");

            MailPlugin.send(email).map(r => println("Mail sent ? "+r))

            success
         }
      }
   }
}