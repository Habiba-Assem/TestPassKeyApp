package com.test.passkeyapp.ui.signUp.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePasswordResponse
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialCustomException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.test.passkeyapp.data.DataProvider
import com.test.passkeyapp.databinding.FragmentSignUpBinding
import com.test.passkeyapp.utilities.observe
import com.test.passkeyapp.utilities.readFromAsset
import com.test.passkeyapp.utilities.showErrorAlert
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.SecureRandom

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding

    private lateinit var credentialManager: CredentialManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
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
        observe(binding.signUpWithPasswordBtn, ::signUpWithPassword)
        observe(binding.signUpWithPasskeyBtn, ::signUpWithPasskeys)
        observe(binding.backBtn, findNavController()::navigateUp)
    }

    private fun signUpWithPassword() {
        binding.passwordEt.visibility = View.VISIBLE

        if (binding.usernameEt.text.isNullOrEmpty()) {
            binding.usernameEt.error = "User name required"
            binding.usernameEt.requestFocus()
        } else if (binding.passwordEt.text.isNullOrEmpty()) {
            binding.passwordEt.error = "Password required"
            binding.passwordEt.requestFocus()
        } else {
            lifecycleScope.launch {
                configureViews(View.VISIBLE, false)
                //Save the user credential password with their password provider
                createPassword()
                simulateServerDelayAndLogIn()
            }
        }
    }

    private fun simulateServerDelayAndLogIn() {
        Handler(Looper.getMainLooper()).postDelayed({
            DataProvider.setSignedInThroughPasskeys(false)
            configureViews(View.INVISIBLE, true)
            navigateToHome()
        }, 2000)
    }

    private fun signUpWithPasskeys() {
        binding.passwordEt.visibility = View.GONE

        if (binding.usernameEt.text.isNullOrEmpty()) {
            binding.usernameEt.error = "User name required"
            binding.usernameEt.requestFocus()
        } else {
            lifecycleScope.launch {
                configureViews(View.VISIBLE, false)

                //Call createPasskey() to sign up with passkey
                val data = createPasskey()
                configureViews(View.INVISIBLE, true)

                //complete the registration process after sending public key credential to your server and let the user in
                data?.let {
                    registerResponse()
                    DataProvider.setSignedInThroughPasskeys(true)
                    navigateToHome()
                }
            }
        }
    }

    private fun fetchRegistrationJsonFromServer(): String {
        //fetch registration mock response
        val response = requireContext().readFromAsset("RegFromServer")

        //Update userId,challenge, name and Display name in the mock
        return response.replace("<userId>", getEncodedUserId())
            .replace("<userName>", binding.usernameEt.text.toString())
            .replace("<userDisplayName>", binding.usernameEt.text.toString())
            .replace("<challenge>", getEncodedChallenge())
    }

    private fun getEncodedUserId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(64)
        random.nextBytes(bytes)
        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
    }

    private fun getEncodedChallenge(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
    }

    private suspend fun createPassword() {
        //CreatePasswordRequest with entered username and password
        val request = CreatePasswordRequest(
            binding.usernameEt.text.toString(),
            binding.passwordEt.text.toString()
        )
        //Create credential with created password request
        try {
            credentialManager.createCredential(requireActivity(), request) as CreatePasswordResponse
        } catch (e: Exception) {
            Timber.tag("Auth").e(" Exception Message : %s", e.message)
        }
    }

    private suspend fun createPasskey(): CreatePublicKeyCredentialResponse? {
        var response: CreatePublicKeyCredentialResponse? = null

        //create a CreatePublicKeyCredentialRequest() with necessary registration json from server
        val request = CreatePublicKeyCredentialRequest(fetchRegistrationJsonFromServer())

        //call createCredential() with createPublicKeyCredentialRequest
        try {
            response = credentialManager.createCredential(
                requireActivity(),
                request
            ) as CreatePublicKeyCredentialResponse
        } catch (e: CreateCredentialException) {
            configureProgress(View.INVISIBLE)
            handlePasskeyFailure(e)
        }
        return response
    }

    private fun configureViews(visibility: Int, flag: Boolean) {
        configureProgress(visibility)
        binding.signUpWithPasskeyBtn.isEnabled = flag
        binding.signUpWithPasswordBtn.isEnabled = flag
    }

    private fun configureProgress(visibility: Int) {
        binding.textProgress.visibility = visibility
        binding.circularProgressIndicator.visibility = visibility
    }

    // These are types of errors that can occur during passkey creation.
    private fun handlePasskeyFailure(e: CreateCredentialException) {
        val msg = when (e) {
            is CreatePublicKeyCredentialDomException -> {
                // Handle the passkey DOM errors thrown according to the
                // WebAuthn spec using e.domError
                "An error occurred while creating a passkey, please check logs for additional details."
            }

            is CreateCredentialCancellationException -> {
                // The user intentionally canceled the operation and chose not
                // to register the credential.
                "The user intentionally canceled the operation and chose not to register the credential. Check logs for additional details."
            }

            is CreateCredentialInterruptedException -> {
                // Retry-able error. Consider retrying the call.
                "The operation was interrupted, please retry the call. Check logs for additional details."
            }

            is CreateCredentialProviderConfigurationException -> {
                // Your app is missing the provider configuration dependency.
                // Most likely, you're missing "credentials-play-services-auth".
                "Your app is missing the provider configuration dependency. Check logs for additional details."
            }

            is CreateCredentialUnknownException -> {
                "An unknown error occurred while creating passkey. Check logs for additional details."
            }

            is CreateCredentialCustomException -> {
                // You have encountered an error from a 3rd-party SDK. If you
                // make the API call with a request object that's a subclass of
                // CreateCustomCredentialRequest using a 3rd-party SDK, then you
                // should check for any custom exception type constants within
                // that SDK to match with e.type. Otherwise, drop or log the
                // exception.
                "An unknown error occurred from a 3rd party SDK. Check logs for additional details."
            }

            else -> {
                Timber.tag("Auth").w("Unexpected exception type ${e::class.java.name}")
                "An unknown error occurred."
            }
        }
        Timber.tag("Auth").e("createPasskey failed with exception: %s", e.message.toString())
        activity?.showErrorAlert(msg)
    }

    private fun registerResponse(): Boolean {
        return true
    }

    //navigation
    private fun navigateToHome() {
        DataProvider.configureSignedInPref(true)
        findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToHomeFragment())
    }
}