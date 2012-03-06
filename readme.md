A Play 2.0 plugin providing a scala wrapper to simple-java-mail.

Installation
============

As a binary
-----------

Checkout the project, build it from the sources with `sbt package` command.
Put the jar available in target/scala-2.9.1 to the lib folder of your play app.

As a Git submodule
------------------
You can add it as a submodule of your play project.
Checkout the project in modules/mail-plugin, then do `git submodule add`

In your project Build.scala add the dependency to the plugin :

        val mailPlugin = Project("mailPlugin", file("modules/mail-plugin"))
        val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
                    .dependsOn(mailPlugin)


Usage
=====

Add a play.plugins file in your conf directory with :
        400:play.modules.mail.MailPlugin

Then in your controller, you can do :

        import play.modules.mail._
        import play.modules.mail.MailBuilder._
        import play.api.Play.current

        def sendMail = Action { request =>
            val m = Mail()
                        .from("Bibi","no-reply@bibi.com")
                        .to(List(("Toto","toto@bibi.com",To())))
                        .subject("A subject")
                        .html(views.html.mail())
            MailPlugin.send(m)
            Ok("It works")
        }

Configuration
=============
In application.conf :

        #put this setting in you want to mock the mail server in development
        mail.smtp=dev

        #smtp server settings
        smtp.host=smtp.server.com
        smtp.port=25
        smtp.username=
        smtp.password=


