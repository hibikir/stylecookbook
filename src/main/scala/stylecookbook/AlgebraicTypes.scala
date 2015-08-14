package stylecookbook

import spray.json._
import scala.reflect.runtime.universe._

trait AlgebraicTypes {

  sealed trait Stooge

  case object Larry extends Stooge
  case object Moe extends Stooge
  case object Curly extends Stooge


  object Stooge {
    val values = Seq(Larry,Moe,Curly)
  }

  sealed trait Marx {
    def name:String
    def quote:String
  }

  case object Marx {
    val values = Seq(Groucho,Harpo,Chico,Karl)
    implicit val marxFormat = new EnumFormat[Marx](values,_.name)
  }

  case object Groucho extends Marx{
    val name = "Groucho"
    val quote = "It is better to have loft and lost than to never have loft at all."
  }


  case object Harpo extends Marx{
    val name = "Harpo"
    val quote = "Honk Honk."
  }


  case object Chico extends Marx{
    val name = "Chico"
    val quote = "Who are you going to believe, me or your own eyes?"
  }

  case object Karl extends Marx{
    val name = "Karl"
    val quote = "Let the ruling classes tremble at a communist revolution. The proletarians have " +
      "nothing to lose but their chains. They have a world to win. Workingmen of all countries, unite!"
  }


}

class EnumFormat[T](values:Seq[T], stringifier: T=>String)(implicit tag: TypeTag[T]) extends JsonFormat[T] {
  override def read(json: JsValue):T =
    json match {
      case s: JsString =>
        values.find(x=>stringifier(x) == s.value).getOrElse(deserializationError(s.toString + " is not a valid " + tag.tpe))
      case x  => deserializationError(x.toString + " is not a String")
    }
  override def write(obj: T) = JsString(stringifier(obj))
}
