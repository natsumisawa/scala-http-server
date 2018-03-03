/**
  * Created by natsumi.sawa on 2017/02/14.
  */

import java.io.{File, FileInputStream, InputStream}
import java.net.Socket
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class HttpProtocol(socket: Socket) extends Thread {

  override def run() {
    for {
      input <- Loan(socket.getInputStream)
      output <- Loan(socket.getOutputStream)
       }{
      val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
      val headerStringList = readUntilEmptyLine(input).split("\r\n").toList
      val Array(method, path, httpVersion, _*) = headerStringList.head.split(" ")
      val headers = headerStringList.tail.map{ line =>
        val (k, v) = line.span(_ != ':')
          k.trim -> v.tail.trim
        }.toMap
      val ifModifiedSince = headers.get("If-Modified-Since").map{x => OffsetDateTime.parse(x, formatter).toEpochSecond*1000}
      val req = Request(method, path, httpVersion, ifModifiedSince)

      val file = if (req.path == "/" || req.path == "") {
        new File("project/public/index.html")
      } else if (req.path.contains(".")) {
        new File(s"project/public${req.path}")
      } else {
        new File(s"project/public${req.path}/index.html")
      }

      val res = if (req.ifModifiedSince.exists(x => x <= file.lastModified())){
          Response("304 Not Modified", "text/plane", Array(), file.lastModified())
      } else if(file.exists) {
        val file_is = new FileInputStream(file)
        for (file_is <- Loan(file_is))
          Response("200 OK", path2mime(req.path), readFile(file_is), file.lastModified())
      } else {
        val file_is = new FileInputStream(new File("project/public/notfound.html"))
        for (file_is <- Loan(file_is))
          Response("404 Not Found", "text/html", readFile(file_is), file.lastModified())
      }
      res.writeTo(output)
    }

    def readUntilEmptyLine(is: InputStream, acc: List[Int] = Nil): String = {
      is.read :: acc match {
        case x if x.take(4) == List(10, 13, 10, 13) => x.reverse.map(_.toChar).mkString //10,13は改行
        case x if x.head == -1 => acc.reverse.map(_.toChar).mkString
        case x => readUntilEmptyLine(is, x)
      }
    }

    def readFile(br: FileInputStream, acc: List[Int] = Nil): Array[Byte] = {
      br.read() match {
        case x if x == -1 => acc.reverse.map(_.toByte).toArray
        case x => readFile(br, x :: acc)
      }
    }

    def path2mime(path: String): String = {
      val mimeTable = Map(
        "jpg" -> "image/jpg",
        "jpeg" -> "image/jpeg",
        "png" -> "image/png",
        "html" -> "text/html"
      )
      mimeTable.getOrElse(path.split("\\.").last, "text/html")
    }
  }
}