package de.welt.contentapi.core.client.models

import de.welt.contentapi.utils.Loggable
import de.welt.contentapi.utils.Strings._

import java.time.Instant

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
                            includeFields: IncludeFieldsParam = IncludeFieldsParam(),
                            teaserOnlyParam: TeaserOnlyParam = TeaserOnlyParam()
                           ) {

  /**
   * Returns tuples of params and their respective values {{{type -> news}}}.
   * If it is a list parameter, the values are joined according to the defined operator (`,` or `|)`: {{{sectionHome -> section1,section2}}}
   *
   * @return parameters as tuple to be used directly with [[play.api.libs.ws.WSRequest.withQueryStringParameters()]]
   */
  def getAllParamsUnwrapped: Seq[(String, String)] =
    productIterator.flatMap { case value: AbstractParam[_] => value.asTuple }.toSeq
}

sealed trait AbstractParam[T] {

  def name: String

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

trait ListParam[T] extends AbstractParam[List[T]] {
  def operator: String = Operator.And

  override def valueToStringOpt: List[T] => Option[String] = {
    case Nil => None
    case list =>
      val cleanedList = list
        .map(PrimitiveParam.valueToStringOpt)
        .collect { case Some(v) => v }
      Some(cleanedList.mkString(operator))
  }
}

trait ValueParam[T] extends AbstractParam[T] {
  override def valueToStringOpt: T => Option[String] = PrimitiveParam.valueToStringOpt
}

case class MainTypeParam(override val value: List[String] = Nil) extends ListParam[String] {

  override val name: String = "type"
}

case class SubTypeParam(override val value: List[String] = Nil) extends ListParam[String] {

  override val name: String = "subType"
}

case class SectionParam(override val value: List[String] = Nil) extends ListParam[String] {

  override val name: String = "sectionPath"

  override def operator: String = Operator.Or
}

case class HomeSectionParam(override val value: List[String] = Nil) extends ListParam[String] {

  override val name: String = "sectionHome"

  override def operator: String = Operator.Or
}

case class SectionExcludes(override val value: List[String] = Nil) extends ListParam[String] {

  override val name: String = "excludeSections"
}

case class FlagParam(override val value: List[String] = Nil) extends ListParam[String] {

  override val name: String = "flag"
}

trait TagParam extends ListParam[String] {
  override val name: String = "tag"
}

case class AnyTagParam(override val value: List[String] = Nil) extends TagParam {
  override def operator: String = Operator.Or
}

case class AllTagParam(override val value: List[String] = Nil) extends TagParam {
  override def operator: String = Operator.And
}

case class PageSizeParam(override val value: Int = Int.MinValue) extends ValueParam[Int] {
  override val name: String = "pageSize"
}

case class PageParam(override val value: Int = Int.MinValue) extends ValueParam[Int] {
  override val name: String = "page"
}

case class FromDateParam(override val value: Instant = Instant.MIN) extends ValueParam[Instant] {
  override val name: String = "fromDate"
}

case class ThemePageIdParam(override val value: String = "") extends ValueParam[String] {
  override val name: String = "themepage"
}

case class IdParam(override val value: List[String] = Nil) extends ListParam[String] {
  override val name: String = "id"
}

case class IncludeFieldsParam(override val value: List[String] = Nil) extends ListParam[String] {
  override val name: String = "fields"
}

case class TeaserOnlyParam(override val value: Boolean = false) extends ValueParam[Boolean] {
  override val name: String = "teaserOnly"
}

object PrimitiveParam extends Loggable {

  def valueToStringOpt[T]: T => Option[String] = {
    case s: String => Option.when(containsTextContent(s))(stripWhiteSpaces(s))

    case i: Int => Option.when(Int.MinValue != i)(i.toString)

    case i: Instant => Option.when(Instant.MIN != i)(i.toString)

    case b: Boolean => Some(b.toString)

    case unknown =>
      log.warn(s"Unknown value type: ${unknown.getClass.toString}")
      None
  }
}

object Operator {
  val Or = "|"
  val And = ","
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

