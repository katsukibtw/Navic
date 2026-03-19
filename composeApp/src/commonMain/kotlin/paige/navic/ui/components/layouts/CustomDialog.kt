package paige.navic.ui.components.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomDialog(
	title: @Composable RowScope.() -> Unit = {},
	subtitle: @Composable () -> Unit = {},
	buttons: @Composable RowScope.() -> Unit = {},
	content: @Composable ColumnScope.() -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.wrapContentHeight(),
		shape = MaterialTheme.shapes.extraExtraLarge,
		color = MaterialTheme.colorScheme.surfaceContainerHigh
	) {
		Column(
			modifier = Modifier
				.padding(24.dp)
				.fillMaxWidth()
		) {
			CompositionLocalProvider(
				LocalTextStyle provides MaterialTheme.typography.headlineMedium
			) {
				Row(modifier = Modifier.fillMaxWidth()) {
					title()
				}
			}
			subtitle()

			Spacer(modifier = Modifier.height(16.dp))

			Column(
				modifier = Modifier
					.weight(1f, fill = false)
					.fillMaxWidth()
			) {
				content()
			}

			Spacer(modifier = Modifier.height(24.dp))

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
				verticalAlignment = Alignment.CenterVertically
			) {
				buttons()
			}
		}
	}
}