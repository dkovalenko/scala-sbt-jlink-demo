import com.typesafe.sbt.packager.docker.Cmd
import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._

name := """test-jlink"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, JlinkPlugin, DockerPlugin, AshScriptPlugin)

scalaVersion := "2.13.5"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

jlinkModulePath := {
  fullClasspath
    .in(jlinkBuildImage)
    .value
    .filter { item =>
      item.get(moduleID.key).exists { modId =>
        modId.name == "paranamer"
      } || 
      item.data.toString().contains("icu4j")
    }
    .map(_.data)
}

jlinkIgnoreMissingDependency := JlinkIgnore.only(
  "ch.qos.logback.classic" -> "javax.servlet.http",
  "ch.qos.logback.classic.boolex" -> "groovy.lang",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.control",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.reflection",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime.callsite",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime.typehandling",
  "ch.qos.logback.classic.gaffer" -> "groovy.lang",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.control",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.control.customizers",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.reflection",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.callsite",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.typehandling",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.wrappers",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.transform",
  "ch.qos.logback.classic.helpers" -> "javax.servlet",
  "ch.qos.logback.classic.helpers" -> "javax.servlet.http",
  "ch.qos.logback.classic.selector.servlet" -> "javax.servlet",
  "ch.qos.logback.classic.servlet" -> "javax.servlet",
  "ch.qos.logback.core.boolex" -> "org.codehaus.janino",
  "ch.qos.logback.core.joran.conditional" -> "org.codehaus.commons.compiler",
  "ch.qos.logback.core.joran.conditional" -> "org.codehaus.janino",
  "ch.qos.logback.core.net" -> "javax.mail",
  "ch.qos.logback.core.net" -> "javax.mail.internet",
  "ch.qos.logback.core.status" -> "javax.servlet",
  "ch.qos.logback.core.status" -> "javax.servlet.http",
  "io.jsonwebtoken.impl" -> "android.util",
  "io.jsonwebtoken.impl.crypto" -> "org.bouncycastle.jce",
  "io.jsonwebtoken.impl.crypto" -> "org.bouncycastle.jce.spec",
  "javax.transaction" -> "javax.enterprise.context",
  "javax.transaction" -> "javax.enterprise.util",
  "javax.transaction" -> "javax.interceptor",
  "org.joda.time" -> "org.joda.convert",
  "org.joda.time.base" -> "org.joda.convert",
)

jlinkOptions ++= Seq(
  "--no-header-files",
  "--no-man-pages",
  "--compress=2"
)

jlinkModules ++= Seq(
  "jdk.crypto.ec",
  "jdk.unsupported",
  // "jdk.jdwp.agent"
)

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

//play bin file contains is_cygwin windows supported function, which not available in alpine environment with sh
bashScriptExtraDefines := bashScriptExtraDefines.value.map { extra =>
  extra.replace("is_cygwin", """echo "alpine is_cygwin fixing"""")
}

mappings in Universal ++= directory(baseDirectory.value / "jre")

dockerBaseImage := "debian:stable-slim"
dockerExposedPorts := Seq(9000)
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerAdditionalPermissions += (DockerChmodType.UserGroupPlusExecute, "/opt/docker/jre/bin/java")

//docker run -p 9000:9000 test-jlink:1.0-SNAPSHOT