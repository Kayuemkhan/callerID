package com.chromatics.caller_id.ui.contacts
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chromatics.caller_id.databinding.ContactsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsFragment : Fragment() {
    private val REQUEST_CODE_READ_CONTACTS = 100
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var binding: ContactsFragmentBinding
    private lateinit var adapter: ContactsAdapter
    private var fullContactsList: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ContactsFragmentBinding.inflate(inflater, container, false)

        setupRecyclerView()
        checkPermissions()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ContactsAdapter()
        binding.contactsRecyclerView.adapter = adapter
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.noContactsFound.text = "No permissions for reading contacts"
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                REQUEST_CODE_READ_CONTACTS
            )
        } else {
            loadContacts()
        }
    }

    private fun loadContacts() {
        binding.noContactsFound.visibility = View.GONE
        binding.contactsRecyclerView.visibility = View.VISIBLE

        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            fullContactsList = contacts
            adapter.submitList(contacts)
        }

        viewModel.loadContacts(requireContext().contentResolver)
        setupSearchFunctionality()
    }

    private fun setupSearchFunctionality() {
        binding.searchContactsEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterContacts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterContacts(query: String) {
        val filteredContacts = if (query.isEmpty()) {
            fullContactsList
        } else {
            fullContactsList.filter { it.contains(query, ignoreCase = true) }
        }

        if (filteredContacts.isEmpty()) {
            binding.noContactsFound.visibility = View.VISIBLE
            binding.contactsRecyclerView.visibility = View.GONE
        } else {
            binding.noContactsFound.visibility = View.GONE
            binding.contactsRecyclerView.visibility = View.VISIBLE
            adapter.submitList(filteredContacts)
        }
    }
}

