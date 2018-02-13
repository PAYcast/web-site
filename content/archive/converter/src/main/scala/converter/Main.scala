package converter

import java.nio.file.{ Files, Paths, StandardOpenOption }
import java.text.SimpleDateFormat
import java.util.Date

import cats.effect.IO
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import io.circe.Decoder
import io.circe.parser._

import scala.io.Source

object Main extends App {

  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  private def decodeJson[A: Decoder](input: String) = IO.fromEither(decode[A](input))

  private def convertToMarkdown(post: Post) =
    s"""---
       |title: "${post.title}"
       |date: ${dateFormat.format(post.issued)}
       |tags: [ ${post.categories.map(c => s""""$c"""").mkString(", ")} ]
       |draft: false
       |---
       |${post.body}
     """.stripMargin

  private def createPost(post: Post) = IO {
    val fileName = post.title.replace(' ', '-')
    val markdown = convertToMarkdown(post)
    Files.write(Paths.get(s"../../post/$fileName.md"), markdown.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING)
  }

  (for {
    input <- IO(Source.fromFile("../paycast_archive.json", "UTF-8").mkString)
    posts <- decodeJson[List[Post]](input)
    _     <- posts.traverse(createPost)
  } yield ()).unsafeRunSync()

  case class Enclosure(length: Long, url: String, `type`: String = "audio/mpeg")
  object Enclosure {
    implicit val EnclosureDecoder: Decoder[Enclosure] = Decoder.forProduct3("length", "url", "type")(Enclosure.apply)
  }

  case class Post(title: String, guid: String, link: String, enclosures: List[Enclosure], body: String, itemTime: Long, categories: Set[String], comments: String, issued: Date)
  object Post {
    implicit val archiveDateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    implicit def DateDecodeJson(implicit formatter: SimpleDateFormat): Decoder[Date] = Decoder.decodeString.emap { str =>
      Either.catchNonFatal(formatter.parse(str)).leftMap(_ => "Date")
    }
    implicit val PostDecoder: Decoder[Post] = Decoder.forProduct9("_title", "_guid", "_link", "_enclosures", "_body", "_itemtime", "_categories", "_comments", "_issued")(Post.apply)
  }

}
