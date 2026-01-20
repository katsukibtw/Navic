package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.count_songs
import navic.composeapp.generated.resources.playlist_remove
import navic.composeapp.generated.resources.share
import org.jetbrains.compose.resources.pluralStringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.Tracks
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.DropdownItem
import paige.navic.ui.component.common.RefreshBox
import paige.navic.ui.component.dialog.DeletionDialog
import paige.navic.ui.component.dialog.DeletionEndpoint
import paige.navic.ui.component.dialog.ShareDialog
import paige.navic.ui.component.layout.ArtGrid
import paige.navic.ui.component.layout.ArtGridItem
import paige.navic.ui.component.layout.artGridError
import paige.navic.ui.component.layout.artGridPlaceholder
import paige.navic.ui.viewmodel.PlaylistsViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.Playlist
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
	viewModel: PlaylistsViewModel = viewModel { PlaylistsViewModel() }
) {
	val playlistsState by viewModel.playlistsState.collectAsState()
	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var deletionId by remember { mutableStateOf<String?>(null) }

	RefreshBox(
		modifier = Modifier.background(MaterialTheme.colorScheme.surface),
		isRefreshing = playlistsState is UiState.Loading,
		onRefresh = { viewModel.refreshPlaylists() }
	) { topPadding ->
		AnimatedContent(playlistsState, Modifier.padding(top = topPadding)) {
			ArtGrid {
				when (it) {
					is UiState.Loading -> artGridPlaceholder()
					is UiState.Error -> artGridError(it)
					is UiState.Success -> {
						items(it.data, { it.id }) { playlist ->
							PlaylistsScreenItem(
								modifier = Modifier.animateItem(),
								playlist = playlist,
								viewModel = viewModel,
								onSetShareId = { newShareId ->
									shareId = newShareId
								},
								onSetDeletionId = { newDeletionId ->
									deletionId = newDeletionId
								}
							)
						}
					}
				}
			}
		}
	}

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)

	@Suppress("AssignedValueIsNeverRead")
	DeletionDialog(
		endpoint = DeletionEndpoint.PLAYLIST,
		id = deletionId,
		onIdClear = { deletionId = null }
	)
}

@Composable
fun PlaylistsScreenItem(
	modifier: Modifier = Modifier,
	playlist: Playlist,
	viewModel: PlaylistsViewModel,
	onSetShareId: (String) -> Unit,
	onSetDeletionId: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val selection by viewModel.selectedPlaylist.collectAsState()
	Box(modifier) {
		ArtGridItem(
			imageModifier = Modifier.combinedClickable(
				onClick = {
					ctx.clickSound()
					backStack.add(Tracks(playlist))
				},
				onLongClick = {
					viewModel.selectPlaylist(playlist)
				}
			),
			imageUrl = playlist.coverArt,
			title = playlist.name,
			subtitle = buildString {
				append(
					pluralStringResource(
						Res.plurals.count_songs,
						playlist.songCount,
						playlist.songCount
					) + "\n"
				)
				playlist.comment?.let {
					append("${playlist.comment}\n")
				}
			}
		)
		Dropdown(
			expanded = selection == playlist,
			onDismissRequest = {
				viewModel.clearSelection()
			}
		) {
			DropdownItem(
				text = Res.string.action_share,
				leadingIcon = Res.drawable.share,
				onClick = {
					onSetShareId(playlist.id)
					viewModel.clearSelection()
				},
			)
			DropdownItem(
				text = Res.string.action_delete,
				leadingIcon = Res.drawable.playlist_remove,
				onClick = {
					onSetDeletionId(playlist.id)
					viewModel.clearSelection()
				}
			)
		}
	}
}
