package de.welt.contentapi.core.models

/**
  * The [[ApiResponse]] is a wrapper for the [[ApiContent]] with it's (maybe) resolved related Content. Related Content
  * is SEO relevant -- here with one API call. Generated by WeltN24/underwood (Frank).
  *
  * @param content Single content item
  * @param related Related stuff like content or playlist of the single content item
  */
case class ApiResponse(content: ApiContent,
                       related: Option[List[ApiContent]] = None) {
  lazy val unwrappedRelated: List[ApiContent] = related.getOrElse(Nil)
  lazy val relatedContent: List[ApiContent] = relatedFilteredBy("related")
  lazy val relatedPlaylist: List[ApiContent] = relatedFilteredBy("playlist")

  private[this] def relatedFilteredBy(`type`: String): List[ApiContent] = unwrappedRelated.filter(_.unwrappedRoles.contains(`type`))
}

/**
  * This class is returned by the API, when a batch-id-get request is performed
  *
  * @param results  result of type [[ApiContent]]
  * @since 0.22
 */
case class ApiBatchResult(results: Seq[ApiContent])

/**
  * This class is returned by the API as a Response Wrapper
  *
  * @param response  Wrapper for [[ApiBatchResult]]
  * @since 0.22
 */
case class ApiBatchResponse(response: ApiBatchResult)

/**
  * This class wraps search results from our API. Official documentation at https://content-api.up.welt.de
  *
  * @param page     current result page
  * @param pageSize result page size
  * @param pages    total number of pages available
  * @param total    total number of items that match the search
  * @param results  search result of type [[ApiContent]]
  * @since 0.8
  */
case class ApiSearchResponse(page: Int,
                             pageSize: Int,
                             pages: Int,
                             total: Int,
                             results: List[ApiContent])

/**
  * A [[ApiContent]] is a converted representation of a Escenic Article Type. In Escenic everything is a 'Article'.
  * This is our generic interpretation generated by WeltN24/brian.
  *
  * @param webUrl   Relative web url of the content
  * @param `type`   Main content type (Escenic based) like: article, video, live
  * @param id       Escenic ID. This is needed for: tracking, commercials, videos
  * @param subType  Differentiation of main types like satire, commentary (both article types)
  * @param fields   Generic map with 'data/content' based on the content type. Each main content type has different fields-values
  * @param authors  Authors of the content
  * @param elements All elements for the content. A element has a relation (teaser, opener, inline) and type (image, video)
  * @param roles    Needed for related content (role: playlist or related) (Frank)
  * @param sections Section paths where the content is published
  * @param tags     Tags of the content. Only the Escenic Tags for this content
  * @param onward   Non resolved relations (related content) (GraphQl)
  */
case class ApiContent(webUrl: String,
                      `type`: String,
                      id: Option[String] = None,
                      subType: Option[String] = None,
                      fields: Option[Map[String, String]] = None,
                      authors: Option[List[ApiAuthor]] = None,
                      elements: Option[List[ApiElement]] = None,
                      roles: Option[List[String]] = None,
                      sections: Option[ApiSectionData] = None,
                      tags: Option[List[ApiTag]] = None,
                      onward: Option[List[ApiOnward]] = None) {

  lazy val unwrappedFields: Map[String, String] = fields.getOrElse(Map.empty[String, String])
  lazy val unwrappedAuthors: List[ApiAuthor] = authors.getOrElse(Nil)
  lazy val unwrappedElements: List[ApiElement] = elements.getOrElse(Nil)
  lazy val unwrappedRoles: List[String] = roles.getOrElse(Nil)
  lazy val unwrappedTags: List[ApiTag] = tags.getOrElse(Nil)

  /**
    * Checks if the `entry` is in `fields`
    *
    * @param entry Key-Value pair to check
    */
  def fieldsContainEntry(entry: (String, String)): Boolean = unwrappedFields.exists(_ == entry)

  /**
    * Returns the mandatory `field` value of the key. Otherwise a exception is thrown. Use it only when `field` value is
    * always delivered. E.g. 'headline', 'lastModifyDate' or 'lastPublishedDate'.
    *
    * @throws IllegalStateException when key is not in `fields`
    * @param key key to get the value for
    */
  def getMandatoryFieldEntry(key: String): String = unwrappedFields
    .getOrElse(key, throw new IllegalStateException(s"Mandatory field '$key' not found. Content is not valid. Check BACKEND."))
}

/**
  * Non resolved related content for on-demand fetching
  *
  * @param id    Escenic content id
  * @param roles Usage of the related content like 'playlist' or 'related'
  */
case class ApiOnward(id: String, roles: Seq[String])

/**
  * @param id       Escenic id of author content page (author page)
  * @param name     Name of the author
  * @param position Position of the author
  * @param url      Relative web url the author page
  * @param elements Images of the author
  */
case class ApiAuthor(id: Option[String] = None,
                     name: Option[String] = None,
                     position: Option[String] = None,
                     url: Option[String] = None,
                     elements: Option[List[ApiElement]] = None) {
  lazy val unwrappedElements: List[ApiElement] = elements.getOrElse(Nil)
}

/**
  * @param id        Unique identifier from WeltN24/brian to find a single element inside the content body text
  * @param `type`    Type of element like: image, video, oembed ...
  * @param relations On witch relations is the element used: Teaser, Opener, Closer, Inline ...
  * @param assets    All assets of the element: image asset with url, video asset with url, poster, width, height
  */
case class ApiElement(id: String,
                      `type`: String,
                      assets: Option[List[ApiAsset]],
                      relations: Option[List[String]] = None) {
  lazy val unwrappedRelations: List[String] = relations.getOrElse(Nil)
  lazy val unwrappedAssets: List[ApiAsset] = assets.getOrElse(Nil)
  lazy val metadataAsset: Option[ApiAsset] = unwrappedAssets.find(_.`type` == "metadata")
}


/**
  * @param `type`   Type of the asset like: image, video
  * @param fields   Generic 'data/content' based on the type of asset. E.g. source, width, height
  * @param metadata Api processing meta data like: state, fetchErrors, transformationDate
  * @param index    The index for multiple assets like a gallery
  */
case class ApiAsset(`type`: String,
                    fields: Option[Map[String, String]] = None,
                    metadata: Option[ApiMetadata] = None,
                    index: Option[Int] = None) {
  lazy val unwrappedFields: Map[String, String] = fields.getOrElse(Map.empty[String, String])
  lazy val unwrappedMetadata: Map[String, String] = metadata.map(_.asMap).getOrElse(Map.empty[String, String])
}

/**
  * Some meta data of the content and transformation values of our services
  *
  * @param validToDate   end of license - used for Video/Broadcast articles
  * @param validFromDate start of license - used for Broadcast articles
  * @param catchUp       CatchUp license interval for Broadcast articles
  */
case class ApiMetadata(validToDate: String, validFromDate: Option[String] = None, catchUp: Option[ApiMetadataCatchUp] = None) {
  def asMap: Map[String, String] = Map(
    "validToDate" → validToDate
  ) ++ validFromDate.map("validFromDate" → _)
}

/**
  * CatchUp License Interval for Broadcasts
  *
  * @param gte Start Date of License as Java.Instant as String in Zulu Timezone (GreaterThanOrEquals)
  * @param lt  End Date of License as Java.Instant as String in Zulu Timezone (lowerThan)
  * @since 0.12
  */
case class ApiMetadataCatchUp(gte: String, lt: String)

/**
  * Escenic data of the Sections path the Content is published. This is only a internal escenic representation. Do not
  * use this for labels or breadcrumb. If you want the real Section and breadcrumb use the `ApiChannel` from
  * ApiPressedContent.
  *
  * We use this Model to find the correct Channel in CMCF/Janus.
  *
  * @param home The path of the 'Home' Section.
  *             E.g. `/icon/mode/`
  * @param all  The paths of all Sections the Content is published.
  *             E.g. [`/icon/`, `/icon/mode/`, `/icon/icon-newsletter/`]
  */
case class ApiSectionData(home: Option[String], all: Option[List[String]] = None)

/**
  * @param id    the real value of the tag
  * @param value crap escenic format. don't use this.
  */
case class ApiTag(id: Option[String], value: Option[String] = None)

/**
  * A reference is model to render a <a/>. Used for internal (like Sections) or external links.
  *
  * @param label label of the <a/>
  * @param href  href of the <a/>
  */
case class ApiReference(label: Option[String] = None, href: Option[String] = None)
