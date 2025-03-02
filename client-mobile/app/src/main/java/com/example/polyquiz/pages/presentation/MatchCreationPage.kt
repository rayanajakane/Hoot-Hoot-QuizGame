package com.example.polyquiz

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.AuthFeedbackText
import com.example.polyquiz.constants.DisplayAuthenticationText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

@Composable
fun MatchCreationPage(modifier: Modifier, navigateToLogin: () -> Unit, navigateToHome: () -> Unit, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val questionService = QuestionService()

    fun getQuestionById(questionId: String) {
        questionService.getQuestionById(questionId,
            onSuccess = { question ->
                println("Question: $question")
                val result = questionService.convertToGenericType<Question>(question)
                println("Result: $result")
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun validateQuestion() {
        val question = Question(id="", type="QCM", text="ça marche?", points=80.0, choices= listOf(Choice(text="yooo", isCorrect=true), Choice(text="ff", isCorrect=false), Choice(text="yosdfsoo", isCorrect=false), Choice(text="yoofsdfsdfo", isCorrect=false)), lastModification="")
        questionService.verifyQuestion(question,
            onSuccess = { response ->
                println("Response: $response")
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun deleteQuestion(questionId: String) {
        questionService.deleteQuestion(questionId,
            onSuccess = { response ->
                println("Response: $response")
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun fetchQuestions() {
        questionService.getAllQuestions(
            onSuccess = { questions ->
                println("Questions: $questions")
                val result = questionService.convertToGenericType<List<Question>>(questions)
                result?.forEach { question ->
                    println("Question: ${question}")
                }
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun createQuestion() {
        val question = Question(id="", type="QCM", text="ça marche?", points=80.0, choices= listOf(Choice(text="yooo", isCorrect=true), Choice(text="ff", isCorrect=false), Choice(text="yosdfsoo", isCorrect=false), Choice(text="yoofsdfsdfo", isCorrect=false)), lastModification="")
        questionService.createQuestion(question,
            onSuccess = { response ->
                val result = questionService.convertToGenericType<Question>(response)
                println("Response: $result")
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> {
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = AuthFeedbackText.SIGN_OUT.value,
                        )
                    )
                }
                navigateToLogin()
            }
            is AuthState.Error -> {
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = (authState.value as AuthState.Error).message,
                        )
                    )
                }
            }
            else -> Unit
        }
    }

    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ){
        ChatComponent(modifier = modifier, authViewModel = authViewModel)
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Button(
                onClick = {
                    getQuestionById("b7a17fe8-dd3a-4751-a58d-b0b9d92a0173")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Obtenir la question")
            }
            Button(
                onClick = {
                    validateQuestion()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Valider question")
            }
            Button(
                onClick = {
                    deleteQuestion("1c0fed78-4a04-4d96-a0d6-b486aa31fb7d")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Détruire question")
            }
            Button(
                onClick = {
                    fetchQuestions()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Jouer")
            }
            Button(
                onClick = {
                    createQuestion()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Créer question")
            }
            Surface(
                shadowElevation = 10.dp,
                tonalElevation = 10.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
                modifier = Modifier.padding(10.dp)
            ){
                Button(
                    onClick = {
                        navigateToHome()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        contentColor = MaterialTheme.colorScheme.onSurface),
                ) {
                    Text(text = "Retourner à la page d'accueil")
                }
            }
        }
        ElevatedButton(
            onClick = {
                authViewModel.signOut()
            },
            modifier = Modifier.padding(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright,
                contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text(text = DisplayAuthenticationText.LOGOUT.value)
        }
    }
}
