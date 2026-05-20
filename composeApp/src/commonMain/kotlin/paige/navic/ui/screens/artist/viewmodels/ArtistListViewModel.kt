package paige.navic.ui.screens.artist.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainArtistListType
import paige.navic.domain.repositories.ArtistRepository
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.utils.UiState

class ArtistListViewModel(
	initialListType: DomainArtistListType = DomainArtistListType.AlphabeticalByName,
	private val repository: ArtistRepository,
	private val albumDao: AlbumDao
) : ViewModel() {
	private val _listType = MutableStateFlow(DomainArtistListType.AlphabeticalByName)
	val listType = _listType.asStateFlow()

	@OptIn(ExperimentalCoroutinesApi::class)
	val artistsPaging: Flow<PagingData<DomainArtist>> = _listType
		.flatMapLatest { repository.getArtistsPaging(it) }
		.cachedIn(viewModelScope)

	@OptIn(ExperimentalCoroutinesApi::class)
	val totalArtistsCount: StateFlow<Int> = _listType
		.flatMapLatest { repository.getArtistsCount(it) }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = 0
		)

	private val _starred = MutableStateFlow(false)
	val starred = _starred.asStateFlow()

	private val _selectedArtist = MutableStateFlow<DomainArtist?>(null)
	val selectedArtist = _selectedArtist.asStateFlow()

	private val _selectedArtistAlbums = MutableStateFlow<List<DomainAlbum>?>(null)
	val selectedArtistAlbums = _selectedArtistAlbums.asStateFlow()

	private val _listType = MutableStateFlow(initialListType)
	val listType = _listType.asStateFlow()

	val gridState = LazyGridState()

	fun refreshArtists() {
		viewModelScope.launch {
			repository.syncArtists()
		}
	}

	fun selectArtist(artist: DomainArtist) {
		viewModelScope.launch {
			_selectedArtist.value = artist
			val artistAlbums = 
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			_selectedArtistAlbums.value = artistAlbums.map { it.toDomainModel() }
			_starred.value = repository.isArtistStarred(artist)
		}
	}

	fun clearSelection() {
		_selectedArtist.value = null
	}

	fun starArtist(starred: Boolean) {
		val artist = _selectedArtist.value ?: return
		viewModelScope.launch {
			runCatching {
				if (starred) {
					repository.starArtist(artist)
				} else {
					repository.unstarArtist(artist)
				}
				_starred.value = starred
			}
		}
	}

	fun addArtistAlbumsToQueue(player: MediaPlayerViewModel) {
		val artist = _selectedArtist.value ?: return
		viewModelScope.launch {
			val artistAlbums = 
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			artistAlbums.map { it.toDomainModel() }.forEach { album ->
				player.addToQueue(album)
			}
		}
	}

	fun playArtistAlbumsNext(player: MediaPlayerViewModel) {
		val artist = _selectedArtist.value ?: return
		viewModelScope.launch {
			val artistAlbums = 
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			artistAlbums.map { it.toDomainModel() }.forEach { album ->
				player.playNext(album)
			}
		}
	}

	fun setListType(listType: DomainArtistListType) {
		_listType.value = listType
	}

	fun clearError() {
		_artistsState.value = UiState.Success(_artistsState.value.data ?: persistentListOf())
	}
}
