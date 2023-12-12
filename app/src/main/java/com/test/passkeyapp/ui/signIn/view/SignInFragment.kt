package com.test.passkeyapp.ui.signIn.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.test.passkeyapp.data.DataProvider
import com.test.passkeyapp.databinding.FragmentSignInBinding
import com.test.passkeyapp.databinding.FragmentStartUpScreenBinding
import com.test.passkeyapp.ui.startUp.view.StartUpScreenFragmentDirections
import com.test.passkeyapp.utilities.observe
import com.test.passkeyapp.utilities.readFromAsset
import com.test.passkeyapp.utilities.showErrorAlert
import kotlinx.coroutines.launch
import timber.log.Timber

class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding

    private lateinit var credentialManager: CredentialManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        subscribeUi()
    }

    private fun initUi() {
        credentialManager = CredentialManager.create(requireActivity())
    }

    private fun subscribeUi() {
        observe(binding.signInWithSavedCredentialsBtn, ::signInWithSavedCredentials)
        observe(binding.backBtn, findNavController()::navigateUp)
    }

    private fun signInWithSavedCredentials() {
        lifecycleScope.launch {
            configureViews(View.VISIBLE, false)

            //Call getSavedCredentials() method to signin using passkey/password
            val data = getSavedCredentials()
            configureViews(View.INVISIBLE, true)

            //complete the authentication process after validating the public key credential to your server and let the user in.
            data?.let {
                sendSignInResponseToServer()
                navigateToHome()
            }

        }
    }

    private fun configureViews(visibility: Int, flag: Boolean) {
        binding.textProgress.visibility = visibility
        binding.circularProgressIndicator.visibility = visibility
        binding.signInWithSavedCredentialsBtn.isEnabled = flag
    }

    private fun fetchAuthJsonFromServer(): String {
        //fetch authentication mock json
        return requireContext().readFromAsset("AuthFromServer")
    }

    private fun sendSignInResponseToServer(): Boolean {
        return true
    }

    private suspend fun getSavedCredentials(): String? {
        //create a GetPublicKeyCredentialOption() with necessary authentication json from server
        val getPublicKeyCredentialOption =
            GetPublicKeyCredentialOption(fetchAuthJsonFromServer(), null)
        //create a PasswordOption to retrieve all the associated user's password
        val getPasswordOption = GetPasswordOption()
        //call getCredential() with required credential options
        val result = try {
            credentialManager.getCredential(
                requireActivity(),
                GetCredentialRequest(
                    listOf(
                        getPublicKeyCredentialOption,
                        getPasswordOption
                    )
                )
            )
        } catch (e: Exception) {
            configureViews(View.INVISIBLE, true)
            Timber.tag("Auth").e("getCredential failed with exception: %s", e.message.toString())
            activity?.showErrorAlert(
                "An error occurred while authenticating through saved credentials. Check logs for additional details"
            )
            return null
        }

        if (result.credential is PublicKeyCredential) {
            val cred = result.credential as PublicKeyCredential
            DataProvider.setSignedInThroughPasskeys(true)
            return "Passkey: ${cred.authenticationResponseJson}"
        }
        if (result.credential is PasswordCredential) {
            val cred = result.credential as PasswordCredential
            DataProvider.setSignedInThroughPasskeys(false)
            return "Got Password - User:${cred.id} Password: ${cred.password}"
        }
        return null
    }

    //navigation
    private fun navigateToHome() {
        DataProvider.configureSignedInPref(true)
        findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToHomeFragment())
    }
}