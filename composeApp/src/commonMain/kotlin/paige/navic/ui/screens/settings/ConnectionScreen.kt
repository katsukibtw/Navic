package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_auto_offline
import navic.composeapp.generated.resources.option_manual_offline
import navic.composeapp.generated.resources.subtitle_auto_offline
import navic.composeapp.generated.resources.subtitle_manual_offline
import navic.composeapp.generated.resources.title_offline_mode
import navic.composeapp.generated.resources.title_connection
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingSwitchRow
import paige.navic.utils.fadeFromTop

@Composable
fun SettingsConnectionScreen() {
	val ctx = LocalCtx.current

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_connection)) },
				hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
			)
		}
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(top = 16.dp, end = 16.dp, start = 16.dp)
					.fadeFromTop()
			) {
				FormTitle(stringResource(Res.string.title_offline_mode))
				Form {
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_manual_offline)) },
						subtitle = { Text(stringResource(Res.string.subtitle_manual_offline)) },
						value = Settings.shared.manualOffline,
						onSetValue = { Settings.shared.manualOffline = it }
					)
					if (!listOf("ipados", "ios").contains(ctx.name.lowercase())) {
						SettingSwitchRow(
							title = { Text(stringResource(Res.string.option_auto_offline)) },
							subtitle = { Text(stringResource(Res.string.subtitle_auto_offline)) },
							value = Settings.shared.autoOffline,
							onSetValue = { Settings.shared.autoOffline = it }
						)
					}
				}
			}
		}
	}
}
