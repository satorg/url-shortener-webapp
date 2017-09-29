package services

import java.nio.{ByteBuffer, ByteOrder}
import java.util.Base64

import com.google.inject.ImplementedBy

import scala.util.Try

@ImplementedBy(classOf[Base64StringEncoderService])
trait StringEncoderService {

  def encodeLong(value: Long): String

  def decodeLong(str: String): Option[Long]
}

class Base64StringEncoderService extends StringEncoderService {

  override def encodeLong(value: Long): String = {
    val bytes = createBuffer().putLong(value).array().dropWhile(_ == 0)
    Base64.getUrlEncoder.encodeToString(bytes)
  }

  override def decodeLong(str: String): Option[Long] =
    Try {
      val bytes = Base64.getUrlDecoder.decode(str)
      val buffer = createBuffer()

      buffer.position(buffer.capacity() - bytes.length)
      bytes.foreach(buffer.put)

      buffer.getLong(0)
    }.toOption

  private def createBuffer() = {
    ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN)
  }
}
