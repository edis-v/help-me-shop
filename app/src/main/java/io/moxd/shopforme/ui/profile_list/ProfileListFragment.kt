package io.moxd.shopforme.ui.profile_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.moxd.shopforme.MainActivity
import io.moxd.shopforme.R
import io.moxd.shopforme.requireAuthManager
import io.moxd.shopforme.requireUserManager
import io.moxd.shopforme.ui.profile.ProfileFragment
import io.moxd.shopforme.ui.profile.ProfileFragment2
import kotlinx.coroutines.launch

class ProfileListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.profile_list_fragment, container, false)
        root.findViewById<TextView>(R.id.profilelist_ausloggen).setOnClickListener{
            viewLifecycleOwner.lifecycleScope.launch {
                requireAuthManager().unauth()
              //  requireUserManager().sessionRevoked()
            }
            (requireActivity() as MainActivity).setupActionBarWithGraph(R.navigation.nav_graph_auth)
        }
        root.findViewById<TextView>(R.id.profilelist_profile).setOnClickListener{
            val ft = (requireActivity() as MainActivity).getSupportFragmentManager().beginTransaction()
            ft.replace(R.id.mainframe, ProfileFragment2())
            ft.commit()
        }
        return root
    }
}
