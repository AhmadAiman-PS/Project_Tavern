package com.example.tavern.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tavern.data.PostEntity
import com.example.tavern.data.TavernDatabase
import com.example.tavern.data.TavernRepository
import com.example.tavern.data.CommentEntity
import androidx.compose.material.icons.automirrored.filled.Send

@Composable
fun TavernApp() {
    val context = LocalContext.current
    val database = TavernDatabase.getDatabase(context)
    val repository = TavernRepository(database.postDao(), database.userDao(), database.commentDao())
    val viewModel: TavernViewModel = viewModel(factory = TavernViewModelFactory(repository))

    val currentUser by viewModel.currentUser.collectAsState()
    val selectedPost by viewModel.selectedPost.collectAsState()


    // State to toggle between Login and Register screens
    var isRegistering by remember { mutableStateOf(false) }

    // --- NAVIGATION LOGIC ---
    if (currentUser != null) {
        // If logged in, check if we are looking at a specific post
        if (selectedPost != null) {
            PostDetailScreen(viewModel) // <--- NEW SCREEN
        } else {
            TavernFeedScreen(viewModel, currentUser!!.username)
        }
    } else {
        // If not logged in, decide which form to show
        if (isRegistering) {
            RegisterScreen(
                viewModel = viewModel,
                onBackToLogin = { isRegistering = false }
            )
        } else {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { isRegistering = true }
            )
        }
    }
}

// --- SCREEN 1: LOGIN (The Gate) ---
@Composable
fun LoginScreen(viewModel: TavernViewModel, onNavigateToRegister: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error by viewModel.loginError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Tavern background color
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocalBar,
            contentDescription = "Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "The Tavern Gate",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Username Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Traveller's Name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Secret Word") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Error Message
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Enter Tavern", fontSize = 18.sp, fontFamily = FontFamily.Serif)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch to Register
        TextButton(onClick = onNavigateToRegister) {
            Text("New here? Sign the Guestbook (Register)")
        }
    }
}



// --- SCREEN 3: FEED (The Tavern Board) ---
// (This remains largely the same, just keeping it here for completeness)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TavernFeedScreen(viewModel: TavernViewModel, username: String) {
    val posts by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("The Tavern Board", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                        Text("Welcome, $username", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEFEBE9))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color(0xFFFFF8E1)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Default.HistoryEdu, contentDescription = "Write")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PostList(posts = posts, viewModel = viewModel, modifier = Modifier.padding(padding))

        if (showDialog) {
            AddPostDialog(
                onDismiss = { showDialog = false },
                onConfirm = { title, body ->
                    viewModel.createPost(title, body)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(viewModel: TavernViewModel) {
    val post = viewModel.selectedPost.collectAsState().value ?: return
    val comments by viewModel.currentComments.collectAsState()
    var newCommentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discussion", fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.selectPost(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Add a comment...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    IconButton(onClick = {
                        if (newCommentText.isNotBlank()) {
                            viewModel.addComment(newCommentText)
                            newCommentText = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                PostCard(post, onClick = {}) // Show post, but no click action needed here
                Spacer(modifier = Modifier.height(16.dp))
                Text("Comments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (comments.isEmpty()) {
                item { Text("No voices yet. Be the first!", fontStyle = FontStyle.Italic, color = Color.Gray) }
            } else {
                items(comments) { comment ->
                    CommentItem(comment)
                }
            }
        }
    }
}

@Composable

fun PostList(posts: List<PostEntity>, viewModel: TavernViewModel, modifier: Modifier = Modifier) {
    if (posts.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalBar, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No tales yet...", fontFamily = FontFamily.Serif, color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                PostCard(post, onClick = { viewModel.selectPost(post) })
            }
        }
    }
}

@Composable
fun PostCard(post: PostEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Tale by ${post.author}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontStyle = FontStyle.Italic)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.title, style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalBar, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = "${post.upvotes} Cheers", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
            Text(
                text = comment.author,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AddPostDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share a Tale", fontFamily = FontFamily.Serif) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Story") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, body) }) { Text("Post") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}