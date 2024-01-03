package com.test.passkeyapp.ui.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.test.customsnackbar.CustomSnackBar
import com.test.passkeyapp.data.DataProvider
import com.test.passkeyapp.databinding.FragmentHomeBinding
import com.test.passkeyapp.utilities.observe

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        subscribeUi()
    }
    private fun initUi(){
        configureSignedInText()
    }

    private fun subscribeUi(){
        observe(binding.logoutBtn, ::navigateToStart)
    }

    private fun configureSignedInText() {
        if (DataProvider.isSignedInThroughPasskeys()) {
            binding.signedInText.text = LOGGED_IN_THROUGH_PASSKEYS
        } else {
            binding.signedInText.text = LOGGED_IN_THROUGH_PASSWORD
        }
    }

    companion object {
        private const val LOGGED_IN_THROUGH_PASSWORD = "Logged in successfully through password"
        private const val LOGGED_IN_THROUGH_PASSKEYS = "Logged in successfully through passkeys"
    }

    //navigation
    private fun navigateToStart(){
        CustomSnackBar.showSnackBar(binding.root, "You just signed out")
        DataProvider.configureSignedInPref(false)
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToStartUpScreenFragment())
    }
}