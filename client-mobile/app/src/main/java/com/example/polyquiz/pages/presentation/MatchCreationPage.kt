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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.AuthState
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.http.QuestionService
import com.example.polyquiz.match.domain.Choice
import com.example.polyquiz.match.domain.Question
import kotlinx.coroutines.launch


@Composable
fun MatchCreationPage(modifier: Modifier, navigateToLogin: () -> Unit, navigateToHome: () -> Unit, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun updateQuestion() {
        QuestionService.updateQuestion(
            modifiedQuestion = Question(id="c7f6e692-d8f7-4c07-a45a-b33659e4ad68", type="QCM", text="noo mais est-ce que la vie???", points=80, choices= listOf(
                Choice(text="yooo", isCorrect=true), Choice(text="ff", isCorrect=false), Choice(text="yosdfsoo", isCorrect=false), Choice(text="yoofsdfsdfo", isCorrect=false)), lastModification="", photoUrl="", creatorName=""),
            onSuccess = {
                println("It worked")
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun verifyQuestion() {
        QuestionService.verifyQuestion(
            question = Question(id="", type="QCM", text="noo mais est-ce que la vie?", points=80, choices= listOf(Choice(text="yooo", isCorrect=true), Choice(text="ff", isCorrect=false), Choice(text="yosdfsoo", isCorrect=false), Choice(text="yoofsdfsdfo", isCorrect=false)), lastModification="", photoUrl="", creatorName=""),
                onSuccess = { response ->
                println("Response: $response")
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun deleteQuestion() {
        QuestionService.deleteQuestion(
            questionId = "322c6c04-76fe-49b6-8fc3-171dc4e7fb5f",
            onSuccess = {
                println("It worked")
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun addQuestion() {
        val question = Question(id="", type="QCM", text="noo mais est-ce que la vie?", points=80, choices= listOf(Choice(text="yooo", isCorrect=true), Choice(text="ff", isCorrect=false), Choice(text="yosdfsoo", isCorrect=false), Choice(text="yoofsdfsdfo", isCorrect=false)), lastModification="", photoUrl="", creatorName="")
        QuestionService.createQuestion(question,
            onSuccess = { response ->
                println("Response: $response")
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
    }

    fun fetchQuestion() {
        QuestionService.getAllQuestions(
            onSuccess = { questions ->
                questions.forEach { question ->
                    println("Question: ${question.text}")
                }
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
        /*questionService.getQuestionById(
            questionId = "15570586-86be-4a7d-9d92-d99b7b716760",
            onSuccess = { question ->
                println("Question: ${question.text}")
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )*/
    }

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> {
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.StringResource(R.string.sign_out_feedback)
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
                    updateQuestion()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Modifier une question")
            }
            Button(
                onClick = {
                    verifyQuestion()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Vérifier une question")
            }
            Button(
                onClick = {
                    deleteQuestion()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Supprimer une question")
            }
            Button(
                onClick = {
                    addQuestion()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Ajouter une question")
            }
            Button(
                onClick = {
                    fetchQuestion()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Obtenir une question")
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
            Text(text = stringResource(R.string.logout_action))
        }
    }
}
