import java.io.File
import java.nio.file.{StandardCopyOption, Files, Paths}
import sys.process._
import xml.XML

val pom = XML.loadFile("pom.xml")
val artId = (pom \ "artifactId").text
val ver = (pom \ "version").text
val filePrefix = "%s-%s".format(artId, ver)

"mvn.bat clean package scala:doc".!
Process("jar cf ../../%s-javadoc.jar *".format(filePrefix), new File("target/site/scaladocs")).!
Files.copy(Paths.get("pom.xml"), Paths.get("target/%s.pom".format(filePrefix)), StandardCopyOption.REPLACE_EXISTING)

"mvn.bat gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=target/%s.pom -Dfile=target/%s.jar".format(filePrefix, filePrefix).!
"mvn.bat gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=target/%s.pom -Dfile=target/%s-sources.jar -Dclassifier=sources".format(filePrefix, filePrefix).!
"mvn.bat gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=target/%s.pom -Dfile=target/%s-javadoc.jar -Dclassifier=javadoc".format(filePrefix, filePrefix).!