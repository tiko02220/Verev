package com.vector.verevcodex.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.vector.verevcodex.domain.model.analytics.AnalyticsPoint
import com.vector.verevcodex.domain.model.analytics.AnalyticsSegment
import com.vector.verevcodex.presentation.theme.VerevColors
import kotlin.math.roundToInt

@Composable
internal fun VicoAnalyticsAreaChartFromPoints(
    points: List<AnalyticsPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 180.dp,
    animationEpoch: Int = 0,
    valueFormatter: (Float) -> String = { vicoFormatCompactCount(it.toInt()) },
) {
    val displayPoints = remember(points) {
        when {
            points.isEmpty() -> List(7) { AnalyticsPoint(if (it == 3) "—" else "", 0f) }
            points.size == 1 -> listOf(points.first(), AnalyticsPoint("", 0f))
            else -> points
        }
    }
    val xLabels = remember(displayPoints) { displayPoints.map { it.label } }
    val model = remember(displayPoints) { entryModelOf(*displayPoints.map { it.value }.toTypedArray()) }

    AnalyticsVicoChartFrame(modifier = modifier) {
        ProvideChartStyle(
            chartStyle = analyticsChartStyle(entityColors = listOf(lineColor)),
        ) {
            Chart(
                chart = lineChart(),
                model = model,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight),
                startAxis = rememberStartAxis(
                    valueFormatter = remember(valueFormatter) { verticalValueFormatter(valueFormatter) },
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = remember(xLabels) { horizontalValueFormatter(xLabels) },
                ),
            )
        }
    }
}

@Composable
internal fun VicoAnalyticsBarChartFromPoints(
    points: List<AnalyticsPoint>,
    accent: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 140.dp,
    animationEpoch: Int = 0,
    valueFormatter: (Float) -> String = { vicoFormatCompactCount(it.toInt()) },
) {
    val displayPoints = remember(points) {
        points.ifEmpty { List(7) { AnalyticsPoint(if (it == 3) "—" else "", 0f) } }
    }
    val xLabels = remember(displayPoints) { displayPoints.map { it.label } }
    val model = remember(displayPoints) { entryModelOf(*displayPoints.map { it.value }.toTypedArray()) }

    AnalyticsVicoChartFrame(modifier = modifier) {
        ProvideChartStyle(
            chartStyle = analyticsChartStyle(entityColors = listOf(accent)),
        ) {
            Chart(
                chart = columnChart(),
                model = model,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight),
                startAxis = rememberStartAxis(
                    valueFormatter = remember(valueFormatter) { verticalValueFormatter(valueFormatter) },
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = remember(xLabels) { horizontalValueFormatter(xLabels) },
                ),
            )
        }
    }
}

@Composable
internal fun VicoAnalyticsGroupedBarChartFromPoints(
    primaryPoints: List<AnalyticsPoint>,
    secondaryPoints: List<AnalyticsPoint>,
    primaryAccent: Color,
    secondaryAccent: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 148.dp,
    animationEpoch: Int = 0,
    valueFormatter: (Float) -> String = { vicoFormatCompactCount(it.toInt()) },
) {
    val chartPoints = remember(primaryPoints, secondaryPoints) {
        val maxLen = maxOf(primaryPoints.size, secondaryPoints.size, 1)
        (0 until maxLen).map { index ->
            val p = primaryPoints.getOrNull(index)
            val s = secondaryPoints.getOrNull(index)
            VicoGroupedChartPoint(
                label = p?.label ?: s?.label ?: "",
                primary = p?.value ?: 0f,
                secondary = s?.value ?: 0f,
            )
        }
    }
    val xLabels = remember(chartPoints) { chartPoints.map { it.label } }
    val primaryEntries = remember(chartPoints) { entriesOf(*chartPoints.map { it.primary }.toTypedArray()) }
    val secondaryEntries = remember(chartPoints) { entriesOf(*chartPoints.map { it.secondary }.toTypedArray()) }
    val model = remember(primaryEntries, secondaryEntries) { entryModelOf(primaryEntries, secondaryEntries) }

    AnalyticsVicoChartFrame(modifier = modifier) {
        ProvideChartStyle(
            chartStyle = analyticsChartStyle(entityColors = listOf(primaryAccent, secondaryAccent)),
        ) {
            Chart(
                chart = columnChart(mergeMode = ColumnChart.MergeMode.Grouped),
                model = model,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight),
                startAxis = rememberStartAxis(
                    valueFormatter = remember(valueFormatter) { verticalValueFormatter(valueFormatter) },
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = remember(xLabels) { horizontalValueFormatter(xLabels) },
                ),
            )
        }
    }
}

@Composable
internal fun VicoAnalyticsSegmentChart(
    segments: List<AnalyticsSegment>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 168.dp,
) {
    val displaySegments = remember(segments) {
        segments.ifEmpty { listOf(AnalyticsSegment(label = "—", value = 0)) }
    }
    val xLabels = remember(displaySegments) { displaySegments.map { it.label } }
    val colors = remember(displaySegments) {
        listOf(VerevColors.Gold, VerevColors.Moss, VerevColors.Tan, VerevColors.ForestBright)
            .take(maxOf(displaySegments.size, 1))
    }
    val model = remember(displaySegments) { entryModelOf(*displaySegments.map { it.value }.toTypedArray()) }

    AnalyticsVicoChartFrame(modifier = modifier) {
        ProvideChartStyle(
            chartStyle = analyticsChartStyle(entityColors = colors),
        ) {
            Chart(
                chart = columnChart(),
                model = model,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight),
                startAxis = rememberStartAxis(
                    valueFormatter = remember { verticalValueFormatter { vicoFormatCompactCount(it.toInt()) } },
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = remember(xLabels) { horizontalValueFormatter(xLabels, allowSparseLabels = false) },
                ),
            )
        }
    }
}

@Composable
private fun AnalyticsVicoChartFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(VerevColors.SurfaceSoft)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        content = content,
    )
}

@Composable
private fun analyticsChartStyle(entityColors: List<Color>) = m3ChartStyle(
    axisLabelColor = VerevColors.Forest.copy(alpha = 0.72f),
    axisGuidelineColor = VerevColors.Forest.copy(alpha = 0.14f),
    axisLineColor = VerevColors.Forest.copy(alpha = 0.22f),
    entityColors = entityColors,
    elevationOverlayColor = VerevColors.SurfaceSoft,
)

private fun verticalValueFormatter(
    formatter: (Float) -> String,
) = object : AxisValueFormatter<AxisPosition.Vertical.Start> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence = formatter(value)
}

private fun horizontalValueFormatter(
    labels: List<String>,
    allowSparseLabels: Boolean = true,
) = object : AxisValueFormatter<AxisPosition.Horizontal.Bottom> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        val index = value.roundToInt()
        if (index !in labels.indices) return ""
        val label = labels[index]
        return if (allowSparseLabels && !vicoShouldShowChartLabel(index, labels.size)) {
            ""
        } else {
            vicoFormatChartLabel(label)
        }
    }
}

private data class VicoGroupedChartPoint(
    val label: String,
    val primary: Float,
    val secondary: Float,
)

private fun vicoShouldShowChartLabel(index: Int, totalCount: Int): Boolean =
    when {
        totalCount <= 7 -> true
        totalCount <= 14 -> index % 2 == 0
        totalCount <= 24 -> index % 3 == 0
        else -> index % 4 == 0
    }

private fun vicoFormatChartLabel(raw: String): String {
    val compact = raw.trim()
    if (compact.length <= 4) return compact
    return compact.take(4)
}

private fun vicoFormatCompactCount(value: Int): String =
    when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000f)
        value >= 1_000 -> String.format("%.1fK", value / 1_000f)
        else -> value.toString()
    }
