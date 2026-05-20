package paige.navic.ui.screens.song.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongListType
import paige.navic.domain.repositories.SongRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager
import paige.navic.shared.Logger
import kotlin.time.Clock

class SongListViewModel(
	initialListType: DomainSongListType = DomainSongListType.FrequentlyPlayed,
	artistId: String? = null,
	private val repository: SongRepository,
	private val downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager
) : ViewModel() {
	private val _selectedSorting = MutableStateFlow(initialListType)
	val selectedSorting = _selectedSorting.asStateFlow()

	private val _refreshTrigger = MutableStateFlow(Clock.System.now())

	val songsPaging: Flow<PagingData<DomainSong>> = combine(
		_selectedSorting,
		flowOf(artistId),
		_refreshTrigger
	) { type, artistId, _ ->
		type to artistId
	}.flatMapLatest { (type, artistId) ->
		repository.getSongsPaging(type, artistId)
	}.cachedIn(viewModelScope)

	val allDownloads = downloadManager.allDownloads
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = emptyList()
		)

	private val _selectedSong = MutableStateFlow<DomainSong?>(null)
	val selectedSong = _selectedSong.asStateFlow()

	private val _starred = MutableStateFlow(false)
	val starred = _starred.asStateFlow()

	private val _selectedSongRating = MutableStateFlow(0)
	val selectedSongRating = _selectedSongRating.asStateFlow()

	private val _selectedReversed = MutableStateFlow(false)
	val selectedReversed = _selectedReversed.asStateFlow()

	val isOnline = connectivityManager.isOnline

	init {
		Logger.i("SongListViewModel", "Initialized for artist: $artistId")
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect { if (it) refreshSongs(false) }
		}
	}

	fun selectSong(song: DomainSong) {
		viewModelScope.launch {
			_selectedSong.value = song
			_starred.value = repository.isSongStarred(song)
			_selectedSongRating.value = repository.getSongRating(song)
		}
	}

	fun clearSelection() {
		_selectedSong.value = null
	}

	fun refreshSongs(fullRefresh: Boolean) {
		_refreshTrigger.value = Clock.System.now()

		if (!fullRefresh) return
		viewModelScope.launch {
			repository.syncLibrarySongs()
		}
	}

	fun starSong(starred: Boolean) {
		viewModelScope.launch {
			val selection = _selectedSong.value ?: return@launch
			runCatching {
				if (starred) {
					repository.starSong(selection)
				} else {
					repository.unstarSong(selection)
				}
				_starred.value = starred
				refreshSongs(true)
			}
		}
	}

	fun rateSelectedSong(rating: Int) {
		viewModelScope.launch {
			val selection = _selectedSong.value ?: return@launch
			runCatching {
				repository.rateSong(selection, rating)
				_selectedSongRating.value = rating
			}
		}
	}

	fun setSorting(sorting: DomainSongListType) {
		_selectedSorting.value = sorting
	}

	fun setReversed(reversed: Boolean) {
		_selectedReversed.value = reversed
	}

	fun downloadSong(song: DomainSong) {
		downloadManager.downloadSong(song)
	}

	fun cancelDownload(songId: String) {
		downloadManager.cancelDownload(songId)
	}

	fun deleteDownload(songId: String) {
		downloadManager.deleteDownload(songId)
	}
}
