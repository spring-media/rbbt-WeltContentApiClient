package de.welt.contentapi.pressed.client.converter

import de.welt.contentapi.pressed.models._
import de.welt.contentapi.raw.models._
import de.welt.testing.TestHelper.raw.channel._
import org.scalatestplus.play.PlaySpec

class Raw2ApiConfigurationTests extends PlaySpec {

  trait TestScopeConfiguration {
    val rawChannelMetadata = RawChannelMetadata(
      title = Some("Meta Title"),
      description = Some("Meta Description"),
      keywords = Some(Seq("Keyword1", "Keyword2")),
      contentRobots = Some(RawChannelMetaRobotsTag(noFollow = Some(false), noIndex = Some(false))),
      sectionRobots = Some(RawChannelMetaRobotsTag(noFollow = Some(true), noIndex = Some(true)))
    )
    val rawChannelTheme: RawChannelTheme = RawChannelTheme(
      name = Some("theme"),
      fields = Some(Map("key1" -> "value2", "key2" -> "value2"))
    )
    val rawChannelSponsoring = RawSponsoringConfig(
      logo = Some("logo"),
      slogan = Some("slogan"),
      hidden = true,
      link = Some(RawSectionReference(Some(""), Some("/link-for-sponsor-logo.html"))),
      brandstation = Some("presented")
    )
    val rawChannelCommercial = RawChannelCommercial(
      definesAdTag = true,
      definesVideoAdTag = true,
      contentTaboola = RawChannelTaboolaCommercial(
        showNetwork = false,
        showNews = false,
        showWeb = false,
        showWebExtended = false
      ),
      showFallbackAds = false,
      disableAdvertisement = true,
      isTrackingOnly = true
    )

    val rawChannelSiteBuilding = RawChannelSiteBuilding(
      fields = Some(Map("key1" -> "value2", "key2" -> "value2")),
      sub_navigation = Some(Seq(RawSectionReference(Some("Label"), Some("/Path/")))),
      elements = Some(Seq(
        RawElement(
          id = RawChannelElement.IdDefault,
          `type` = "mood",
          assets = Some(List(
            RawAsset(
              `type` = "image",
              fields = Some(Map("key1" -> "value2", "key2" -> "value2", "sponsoring_ad_indicator" -> "true"))
            )
          ))
        )
      ))
    )

    val rawChannelConfiguration = RawChannelConfiguration(
      metadata = Some(rawChannelMetadata),
      theme = Some(rawChannelTheme),
      siteBuilding = Some(rawChannelSiteBuilding),
      commercial = rawChannelCommercial,
      brand = true
    )

    private val rawChannelId = 100
    val converter: RawToApiConverter = new RawToApiConverter(new InheritanceCalculator())
    val rawChannel: RawChannel = emptyWithIdAndConfig(rawChannelId, rawChannelConfiguration)
    val apiChannel: ApiChannel = converter.apiChannelFromRawChannel(rawChannel)
    val apiSiteBuildingConfiguration: ApiSiteBuildingConfiguration = converter.calculateSiteBuilding(rawChannel).get
    val apiCommercialConfiguration: ApiCommercialConfiguration = converter.apiCommercialConfigurationFromRawChannel(rawChannel)

    val apiMetaConfiguration: ApiMetaConfiguration = converter.apiMetaConfigurationFromRawChannel(rawChannel = rawChannel)
      .getOrElse(throw new scala.Error("Test failed. MetaConfiguration must be defined."))
    val apiCommercial3rdPartyConfiguration: ApiCommercial3rdPartyConfiguration = apiCommercialConfiguration.thirdParty
      .getOrElse(throw new scala.Error("Test failed. 3rdParty must be defined."))
    val apiThemeConfiguration: ApiThemeConfiguration = converter.apiThemeFromRawChannel(rawChannel)
      .getOrElse(throw new scala.Error("Test failed. ThemeConfig must be defined."))
  }

  "RawChannel to ApiChannel" must {

    "convert `section`" in new TestScopeConfiguration {
      apiChannel.section.map(s => s.label ++ s.href) mustBe Some(List(rawChannel.id.label, rawChannel.id.path))
    }

    "calculate `master`" in new TestScopeConfiguration {
      apiChannel.master mustBe defined
    }

    "convert `breadcrumb`" in new TestScopeConfiguration {
      apiChannel.unwrappedBreadcrumb.size mustBe 1
    }

    "calculate `brand`" in new TestScopeConfiguration {
      apiChannel.brand mustBe defined
    }

  }

  "RawChannelMetadata to ApiMetaConfiguration" must {

    "convert `title`" in new TestScopeConfiguration {
      apiMetaConfiguration.title mustBe rawChannelMetadata.title
    }

    "convert `keyword` to `tags`" in new TestScopeConfiguration {
      apiMetaConfiguration.tags mustBe rawChannelMetadata.keywords
    }

    "convert `description`" in new TestScopeConfiguration {
      apiMetaConfiguration.description mustBe rawChannelMetadata.description
    }

    "convert `contentRobots` to `contentMetaRobots`" in new TestScopeConfiguration {
      apiMetaConfiguration.contentMetaRobots.flatMap(_.noFollow) mustBe rawChannelMetadata.contentRobots.flatMap(_.noFollow)
      apiMetaConfiguration.contentMetaRobots.flatMap(_.noIndex) mustBe rawChannelMetadata.contentRobots.flatMap(_.noIndex)
    }

    "convert `sectionRobots` to `sectionMetaRobots`" in new TestScopeConfiguration {
      apiMetaConfiguration.sectionMetaRobots.flatMap(_.noFollow) mustBe rawChannelMetadata.sectionRobots.flatMap(_.noFollow)
      apiMetaConfiguration.sectionMetaRobots.flatMap(_.noIndex) mustBe rawChannelMetadata.sectionRobots.flatMap(_.noIndex)
    }

  }

  "RawChannelSiteBuilding to ApiSiteBuildingConfiguration" must {

    "convert `fields`" in new TestScopeConfiguration {
      apiSiteBuildingConfiguration.fields mustBe rawChannelSiteBuilding.fields
    }

    "convert `sub_navigation`" in new TestScopeConfiguration {
      apiSiteBuildingConfiguration
        .unwrappedSubNavigation
        .flatMap(r => r.label ++ r.href) must contain theSameElementsAs rawChannelSiteBuilding
        .unwrappedSubNavigation
        .flatMap(r => r.label ++ r.path)
    }

    "convert `elements`" in new TestScopeConfiguration {
      apiSiteBuildingConfiguration
        .unwrappedElements
        .flatMap(
          r => r.`type` + r.unwrappedAssets.flatMap(r2 => r2.`type`) + r.unwrappedAssets.flatMap(r2 => r2.fields)
        ) must contain theSameElementsAs rawChannelSiteBuilding
        .unwrappedElements
        .flatMap(r => r.`type` + r.unwrappedAssets.flatMap(r2 => r2.`type`) + r.unwrappedAssets.flatMap(r2 => r2.fields))
    }
  }

  "RawChannelCommercial to ApiCommercialConfiguration" must {

    "calculate `pathForAdTag` from `definesAdTag`" in new TestScopeConfiguration {
      apiCommercialConfiguration.pathForAdTag mustBe defined
    }

    "calculate `pathForVideoAdTag` from `definesVideoAdTag`" in new TestScopeConfiguration {
      apiCommercialConfiguration.pathForVideoAdTag mustBe defined
    }

    "convert `RawChannelTaboolaCommercial` to `ApiCommercial3rdPartyConfiguration.ApiCommercialTaboolaConfiguration`" in new TestScopeConfiguration {
      apiCommercial3rdPartyConfiguration.contentTaboola.flatMap(_.showNetwork) mustBe Some(rawChannelCommercial.contentTaboola.showNetwork)
      apiCommercial3rdPartyConfiguration.contentTaboola.flatMap(_.showWeb) mustBe Some(rawChannelCommercial.contentTaboola.showWeb)
      apiCommercial3rdPartyConfiguration.contentTaboola.flatMap(_.showWeb) mustBe Some(rawChannelCommercial.contentTaboola.showWebExtended)
      apiCommercial3rdPartyConfiguration.contentTaboola.flatMap(_.showNews) mustBe Some(rawChannelCommercial.contentTaboola.showNews)
    }

    "copy 'showFallbackAds = false' value from RAW to API" in new TestScopeConfiguration {
      apiCommercialConfiguration.showFallbackAds mustBe Some(false)
    }

    "copy 'isTrackingOnly = true' value from RAW to API" in new TestScopeConfiguration {
      apiCommercialConfiguration.isTrackingOnly mustBe Some(true)
    }

    "assume true as default value for showFallbackAds" in new TestScopeConfiguration {
      val channel = emptyWithId(123L)
      val convertedCommercialConfig: ApiCommercialConfiguration = converter.apiCommercialConfigurationFromRawChannel(channel)
      convertedCommercialConfig.showFallbackAds mustBe Some(true)
    }

    "by default advertisements are enabled per channel" in new TestScopeConfiguration {
      val channel = emptyWithId(123L)
      val convertedCommercialConfig: ApiCommercialConfiguration = converter.apiCommercialConfigurationFromRawChannel(channel)
      convertedCommercialConfig.disableAdvertisement mustBe Some(false)
    }

    "disabled advertisements on a channel(raw model) results in true option (api model)" in new TestScopeConfiguration {
      apiCommercialConfiguration.disableAdvertisement mustBe Some(true)
    }

  }

  "RawChannelTheme to ApiThemeConfiguration (calculated)" must {

    "convert `name`" in new TestScopeConfiguration {
      apiThemeConfiguration.name mustBe rawChannelTheme.name
    }

    "convert `fields`" in new TestScopeConfiguration {
      apiThemeConfiguration.unwrappedFields mustBe rawChannelTheme.unwrappedFields
    }

  }

}



