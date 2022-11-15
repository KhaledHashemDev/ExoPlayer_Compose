package com.example.exoplayer_compose

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel //to be able to inject dependencies to the view model
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle, // an instance of saved state handle used to survive process death, it contains a bundle
    val player: Player,
    private val metaDataReader: MetaDataReader
): ViewModel() {

    private val videoUris = savedStateHandle.getStateFlow("videoUris", emptyList<Uri>())

    val videoItems = videoUris.map { uris ->
        uris.map{ uri ->
            VideoItem(
            contentUri = uri,
            mediaItem = MediaItem.fromUri(uri),
            name = metaDataReader.getMetaDataFromUri(uri)?.fileName ?:"No name"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init{
        player.prepare()
    }

    fun addVideoUri(uri: Uri) {
        savedStateHandle["videoUris"] = videoUris.value + uri
        player.addMediaItem(MediaItem.fromUri(uri))
    }

    fun playVideo(uri: Uri) {
        player.setMediaItem(
            videoItems.value.find {it.contentUri == uri }?.mediaItem ?: return
        )
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}