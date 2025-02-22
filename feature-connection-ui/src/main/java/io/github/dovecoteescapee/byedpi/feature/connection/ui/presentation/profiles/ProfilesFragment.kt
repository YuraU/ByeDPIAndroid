package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.deleteProfile
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProfiles
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProfilesList
import io.github.dovecoteescapee.byedpi.common.storage.utils.renameProfile
import io.github.dovecoteescapee.byedpi.common.storage.utils.setProfiles
import io.github.dovecoteescapee.byedpi.feature.connection.ui.R
import io.github.dovecoteescapee.byedpi.feature.connection.ui.di.ConnectionComponentHolder
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles.swipemenu.OneTouchHelperCallback
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class ProfilesFragment : BottomSheetDialogFragment() {

    internal lateinit var appSettings: KeyValueStorage<StorageType.AppSettings>
    internal lateinit var byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>

    private var adapter: ProfilesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ConnectionComponentHolder.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profiles, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerview: RecyclerView = view.findViewById(R.id.rv_profiles)

        lifecycleScope.launch {
            val data = appSettings.getProfilesList().toMutableList()

            adapter = ProfilesAdapter(
                items = data,
                swapListener = { profiles ->
                    lifecycleScope.launch {
                        appSettings.setProfiles(profiles.joinToString(";"))
                    }
                },
                editClickListener = { profile ->
                    showRenameDialog(
                        title = "Имя профиля",
                        curName = profile,
                        action = { name ->
                            lifecycleScope.launch {
                                appSettings.getProfilesList().forEach {
                                    if (it == profile) {
                                        appSettings.getProfiles()?.replace(profile, name)?.let {
                                            appSettings.setProfiles(it)
                                        }
                                        adapter?.updateItems(appSettings.getProfilesList())
                                    }
                                }
                                byeDpiSettings.renameProfile(profile, name)
                            }
                        },
                    )
                },
                deleteClickListener = { profile ->
                    runBlocking {
                        if (appSettings.getProfilesList().size <= 1) {
                            false
                        } else {
                            appSettings.getProfiles()?.replace("$profile;", "")?.let {
                                appSettings.setProfiles(it)

                                lifecycleScope.launch {
                                    byeDpiSettings.deleteProfile(profile)
                                }
                            }
                            true
                        }
                    }
                },
            )
            recyclerview.hasFixedSize()
            recyclerview.layoutManager = LinearLayoutManager(requireContext())
            recyclerview.adapter = adapter

            OneTouchHelperCallback(recyclerview).build()
        }


        view.findViewById<View>(R.id.add_btn).setOnClickListener {
            showRenameDialog("Имя профиля") { Unit }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().recreate()
    }

    private fun showRenameDialog(title: String, curName: String? = null, action: (String) -> Unit) {
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
                    lifecycleScope.launch {
                        if (curName != null) {
                            appSettings.getProfilesList().forEach {
                                if (it == curName) {
                                    appSettings.getProfiles()?.replace(curName, name)?.let {
                                        appSettings.setProfiles(it)
                                    }
                                    adapter?.updateItems(appSettings.getProfilesList())
                                }
                            }
                        } else {
                            appSettings.setProfiles(
                                appSettings.getProfiles() + ";" + name
                            )
                            adapter?.updateItems(appSettings.getProfilesList())
                        }
                    }
                }
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }
}
