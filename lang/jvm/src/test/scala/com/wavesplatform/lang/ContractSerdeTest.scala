package com.wavesplatform.lang

import com.wavesplatform.lang.Common.NoShrink
import com.wavesplatform.lang.contract.Contract._
import com.wavesplatform.lang.contract.{Contract, ContractSerDe}
import com.wavesplatform.lang.v1.compiler.Terms._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Assertion, FreeSpec, Matchers}

class ContractSerdeTest extends FreeSpec with PropertyChecks with Matchers with NoShrink {

  def roundTrip(c: Contract): Assertion = {
    val bytes = ContractSerDe.serialize(c)
    val conEi = ContractSerDe.deserialize(bytes)

    conEi shouldBe 'right
    conEi.right.get shouldBe c
  }

  "roundtrip" - {

    "empty" in roundTrip(Contract(Nil, Nil, None))

//    "empty" in {
//      val cf = ContractFunction(
//        CallableAnnotation("whoooo"),
//        FUNC("anotherFunc", List("argssss"), CONST_BOOLEAN(true))
//      )
//      val bytes = ContractSerDe.serializeContractFunction(,cf)
//
//    }

    "one-declaration" in roundTrip(
      Contract(
        List(
          LET("letName", CONST_BOOLEAN(true))
        ),
        List.empty,
        None
      ))

    "two-declarations" in roundTrip(
      Contract(
        List(
          LET("letName", CONST_BOOLEAN(true)),
          FUNC("funcName", List("arg1", "arg2"), CONST_BOOLEAN(false))
        ),
        List.empty,
        None
      ))

    "callable function" in roundTrip(
      Contract(
        List(),
        List(
          CallableFunction(
            CallableAnnotation("sender"),
            FUNC("foo", List("a"), REF("a"))
          )
        ),
        None
      ))

    "verifier function" in roundTrip(
      Contract(
        List(),
        List(),
        Some(VerifierFunction(VerifierAnnotation("t"), FUNC("verify", List(), TRUE)))
      )
    )

    "full contract" in roundTrip(
      Contract(
        List(
          LET("letName", CONST_BOOLEAN(true)),
          FUNC("funcName", List("arg1", "arg2"), CONST_BOOLEAN(false))
        ),
        List(
          CallableFunction(
            CallableAnnotation("whoooo"),
            FUNC("anotherFunc", List("argssss"), CONST_BOOLEAN(true))
          )
        ),
        Some(
          VerifierFunction(
            VerifierAnnotation("hmmm"),
            FUNC("funcAgain", List("arg"), CONST_BOOLEAN(false))
          )
        )
      ))
  }
}
