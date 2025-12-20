package com.example.tavern.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tavern.data.PostEntity
import com.example.tavern.data.TavernDatabase
import com.example.tavern.data.TavernRepository
import com.example.tavern.data.CommentEntity
import com.example.tavern.ui.theme.*

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

    // --- NAVIGATION LOGIC WITH ANIMATIONS ---
    AnimatedContent(
        targetState = when {
            currentUser != null && selectedPost != null -> "detail"
            currentUser != null -> "feed"
            isRegistering -> "register"
            else -> "login"
        },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400)) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(400))
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            "detail" -> PostDetailScreen(viewModel)
            "feed" -> TavernFeedScreen(viewModel, currentUser!!.username)
            "register" -> RegisterScreen(
                viewModel = viewModel,
                onBackToLogin = { isRegistering = false }
            )
            "login" -> LoginScreen(
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
    var triggerShake by remember { mutableStateOf(false) }

    // Trigger shake animation when error occurs
    LaunchedEffect(error) {
        if (error != null) {
            triggerShake = true
            kotlinx.coroutines.delay(500)
            triggerShake = false
        }
    }

    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with pulse animation
            Icon(
                imageVector = Icons.Default.LocalBar,
                contentDescription = "Logo",
                modifier = Modifier
                    .size(80.dp)
                    .pulseAnimation(minScale = 0.95f, maxScale = 1.05f, durationMillis = 2000),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title with fade in animation
            Text(
                "The Tavern Gate",
                style = TitleTavern,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fadeInOnAppear(delayMillis = 100)
            )
            
            Text(
                "Enter your legend",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.fadeInOnAppear(delayMillis = 200)
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Username Input with slide animation
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Traveller's Name") },
                leadingIcon = { 
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) 
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottomOnAppear(delayMillis = 300),
                shape = TextFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Password Input with slide animation
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Secret Word") },
                leadingIcon = { 
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) 
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottomOnAppear(delayMillis = 400)
                    .shakeAnimation(triggerShake),
                shape = TextFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Error Message with animation
            AnimatedVisibility(
                visible = error != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = Shapes.small
                ) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button with bounce animation
            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .slideInFromBottomOnAppear(delayMillis = 500),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = ButtonShape,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    "Enter Tavern",
                    style = ButtonText,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch to Register with fade animation
            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fadeInOnAppear(delayMillis = 600)
            ) {
                Text(
                    "New here? Sign the Guestbook (Register)",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// --- SCREEN 2: FEED (The Tavern Board) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TavernFeedScreen(viewModel: TavernViewModel, username: String) {
    val posts by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fadeInOnAppear(delayMillis = 100)
                    ) {
                        Text(
                            "The Tavern Board",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Welcome, $username",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.bounceOnAppear()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.shadow(8.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .bounceOnAppear()
                    .pulseAnimation(minScale = 0.98f, maxScale = 1.02f, durationMillis = 1500),
                shape = FabShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
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

// --- SCREEN 3: POST DETAIL (Discussion) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(viewModel: TavernViewModel) {
    val post = viewModel.selectedPost.collectAsState().value ?: return
    val comments by viewModel.currentComments.collectAsState()
    var newCommentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Discussion",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.fadeInOnAppear(delayMillis = 100)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.selectPost(null) },
                        modifier = Modifier.bounceOnAppear()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                shape = BottomBarShape,
                modifier = Modifier.slideInFromBottomOnAppear(delayMillis = 200)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Add your voice...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        shape = TextFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    FloatingActionButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                viewModel.addComment(newCommentText)
                                newCommentText = ""
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            "Send",
                            modifier = Modifier.size(24.dp)
                        )
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PostCard(post, onClick = {}, isDetail = true)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fadeInOnAppear(delayMillis = 300)
                ) {
                    Icon(
                        Icons.Default.HistoryEdu,
                        null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Voices (${comments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (comments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fadeInOnAppear(delayMillis = 400),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = Shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.HistoryEdu,
                                null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No voices yet...",
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Be the first to speak!",
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(comments) { index, comment ->
                    CommentItem(comment, index)
                }
            }
        }
    }
}

// --- POST LIST COMPONENT ---
@Composable
fun PostList(posts: List<PostEntity>, viewModel: TavernViewModel, modifier: Modifier = Modifier) {
    if (posts.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.bounceOnAppear()
            ) {
                Icon(
                    Icons.Default.LocalBar,
                    null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .size(64.dp)
                        .pulseAnimation()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No tales yet...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    "Be the first to share your story!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(
                items = posts,
                key = { _, post -> post.id }
            ) { index, post ->
                PostCard(
                    post = post,
                    onClick = { viewModel.selectPost(post) },
                    modifier = Modifier.fadeInOnAppear(delayMillis = index * 50)
                )
            }
        }
    }
}

// --- POST CARD COMPONENT ---
@Composable
fun PostCard(
    post: PostEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDetail: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isDetail) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = if (!isDetail) 8.dp else 4.dp
        ),
        shape = PostCardShape
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Author badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = Shapes.small,
                modifier = Modifier.fadeInOnAppear(delayMillis = 100)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = post.author,
                        style = AuthorName.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = post.title,
                style = PostTitle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fadeInOnAppear(delayMillis = 200)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Content
            Text(
                text = post.content,
                style = PostContent,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = if (isDetail) Int.MAX_VALUE else 3,
                modifier = Modifier.fadeInOnAppear(delayMillis = 300)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Upvotes
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = Shapes.small,
                modifier = Modifier.fadeInOnAppear(delayMillis = 400)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.LocalBar,
                        null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.upvotes} Cheers",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MetaText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- COMMENT ITEM COMPONENT ---
@Composable
fun CommentItem(comment: CommentEntity, index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fadeInOnAppear(delayMillis = 100 + (index * 50)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = CommentCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = Shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = comment.author,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// --- ADD POST DIALOG ---
@Composable
fun AddPostDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.HistoryEdu,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Share a Tale", style = SubtitleTavern)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = TextFieldShape,
                    singleLine = true
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Your Story") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
                    shape = TextFieldShape
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, body) },
                shape = ButtonShape
            ) {
                Text("Post", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = DialogShape,
        containerColor = MaterialTheme.colorScheme.surface
    )
}
