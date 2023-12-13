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
        DataProvider.initSharedPref(applicationContext)
        initUi()
        subscribeUi()
    }

    private fun initUi() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val graphInflater = navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_main)
        val destination = if (DataProvider.isSignedIn()) R.id.homeFragment else R.id.startUpScreenFragment
        navGraph.setStartDestination(destination)
        navController.graph = navGraph

        binding.lifecycleOwner = this
    }

    private fun subscribeUi() {
    }
}
