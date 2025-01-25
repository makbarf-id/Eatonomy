package id.makbarf.eatonomy

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.core.content.ContextCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import id.makbarf.eatonomy.data.FoodDatabase
import id.makbarf.eatonomy.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var scrimView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database in a coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = FoodDatabase.getDatabase(applicationContext)
                val cursor = db.query("PRAGMA user_version", null)
                cursor.use {
                    if (it.moveToFirst()) {
                        val version = it.getInt(0)
                        Log.d("Database", "Current database version: $version")
                    }
                }
            } catch (e: Exception) {
                Log.e("Database", "Error checking version", e)
                withContext(Dispatchers.Main) {
                    // Show error to user if needed
                    Snackbar.make(
                        binding.root,
                        "Error initializing database. Retrying...",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                // If database error occurs, delete and recreate
                applicationContext.deleteDatabase("food_database")
                // Retry initialization
                FoodDatabase.getDatabase(applicationContext)
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Hide the FAB but keep the code for future use
        binding.appBarMain.fab.visibility = View.GONE
        
        // Keep the click listener for future use
        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_household_members
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Add scrim view
        scrimView = View(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.drawer_scrim)
            alpha = 0f
            visibility = View.GONE
            (binding.root as ViewGroup).addView(this, 1) // Add above content, below drawer
        }

        // Setup drawer listener
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                // Only animate scrim
                scrimView?.apply {
                    visibility = View.VISIBLE
                    alpha = slideOffset * 0.6f // Max 60% opacity
                }
                // Remove all animations on main content
                binding.appBarMain.root.apply {
                    translationX = 0f // Remove sliding animation
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                scrimView?.visibility = View.GONE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}