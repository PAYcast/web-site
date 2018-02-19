package converter

import java.nio.file.{ Files, Paths, StandardOpenOption }
import java.text.SimpleDateFormat
import java.util.Date

import cats.effect.{ Effect, IO, Sync }
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.syntax.traverse._
import io.circe.Decoder
import io.circe.parser._

import scala.io.Source

object Main extends App {
  new Main[IO].apply().unsafeRunSync()
}

class Main[F[_]: Effect] {

  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
  private val creativeCommons = Licence(
    name = "Creative Commons License",
    img = "http://i.creativecommons.org/l/by-nc-sa/3.0/80x15.png",
    url = "http://creativecommons.org/licenses/by-nc-sa/3.0/",
    description = "Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License")

  private def toAwsUrl(url: String) = "https://s3-eu-west-1.amazonaws.com/paycast" + url.substring(url.lastIndexOf('/'))

  private def convertToMarkdown(post: Post) =
    s"""---
       |title: "${post.title}"
       |date: ${dateFormat.format(post.issued)}
       |tags: [ ${post.categories.map(c => s""""$c"""").mkString(", ")} ]
       |draft: false
       |podcast_file: "${toAwsUrl(post.enclosures.head.url)}"
       |podcast_type: "${post.enclosures.head.`type`}"
       |---
       |${post.body.showNotes}
     """.stripMargin

  private def createPost(post: Post) = Sync[F].delay {
    val fileName = post.title.replace(' ', '-')
    val markdown = convertToMarkdown(post)
    Files.write(Paths.get(s"../../post/$fileName.md"), markdown.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING)
  }

  def apply(): F[Unit] = for {
    input <- Sync[F].delay(Source.fromFile("../paycast_archive.json", "UTF-8").mkString)
    posts <- Sync[F].fromEither(decode[List[Post]](input))
    _     <- posts.traverse(createPost)
  } yield ()

  case class Enclosure(length: Long, url: String, `type`: String = "audio/mpeg")
  object Enclosure {
    implicit val EnclosureDecoder: Decoder[Enclosure] = Decoder.forProduct3("length", "url", "type")(Enclosure.apply)
  }

  case class Licence(name: String, img: String, url: String, description: String)
  case class Body(showNotes: String,
                  music: String = "<<My First Time>> Mary Poppins and the Dubitative Sex Toys Boys",
                  licence: Licence = creativeCommons)

  case class Post(title: String, guid: String, link: String, enclosures: List[Enclosure], body: Body, itemTime: Long, categories: Set[String], comments: String, issued: Date)
  object Post {
    private val issuedDateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    implicit val DateDecoder: Decoder[Date] = Decoder.decodeString.emap { str =>
      Either.catchNonFatal(issuedDateFormat.parse(str)).leftMap(_ => "Date")
    }
    implicit val BodyDecoder: Decoder[Body] = Decoder.decodeString.emap { str =>
      str.indexOf("""<p><span id="more-""").some
        .filter(_ >= 0)
        .fold(Body(str)) { i => Body(str.substring(0, i)) }
        .asRight
    }
    implicit val PostDecoder: Decoder[Post] = Decoder.forProduct9("_title", "_guid", "_link", "_enclosures", "_body", "_itemtime", "_categories", "_comments", "_issued")(Post.apply)
  }

}
