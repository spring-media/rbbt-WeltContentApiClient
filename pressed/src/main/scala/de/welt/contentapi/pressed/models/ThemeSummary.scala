package de.welt.contentapi.pressed.models

import de.welt.contentapi.core.models.ApiContent

case class ThemeSummary(path: String, id: String, title: String)

case class ThemeDetails(currentPage: Int,
                        pageSize: Int,
                        pages: Int,
                        total: Int,
                        content: ApiContent,
                        related: Seq[ApiContent])
