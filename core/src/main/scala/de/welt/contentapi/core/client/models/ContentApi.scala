package de.welt.contentapi.core.client.models

import java.time.Instant

import de.welt.contentapi.utils.Strings._
import play.api.Logger

/**
 * Wrapper to configure a search against the content API. For params and values see https://doug-ecs-production.up.welt.de/#_request_parameters
 */
case class ApiContentSearch(`type`: MainTypeParam = MainTypeParam(),
                            subType: SubTypeParam = SubTypeParam(),
                            section: SectionParam = SectionParam(),
                            homeSection: HomeSectionParam = HomeSectionParam(),
                            sectionExcludes: SectionExcludes = SectionExcludes(),
                            flag: FlagParam = FlagParam(),
                            tag: TagParam = AnyTagParam(),
                            page: PageParam = PageParam(),
                            pageSize: PageSizeParam = PageSizeParam(),
                            fromDate: FromDateParam = FromDateParam(),
                            id: IdParam = IdParam(),
                            themePageId: ThemePageIdParam = ThemePageIdParam(),
                            includeFields: IncludeFieldsParam = IncludeFieldsParam()
                           ) {
  protected[models] def allParams = Seq(
    id, `type`, subType,
    section, homeSection, sectionExcludes,
    flag, tag,
    pageSize, page, fromDate,
    themePageId, includeFields
  )

  /**
   * Returns tuples of params and their respective values {{{type -> news}}}.
   * If it is a list parameter, the values are joined according to the defined operator (`,` or `|)`: {{{sectionHome -> section1,section2}}}
   *
   * @return parameters as tuple to be used directly with [[play.api.libs.ws.WSRequest.withQueryStringParameters()]]
   */
  def getAllParamsUnwrapped: Seq[(String, String)] = allParams.flatMap(_.asTuple)
}

sealed trait AbstractParam[T] {

  /**
   * @return parameter name
   */
  def name: String

  /**
   * @return parameter value(s)
   */
  def value: T

  /**
   * @return [[scala.Option]] wrapped value if value was validated (according to [[PrimitiveParam.valueToStringOpt]])
   *         <br/> [[scala.None]] otherwise
   */
  def valueToStringOpt: T => Option[String]

  /**
   * @return tuple of key and [[valueToStringOpt]]
   */
  def asTuple: Option[(String, String)] = valueToStringOpt(value).map { v => (name, v) }
}

object Operator {
  val Or = "|"
  val And = ","
}

protected abstract class ListParam[T](override val value: List[T]) extends AbstractParam[List[T]] {
  def operator: String = Operator.And

  override def valueToStringOpt: List[T] => Option[String] = {
    case Nil => None
    case list =>
      val cleanedList = list.map(PrimitiveParam[T]().valueToStringOpt).collect {
        case Some(v) => v
      }.mkString(operator)
      Some(cleanedList)
  }
}

protected abstract class ValueParam[T](override val value: T) extends AbstractParam[T] {
  override def valueToStringOpt: T => Option[String] = PrimitiveParam[T]().valueToStringOpt
}

private case class PrimitiveParam[T]() {
  /**
   * Validate some basic types and return [[scala.None]] if value is invalid or empty
   *
   * @return
   */
  //noinspection ScalaStyle
  def valueToStringOpt: T => Option[String] = {
    case s: String if containsTextContent(s) => Some(stripWhiteSpaces(s))
    case _: String => None

    case i: Int if Int.MinValue != i => Some(i.toString)
    case _: Int => None

    case i: Instant if Instant.MIN != i => Some(i.toString)
    case _: Instant => None

    case unknown@_ =>
      Logger(getClass.getName.stripSuffix("$")).warn(s"Unknown value type: ${unknown.getClass.toString}")
      None
  }
}

case class MainTypeParam(override val value: List[String] = Nil) extends ListParam[String](value) {
  @deprecated(message = "Use the primary constructor instead", since = "0.8")
  def this(singleValue: String) {
    this(List(singleValue))
  }

  override val name: String = "type"
}

case class SubTypeParam(override val value: List[String] = Nil) extends ListParam[String](value) {
  @deprecated(message = "Use the primary constructor instead", since = "0.8")
  def this(singleValue: String) {
    this(List(singleValue))
  }

  override val name: String = "subType"
}

case class SectionParam(override val value: List[String] = Nil) extends ListParam[String](value) {
  @deprecated(message = "Use the primary constructor instead", since = "0.8")
  def this(singleValue: String) {
    this(List(singleValue))
  }

  override val name: String = "sectionPath"

  override def operator: String = Operator.Or
}

case class HomeSectionParam(override val value: List[String] = Nil) extends ListParam[String](value) {
  @deprecated(message = "Use the primary constructor instead", since = "0.8")
  def this(singleValue: String) {
    this(List(singleValue))
  }

  override val name: String = "sectionHome"

  override def operator: String = Operator.Or
}

case class SectionExcludes(override val value: List[String] = Nil) extends ListParam[String](value) {
  @deprecated(message = "Use the primary constructor instead", since = "0.8")
  def this(singleValue: String) {
    this(List(singleValue))
  }

  override val name: String = "excludeSections"
}

case class FlagParam(override val value: List[String] = Nil) extends ListParam[String](value) {
  @deprecated(message = "Use the primary constructor instead", since = "0.8")
  def this(singleValue: String) {
    this(List(singleValue))
  }

  override val name: String = "flag"
}

abstract class TagParam(override val value: List[String] = Nil) extends ListParam[String](value) {
  override val name: String = "tag"

  override def operator: String = Operator.Or
}

case class AnyTagParam(override val value: List[String] = Nil) extends TagParam(value) {
  override def operator: String = Operator.Or
}

case class AllTagParam(override val value: List[String] = Nil) extends TagParam(value) {
  override def operator: String = Operator.And
}

case class PageSizeParam(override val value: Int = Int.MinValue) extends ValueParam[Int](value) {
  override val name: String = "pageSize"
}

case class PageParam(override val value: Int = Int.MinValue) extends ValueParam[Int](value) {
  override val name: String = "page"
}

case class FromDateParam(override val value: Instant = Instant.MIN) extends ValueParam[Instant](value) {
  override val name: String = "fromDate"
}

case class ThemePageIdParam(override val value: String = "") extends ValueParam[String](value) {
  override val name: String = "themepage"
}

case class IdParam(override val value: List[String] = Nil) extends ListParam[String](value) {
  override val name: String = "id"
}

case class IncludeFieldsParam(override val value: List[String] = Nil) extends ListParam[String](value) {
  override val name: String = "fields"
}

object IncludeFieldsParam {
  val IdField = "id"
  val WebUrlField = "webUrl"
  val TypeField = "type"
  val SubTypeField = "subType"

  val OnwardField = "onward"
  val FieldsField = "fields"
  val ElementsField = "elements"

  val SectionsField = "sections"

  val TagsField = "tags"
  val AuthorsField = "authors"
  val KeywordsField = "keywords"

  def apply(includeFields: String*): IncludeFieldsParam = if (includeFields.isEmpty) {
    IncludeFieldsParam(Nil)
  } else {
    IncludeFieldsParam(
      List(WebUrlField, TypeField)
        .appendedAll(includeFields)
        .filterNot(_.isBlank)
        .distinct
    )
  }

  /**
   * @param innerFields list of field/key names for [[de.welt.contentapi.core.models.ApiContent ApiContent]]`.fields`
   *                    (i.e. `publicationDate`, `headline` ...)
   * @return prefixed field/key nameds (i.e. `fields.publicationDate,fields.headline` ...)
   */
  def fieldsPrefixed(innerFields: String*): String =
    innerFields
      .filterNot(_.isBlank)
      .map(includeField => s"$FieldsField.$includeField")
      .mkString(Operator.And)
}

sealed trait Datasource {
  val `type`: String
  val maxSize: Option[Int]
}

case class CuratedSource(override val maxSize: Option[Int],
                         folderPath: String,
                         filename: String) extends Datasource {
  override val `type`: String = "curated"
}

case class SearchSource(override val maxSize: Option[Int],
                        queries: Seq[AbstractParam[_]] = Seq()) extends Datasource {
  override val `type`: String = "search"
}

