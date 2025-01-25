package com.chromatics.caller_id.ui.contacts

import android.content.pm.PackageManager
import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ContactsFragmentBinding.inflate(inflater, container, false)


        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = ContactsAdapter()
        binding.contactsRecyclerView.adapter = adapter



        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.READ_CONTACTS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            binding.noContactsFound.text = "No permissions for reading contacts"
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                REQUEST_CODE_READ_CONTACTS
            )


        } else {

            binding.noContactsFound.visibility = View.GONE
            binding.contactsRecyclerView.visibility = View.VISIBLE

            viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
                adapter.submitList(contacts)
            }

            viewModel.loadContacts(requireContext().contentResolver)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}