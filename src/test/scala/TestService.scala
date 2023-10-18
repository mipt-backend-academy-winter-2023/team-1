import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object Spec extends ZIOSpecDefault {
  def spec = suite("Tests")(
    test("Test 1") {
      assertTrue(2 + 2 == 4)
    },
    test("Test 2") {
      assertFalse(2 + 2 == 5)
    }
  )
}