package com.bookmedia.gallery

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.bookmedia.gallery.data.ProfilePreferences
import com.bookmedia.gallery.ui.GalleryColorScheme
import com.bookmedia.gallery.ui.GalleryScreen
import com.bookmedia.gallery.ui.GalleryTypography
import com.bookmedia.gallery.ui.GalleryViewModel
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {

    private val viewModel: GalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val profilePrefs = ProfilePreferences(applicationContext)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val token = profilePrefs.getActiveProfile().token.trim()
                val req = if (token.isNotBlank()) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }
                chain.proceed(req)
            }
            .build()

        val imageLoader = ImageLoader.Builder(applicationContext)
            .okHttpClient(okHttpClient)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)

        handleIntent(intent)

        setContent {
            MaterialTheme(
                colorScheme = GalleryColorScheme,
                typography = GalleryTypography
            ) {
                GalleryScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                viewModel.handleSharedText(sharedText) { message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
