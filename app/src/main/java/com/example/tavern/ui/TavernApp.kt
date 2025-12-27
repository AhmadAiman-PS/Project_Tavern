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
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TavernApp() {
    val context = LocalContext.current
    val database = TavernDatabase.getDatabase(context)
    // Initialize repository with all DAOs including cheerDao for like system
    val repository = TavernRepository(database.postDao(), database.userDao(), database.commentDao(), database.cheerDao())
    val viewModel: TavernViewModel = viewModel(factory = TavernViewModelFactory(repository))

    val currentUser by viewModel.currentUser.collectAsState()
    val selectedPost by viewModel.selectedPost.collectAsState()
    val profileUser by viewModel.profileUser.collectAsState()

    // State to toggle between Login and Register screens
    var isRegistering by remember { mutableStateOf(false) }

    // Track where we came from for proper back navigation
    var cameFromProfile by remember { mutableStateOf(false) }

    // --- NAVIGATION LOGIC WITH ANIMATIONS ---
    AnimatedContent(
        targetState = when {
            currentUser != null && profileUser != null -> "profile"
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
                "profile" -> {
            ProfileScreen(
                viewModel = viewModel,
                repository = repository, // Tambahkan baris ini
                onBack = {
                    cameFromProfile = false
                    viewModel.exitProfile()
                },
                onPostClick = { post ->
                    cameFromProfile = true
                    viewModel.selectPost(post)
                }
            )
        }
            "detail" -> PostDetailScreen(
                viewModel = viewModel,
                repository = repository,
                onBack = {
                    viewModel.selectPost(null)
                    if (cameFromProfile) {
                        // Stay in profile, don't exit
                        // We do NOT set cameFromProfile = false here so the when block keeps us in profile
                    } else {
                        cameFromProfile = false
                    }
                }
            )
            "feed" -> TavernFeedScreen(viewModel, repository, currentUser!!.username)
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
    val isLoading by viewModel.isLoading.collectAsState()
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
                enabled = !isLoading,
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
                enabled = !isLoading,
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

            // Login Button with loading state
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
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Enter Tavern",
                        style = ButtonText,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch to Register with fade animation
            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fadeInOnAppear(delayMillis = 600),
                enabled = !isLoading
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
fun TavernFeedScreen(viewModel: TavernViewModel, repository: TavernRepository, username: String) {
    val posts by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (showSearchBar) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = {
                                Text(
                                    "Search posts...",
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(48.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    null,
                                    tint = Color.Black.copy(alpha = 0.7f)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.clearSearch()
                                    showSearchBar = false
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        tint = Color.Black.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            shape = TextFieldShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black,
                                focusedLeadingIconColor = Color.Black.copy(alpha = 0.7f),
                                unfocusedLeadingIconColor = Color.Black.copy(alpha = 0.5f),
                                focusedTrailingIconColor = Color.Black.copy(alpha = 0.7f),
                                unfocusedTrailingIconColor = Color.Black.copy(alpha = 0.5f)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black
                            )
                        )
                    } else {
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
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.viewProfile(username) },
                        modifier = Modifier.bounceOnAppear()
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (!showSearchBar) {
                        IconButton(
                            onClick = { showSearchBar = true },
                            modifier = Modifier.bounceOnAppear()
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
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
        val displayPosts = if (isSearching) searchResults else posts

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Show search indicator
            if (isSearching) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Found ${searchResults.size} posts matching \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            PostList(
                posts = displayPosts,
                viewModel = viewModel,
                repository = repository,
                modifier = Modifier.weight(1f),
                emptyMessage = if (isSearching) "No posts found" else "No tales yet..."
            )
        }

        if (showDialog) {
            AddPostDialog(
                viewModel = viewModel,
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
fun PostDetailScreen(viewModel: TavernViewModel, repository: TavernRepository, onBack: () -> Unit) {
    val post = viewModel.selectedPost.collectAsState().value ?: return
    val comments by viewModel.currentComments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
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
                        onClick = onBack,
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
                        enabled = !isLoading,
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
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                "Send",
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
                PostCard(
                    post = post,
                    onClick = {},
                    isDetail = true,
                    viewModel = viewModel,
                    repository = repository
                )
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
fun PostList(
    posts: List<PostEntity>,
    viewModel: TavernViewModel,
    repository: TavernRepository,
    modifier: Modifier = Modifier,
    emptyMessage: String = "No tales yet..."
) {
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
                    emptyMessage,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.outline
                )
                if (emptyMessage == "No tales yet...") {
                    Text(
                        "Be the first to share your story!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
                }
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
                    modifier = Modifier.fadeInOnAppear(delayMillis = index * 50),
                    viewModel = viewModel,
                    repository = repository
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
    isDetail: Boolean = false,
    viewModel: TavernViewModel? = null,
    repository: TavernRepository
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val currentUser = viewModel?.currentUser?.collectAsState()?.value

    // Get cheer count
    val cheerCount by remember(post.id) {
        repository.getCheerCount(post.id)
    }.collectAsState(initial = 0)

    // Check if user cheered
    val hasUserCheered by remember(post.id) {
        val username = viewModel?.currentUser?.value?.username ?: ""
        repository.hasUserCheered(username, post.id)
    }.collectAsState(initial = 0)

    if (showDeleteDialog && viewModel != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post?") },
            text = { Text("Are you sure you want to delete this tale? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePost(post)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
            // Author badge and timestamp row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author badge (clickable)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = Shapes.small,
                    modifier = Modifier
                        .fadeInOnAppear(delayMillis = 100)
                        .then(
                            if (viewModel != null && !isDetail) {
                                Modifier.clickable {
                                    viewModel.viewProfile(post.author)
                                }
                            } else Modifier
                        )
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Delete Button for Owner
                    if (currentUser?.username == post.author && !isDetail) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Timestamp
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = Shapes.small,
                        modifier = Modifier.fadeInOnAppear(delayMillis = 150)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = formatTimestamp(post.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cheer button dengan animation
                Button(
                    onClick = { viewModel?.toggleCheer(post) },
                    colors = if (hasUserCheered > 0)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    else
                        ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fadeInOnAppear(delayMillis = 400),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = Shapes.small
                ) {
                    Icon(
                        Icons.Default.LocalBar,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$cheerCount Cheers")
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
fun AddPostDialog(viewModel: TavernViewModel, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
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
                    singleLine = true,
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Your Story") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
                    shape = TextFieldShape,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, body) },
                shape = ButtonShape,
                enabled = !isLoading && title.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Post", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        },
        shape = DialogShape,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// Helper function to format timestamp
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now" // Less than 1 minute
        diff < 3600000 -> "${diff / 60000}m ago" // Less than 1 hour
        diff < 86400000 -> "${diff / 3600000}h ago" // Less than 1 day
        diff < 604800000 -> "${diff / 86400000}d ago" // Less than 1 week
        else -> {
            // More than 1 week, show date
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}