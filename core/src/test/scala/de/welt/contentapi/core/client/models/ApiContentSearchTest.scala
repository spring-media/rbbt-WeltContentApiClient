package de.welt.contentapi.core.client.models

import java.time.Instant

import de.welt.contentapi.core.client.models.IncludeFieldsParam._
import de.welt.contentapi.core.models.ApiContent
import org.scalatestplus.play.PlaySpec

class ApiContentSearchTest extends PlaySpec {

  "ApiContentSearch" should {

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
      themePageId = ThemePageIdParam("109266998"),
      includeFields = IncludeFieldsParam("webUrl", "id"),
      teaserOnlyParam = TeaserOnlyParam(true)
    )

    val allParams = query.getAllParamsUnwrapped

    allParams must contain("type" -> "live")
    allParams must contain("subType" -> "ticker")
    allParams must contain("sectionPath" -> "/dickButt/")
    allParams must contain("sectionHome" -> "/dickButt/")
    allParams must contain("excludeSections" -> "-derpSection,-derpinaSection")
    allParams must contain("flag" -> "highlight")
    allParams must contain("tag" -> "person-574114|thing-574111")
    allParams must contain("pageSize" -> "10")
    allParams must contain("page" -> "1")
    allParams must contain("id" -> "-157673104,-157078453")
    allParams must contain("themepage" -> "109266998")
    allParams must contain("fields" -> "webUrl,type,id")
    allParams must contain("teaserOnly" -> "true")
  }

  "create comma separated tags parameter" in {
    val query = ApiContentSearch(tag = AllTagParam(List("person-5", "thing-57")))

    query.getAllParamsUnwrapped mustBe List("tag" -> "person-5,thing-57", "teaserOnly" -> "false")
  }

  "create a list of key value strings only from defined parameters" in {
    val query: ApiContentSearch = ApiContentSearch(`type` = MainTypeParam(List("live")))

    query.getAllParamsUnwrapped mustBe List("type" -> "live", "teaserOnly" -> "false")
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

  "include field names match ApiContent root fields" in {
    val apiContentFieldNames = ApiContent(webUrl = "required", `type` = "required").productElementNames.toSeq

    apiContentFieldNames must contain(IdField)
    apiContentFieldNames must contain(WebUrlField)
    apiContentFieldNames must contain(TypeField)
    apiContentFieldNames must contain(SubTypeField)

    apiContentFieldNames must contain(OnwardField)
    apiContentFieldNames must contain(FieldsField)
    apiContentFieldNames must contain(SectionsField)
    apiContentFieldNames must contain(ElementsField)

    apiContentFieldNames must contain(TagsField)
    apiContentFieldNames must contain(AuthorsField)
    apiContentFieldNames must contain(KeywordsField)
  }

  "include fields param with no fields returns none" in {
    IncludeFieldsParam()
      .asTuple mustBe None
  }

  "include fields param with fields returns param with required fields" in {
    val apiSearchContentResponse = ApiContent(webUrl = "required", `type` = "required")

    IncludeFieldsParam(ElementsField, AuthorsField)
      .asTuple mustBe Some("fields", "webUrl,type,elements,authors")
  }

  "include fields param concatenates required root fields and inner fields" in {
    IncludeFieldsParam(
      IdField, "", "   ", fieldsPrefixed("publicationDate", "headline", "", " "), SubTypeField
    )
      .asTuple mustBe Some("fields", "webUrl,type,id,fields.publicationDate,fields.headline,subType")
  }
}
