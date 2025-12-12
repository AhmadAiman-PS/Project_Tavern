package com.example.tavern.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tavern.data.PostEntity
import com.example.tavern.data.TavernDatabase
import com.example.tavern.data.TavernRepository

@Composable
fun TavernApp() {
    val context = LocalContext.current
    val database = TavernDatabase.getDatabase(context)
    // Update: Pass both DAOs to the repository
    val repository = TavernRepository(database.postDao(), database.userDao())
    val viewModel: TavernViewModel = viewModel(factory = TavernViewModelFactory(repository))

    val currentUser by viewModel.currentUser.collectAsState()

    // NAVIGATION LOGIC: Switch screens based on login state
    if (currentUser == null) {
        LoginScreen(viewModel)
    } else {
        TavernFeedScreen(viewModel, currentUser!!.username)
    }
}

// --- SCREEN 1: THE LOGIN GATE ---
@Composable
fun LoginScreen(viewModel: TavernViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error by viewModel.loginError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Traveller's Name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Secret Word") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Enter Tavern")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { viewModel.register(username, password) }) {
            Text("New here? Join the Guild (Register)")
        }
    }
}

// --- SCREEN 2: THE MAIN FEED (What you built before) ---
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
                        Text("Welcome, $username", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
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
        PostList(posts = posts, modifier = Modifier.padding(padding))

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

// Re-use your existing PostList, PostCard, and AddPostDialog here
// (Paste them below this line just like in the previous code)
@Composable
fun PostList(posts: List<PostEntity>, modifier: Modifier = Modifier) {
    if (posts.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tales yet...", fontFamily = FontFamily.Serif)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                PostCard(post)
            }
        }
    }
}

@Composable
fun PostCard(post: PostEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Tale by ${post.author}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontStyle = FontStyle.Italic)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.title, style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Text(text = post.content, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${post.upvotes} Cheers", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
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
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Story") })
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, body) }) { Text("Post") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}