
import java.net.{ServerSocket}

object HTTPServer {

  def main(args: Array[String]): Unit = {
    println("Start----->")
    val serverSocket = new ServerSocket(8000)
    println("Serving HTTP on localhost port 8000 ...")
    while (true) {
      val socket = serverSocket.accept()
      new HttpProtocol(socket).start()
    }
    serverSocket.close()
  }
}