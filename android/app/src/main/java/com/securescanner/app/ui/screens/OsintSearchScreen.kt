package com.securescanner.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.model.CheckStatus
import com.securescanner.app.data.model.UsernameCheckResult
import com.securescanner.app.data.repository.OsintRepository
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.EmptyState
import com.securescanner.app.ui.components.SectionHeader
import com.securescanner.app.ui.theme.Charcoal400
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.Charcoal800
import com.securescanner.app.ui.theme.Charcoal900
import com.securescanner.app.ui.theme.FlagExplicit
import com.securescanner.app.ui.theme.MatteCyan400
import com.securescanner.app.ui.theme.MatteCyan600
import com.securescanner.app.ui.theme.StatusSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

// ─── ViewModel ────────────────────────────────────────────────────

@HiltViewModel
class OsintSearchViewModel @Inject constructor(
    private val osintRepository: OsintRepository,
    val settingsDataStore: SettingsDataStore
) : ViewModel() {

    // Username search state
    private val _usernameResults = MutableStateFlow<List<UsernameCheckResult>>(emptyList())
    val usernameResults: StateFlow<List<UsernameCheckResult>> = _usernameResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchProgress = MutableStateFlow(0f)
    val searchProgress: StateFlow<Float> = _searchProgress.asStateFlow()

    private val _totalSites = MutableStateFlow(0)
    val totalSites: StateFlow<Int> = _totalSites.asStateFlow()

    private val _checkedCount = MutableStateFlow(0)
    val checkedCount: StateFlow<Int> = _checkedCount.asStateFlow()

    private val _foundCount = MutableStateFlow(0)
    val foundCount: StateFlow<Int> = _foundCount.asStateFlow()

    private var searchJob: Job? = null

    // OSINT Industries state
    private val _osintResult = MutableStateFlow<JsonObject?>(null)
    val osintResult: StateFlow<JsonObject?> = _osintResult.asStateFlow()

    private val _osintLoading = MutableStateFlow(false)
    val osintLoading: StateFlow<Boolean> = _osintLoading.asStateFlow()

    private val _osintError = MutableStateFlow<String?>(null)
    val osintError: StateFlow<String?> = _osintError.asStateFlow()

    private val _credits = MutableStateFlow<Int?>(null)
    val credits: StateFlow<Int?> = _credits.asStateFlow()

    val siteCount: Int get() = osintRepository.totalSiteCount

    val allTags: List<String> get() = osintRepository.getAllTags()

    fun searchUsername(username: String, selectedTags: Set<String> = emptySet()) {
        searchJob?.cancel()
        _usernameResults.value = emptyList()
        _isSearching.value = true
        _checkedCount.value = 0
        _foundCount.value = 0

        val sites = if (selectedTags.isEmpty()) {
            osintRepository.getAllSites()
        } else {
            osintRepository.getAllSites().filter { site ->
                site.tags.any { it in selectedTags }
            }
        }
        _totalSites.value = sites.size

        searchJob = viewModelScope.launch {
            osintRepository.checkUsername(username, sites).collect { result ->
                val current = _usernameResults.value.toMutableList()
                current.add(result)
                _usernameResults.value = current
                _checkedCount.value = current.size
                _searchProgress.value = current.size.toFloat() / sites.size
                if (result.status == CheckStatus.FOUND) {
                    _foundCount.value = _foundCount.value + 1
                }
            }
            _isSearching.value = false
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        _isSearching.value = false
    }

    fun searchOsintIndustries(apiKey: String, type: String, query: String) {
        _osintLoading.value = true
        _osintError.value = null
        _osintResult.value = null

        viewModelScope.launch {
            osintRepository.osintIndustriesSearch(apiKey, type, query)
                .onSuccess { _osintResult.value = it }
                .onFailure { _osintError.value = it.message ?: "Unknown error" }
            _osintLoading.value = false
        }
    }

    fun loadCredits(apiKey: String) {
        viewModelScope.launch {
            osintRepository.osintIndustriesCredits(apiKey)
                .onSuccess { _credits.value = it.remaining }
        }
    }
}

// ─── Screen ───────────────────────────────────────────────────────

@Composable
fun OsintSearchScreen(viewModel: OsintSearchViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Username Search", "OSINT Industries")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Charcoal900,
            contentColor = MatteCyan400,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MatteCyan600
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    selectedContentColor = MatteCyan400,
                    unselectedContentColor = Charcoal400
                )
            }
        }

        when (selectedTab) {
            0 -> UsernameSearchTab(viewModel)
            1 -> OsintIndustriesTab(viewModel)
        }
    }
}

// ─── Username Search Tab (Maigret) ───────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UsernameSearchTab(viewModel: OsintSearchViewModel) {
    var username by remember { mutableStateOf("") }
    var showTagFilter by remember { mutableStateOf(false) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val results by viewModel.usernameResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val progress by viewModel.searchProgress.collectAsState()
    val totalSites by viewModel.totalSites.collectAsState()
    val checkedCount by viewModel.checkedCount.collectAsState()
    val foundCount by viewModel.foundCount.collectAsState()

    val foundResults = results.filter { it.status == CheckStatus.FOUND }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search input
        item {
            CardSurface {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MatteCyan600)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Username Lookup",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${viewModel.siteCount} sites",
                            style = MaterialTheme.typography.bodySmall,
                            color = Charcoal400
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Check a username across ${viewModel.siteCount} sites (powered by Maigret database)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Charcoal400
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter username") },
                        singleLine = true,
                        enabled = !isSearching,
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = MatteCyan600)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatteCyan600,
                            unfocusedBorderColor = Charcoal700,
                            cursorColor = MatteCyan400,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedPlaceholderColor = Charcoal400,
                            unfocusedPlaceholderColor = Charcoal400
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    // Tag filter toggle
                    OutlinedButton(
                        onClick = { showTagFilter = !showTagFilter },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MatteCyan600)
                    ) {
                        Text(
                            if (selectedTags.isEmpty()) "Filter by tags"
                            else "${selectedTags.size} tag(s) selected"
                        )
                    }

                    AnimatedVisibility(visible = showTagFilter) {
                        FlowRow(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Show top 30 tags for performance
                            val topTags = remember {
                                viewModel.allTags.take(30)
                            }
                            topTags.forEach { tag ->
                                FilterChip(
                                    selected = tag in selectedTags,
                                    onClick = {
                                        selectedTags = if (tag in selectedTags)
                                            selectedTags - tag else selectedTags + tag
                                    },
                                    label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MatteCyan600,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = Charcoal700,
                                        labelColor = Charcoal400
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (isSearching) {
                        OutlinedButton(
                            onClick = { viewModel.cancelSearch() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FlagExplicit)
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Stop Search")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (username.isNotBlank()) {
                                    viewModel.searchUsername(username.trim(), selectedTags)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = username.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Search Username")
                        }
                    }
                }
            }
        }

        // Progress
        if (isSearching || results.isNotEmpty()) {
            item {
                CardSurface {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Checked: $checkedCount / $totalSites",
                                style = MaterialTheme.typography.bodySmall,
                                color = Charcoal400
                            )
                            Text(
                                "Found: $foundCount",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MatteCyan600,
                            trackColor = Charcoal700
                        )
                    }
                }
            }
        }

        // Results header
        if (foundResults.isNotEmpty()) {
            item {
                SectionHeader(title = "Found on ${foundResults.size} site(s)")
            }
        }

        // Found results
        items(foundResults, key = { it.site.name }) { result ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.url))
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(12.dp),
                color = Charcoal800
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            result.site.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            result.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MatteCyan400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (result.site.tags.isNotEmpty()) {
                            Text(
                                result.site.tags.joinToString(", "),
                                style = MaterialTheme.typography.labelSmall,
                                color = Charcoal400
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open",
                        tint = MatteCyan600,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Empty state
        if (!isSearching && results.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Person,
                    title = "Username OSINT Search",
                    subtitle = "Enter a username to check across ${viewModel.siteCount} sites from the Maigret database"
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ─── OSINT Industries Tab ────────────────────────────────────────

@Composable
private fun OsintIndustriesTab(viewModel: OsintSearchViewModel) {
    var query by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("email") }

    val apiKey by viewModel.settingsDataStore.osintIndustriesApiKey.collectAsState(initial = "")
    val result by viewModel.osintResult.collectAsState()
    val isLoading by viewModel.osintLoading.collectAsState()
    val error by viewModel.osintError.collectAsState()
    val credits by viewModel.credits.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // API key check
        if (apiKey.isBlank()) {
            item {
                CardSurface {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = FlagExplicit,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "API Key Required",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Set your OSINT Industries API key in Settings to use this feature.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Charcoal400
                        )
                    }
                }
            }
            return@LazyColumn
        }

        // Search input
        item {
            CardSurface {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = MatteCyan600)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "OSINT Industries Search",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Search by email or phone number across OSINT Industries database",
                        style = MaterialTheme.typography.bodySmall,
                        color = Charcoal400
                    )

                    if (credits != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Credits remaining: $credits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MatteCyan400
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Type selector
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { searchType = "email" },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (searchType == "email") MatteCyan600 else Charcoal700,
                                labelColor = if (searchType == "email") MaterialTheme.colorScheme.onPrimary else Charcoal400,
                                leadingIconContentColor = if (searchType == "email") MaterialTheme.colorScheme.onPrimary else Charcoal400
                            )
                        )
                        AssistChip(
                            onClick = { searchType = "phone" },
                            label = { Text("Phone") },
                            leadingIcon = {
                                Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (searchType == "phone") MatteCyan600 else Charcoal700,
                                labelColor = if (searchType == "phone") MaterialTheme.colorScheme.onPrimary else Charcoal400,
                                leadingIconContentColor = if (searchType == "phone") MaterialTheme.colorScheme.onPrimary else Charcoal400
                            )
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(if (searchType == "email") "user@example.com" else "+1234567890")
                        },
                        singleLine = true,
                        enabled = !isLoading,
                        leadingIcon = {
                            Icon(
                                if (searchType == "email") Icons.Filled.Email else Icons.Filled.Phone,
                                contentDescription = null,
                                tint = MatteCyan600
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatteCyan600,
                            unfocusedBorderColor = Charcoal700,
                            cursorColor = MatteCyan400,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedPlaceholderColor = Charcoal400,
                            unfocusedPlaceholderColor = Charcoal400
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (query.isNotBlank()) {
                                viewModel.searchOsintIndustries(apiKey, searchType, query.trim())
                                viewModel.loadCredits(apiKey)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = query.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Searching...")
                        } else {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Search")
                        }
                    }
                }
            }
        }

        // Error
        if (error != null) {
            item {
                CardSurface {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, contentDescription = null, tint = FlagExplicit)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FlagExplicit
                        )
                    }
                }
            }
        }

        // Results
        if (result != null) {
            item {
                SectionHeader(title = "Results")
            }
            item {
                CardSurface {
                    Column {
                        val entries = result?.entries?.toList() ?: emptyList()
                        if (entries.isEmpty()) {
                            Text(
                                "No results found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Charcoal400
                            )
                        } else {
                            entries.forEachIndexed { index, (key, value) ->
                                if (index > 0) Spacer(Modifier.height(8.dp))
                                Text(
                                    key,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MatteCyan400,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    value.toString().take(500),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 10,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Empty state
        if (!isLoading && result == null && error == null) {
            item {
                EmptyState(
                    icon = Icons.Filled.Search,
                    title = "OSINT Industries",
                    subtitle = "Search email addresses or phone numbers across the OSINT Industries database"
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
