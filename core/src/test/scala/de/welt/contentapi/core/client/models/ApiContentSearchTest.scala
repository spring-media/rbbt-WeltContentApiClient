package de.welt.contentapi.core.client.models

import java.time.Instant

import org.scalatestplus.play.PlaySpec

class ApiContentSearchTest extends PlaySpec {

  "ApiContentSearch" should {

    "use all declared fields for creating the query parameters" in {
      val query: ApiContentSearch = ApiContentSearch(`type` = MainTypeParam(List("live")))

      query.allParams.size mustBe query.getClass.getDeclaredFields.length
    }

    "create a list of key value strings from all passed parameters which can be passed into the model" in {
      val query: ApiContentSearch = ApiContentSearch(
        `type` = MainTypeParam(List("live")),
        subType = SubTypeParam(List("ticker")),
        section = SectionParam(List("/dickButt/")),
        homeSection = HomeSectionParam(List("/dickButt/")),
        sectionExcludes = SectionExcludes(List("-derpSection", "-derpinaSection")),
        flag = FlagParam(List("highlight")),
        tag = AnyTagParam(List("person-574114", "thing-574111")),
        pageSize = PageSizeParam(10),
        page = PageParam(1),
        id = IdParam(List("-157673104", "-157078453")),
        themePageId = ThemePageIdParam("109266998")
      )

      query.getAllParamsUnwrapped mustBe List(
        "type" -> "live",
        "subType" -> "ticker",
        "sectionPath" -> "/dickButt/",
        "sectionHome" -> "/dickButt/",
        "excludeSections" -> "-derpSection,-derpinaSection",
        "flag" -> "highlight",
        "tag" -> "person-574114|thing-574111",
        "pageSize" -> "10",
        "page" -> "1",
        "id" -> "-157673104,-157078453",
        "themepage" -> "109266998",
      )
    }

    "create comma separated tags parameter" in {
      val query = ApiContentSearch(tag = AllTagParam(List("person-5", "thing-57")))

      query.getAllParamsUnwrapped mustBe List("tag" -> "person-5,thing-57")
    }

    "create a list of key value strings only from defined parameters" in {
      val query: ApiContentSearch = ApiContentSearch(`type` = MainTypeParam(List("live")))

      query.getAllParamsUnwrapped mustBe List("type" -> "live")
    }

    "main type is ','ed" in {
      val mainTypeParam = ApiContentSearch(`type` = MainTypeParam(List("main1", "main2"))).getAllParamsUnwrapped

      mainTypeParam must contain("type" -> "main1,main2")
    }

    "home section is '|'ed" in {
      val homeParam = ApiContentSearch(homeSection = HomeSectionParam(List("home1", "home2"))).getAllParamsUnwrapped

      homeParam must contain("sectionHome" -> "home1|home2")
    }

    "date types are handled correctly" in {
      val dateParam = ApiContentSearch(fromDate = FromDateParam(Instant.ofEpochMilli(0))).getAllParamsUnwrapped

      dateParam must contain("fromDate" -> "1970-01-01T00:00:00Z")
    }
  }
}
