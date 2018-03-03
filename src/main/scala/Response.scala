/**
  * Created by natsumi.sawa on 2017/02/06.
  */

import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

case class Response (
  status: String,
  contentType: String,
  body: Array[Byte],
  lastModified: Long
  ){
  def writeTo(output: OutputStream): Unit ={
    val CRLF = "\r\n"
    val responseBodySize = body.length
    val sdf = new SimpleDateFormat("E d MMM yyyy HH:mm:ss Z")
    val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
    val responseHeader = "HTTP/1.1 " + status + CRLF +
      "Content-Length: " + responseBodySize + CRLF +
      "Content-Type: " + contentType + CRLF +
      "Content-Language: ja" + CRLF +
      "Connection: Close" + CRLF +
      "Last-Modified: " + sdf.format(lastModified) + CRLF +
      "Server: pnuts" + CRLF + CRLF
    output.write(responseHeader.getBytes(StandardCharsets.UTF_8))
    if(body.nonEmpty)output.write(body)
  }
}

