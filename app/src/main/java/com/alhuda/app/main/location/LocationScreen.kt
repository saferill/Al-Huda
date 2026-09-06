package com.alhuda.app.main.location

import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.geo.CityGeoInfo
import com.alhuda.app.core.domain.model.geo.CountryGeoInfo
import com.alhuda.app.core.presentation.AlHudaTheme
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.navigation.NavigationController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    uiState: LocationUiState,
    onAction: (LocationUiAction) -> Unit,
    getCountries: suspend () -> List<CountryGeoInfo>,
    getCities: suspend (countryCode: String) -> List<CityGeoInfo>,
    modifier: Modifier = Modifier,
) {
    val pageScrollState = rememberScrollState()
    ScreenScaffold(
        title = stringResource(R.string.location_title),
        onBackClick = { NavigationController.navigateBack() },
        modifier = modifier,
        scrollState = pageScrollState,
        floatingActionButton = {
            if (uiState.locations.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        onAction(LocationUiAction.OnNewLocationClick)
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        painterResource(R.drawable.add),
                        contentDescription = stringResource(R.string.add_new_location_button),
                    )
                }
            }
        },
    ) {
        LocationScreenContent(uiState, onAction, getCountries, getCities, pageScrollState = pageScrollState)
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF00585A,
)
@Composable
private fun LocationScreenPreview() {
    AlHudaTheme {
        LocationScreen(
            uiState = LocationUiState(),
            onAction = {},
            getCities = { emptyList() },
            getCountries = { emptyList() },
        )
    }
}
