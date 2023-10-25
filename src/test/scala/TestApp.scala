import zio.test.{ZIOSpecDefault, suite, test, assertTrue}

object ExampleSpec extends ZIOSpecDefault {
  override def spec = suite("Tests")(
    test("Test 1") {
      assertTrue(2 + 2 == 4)
    },
    test("Test 2") {
      assertTrue(2 + 3 == 5)
    }
  )
}