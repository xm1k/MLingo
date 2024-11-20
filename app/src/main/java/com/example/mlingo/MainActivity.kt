package com.example.mlingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mlingo.ui.theme.MLingoTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.text.TextStyle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import kotlin.math.roundToInt
import androidx.compose.ui.unit.IntOffset
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.border
import com.example.mlingo.ui.theme.MLingoTheme

val Nothing = FontFamily(
    Font(R.font.nothing)
)

val Anon = FontFamily(
    Font(R.font.anon)
)

data class Question(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appBackgroundColor = Color(0xFF121212).toArgb()
        window.statusBarColor = appBackgroundColor

        setContent {
            MLingoTheme {
                QuizScreen()
            }
        }
    }
}

@Composable
fun QuizScreen() {
    val questions = listOf(
        Question(
            "Что такое машинное обучение?",
            listOf("Метод программирования", "Алгоритмы, которые учатся на данных", "Искусственный интеллект без данных", "Программирование роботов"),
            correctAnswerIndex = 1,
            explanation = "Машинное обучение — это создание моделей, которые учатся на данных для выполнения задач."
        ),
        Question(
            "Как называется библиотека для работы с нейронными сетями?",
            listOf("PyTorch", "TensorFlow", "Keras", "Все вышеупомянутые"),
            correctAnswerIndex = 3,
            explanation = "Все перечисленные библиотеки используются для работы с нейронными сетями."
        )
    )

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var explanation by remember { mutableStateOf("") }
    var isAnswerSelected by remember { mutableStateOf(false) }

    val currentQuestion = questions[currentQuestionIndex]

    fun checkAnswer(answerIndex: Int) {
        if (!isAnswerSelected) {
            selectedAnswer = answerIndex
            isAnswerCorrect = answerIndex == currentQuestion.correctAnswerIndex
            explanation = if (isAnswerCorrect == true) "" else currentQuestion.explanation
            isAnswerSelected = true
        }
    }

    fun nextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size
        selectedAnswer = null
        isAnswerCorrect = null
        explanation = ""
        isAnswerSelected = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(bottom = 128.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TypingText(fullText = currentQuestion.question)
        }

        if (selectedAnswer != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.2f)
                    .padding(0.dp)
                    .background(Color(0xFF212121))
            ) {
                ResultPanel(
                    isAnswerCorrect = isAnswerCorrect,
                    explanation = explanation,
                    onSwipeNext = { nextQuestion() }
                )
            }
        }

        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val panelHeight = screenHeight * 0.2f

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = panelHeight + 32.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            currentQuestion.options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowOptions.forEach { option ->
                        Button(
                            onClick = {
                                if (!isAnswerSelected) {
                                    checkAnswer(currentQuestion.options.indexOf(option))
                                }
                            },
                            modifier = Modifier
                                .height(100.dp)
                                .weight(1f)
                                .padding(bottom = 16.dp)
                                .border(2.dp, when {
                                    selectedAnswer == currentQuestion.options.indexOf(option) && isAnswerCorrect == true -> Color.Green
                                    selectedAnswer == currentQuestion.options.indexOf(option) && isAnswerCorrect == false -> Color.Red
                                    else -> Color.White
                                }),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF121212)
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                        ) {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                style = TextStyle(fontFamily = Anon)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingText(
    fullText: String,
    typingSpeed: Long = 35L
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(fullText) {
        for (index in fullText.indices) {
            currentIndex = index
            displayedText = fullText.substring(0, currentIndex + 1)
            kotlinx.coroutines.delay(typingSpeed)
        }
    }

    Text(
        text = displayedText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center,
        style = TextStyle(fontFamily = Nothing)
    )
}

@Composable
fun ResultPanel(
    isAnswerCorrect: Boolean?,
    explanation: String,
    onSwipeNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var dragStartPosition by remember { mutableStateOf(0f) }
    var isPanelVisible by remember { mutableStateOf(false) }
    var alpha by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        isPanelVisible = true
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    alpha = remember(offsetX) {
        val maxSwipeDistance = screenWidth.value
        (-offsetX / maxSwipeDistance).coerceIn(-1f, 1f)
    }

    AnimatedVisibility(
        visible = isPanelVisible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(initialAlpha = 0f),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .background(Color(0xFF212121))
                .padding(16.dp)
                .offset { IntOffset(offsetX.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { startPoint ->
                            dragStartPosition = startPoint.x
                        },
                        onDragEnd = {
                            if (offsetX < -screenWidth.value * 0.25f) {
                                offsetX = 0f
                                onSwipeNext()
                            } else {
                                offsetX = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX += dragAmount
                        }
                    )
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                if (isAnswerCorrect != null) {
                    Text(
                        text = if (isAnswerCorrect) "Правильно!" else "Неправильно!",
                        color = if (isAnswerCorrect) Color.Green.copy(alpha = (1f - alpha).coerceIn(0f, 1f)) else Color.Red.copy(alpha = (1f - alpha).coerceIn(0f, 1f)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        style = TextStyle(fontFamily = Anon)
                    )
                    if (!isAnswerCorrect) {
                        Text(
                            text = explanation,
                            color = Color.White,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontFamily = Anon)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MLingoTheme {
        QuizScreen()
    }
}
