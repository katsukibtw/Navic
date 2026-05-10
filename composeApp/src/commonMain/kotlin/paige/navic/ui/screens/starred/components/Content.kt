package paige.navic.ui.screens.starred.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.paging.compose.LazyPagingItems
import org.jetbrains.compose.resources.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_see_all
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_songs
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainArtistListType
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongListType
import paige.navic.managers.DownloadManager
import paige.navic.ui.components.common.SongRow
import paige.navic.ui.components.layouts.ArtCarousel
import paige.navic.ui.components.layouts.ArtCarouselItem
import paige.navic.ui.components.sheets.CollectionSheet
import paige.navic.ui.screens.album.components.AlbumListScreenItem
import paige.navic.ui.screens.artist.ArtistsScreenItem
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarredScreenContent(
	scrollBehavior: TopAppBarScrollBehavior,
	innerPadding: PaddingValues,
	onSetShareId: (String) -> Unit,
	isOnline: Boolean = false,

	songsState: UiState<ImmutableList<DomainSong>>,
	selectedSong: DomainSong?,
	selectedSongIsStarred: Boolean,
	selectedSongRating: Int,
	allDownloads: List<DownloadEntity>,
	onSelectSong: (DomainSong) -> Unit,
	onClearSongSelection: () -> Unit,
	onAddSongStar: () -> Unit,
	onRemoveSongStar: () -> Unit,
	onPlaySongNext: (DomainSong) -> Unit,
	onAddSongToQueue: (DomainSong) -> Unit,
	onPlaySong: (DomainSong) -> Unit,
	onSetSongRating: (Int) -> Unit,
	onDownloadSong: (DomainSong) -> Unit,
	onCancelDownloadSong: (DomainSong) -> Unit,
	onDeleteDownloadSong: (DomainSong) -> Unit,

	// albums
	pagedAlbums: LazyPagingItems<DomainAlbum>,
	selectedAlbum: DomainAlbum?,
	selectedAlbumIsStarred: Boolean,
	selectedAlbumRating: Int,
	onSelectAlbum: (DomainAlbum) -> Unit,
	onClearAlbumSelection: () -> Unit,
	onStarSelectedAlbum: (Boolean) -> Unit,
	onRateSelectedAlbum: (Int) -> Unit,

	// artists
	artistsState: UiState<ImmutableList<DomainArtist>>,
	selectedArtist: DomainArtist?,
	selectedArtistIsStarred: Boolean,
	onSelectArtist: (DomainArtist) -> Unit,
	onClearArtistSelection: () -> Unit,
	onStarSelectedArtist: (Boolean) -> Unit,
	onPlayAlbumNext: () -> Unit,
	onAddAlbumToQueue: () -> Unit,
) {
	val gridState = rememberLazyGridState()
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val songs = songsState.data.orEmpty()
	val artists = artistsState.data.orEmpty()
	val downloadManager = koinInject<DownloadManager>()

	val scope = rememberCoroutineScope()

	val layoutDirection = LocalLayoutDirection.current
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	Column( 
		modifier = Modifier
			.fillMaxWidth()
			.padding(
				start = innerPadding.calculateStartPadding(
					layoutDirection
				)
			)
			.padding(
				end = innerPadding.calculateEndPadding(
					layoutDirection
				)
			),
		verticalArrangement = Arrangement.spacedBy(12.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Row(
			modifier = Modifier
				.heightIn(min = 32.dp)
				.padding(top = 8.dp)
				.padding(horizontal = 16.dp)
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				stringResource(Res.string.title_songs),
				style = MaterialTheme.typography.titleMediumEmphasized,
				fontWeight = FontWeight(600)
			)
			Text(
				stringResource(Res.string.action_see_all),
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.clickable(onClick = dropUnlessResumed {
					ctx.clickSound()
					backStack.add(
						Screen.SongList(
							nested = true,
							listType = DomainSongListType.Starred
						)
					)
				})
			)
		}
		LazyHorizontalGrid(
			rows = GridCells.Fixed(3),
			state = gridState,
			flingBehavior = rememberSnapFlingBehavior(lazyGridState = gridState),
			modifier = Modifier.fillMaxWidth().height(250.dp)
		) {
			itemsIndexed(songs) { index, song ->
				val download = allDownloads.find { it.songId == song.id }
				SongRow(
					modifier = Modifier.weight(1f),
					song = song,
					selected = selectedSong == song,
					onClick = { onPlaySong(song) },
					onLongClick = { onSelectSong(song) },
					onDismissRequest = { onClearSongSelection() },
					starredState = selectedSongIsStarred,
					onAddStar = onAddSongStar,
					onRemoveStar = onRemoveSongStar,
					download = download,
					onDownload = { onDownloadSong(song) },
					onCancelDownload = { onCancelDownloadSong(song) },
					onDeleteDownload = { onDeleteDownloadSong(song) },
					onPlayNext = { onPlaySongNext(song) },
					onAddToQueue = { onAddSongToQueue(song) },
					onShare = { onSetShareId(song.id) },
					isOnline = isOnline,
					rating = selectedSongRating,
					onSetRating = { onSetSongRating(it) }
				)
			}
		}
		ArtCarousel(
			stringResource(Res.string.title_albums),
			pagedAlbums.itemSnapshotList.items.toImmutableList()
		) { album ->
			val albumDownloadStatus by downloadManager
				.getCollectionDownloadStatus(album.songs.map { it.id })
				.collectAsState(initial = DownloadStatus.NOT_DOWNLOADED)
			ArtCarouselItem(
				coverArtId = album.coverArtId, 
				title = album.name, 
				contentDescription = null,
				onSelect = { onSelectAlbum(album) },
				onClick = dropUnlessResumed {
					backStack.add(Screen.CollectionDetail(album.id, "artist"))
				}
			)
			if (selectedAlbum == album) {
				CollectionSheet(
					onDismissRequest = { onClearAlbumSelection() },
					collection = album,
					starred = selectedAlbumIsStarred,
					onShare = { onSetShareId(album.id) },
					onPlayNext = onPlayAlbumNext,
					onAddToQueue = onAddAlbumToQueue,
					onSetStarred = { onStarSelectedAlbum(!selectedAlbumIsStarred) },
					onAddAllToPlaylist = { playlistDialogShown = true },
					downloadStatus = albumDownloadStatus,
					onDownloadAll = { 
						scope.launch {
							downloadManager.downloadCollection(album)
						}
					},
					onCancelDownloadAll = {
						scope.launch {
							album.songs.forEach { downloadManager.cancelDownload(it.id) }
						}
					},
					onDeleteDownloadAll = {
						scope.launch {
							downloadManager.deleteDownloadedCollection(album)
						}
					},
					rating = selectedAlbumRating,
					onSetRating = onRateSelectedAlbum
				)
			}
		}
		if (artists.isEmpty()) return@Column
		ArtCarousel(
			stringResource(Res.string.title_artists),
			artists.toImmutableList()
		) { artist ->
			ArtCarouselItem(
				coverArtId = artist.coverArtId, 
				title = artist.name, 
				contentDescription = null,
				onClick = dropUnlessResumed {
					backStack.add(Screen.ArtistDetail(artist.id))
				}
			)
		}
		Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
	}

	if (playlistDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			songs = selectedAlbum?.songs.orEmpty().toPersistentList(),
			onDismissRequest = { playlistDialogShown = false }
		)
	}
}
