package com.example.mlingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import com.example.mlingo.ui.theme.MLingoTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.text.TextStyle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import kotlin.math.roundToInt
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.border
import android.content.Context
//import androidx.compose.foundation.layout.FlowRowScopeInstance.align
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import androidx.compose.ui.platform.LocalContext
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
// Для Material3
import androidx.compose.material3.ButtonDefaults

// Для работы с композициями и модификаторами
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import org.json.JSONObject
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.content.Intent
import android.os.Handler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import kotlinx.coroutines.delay
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mlingo.ui.theme.MLingoTheme



fun loadQuestionsFromJson(context: Context): List<Question> {
    val inputStream = context.resources.openRawResource(R.raw.questions) // указываем путь к JSON
    val reader = InputStreamReader(inputStream)
    val type = object : TypeToken<List<Question>>() {}.type
    val questions: List<Question> = Gson().fromJson<List<Question>?>(reader, type).shuffled()

    // Перемешиваем options для каждого вопроса
    questions.forEach { question ->
        // Преобразуем options в MutableList, чтобы можно было использовать shuffle
        val mutableOptions = question.options.toMutableList()
        val correctAnswer = mutableOptions[question.correctAnswerIndex] // Сохраняем правильный ответ
        mutableOptions.shuffle()  // Перемешиваем варианты ответов

        // Обновляем options с перемешанными вариантами
        question.options = mutableOptions

        // Обновляем правильный индекс ответа после перемешивания
        question.correctAnswerIndex = mutableOptions.indexOf(correctAnswer)
    }

    return questions
}



val Nothing = FontFamily(
    Font(R.font.nothing)
)

val Anon = FontFamily(
    Font(R.font.anon)
)

data class Question(
    var question: String,
    var options: List<String>,
    var correctAnswerIndex: Int,
    var explanation: String
)


@Composable
fun SplashScreen(navController: NavController) {
    var text by remember { mutableStateOf("") }
    val fullText = "Created by xm1k"

    // Анимация печати текста
    LaunchedEffect(Unit) {
        for (i in fullText.indices) {
            delay(70) // Задержка между буквами
            text += fullText[i]
        }
        delay(1500)  // Задержка после завершения печати
        navController.navigate("menu_screen")  // Переход к экрану меню
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)), // Цвет фона
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontFamily = Nothing)
        )
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(navController) // Экран заставки
        }
        composable("menu_screen") {
            MenuScreen() // Экран меню
        }
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        val appBackgroundColor = Color(0xFF121212).toArgb()
        window.statusBarColor = appBackgroundColor
        setContent {
            MyApp()
        }
    }
}





@Composable
fun MenuScreen() {
    val context = LocalContext.current
    // Состояние для перехода между экранами
    var isQuizStarted by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(16) } // Выбранный вариант, по умолчанию 16

    // Состояния для статистики
    val statsState = remember { mutableStateOf<Map<String, Any>?>(null) }
    val codeText = remember { mutableStateOf("") } // Текст для анимации печати

    // Загрузка статистики
    LaunchedEffect(Unit) {
        delay(150)
        val stats = loadStatsFromJson(context) // Загружаем данные
        statsState.value = stats // Сохраняем в состояние
    }

    LaunchedEffect(statsState.value) {
        // Если статистика изменилась, обновляем экран
        statsState.value?.let { stats ->
            Log.d("Updated Stats", "Stats updated: $stats")
        }
    }

    // Зацикленная анимация текста
    LaunchedEffect(Unit) {
        val codeLines = listOf(
            "Initializing neural network with batch size of ${selectedOption}...",
            "Training model with ${statsState.value?.get("totalTrainings") ?: 0} total epochs...",
            "Epoch 1/50: Loss - ${statsState.value?.get("averageLoss") ?: 0.4}, Accuracy - ${statsState.value?.get("lastTrainingAccuracy") ?: 0.75}...",
            "Adjusting learning rate... Loss decreasing steadily.",
            "Epoch 5/50: Current loss: ${statsState.value?.get("averageLoss") ?: 0.3}...",
            "Training progress: 20%. Accuracy improving...",
            "Neural network making predictions with accuracy: ${statsState.value?.get("lastTrainingAccuracy") ?: 0.80}%",
            "Processing data for the next epoch... Time spent: ${statsState.value?.get("timeSpent") ?: "N/A"}",
            "Compiling training data... Please wait while we debug the future.",
            "TensorFlow says 'Hello World'... But we prefer 'Hello Accuracy'.",
            "Epoch 10/50: The loss is down. The model is learning. Or is it?",
            "Training at the speed of light. Please do not stare directly at the process.",
            "Neural network debugging... Accuracy increasing by 0.01%. The future is bright!",
            "Warning: The model might be smarter than the programmer.",
            "Epoch 25/50: Training slowing down, but hey, we’re not in a rush, right?",
            "Adjusting hyperparameters... Why not try 'turn it off and on again'?",
            "Neural network on a coffee break... Accuracy still climbing.",
            "Epoch 35/50: Neural network predicting outcomes with an accuracy of ${statsState.value?.get("lastTrainingAccuracy") ?: 0.85}%. Almost there.",
            "Exploding gradients? We’ll handle it. Model trained with more than enough layers.",
            "Epoch 40/50: We've reached the point of diminishing returns. Let's pretend it's still improving.",
            "Model is almost done... Think of all the training data we've sacrificed.",
            "Building a smarter neural network, one epoch at a time. Are we ready for the singularity?",
            "Accuracy improving... Slowly but surely... Like a good code review.",
            "Epoch 50/50: Final epoch reached! Now we wait for the model to thank us for all our hard work.",
            "Machine learning is like cooking... Throw some data in the pot and hope it comes out delicious."
        )

        var currentLine = 0
        while (true) {
            codeText.value = "" // Сбросим текст перед новым циклом
            codeLines[currentLine].forEachIndexed { index, char ->
                delay(100) // Интервал между буквами
                codeText.value += char
            }
            delay(1000) // Пауза после завершения строки
            currentLine = (currentLine + 1) % codeLines.size // Переход к следующей строке
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Фон как в QuizScreen
    ) {
        if (!isQuizStarted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally, // Выровняем весь текст к началу
                verticalArrangement = Arrangement.Top // Переместим текст наверх
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF3EB489))) { // Зеленый цвет для первых двух букв
                            append("ML")
                        }
                        withStyle(style = SpanStyle(color = Color.White)) { // Белый цвет для остальных букв
                            append("ingo")
                        }
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFamily = Nothing)
                )


                Spacer(modifier = Modifier.height(24.dp))

                // Добавляем надпись над кнопками
                Text(
                    text = "Batch size: ${selectedOption}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    style = TextStyle(fontFamily = Nothing)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки с вариантами
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround // Сдвинуть кнопки
                ) {
                    listOf(8, 16, 32).forEach { option ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp) // Квадратная форма кнопки
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedOption == option) Color.White else Color.Gray,
                                        shape = RoundedCornerShape(0.dp) // Прямоугольная форма
                                    )
                                    .clickable { selectedOption = option },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.toString(),
                                    color = if (selectedOption == option) Color.White else Color.Gray,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(fontFamily = Nothing)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val padding = 25.dp
// Отображение статистики
                statsState.value?.let { stats ->
                    Text(
                        text = "Total epochs: ${stats["totalTrainings"]}",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = padding).align(Alignment.Start), // Добавляем отступ
                        style = TextStyle(fontFamily = Nothing)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Average Loss: ${stats["averageLoss"]}",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = padding).align(Alignment.Start), // Добавляем отступ
                        style = TextStyle(fontFamily = Nothing)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last Accuracy: ${stats["lastTrainingAccuracy"]}",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = padding).align(Alignment.Start), // Добавляем отступ
                        style = TextStyle(fontFamily = Nothing)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Time Spent: ${stats["timeSpent"]}",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = padding).align(Alignment.Start), // Добавляем отступ
                        style = TextStyle(fontFamily = Nothing)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Печать строки кода
                Text(
                    text = codeText.value,
                    color = Color.Green,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Button(
                onClick = {
                    isQuizStarted = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .height(100.dp)
                    .fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF121212)
                ),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = "train",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    style = TextStyle(fontFamily = Nothing)
                )
            }
        } else {
            // Переход к экрану викторины, передаем количество вопросов
            QuizScreen(questionCount = selectedOption)
        }
    }
}


suspend fun loadStatsFromJson(context: Context): Map<String, Any> = withContext(Dispatchers.IO) {
    val file = File(context.filesDir, "stats.json")

    if (!file.exists()) {
        // Если файла нет, создаем его с нулевыми значениями
        val initialStats = JSONObject().apply {
            put("total_trainings", 0)
            put("average_loss", 0f)
            put("last_training_accuracy", 0f)
            put("time_spent", 0f)
        }
        context.openFileOutput("stats.json", Context.MODE_PRIVATE).use {
            it.write(initialStats.toString().toByteArray())
        }
        return@withContext mapOf(
            "totalTrainings" to 0,
            "averageLoss" to 0f,
            "lastTrainingAccuracy" to 0f,
            "timeSpent" to 0f
        )
    }

    val fileInputStream = context.openFileInput("stats.json")
    val jsonString = fileInputStream.bufferedReader().use { it.readText() }
    val jsonObject = JSONObject(jsonString)

    mapOf(
        "totalTrainings" to jsonObject.getInt("total_trainings"),
        "averageLoss" to jsonObject.getDouble("average_loss").toFloat(),
        "lastTrainingAccuracy" to jsonObject.getDouble("last_training_accuracy").toFloat(),
        "timeSpent" to jsonObject.getDouble("time_spent").toFloat()
    )
}


suspend fun saveToJson(
    context: Context,
    currentLoss: Float,
    accuracy: Float,
    elapsedTimeSeconds: Long
) = withContext(Dispatchers.IO) {
    // Загружаем старые метрики
    val stats = loadStatsFromJson(context)
    val acc = 1-accuracy
    // Получаем старые значения
    val totalTrainings = stats["totalTrainings"] as Int
    val averageLoss = stats["averageLoss"] as Float
    val lastTrainingAccuracy = stats["lastTrainingAccuracy"] as Float
    val timeSpent = stats["timeSpent"] as Float

    // Обновляем метрики
    val newTotalTrainings = totalTrainings + 1
    val newAverageLoss = (averageLoss * totalTrainings + (1 - acc)) / newTotalTrainings
    val newLastTrainingAccuracy = acc
    val newTimeSpent = timeSpent + elapsedTimeSeconds

    // Создаем новый JSON-объект с обновленными метриками
    val updatedStats = JSONObject().apply {
        put("total_trainings", newTotalTrainings)
        put("average_loss", newAverageLoss)
        put("last_training_accuracy", newLastTrainingAccuracy)
        put("time_spent", newTimeSpent) // время в секундах
    }
//    val updatedStats = JSONObject().apply {
//        put("total_trainings", 0)
//        put("average_loss", 0.0f)
//        put("last_training_accuracy", 0.0f)
//        put("time_spent", 0.0f) // время в секундах
//    }

    // Сохраняем обновленный JSON в файл
    context.openFileOutput("stats.json", Context.MODE_PRIVATE).use { output ->
        output.write(updatedStats.toString().toByteArray())
        Log.d("SaveToJson", "Stats saved successfully.")
    }
}

@Composable
fun QuizScreen(questionCount: Int) {
    val context = LocalContext.current

    // Загрузка только необходимого количества вопросов
    var questions by remember { mutableStateOf(loadQuestionsFromJson(context).take(questionCount)) }
    var currentQuestionIndex by remember { mutableStateOf(0) } // Индекс текущего вопроса
    var answeredQuestionsCount by remember { mutableStateOf(0) } // Количество вопросов, на которые ответили
    var correctAnswersCount by remember { mutableStateOf(0) } // Количество правильных ответов
    var selectedAnswer by remember { mutableStateOf<Int?>(null) } // Выбранный ответ
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) } // Правильность ответа
    var explanation by remember { mutableStateOf("") } // Объяснение для неправильного ответа
    var isAnswerSelected by remember { mutableStateOf(false) } // Флаг выбора ответа
    var isQuizFinished by remember { mutableStateOf(false) } // Завершение викторины

    val totalQuestions = questions.size
    val currentQuestion = questions[currentQuestionIndex]

    // Время начала викторины
    val startTime = remember { System.currentTimeMillis() }

    // Плавная анимация для прогресса
    val progress by animateFloatAsState(
        targetValue = (answeredQuestionsCount.toFloat() / totalQuestions.toFloat()) * 100,
        animationSpec = tween(durationMillis = 1000)
    )

    // Плавная анимация для точности
    val accuracy by animateFloatAsState(
        targetValue = if (answeredQuestionsCount > 0) {
            (correctAnswersCount.toFloat() / answeredQuestionsCount) * 100
        } else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    // Loss (условно: 1 - accuracy)
    val loss by animateFloatAsState(
        targetValue = if (answeredQuestionsCount > 0) {
            1 - (correctAnswersCount.toFloat() / answeredQuestionsCount)
        } else 1f,
        animationSpec = tween(durationMillis = 1000)
    )

    // Precision и Recall
    val precision by animateFloatAsState(
        targetValue = if (answeredQuestionsCount > 0) {
            correctAnswersCount.toFloat() / answeredQuestionsCount
        } else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    val recall by animateFloatAsState(
        targetValue = if (totalQuestions > 0) {
            correctAnswersCount.toFloat() / totalQuestions
        } else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    // F1-Score
    val f1Score by animateFloatAsState(
        targetValue = if (precision + recall > 0) {
            2 * (precision * recall) / (precision + recall)
        } else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    // Функция для вибрации
    fun vibrateOnAnswer(isCorrect: Boolean) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val vibrationEffect = if (isCorrect) {
                VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE) // Короткая вибрация для правильного ответа
            } else {
                VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE) // Длительная вибрация для неправильного ответа
            }
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(100) // Короткая вибрация для старых версий
        }
    }

    // Проверка ответа
    fun checkAnswer(answerIndex: Int) {
        if (!isAnswerSelected) {
            selectedAnswer = answerIndex
            val isCorrect = answerIndex == currentQuestion.correctAnswerIndex
            isAnswerCorrect = isCorrect
            explanation = if (isCorrect) "" else currentQuestion.explanation
            isAnswerSelected = true

            // Вибрация
            vibrateOnAnswer(isCorrect)

            // Увеличиваем прогресс
            answeredQuestionsCount += 1
            if (isCorrect) {
                correctAnswersCount += 1
            }
        }
    }

    // Переход к следующему вопросу
    fun nextQuestion() {
        if (currentQuestionIndex < totalQuestions - 1) {
            currentQuestionIndex += 1
            selectedAnswer = null
            isAnswerCorrect = null
            explanation = ""
            isAnswerSelected = false
        } else {
            isQuizFinished = true // Завершаем викторину
        }
    }

    // LaunchedEffect для сохранения метрик после завершения викторины
    LaunchedEffect(isQuizFinished) {
        if (isQuizFinished) {
            val endTime = System.currentTimeMillis()
            val elapsedTime = (endTime - startTime) / 1000 // Время в секундах

            val calculatedAccuracy = if (answeredQuestionsCount > 0) {
                (correctAnswersCount.toFloat() / answeredQuestionsCount) * 100
            } else 0f
            val calculatedLoss = if (answeredQuestionsCount > 0) {
                1 - (correctAnswersCount.toFloat() / answeredQuestionsCount)
            } else 1f

            // Сохранение метрик
            saveToJson(context, calculatedAccuracy, calculatedLoss, elapsedTime)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Если викторина завершена, показываем финальный результат
        if (isQuizFinished) {
            MenuScreen()
        } else {
            // Прогресс-бар в верхней части экрана
            Column {
                // Прогресс-бар
                LinearProgressBar(
                    totalQuestions = totalQuestions,
                    currentQuestionIndex = answeredQuestionsCount // Обновляем прогресс
                )

                // Точность и метрики
                MLStatsDisplay(
                    accuracy = accuracy,
                    loss = loss,
                    recall = recall,
                    f1Score = f1Score
                )
            }

            // Вопрос и варианты ответов
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(bottom = 256.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TypingText(fullText = currentQuestion.question)
            }

            // Панель результатов при выборе ответа
            if (selectedAnswer != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .fillMaxHeight(0.20f)
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

            // Кнопки выбора ответа
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val panelHeight = screenHeight * 0.20f

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = panelHeight, start = 16.dp, end = 16.dp)
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
                                    .height(150.dp)
                                    .weight(1f)
                                    .padding(bottom = 16.dp)
                                    .border(2.dp, when {
                                        selectedAnswer == currentQuestion.options.indexOf(option) && isAnswerCorrect == true -> Color(0xFF3EB489)
                                        selectedAnswer == currentQuestion.options.indexOf(option) && isAnswerCorrect == false -> Color(0xFFFF4C4F)
                                        else -> Color.White
                                    }),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF121212)
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
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
}



@Composable
fun MLStatsDisplay(
    accuracy: Float,
    loss: Float,
    recall: Float,
    f1Score: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Accuracy: ${accuracy.roundToInt()}%",
                color = Color.White,
                fontSize = 16.sp,
                style = TextStyle(fontFamily = Nothing)
            )
            Text(
                text = "Loss: ${"%.2f".format(loss)}",
                color = Color.White,
                fontSize = 16.sp,
                style = TextStyle(fontFamily = Nothing)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Recall: ${"%.2f".format(recall * 100)}%",
                color = Color.White,
                fontSize = 16.sp,
                style = TextStyle(fontFamily = Nothing)
            )
            Text(
                text = "F1 Score: ${"%.2f".format(f1Score * 100)}%",
                color = Color.White,
                fontSize = 16.sp,
                style = TextStyle(fontFamily = Nothing)
            )
        }
    }
}

@Composable
fun LinearProgressBar(totalQuestions: Int, currentQuestionIndex: Int) {
    // Рассчитываем процент пройденных вопросов
    val progress = (currentQuestionIndex.toFloat() / totalQuestions.toFloat()) * 100
    val filledSquares = (progress / 5).toInt() // Каждый квадрат - 5%

    // Плавное изменение прогресса с анимацией
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000) // Плавное изменение за 1 секунду
    )

    val filledAnimatedSquares = (animatedProgress / 5).toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth() // Гарантируем, что Box занимает всю ширину
            .height(40.dp)
            .padding(8.dp, top = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.Center) // Центрирование Row по горизонтали
        ) {
            repeat(20) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (index < filledAnimatedSquares) Color(0xFF3EB489)
                            else Color.Gray
                        )
                )
            }
        }

        // Текст с процентом пройденного прогресса, сдвигаем его влево
        Text(
            text = "${animatedProgress.toInt()}%",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart) // Центрирование текста слева
                .padding(start = 16.dp),
            fontSize = 16.sp,
            style = TextStyle(fontFamily = Nothing)
        )
    }
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
                                onSwipeNext() // Переход к следующему вопросу
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
                        color = if (isAnswerCorrect) Color(0xFF3EB489).copy(alpha = (1f - alpha).coerceIn(0f, 1f)) else Color(0xFFFF4C4F).copy(alpha = (1f - alpha).coerceIn(0f, 1f)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        style = TextStyle(fontFamily = Nothing)
                    )
                    if (!isAnswerCorrect) {
                        Text(
                            text = explanation,
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontFamily = Anon)
                        )
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MLingoTheme {
        MenuScreen()
    }
}