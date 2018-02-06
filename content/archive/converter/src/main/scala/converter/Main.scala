package converter

import java.nio.file.{ Files, Paths, StandardOpenOption }
import java.text.SimpleDateFormat
import java.util.Date

import argonaut._
import Argonaut._

import scala.io.Source

object Main extends App {
  val input = Source.fromFile("../paycast_archive.json", "UTF-8").mkString
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  for {
    post <- input.decodeOption[List[Post]].getOrElse(Nil)
    markdown = convertToMarkdown(post)
    _ = println(s"Post: $markdown")
    _ = createPost(post.title.replace(' ', '-'), markdown)
  } yield ()

  def convertToMarkdown(post: Post): String =
    s"""---
       |title: "${post.title}"
       |date: ${dateFormat.format(post.issued)}
       |tags: [ ${post.categories.mkString(", ")} ]
       |draft: false
       |---
       |${post.body}
     """.stripMargin

  def createPost(fileName: String, markdown: String): Unit = {
    Files.write(Paths.get(s"../../post/$fileName.md"), markdown.getBytes("UTF-8"), StandardOpenOption.CREATE_NEW)
  }

}

case class Enclosure(length: Long, url: String, `type`: String = "audio/mpeg")
object Enclosure {
  implicit def PersonCodecJson: CodecJson[Enclosure] = casecodec3(Enclosure.apply, Enclosure.unapply)("length", "url", "type")
}
case class Post(title: String, guid: String, link: String, enclosures: List[Enclosure], body: String, itemTime: Long, categories: Set[String], comments: String, issued: Date)
object Post {
  implicit val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit def DateEncodeJson(implicit formatter: SimpleDateFormat): EncodeJson[java.util.Date] =
    EncodeJson(d => jString(formatter.format(d)))

  implicit def DateDecodeJson(implicit formatter: SimpleDateFormat): DecodeJson[java.util.Date] =
    optionDecoder(_.string flatMap (s => tryTo(formatter.parse(s))), "java.util.Date")

  implicit def PersonCodecJson: CodecJson[Post] = casecodec9(Post.apply, Post.unapply)("_title", "_guid", "_link", "_enclosures", "_body", "_itemtime", "_categories", "_comments", "_issued")
}
