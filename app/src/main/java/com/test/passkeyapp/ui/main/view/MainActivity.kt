package com.test.passkeyapp.ui.main.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.test.passkeyapp.BuildConfig
import com.test.passkeyapp.R
import com.test.passkeyapp.data.DataProvider
import com.test.passkeyapp.databinding.ActivityMainBinding
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        initUi()
        subscribeUi()
    }

    private fun initUi() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.lifecycleOwner = this
        DataProvider.initSharedPref(applicationContext)
    }

    private fun subscribeUi() {
    }
}
