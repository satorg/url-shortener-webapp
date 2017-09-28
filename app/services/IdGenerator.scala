package services

import java.util.UUID

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[RandomIdGenerator])
trait IdGenerator {
  def generateId(): String
}

class RandomIdGenerator extends IdGenerator {
  override def generateId(): String = {
    UUID.randomUUID().toString
  }
}
