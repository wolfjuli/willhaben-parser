package solutions.lykos.willhaben.parser.backend.api.wh

import java.time.ZonedDateTime

data class WHSearchResult(
    val id: Int,
    val description: String,
    val heading: String,
    val verticalId: Int,
    val searchId: Int,
    val rowsRequested: Int,
    val rowsFound: Int,
    val rowsReturned: Int,
    val pageRequested: Int,
    val searchDate: ZonedDateTime,
    val lastUserAlertViewedDate: String?,
    val newAdsSeparatorPosition: String?,
    val advertSummaryList: WHAdvertSummaries,
)
