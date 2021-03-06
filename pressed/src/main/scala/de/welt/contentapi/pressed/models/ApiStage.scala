package de.welt.contentapi.pressed.models

import de.welt.contentapi.core.models.ApiReference

/**
  * @param teasers       All Teasers that belong to a Stage
  * @param configuration Configuration for that Stage, with layout, label etc.
  */
case class ApiStage(index: Int,
                    teasers: Seq[ApiTeaser],
                    configuration: Option[ApiStageConfiguration] = None) {
  // stages with content are always valid
  lazy val hasContent = teasers.nonEmpty
  // no content, but has a Commercial? => CommercialStage
  lazy val hasCommercial = configuration.exists(_.commercials.getOrElse(Nil).nonEmpty)
  lazy val isValidStage = hasContent || hasCommercial
}

/**
  * @param layout       Name of the layout for the stage, e.g. 'channel-hero', 'multimedia' or 'hidden'
  * @param label        Label to show above a stage
  * @param logo         An optional logo for the stage. eg.g `/icon/`
  * @param sponsoring   An optional sponsoring consisting of a linked logo and/or slogan
  * @param references   References to render with href and label, e.g. Sub Ressorts
  * @param commercials  contains the format ids for the Ads
  * @param trackingName is used by Funkotron for tracking clicks on articles in stages (e.g. Webtrekk - important for Editors and BI!)
  * @param link         is used by Funkotron for linking the stage header label (e.g. Welt+ stage links to //www.welt.de/weltplus/ channel)
  */
case class ApiStageConfiguration(layout: String = "Default",
                                 label: Option[String],
                                 logo: Option[String] = None,
                                 sponsoring: Option[ApiSponsoringConfiguration] = None,
                                 references: Option[Seq[ApiReference]] = None,
                                 commercials: Option[Seq[String]] = None,
                                 trackingName: Option[String],
                                 link: Option[ApiReference] = None) {
  lazy val unwrappedCommercials: Seq[String] = commercials.getOrElse(Nil)
  lazy val unwrappedReferences: Seq[ApiReference] = references.getOrElse(Nil)
}

/**
  * Wraps the Teaser[[ApiPressedContent]] with it's Layout information.
  *
  * @param teaserConfig How to render a Teaser[[ApiPressedContent]] on a Section Page.
  * @param data         The Teaser content fully pressed with all it's data.
  */
case class ApiTeaser(teaserConfig: ApiTeaserConfig, data: ApiPressedContent)

/**
  * Describes how to render the Teaser[[ApiPressedContent]].
  *
  * @param profile Describes how the Teaser is rendered in a RWD grid. Think of: small, medium, large. This is only
  *                a mapping value for the client.
  * @param `type`  Teaser Type is the name/type of the Teaser -- e.g. 'DefaultTeaser' or 'HeroTeaser'. This is only
  *                a mapping value for the client.
  */
case class ApiTeaserConfig(profile: String, `type`: String)
