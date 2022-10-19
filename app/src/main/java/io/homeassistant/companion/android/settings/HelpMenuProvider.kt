package io.homeassistant.companion.android.settings

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import io.homeassistant.companion.android.R

/**
 * Adds a "Get Help" menu option that opens the given [helpLink] in the browser.
 */
class HelpMenuProvider(private val helpLink: Uri) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_help, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        menu.findItem(R.id.get_help).apply {
            intent = Intent(ACTION_VIEW, helpLink)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = menuItem.itemId == R.id.get_help
}

/**
 * Wrapper around [MenuHost.addMenuProvider] to attach a [HelpMenuProvider] to a fragment's parent activity.
 */
fun Fragment.addHelpMenuProvider(helpLink: Uri) {
    val menuHost: MenuHost = requireActivity()
    menuHost.addMenuProvider(HelpMenuProvider(helpLink), viewLifecycleOwner, Lifecycle.State.RESUMED)
}
