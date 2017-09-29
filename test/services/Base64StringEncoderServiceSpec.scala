package services

import org.scalatest.{FreeSpec, MustMatchers, OptionValues}

class Base64StringEncoderServiceSpec extends FreeSpec with OptionValues with MustMatchers {

  val testee = new Base64StringEncoderService

  def testEncodeDecode(value: Long): Unit = {
    s"$value" in {
      val encoded = testee.encodeLong(value)
      val decoded = testee.decodeLong(encoded)

      decoded.value mustBe value
    }
  }

  "should correctly encode/decode value" - {
    testEncodeDecode(0L)
    testEncodeDecode(1L)
    testEncodeDecode(1000L)
    testEncodeDecode(1000000L)
    testEncodeDecode(1000000000L)
    testEncodeDecode(1000000000000L)
    testEncodeDecode(Byte.MaxValue)
    testEncodeDecode(Short.MaxValue)
    testEncodeDecode(Int.MaxValue)
    testEncodeDecode(Long.MaxValue)
    testEncodeDecode(-1L)
    testEncodeDecode(-1000L)
    testEncodeDecode(-1000000L)
    testEncodeDecode(-1000000000L)
    testEncodeDecode(-1000000000000L)
    testEncodeDecode(Byte.MinValue)
    testEncodeDecode(Short.MinValue)
    testEncodeDecode(Int.MinValue)
    testEncodeDecode(Long.MinValue)
  }
  "decode should return None for incorrect input" - {
    "not base64 string" in {
      testee.decodeLong("$") mustBe None
    }
  }
}
