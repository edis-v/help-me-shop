package io.moxd.shopforme.ui.splashscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.moxd.shopforme.R
import io.moxd.shopforme.databinding.SplashscreenBinding
import io.moxd.shopforme.utils.requireAuthManager
import kotlinx.coroutines.delay


class SplashScreen : Fragment(R.layout.splashscreen) {

    lateinit var binding: SplashscreenBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SplashscreenBinding.bind(view)

        binding.apply {
            activity?.apply {
                packageManager.getPackageInfo(packageName, 0).versionName.let { versionName ->
                    appVersion.text = "Version ${versionName}"
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            delay(500)
            requireAuthManager().checkSessionAndConnectivity()
        }
    }
}