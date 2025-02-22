package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.system.utils.savedStateViewModel
import io.github.dovecoteescapee.byedpi.common.ui.theme.ByeDPITheme
import io.github.dovecoteescapee.byedpi.feature.connection.ui.R
import io.github.dovecoteescapee.byedpi.feature.connection.ui.di.ConnectionComponentHolder
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.ViewModelProvider
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles.swipemenu.OneTouchHelperCallback

internal class ProfilesFragment : BottomSheetDialogFragment(R.layout.fragment_profiles) {

    internal lateinit var appSettings: KeyValueStorage<StorageType.AppSettings>
    internal lateinit var byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
    internal lateinit var vmProvider: ViewModelProvider

    private val vm: ProfilesViewModel by savedStateViewModel {
        vmProvider.getProfilesViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ConnectionComponentHolder.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //vm.actions.observeFlowWithLifecycle(viewLifecycleOwner, block = ::handleAction)

        view.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ByeDPITheme {
                    ProfilesScreen(
                        viewModel = vm,
                    )
                }
            }
        }
    }

    private fun handleAction(action: ProfilesViewModel.Actions) {
        when (action) {
            ProfilesViewModel.Actions.ShowAddProfileDialog -> {
                showAddProfileDialog()
            }

            is ProfilesViewModel.Actions.ShowEditProfileDialog -> {
                showEditProfileDialog(action.profileName)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().recreate()
    }

    private fun showAddProfileDialog() {
        showDialog("Имя профиля") {
            vm.profileAdded(it)
        }
    }

    private fun showEditProfileDialog(name: String) {
        showDialog("Имя профиля", name) {
            vm.profileRename(name, it)
        }
    }

    private fun showDialog(title: String, curName: String? = null, action: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            setText(curName)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(input)
        }

        MaterialAlertDialogBuilder(requireContext(), io.github.dovecoteescapee.byedpi.common.ui.R.style.AlertDialogTheme)
            .setTitle(title)
            .setView(container)
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank() && !name.contains(";")) {
                    action.invoke(name)
                }
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }
}

@Composable
internal fun ProfilesScreen(
    viewModel: ProfilesViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var currentName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                ProfilesViewModel.Actions.ShowAddProfileDialog -> {
                    currentName = "" // Новый профиль
                    showDialog = true
                }

                is ProfilesViewModel.Actions.ShowEditProfileDialog -> {
                    currentName = action.profileName
                    showDialog = true
                }
            }
        }
    }

    if (showDialog) {
        InputDialog(
            title = if (currentName.isEmpty()) "Добавить профиль" else "Редактировать профиль",
            curName = currentName,
            onDismiss = { showDialog = false },
            onConfirm = { name ->
                if (currentName.isEmpty()) {
                    viewModel.profileAdded(name)
                } else {
                    viewModel.profileRename(currentName, name)
                }

                showDialog = false
            }
        )
    }

    val uiState by viewModel.state.collectAsState()

    when (uiState) {
        is ProfilesViewModel.State.Data -> {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Список профайлов",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(
                        top = 16.dp,
                        bottom = 16.dp,
                    )
                )

                Button(
                    onClick = {
                        viewModel.onAddProfileClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Добавить",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { context ->
                        RecyclerView(context).apply {
                            hasFixedSize()
                            layoutManager = LinearLayoutManager(context)
                            adapter = ProfilesAdapter(
                                swapListener = { viewModel.profilesSwapped(it) },
                                editClickListener = { viewModel.onProfileEdited(it) },
                                deleteClickListener = { viewModel.onProfileDeleted(it) },
                            )

                            OneTouchHelperCallback(this).build()
                        }
                    },
                    update = { recyclerView ->
                        (recyclerView.adapter as? ProfilesAdapter)?.updateItems(
                            (uiState as ProfilesViewModel.State.Data).profiles
                        )
                    }
                )
            }
        }
        ProfilesViewModel.State.Empty -> Unit
    }
}

@Composable
fun InputDialog(
    title: String,
    curName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(curName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank() && !text.contains(";")) {
                        onConfirm(text)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
