package paige.navic.ui.screens.track.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zt64.subsonic.api.model.Playlist
import dev.zt64.subsonic.api.model.Song
import dev.zt64.subsonic.api.model.SongCollection
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_another_playlist
import navic.composeapp.generated.resources.action_add_to_playlist
import navic.composeapp.generated.resources.action_remove_from_playlist
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.action_track_info
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Info
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.utils.UiState

@Composable
fun TrackRowDropdown(
	expanded: Boolean,
	onDismissRequest: () -> Unit,
	onRemoveStar: () -> Unit,
	onAddStar: () -> Unit,
	onShare: () -> Unit,
	tracks: SongCollection,
	track: Song,
	onRemoveFromPlaylist: () -> Unit,
	starredState: UiState<Boolean>,
) {
	val backStack = LocalNavStack.current
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	Dropdown(
		expanded = expanded,
		onDismissRequest = onDismissRequest
	) {
		DropdownItem(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
			text = { Text(stringResource(Res.string.action_share)) },
			leadingIcon = { Icon(Icons.Outlined.Share, null) },
			onClick = {
				onShare()
				onDismissRequest()
			},
		)
		val starred =
			(starredState as? UiState.Success)?.data
		DropdownItem(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
			text = {
				Text(
					stringResource(
						if (starred == true)
							Res.string.action_remove_star
						else Res.string.action_star
					)
				)
			},
			leadingIcon = {
				Icon(
					if (starred == true)
						Icons.Filled.Star
					else Icons.Outlined.Star,
					null
				)
			},
			onClick = {
				if (starred == true)
					onRemoveStar()
				else onAddStar()
				onDismissRequest()
			},
			enabled = starred != null
		)
		DropdownItem(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
			text = { Text(stringResource(Res.string.action_track_info)) },
			leadingIcon = { Icon(Icons.Outlined.Info, null) },
			onClick = {
				backStack.add(Screen.TrackDetail(track))
				onDismissRequest()
			},
		)
		DropdownItem(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
			text = {
				Text(
					stringResource(
						if (tracks is Playlist)
							Res.string.action_add_to_another_playlist
						else Res.string.action_add_to_playlist
					)
				)
			},
			leadingIcon = {
				Icon(
					Icons.Outlined.PlaylistAdd,
					null
				)
			},
			onClick = {
				onDismissRequest()
				playlistDialogShown = true
			},
		)
		if (tracks is Playlist) {
			DropdownItem(
				containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
				text = { Text(stringResource(Res.string.action_remove_from_playlist)) },
				leadingIcon = {
					Icon(
						Icons.Outlined.PlaylistRemove,
						null
					)
				},
				onClick = {
					onRemoveFromPlaylist()
				},
			)
		}
	}

	if (playlistDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			tracks = listOf(track),
			playlistToExclude = if (tracks is Playlist)
				tracks.id
			else null,
			onDismissRequest = { playlistDialogShown = false }
		)
	}
}