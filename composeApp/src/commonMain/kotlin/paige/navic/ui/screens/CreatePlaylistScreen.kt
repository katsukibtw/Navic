package paige.navic.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zt64.subsonic.api.model.Song
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_playlist_name
import navic.composeapp.generated.resources.title_create_playlist
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.ui.components.common.FormButton
import paige.navic.ui.components.dialogs.FormDialog
import paige.navic.ui.viewmodels.CreatePlaylistViewModel
import paige.navic.utils.UiState

@Composable
fun CreatePlaylistScreen(
	tracks: List<Song>,
	viewModel: CreatePlaylistViewModel = viewModel(key = tracks.joinToString()) { CreatePlaylistViewModel(tracks) }
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val state by viewModel.creationState.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				is CreatePlaylistViewModel.Event.Dismiss -> {
					if (backStack.lastOrNull() is Screen.CreatePlaylist) {
						backStack.removeLastOrNull()
						backStack.add(Screen.Tracks(event.playlist, "playlists"))
					}
				}
			}
		}
	}

	FormDialog(
		onDismissRequest = {},
		icon = { Icon(Icons.Outlined.PlaylistAdd, null) },
		title = { Text(stringResource(Res.string.title_create_playlist)) },
		buttons = {
			FormButton(
				onClick = {
					ctx.clickSound()
					viewModel.create()
				},
				enabled = state !is UiState.Loading && viewModel.name.text.isNotBlank(),
				color = MaterialTheme.colorScheme.primary
			) {
				if (state !is UiState.Loading) {
					Text(stringResource(Res.string.action_ok))
				} else {
					CircularProgressIndicator(
						modifier = Modifier.size(20.dp)
					)
				}
			}
			FormButton(
				onClick = {
					ctx.clickSound()
					if (backStack.lastOrNull() is Screen.CreatePlaylist) {
						backStack.removeLastOrNull()
					}
				},
				enabled = state !is UiState.Loading,
				content = { Text(stringResource(Res.string.action_cancel)) }
			)
		},
		content = {
			(state as? UiState.Error)?.error?.let {
				SelectionContainer {
					Text("$it")
				}
			}
			TextField(
				state = viewModel.name,
				label = { Text(stringResource(Res.string.option_playlist_name)) },
				lineLimits = TextFieldLineLimits.SingleLine
			)
		}
	)
}