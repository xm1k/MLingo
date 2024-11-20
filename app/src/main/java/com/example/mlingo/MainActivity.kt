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

        // Устанавливаем цвет статус-бара такой же, как у приложения
        val appBackgroundColor = Color(0xFF121212).toArgb() // Преобразуем Compose цвет в ARGB
        window.statusBarColor = appBackgroundColor // Устанавливаем цвет

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
    var isAnswerSelected by remember { mutableStateOf(false) } // Флаг для блокировки кнопок

    val currentQuestion = questions[currentQuestionIndex]

    // Функция для обработки выбранного ответа
    fun checkAnswer(answerIndex: Int) {
        if (!isAnswerSelected) { // Если ответ еще не выбран
            selectedAnswer = answerIndex
            isAnswerCorrect = answerIndex == currentQuestion.correctAnswerIndex
            explanation = if (isAnswerCorrect == true) "" else currentQuestion.explanation
            isAnswerSelected = true // Устанавливаем флаг, чтобы блокировать кнопки
        }
    }

    // Функция для перехода к следующему вопросу
    fun nextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size
        selectedAnswer = null
        isAnswerCorrect = null
        explanation = ""
        isAnswerSelected = false // Сбрасываем флаг для следующего вопроса
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Темный фон для всего приложения
    ) {
        // Размещаем вопрос в верхней части
        Column(
            modifier = Modifier
                .align(Alignment.Center) // Выравниваем по центру
                .fillMaxWidth() // Занимаем всю ширину
                .padding(bottom = 128.dp, start = 16.dp, end = 16.dp), // Указываем padding по бокам
            horizontalAlignment = Alignment.CenterHorizontally // Центрируем содержимое по горизонтали
        ) {
            TypingText(fullText = currentQuestion.question) // Печатаем текст вопроса посимвольно
        }

        // Панель с результатом внизу
        if (selectedAnswer != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter) // Панель с результатом всегда снизу
                    .fillMaxHeight(0.2f) // Растягиваем панель на 20% от экрана
                    .padding(0.dp) // Убираем внутренние отступы
                    .background(Color(0xFF212121)) // Темный фон панели
            ) {
                ResultPanel(
                    isAnswerCorrect = isAnswerCorrect,
                    explanation = explanation,
                    onSwipeNext = { nextQuestion() }
                )
            }
        }

        // Размещение кнопок в центре
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val panelHeight = screenHeight * 0.2f // Высота панели, 20% от высоты экрана

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = panelHeight + 32.dp, start = 16.dp, end = 16.dp) // Используем вычисленную высоту панели
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ваши кнопки
            currentQuestion.options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Горизонтальные отступы между кнопками
                ) {
                    rowOptions.forEach { option ->
                        Button(
                            onClick = {
                                if (!isAnswerSelected) { // Если ответ еще не выбран
                                    checkAnswer(currentQuestion.options.indexOf(option))
                                }
                            },
                            modifier = Modifier
                                .height(100.dp) // Сохраняем высоту кнопки
                                .weight(1f) // Кнопки равной ширины
                                .padding(bottom = 16.dp) // Одинаковые отступы сверху, снизу, слева и справа
                                .border(2.dp, when {
                                    selectedAnswer == currentQuestion.options.indexOf(option) && isAnswerCorrect == true -> Color.Green // Правильный ответ
                                    selectedAnswer == currentQuestion.options.indexOf(option) && isAnswerCorrect == false -> Color.Red  // Неправильный ответ
                                    else -> Color.White // Белая рамка по умолчанию
                                }), // Цвет рамки в зависимости от ответа
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF121212) // Темный фон кнопки
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp) // Убираем скругления
                        ) {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White, // Белый текст
                                style = TextStyle(fontFamily = Anon) // Шрифт Anon
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
    typingSpeed: Long = 35L // Скорость печати в миллисекундах
) {
    var displayedText by remember { mutableStateOf("") } // Отображаемый текст
    var currentIndex by remember { mutableStateOf(0) } // Текущий индекс символа

    // Анимация добавления символов
    LaunchedEffect(fullText) {
        for (index in fullText.indices) {
            currentIndex = index
            displayedText = fullText.substring(0, currentIndex + 1) // Обновляем отображаемый текст
            kotlinx.coroutines.delay(typingSpeed) // Ждем перед добавлением следующего символа
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
    var offsetX by remember { mutableStateOf(0f) } // Смещение панели
    var dragStartPosition by remember { mutableStateOf(0f) } // Начальная точка свайпа
    var isPanelVisible by remember { mutableStateOf(false) } // Видимость панели
    var alpha by remember { mutableStateOf(1f) } // Прозрачность

    // Показываем панель с анимацией
    LaunchedEffect(Unit) {
        isPanelVisible = true
    }

    // Получаем ширину экрана в dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Расчет прозрачности в зависимости от смещения
    alpha = remember(offsetX) {
        // Переводим смещение в отношение от ширины экрана и ограничиваем от 0 до 1
        val maxSwipeDistance = screenWidth.value // ширина экрана в dp
        (-offsetX / maxSwipeDistance).coerceIn(-1f, 1f) // Прозрачность увеличивается по мере свайпа влево
    }

    AnimatedVisibility(
        visible = isPanelVisible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(initialAlpha = 0f),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .background(Color(0xFF212121)) // Темный фон панели
                .padding(16.dp)
                .offset { IntOffset(offsetX.toInt(), 0) } // Смещение по X
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { startPoint ->
                            dragStartPosition = startPoint.x // Сохраняем начальную точку
                        },
                        onDragEnd = {
                            // Проверяем смещение после окончания жеста
                            if (offsetX < -screenWidth.value * 0.25f) {
                                // Если свайп больше порога, переходим к следующему вопросу
                                offsetX = 0f // Сбрасываем смещение панели
                                onSwipeNext()
                            } else {
                                // Если свайп меньше порога, возвращаем панель назад
                                offsetX = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            // Обновляем смещение панели
                            offsetX += dragAmount
                        }
                    )
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(), // Возвращаем предыдущее расположение
                verticalArrangement = Arrangement.Center // Центрируем содержимое
            ) {
                // Текст результата с изменяющейся прозрачностью
                if (isAnswerCorrect != null) {
                    Text(
                        text = if (isAnswerCorrect) "Правильно!" else "Неправильно!",
                        color = if (isAnswerCorrect) Color.Green.copy(alpha = (1f - alpha).coerceIn(0f, 1f)) else Color.Red.copy(alpha = (1f - alpha).coerceIn(0f, 1f)),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(fontFamily = Nothing),
                        textAlign = TextAlign.Center
                    )
                }

                // Пояснение с изменяющейся прозрачностью
                if (isAnswerCorrect == false && explanation.isNotEmpty()) {
                    Text(
                        text = explanation,
                        color = Color.White.copy(alpha = (1f - alpha).coerceIn(0f, 1f)), // Прозрачность меняется по мере свайпа
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(top = 8.dp),
                        style = TextStyle(fontFamily = Anon),
                        textAlign = TextAlign.Center
                    )
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
