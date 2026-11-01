package com.bookmedia.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.bookmedia.gallery.ui.GalleryColorScheme
import com.bookmedia.gallery.ui.GalleryScreen
import com.bookmedia.gallery.ui.GalleryTypography
import com.bookmedia.gallery.ui.GalleryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(
                colorScheme = GalleryColorScheme,
                typography = GalleryTypography
            ) {
                GalleryScreen(viewModel = viewModel)
            }
        }
    }
}
