/**
  * Created by natsumi.sawa on 2017/02/14.
  */

case class Request(
  method: String,
  path: String,
  httpVersion: String,
  ifModifiedSince: Option[Long]
)

