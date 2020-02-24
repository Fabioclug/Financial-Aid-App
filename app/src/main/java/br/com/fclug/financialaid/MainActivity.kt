package br.com.fclug.financialaid

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import br.com.fclug.financialaid.fragments.AccountsFragment
import br.com.fclug.financialaid.fragments.GroupsFragment
import br.com.fclug.financialaid.fragments.StatisticsFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.main_activity.*
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val sessionManager: SessionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        sessionManager.checkLogin()

        setSupportActionBar(toolbar)

        //supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        navigation_view.setNavigationItemSelectedListener(this)
        navigation_view.setCheckedItem(R.id.drawer_item_accounts)
        val fragment: Fragment = AccountsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit()
        setTitle(R.string.app_name)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu - this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId
        return if (id == R.id.action_settings) true else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        var fragment: Fragment = AccountsFragment()
        when (item.itemId) {
            R.id.drawer_item_accounts -> fragment = AccountsFragment()
            R.id.drawer_item_groups -> fragment = GroupsFragment()
            R.id.drawer_item_statistics -> fragment = StatisticsFragment()
            R.id.nav_send -> sessionManager.logoutUser()
            else -> fragment = AccountsFragment()
        }
        
        // Insert the fragment by replacing the content frame
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit()
        drawer_layout.closeDrawer(GravityCompat.START)
        
        return true
    }
}