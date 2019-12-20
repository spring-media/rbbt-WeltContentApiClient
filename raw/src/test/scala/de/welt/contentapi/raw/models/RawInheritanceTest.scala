package de.welt.contentapi.raw.models

import org.scalatestplus.play.PlaySpec
import play.api.Logger

class RawInheritanceTest extends PlaySpec {

  "ContentConfiguration" should {

    "be None for root section without Config" in new TestScope {
      rootNoConfig.getMaybeContentOverrides mustBe None
    }

    "inherit from parent /sport/ to channel /sport/fussball/" in new TestScope {
      fussball.getMaybeContentOverrides mustBe sport.config.content
    }

    "be Some for /sport/" in new TestScope {
      sport.getMaybeContentOverrides mustBe sport.config.content
    }

    "inherit over more than one level for /sport/fussball/bundesliga/" in new TestScope {
      bundesliga.getMaybeContentOverrides mustBe sport.config.content
    }
  }

  "Active Inheritance with CMCF" should {

    "set the same RawChannelStageConfiguration Object for all children" in new TestScope {
      // Given
      private val template = RawChannelStageConfiguration(
        templateName = Some("default")
      )
      // When
      root.batchInheritRawChannelStageConfigurationToAllChildren(template, "user")

      // Then
      root.stageConfiguration mustBe None
      sport.stageConfiguration mustBe Some(template)
      fussball.stageConfiguration mustBe Some(template)
      bundesliga.stageConfiguration mustBe Some(template)
    }

    "set a Taboola Config for all children" in new TestScope {
      // Given
      val newTaboola = RawChannelTaboolaCommercial(
        showNews = false,
        showWeb = false,
        showWebExtended = false,
        showNetwork = false
      )
      // When
      root.batchInheritRawChannelTaboolaCommercialToAllChildren(newTaboola, "user")

      // Then
      root.config.commercial.contentTaboola mustBe RawChannelTaboolaCommercial()
      sport.config.commercial.contentTaboola mustBe newTaboola
      fussball.config.commercial.contentTaboola mustBe newTaboola
      bundesliga.config.commercial.contentTaboola mustBe newTaboola
    }

    "set a RawChannelTheme for all children" in new TestScope {
      // Given
      val newTheme = RawChannelTheme(name = Some("theme"))

      // When
      root.batchInheritRawChannelThemeToAllChildren(newTheme = newTheme, "user")

      // Then
      root.config.theme mustBe None
      sport.config.theme mustBe Some(newTheme)
      fussball.config.theme mustBe Some(newTheme)
      bundesliga.config.theme mustBe Some(newTheme)
    }

    "sets the user and lastMod date after inheritance actions" in new TestScope {
      private val now = 55555555L
      root.batchInheritGenericToAllChildren((rawChannel: RawChannel) => Logger.debug(s"${rawChannel.id.label}"), "user", now)
      root.metadata.changedBy mustNot be("user")
      sport.metadata.changedBy mustBe "user"
      fussball.metadata.changedBy mustBe "user"
      bundesliga.metadata.changedBy mustBe "user"

      bundesliga.metadata.lastModifiedDate mustBe now
    }
  }

}


trait TestScope {

  val rootNoConfig: RawChannel = RawChannel(
    id = RawChannelId(
      path = "/",
      label = "home",
      escenicId = 6
    ),
    config = RawChannelConfiguration(
      content = None
    )
  )

  val bundesliga: RawChannel = RawChannel(
    id = RawChannelId(
      path = "/sport/fussball/bundesliga/",
      label = "bundesliga",
      escenicId = 5000
    ),
    config = RawChannelConfiguration(
      content = None
    )
  )
  val fussball: RawChannel = RawChannel(
    id = RawChannelId(
      path = "/sport/fussball/",
      label = "fussball",
      escenicId = 500
    ),
    config = RawChannelConfiguration(
      content = None
    ),
    children = Seq(bundesliga)
  )
  val sport: RawChannel = RawChannel(
    id = RawChannelId(
      path = "/sport/",
      label = "sport",
      escenicId = 50
    ),
    config = RawChannelConfiguration(
      content = Some(RawChannelContentConfiguration(
        subTypeQueryForText = Some("-ticker"),
        typeQueryForText = Some("article"),
        subTypeQueryForVideo = Some("-broadcast"),
        typeQueryForVideo = Some("video")
      ))
    ), children = Seq(fussball)
  )

  val root: RawChannel = RawChannel(
    id = RawChannelId(
      path = "/",
      label = "home",
      escenicId = 5
    ),
    config = RawChannelConfiguration(
      content = Some(RawChannelContentConfiguration(
        subTypeQueryForText = Some("-ticker"),
        typeQueryForText = Some("article"),
        subTypeQueryForVideo = Some("-broadcast"),
        typeQueryForVideo = Some("video")
      ))
    ),
    children = Seq(sport)
  )

  root.updateParentRelations()

}
