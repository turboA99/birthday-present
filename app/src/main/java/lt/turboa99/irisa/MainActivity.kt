package lt.turboa99.irisa

import android.icu.util.Calendar
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import lt.turboa99.irisa.ui.theme.IrisaTheme
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.floor

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SideEffect {
                //(view.context as Activity).window.navigationBarColor = primary
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
            }

            IrisaTheme {
                Scaffold { paddingValue ->
                    Box(modifier = Modifier.fillMaxSize())
                    {

                        var calendar by remember {
                            mutableStateOf(Calendar.getInstance())
                        }
                        var nowDateTime by remember(calendar) {
                            mutableStateOf(calendar.time.time)
                        }
                        LaunchedEffect(Unit) {
                            while (true) {
                                calendar = Calendar.getInstance()
                                nowDateTime = calendar.time.time
                                delay(100)
                            }
                        }
                        val secFraction by animateFloatAsState(
                            targetValue = ((nowDateTime / 1000 % 60).toFloat() / 60f),
                            tween(200)
                        )
                        val sliderColorsHue = remember {
                            mutableStateListOf(
                                265f,
                                273f,
                                286f
                            )
                        }
                        val sliderColorsLightness = remember {
                            mutableStateListOf(
                                .8f,
                                .73f,
                                .61f
                            )
                        }
                        val dateFormatter: DateTimeFormatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                        val birthDateTime =
                            LocalDateTime.parse("2023-04-28 04:20", dateFormatter)

                        val birthTime = birthDateTime.toInstant(ZoneOffset.UTC).epochSecond
                        val nowTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).epochSecond

                        val maxTimes = listOf(10, 24, 60)
                        val times = remember(birthTime) {
                            //birth 17 - now 18 = -1 -> 24 - 1 = 23 hours
                            //birth 17 - now 4 = 13 -> 13 hours
                            mutableStateListOf(
                                ((birthTime - nowTime) / 60 / 60 / 24 - 31),
                                ((birthTime - nowTime) - ((birthTime - nowTime) / 60 / 60 / 24) * 24 * 60 * 60) / 60 / 60,
                                ((birthTime - nowTime) - ((birthTime - nowTime) / 60 / 60) * 60 * 60) / 60//if (birthDateTime.hour - nowDateTime.hour >= 0) birthDateTime.hour - nowDateTime.hour else maxTimes[1] + (birthDateTime.hour - nowDateTime.hour),
                                //if (birthDateTime.minute - nowDateTime.minute >= 0) birthDateTime.minute - nowDateTime.minute else maxTimes[2] + (birthDateTime.minute - nowDateTime.minute)
                            )
                        }
                        Row(
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValue)
                        ) {
                            val sliderColorsOrig = listOf(
                                Color(0xFF5400cb),
                                Color(0xFF6500b9),
                                Color(0xFF76009b)
                            )
                            val huj = 2
                            for (i in 0..2) {
                                val fraction by animateFloatAsState(
                                    targetValue = if (times[0] >= 0) times[i].toFloat() / maxTimes[i] else 0f,
                                    tween(300)
                                )
                                val color by remember(
                                    sliderColorsHue[i],
                                    sliderColorsLightness[i]
                                ) {
                                    mutableStateOf(
                                        Color.hsl(
                                            sliderColorsHue[i],
                                            1f,
                                            sliderColorsLightness[i]
                                        )
                                    )
                                }
                                val textColor by remember(
                                    sliderColorsHue[i],
                                    sliderColorsLightness[i]
                                ) {
                                    mutableStateOf(
                                        Color.hsl(
                                            sliderColorsHue[i],
                                            1f,
                                            randomFun(sliderColorsLightness[i])
                                        )
                                    )
                                }
                                val haptics = LocalHapticFeedback.current
                                val draggableStateHue = rememberDraggableState(onDelta = {
                                    sliderColorsHue[i] =
                                        (sliderColorsHue[i] + it * .1f).coerceIn(0f, 360f)
                                })
                                val draggableStateLightness = rememberDraggableState(onDelta = {
                                    sliderColorsLightness[i] =
                                        (sliderColorsLightness[i] + it * .0005f).coerceIn(0f, 1f)
                                })
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(1 / (3f - i))
                                        .clickable {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        .draggable(
                                            draggableStateHue,
                                            orientation = Orientation.Vertical
                                        )
                                        .draggable(
                                            draggableStateLightness,
                                            orientation = Orientation.Horizontal
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val name = when (i) {
                                        0 -> "Days"
                                        1 -> "Hours"
                                        2 -> "Minutes"
                                        else -> "Seconds"
                                    }
                                    val namePaddingTop = 120.dp
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        color = textColor
                                    ) {
                                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                                            Text(
                                                text = if (times[0] >= 0) String.format(
                                                    "%02d",
                                                    times[i]
                                                ) else "00",
                                                color = color,
                                                fontWeight = FontWeight.W700,
                                                fontSize = 70.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.TopCenter
                                        ) {
                                            Text(
                                                name.uppercase(),
                                                modifier = Modifier
                                                    .rotate(90f)
                                                    .padding(start = namePaddingTop),
                                                style = MaterialTheme.typography.displayLarge,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Visible,
                                                color = color,
                                            )
                                        }
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            Text(
                                                name.uppercase(),
                                                modifier = Modifier
                                                    .rotate(-90f)
                                                    .padding(start = namePaddingTop),
                                                style = MaterialTheme.typography.displayLarge,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Visible,
                                                color = color,
                                            )
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(FractionProgressShape(1 - fraction)),
                                        color = (color)
                                    ) {

                                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                                            Text(
                                                text = if (times[0] >= 0) String.format(
                                                    "%02d",
                                                    times[i]
                                                ) else "00",
                                                color = textColor,
                                                fontWeight = FontWeight.W700,
                                                fontSize = 70.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.TopCenter
                                        ) {
                                            Text(
                                                name.uppercase(),
                                                modifier = Modifier
                                                    .rotate(90f)
                                                    .padding(start = namePaddingTop),
                                                style = MaterialTheme.typography.displayLarge,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Visible,
                                                color = textColor
                                            )
                                        }
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            Text(
                                                name.uppercase(),
                                                modifier = Modifier
                                                    .rotate(-90f)
                                                    .padding(start = namePaddingTop),
                                                style = MaterialTheme.typography.displayLarge,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Visible,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(FractionProgressShape(secFraction / 2))
                                .background(Color.Red.copy(alpha = 0.1f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(FractionProgressShape(secFraction / 2, true))
                                .background(Color.Blue.copy(alpha = 0.1f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(FractionProgressShapeWidth(secFraction / 2))
                                .background(Color.Cyan.copy(alpha = 0.1f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(FractionProgressShapeWidth(secFraction / 2, true))
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        val colors = listOf(
                            Color.Black,
                            Color.hsl(270f, 1f, 0.6f),
                            Color.hsl(190f, 1f, 0.6f)
                        )
                        val colorsSimilar = remember(
                            sliderColorsHue[0],
                            sliderColorsLightness[0],
                            sliderColorsHue[1],
                            sliderColorsLightness[1],
                            sliderColorsHue[2],
                            sliderColorsLightness[2],
                        ) {
                            mutableListOf(
                                colors[0] - Color.hsl(
                                    sliderColorsHue[0],
                                    1f,
                                    sliderColorsLightness[0]
                                ) <= 0.1f,
                                colors[1] - Color.hsl(
                                    sliderColorsHue[1],
                                    1f,
                                    sliderColorsLightness[1]
                                ) <= 0.1f,
                                colors[2] - Color.hsl(
                                    sliderColorsHue[2],
                                    1f,
                                    sliderColorsLightness[2]
                                ) <= 0.1f
                            )
                        }
                        val allMatch by remember(
                            colorsSimilar[0],
                            colorsSimilar[1],
                            colorsSimilar[2]
                        ) {
                            mutableStateOf(colorsSimilar[0] && colorsSimilar[1] && colorsSimilar[2])
                        }
                        val density = LocalDensity.current
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            val padding = 10.dp
                            val colorHue by remember(
                                sliderColorsHue[0],
                                sliderColorsHue[1],
                                sliderColorsHue[2]
                            ) {
                                mutableStateOf((360f - sliderColorsHue[0] + sliderColorsHue[1] + sliderColorsHue[2]) / 3)
                            }
                            val colorLightness by remember(
                                sliderColorsLightness[0],
                                sliderColorsLightness[1],
                                sliderColorsLightness[2]
                            ) {
                                mutableStateOf((sliderColorsLightness[0] + sliderColorsLightness[1] + sliderColorsLightness[2]) / 3)
                            }
                            val color by remember(colorHue, colorLightness) {
                                mutableStateOf(Color.hsl(colorHue, 1f, colorLightness))
                            }
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            )
                            {
                                AnimatedVisibility(
                                    visible = times[0] < 0 && !allMatch,
                                    enter = slideInVertically {
                                        with(density) { -30.dp.roundToPx() }
                                    } + fadeIn(tween(200)),
                                    exit = slideOutVertically {
                                        with(density) { -30.dp.roundToPx() }
                                    } + fadeOut(tween(200))
                                ) {
                                    Surface(
                                        modifier = Modifier.padding(top = 30.dp),
                                        color = color,
                                        shape = CircleShape
                                    ) {

                                        Row(
                                            modifier = Modifier.padding(padding),
                                            horizontalArrangement = Arrangement.spacedBy(padding)
                                        ) {
                                            for (i in 0..2) {
                                                val fraction by animateFloatAsState(targetValue = if (colorsSimilar[i]) 1f else 0f)
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(colors[i]),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "🔓",
                                                        fontSize = 25.sp,
                                                        modifier = Modifier.clip(
                                                            FractionProgressShape(fraction)
                                                        )
                                                    )
                                                    Text(
                                                        text = "🔒",
                                                        fontSize = 25.sp,
                                                        modifier = Modifier.clip(
                                                            FractionProgressShape(
                                                                1f - fraction,
                                                                true
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                            //Text(text = (colors[1] - Color.hsl(sliderColorsHue[1], 1f, sliderColorsLightness[1])).toString())
                                        }
                                    }
                                }
                                AnimatedVisibility(
                                    visible = times[0] < 0 && allMatch,
                                    enter = slideInVertically {
                                        with(density) { -30.dp.roundToPx() }
                                    } + fadeIn(tween(200)),
                                    exit = slideOutVertically {
                                        with(density) { -30.dp.roundToPx() }
                                    } + fadeOut(tween(200))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.7f)),
                                        contentAlignment = Alignment.Center
                                    )
                                    {
                                        Surface(tonalElevation = 2.dp, modifier = Modifier.padding(30.dp).clip(MaterialTheme.shapes.extraLarge)) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(30.dp)) {
                                                Text("Happy Birthday, babe!!!", textAlign = TextAlign.Center)
                                                Image(painter = painterResource(id = R.drawable.tartaglia), contentDescription = "tartaglia", modifier = Modifier.fillMaxWidth(0.8f))
                                                Text("Tartaglia turned into an animal and now he is going from Snezhnaya directly to you...", textAlign = TextAlign.Center)
                                                Text("Interesting, what could this possibly mean?", textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun randomFun(t: Float): Float {
        return ((2 - t - floor(floor(2 * t))) / 2).coerceIn(0f, 1f)
    }
}

private operator fun Color.minus(color: Color): Float {
    var similarity: Float = Math.abs(this.red - color.red)
    similarity += Math.abs(this.green - color.green)
    similarity += Math.abs(this.blue - color.blue)
    return similarity
}

private operator fun Color.div(i: Int): Color {
    return Color(this.red / i, this.green / i, this.blue / i)
}

class FractionProgressShape(private val fraction: Float, private val fromTop: Boolean = false) :
    Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val rect = Rect(
            Offset(x = 0f, y = if (!fromTop) size.height * (1f - fraction) else 0f),
            Size(width = size.width, height = size.height * fraction)
        )
        return Outline.Rectangle(rect)
    }

}

class FractionProgressShapeWidth(
    private val fraction: Float,
    private val fromRight: Boolean = false
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val rect = Rect(
            Offset(x = if (fromRight) size.width * (1f - fraction) else 0f, y = 0f),
            Size(width = size.width * fraction, height = size.height)
        )
        return Outline.Rectangle(rect)
    }

}