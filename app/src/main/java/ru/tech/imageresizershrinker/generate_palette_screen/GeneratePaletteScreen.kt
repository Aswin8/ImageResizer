package ru.tech.imageresizershrinker.generate_palette_screen

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarttoolfactory.colordetector.ImageColorPalette
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.generate_palette_screen.viewModel.GeneratePaletteViewModel
import ru.tech.imageresizershrinker.main_screen.components.LocalAllowChangeColorByImage
import ru.tech.imageresizershrinker.main_screen.components.LocalBorderWidth
import ru.tech.imageresizershrinker.main_screen.components.Screen
import ru.tech.imageresizershrinker.main_screen.components.block
import ru.tech.imageresizershrinker.main_screen.components.drawHorizontalStroke
import ru.tech.imageresizershrinker.main_screen.components.fabBorder
import ru.tech.imageresizershrinker.main_screen.components.navBarsPaddingOnlyIfTheyAtTheBottom
import ru.tech.imageresizershrinker.pick_color_from_image_screen.copyColorIntoClipboard
import ru.tech.imageresizershrinker.pick_color_from_image_screen.format
import ru.tech.imageresizershrinker.resize_screen.components.ImageNotPickedWidget
import ru.tech.imageresizershrinker.resize_screen.components.LoadingDialog
import ru.tech.imageresizershrinker.theme.PaletteSwatch
import ru.tech.imageresizershrinker.utils.BitmapUtils.decodeBitmapFromUri
import ru.tech.imageresizershrinker.utils.LocalWindowSizeClass
import ru.tech.imageresizershrinker.widget.LocalToastHost
import ru.tech.imageresizershrinker.widget.Marquee

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneratePaletteScreen(
    uriState: Uri?,
    navController: NavController<Screen>,
    onGoBack: () -> Unit,
    pushNewUri: (Uri?) -> Unit,
    viewModel: GeneratePaletteViewModel = viewModel()
) {
    val context = LocalContext.current
    val toastHostState = LocalToastHost.current
    val scope = rememberCoroutineScope()
    val themeState = LocalDynamicThemeState.current
    val allowChangeColor = LocalAllowChangeColorByImage.current

    LaunchedEffect(uriState) {
        uriState?.let {
            viewModel.setUri(it)
            pushNewUri(null)
            context.decodeBitmapFromUri(
                uri = it,
                onGetMimeType = {},
                onGetExif = {},
                onGetBitmap = viewModel::updateBitmap,
                onError = {
                    scope.launch {
                        toastHostState.showToast(
                            context.getString(
                                R.string.smth_went_wrong,
                                it.localizedMessage ?: ""
                            ),
                            Icons.Rounded.ErrorOutline
                        )
                    }
                }
            )
        }
    }
    LaunchedEffect(viewModel.bitmap) {
        viewModel.bitmap?.let {
            if (allowChangeColor) themeState.updateColorByImage(it)
        }
    }

    val pickImageLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                viewModel.setUri(it)
                context.decodeBitmapFromUri(
                    uri = it,
                    onGetMimeType = {},
                    onGetExif = {},
                    onGetBitmap = viewModel::updateBitmap,
                    onError = {
                        scope.launch {
                            toastHostState.showToast(
                                context.getString(
                                    R.string.smth_went_wrong,
                                    it.localizedMessage ?: ""
                                ),
                                Icons.Rounded.ErrorOutline
                            )
                        }
                    }
                )
            }
        }

    val pickImage = {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
    val scrollState = rememberScrollState()

    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val noPalette: @Composable ColumnScope.() -> Unit = {
        Column(
            Modifier.block(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            FilledIconButton(
                enabled = false,
                onClick = {},
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(16.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface,
                )
            ) {
                Icon(
                    Icons.Rounded.PaletteSwatch,
                    null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                stringResource(R.string.no_palette),
                Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LargeTopAppBar(
                scrollBehavior = topAppBarScrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                modifier = Modifier.drawHorizontalStroke(),
                title = {
                    Marquee(
                        edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ) {
                        if (viewModel.bitmap == null) Text(stringResource(R.string.generate_palette))
                        else Text(stringResource(R.string.palette))
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onGoBack()
                        }
                    ) {
                        Icon(Icons.Rounded.ArrowBack, null)
                    }
                },
                actions = {
                    if (viewModel.uri != null) {
                        IconButton(
                            onClick = {
                                if (navController.backstack.entries.isNotEmpty()) navController.pop()
                                navController.navigate(Screen.PickColorFromImage)
                                pushNewUri(viewModel.uri)
                            }
                        ) {
                            Icon(Icons.Rounded.Colorize, null)
                        }
                    }
                }
            )

            AnimatedContent(targetState = viewModel.bitmap) { bitmap ->
                bitmap?.let { b ->
                    val bmp = remember(b) { b.asImageBitmap() }

                    if (LocalWindowSizeClass.current.widthSizeClass != WindowWidthSizeClass.Compact || LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Row {
                            Image(
                                bitmap = bmp,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(16.dp)
                                    .navBarsPaddingOnlyIfTheyAtTheBottom()
                                    .block(RoundedCornerShape(4.dp))
                                    .padding(4.dp),
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight
                            )
                            Column(
                                Modifier
                                    .weight(1f)
                                    .verticalScroll(scrollState)
                            ) {
                                ImageColorPalette(
                                    borderWidth = LocalBorderWidth.current,
                                    imageBitmap = bmp,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 72.dp)
                                        .padding(16.dp)
                                        .navigationBarsPadding()
                                        .block(RoundedCornerShape(24.dp))
                                        .padding(4.dp),
                                    onEmpty = { noPalette() },
                                    onColorChange = {
                                        context.copyColorIntoClipboard(
                                            context.getString(R.string.color),
                                            it.color.format()
                                        )
                                        scope.launch {
                                            toastHostState.showToast(
                                                icon = Icons.Rounded.ContentPaste,
                                                message = context.getString(R.string.color_copied)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Column(
                            Modifier
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                bitmap = bmp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .block(RoundedCornerShape(4.dp))
                                    .padding(4.dp),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth
                            )
                            ImageColorPalette(
                                borderWidth = LocalBorderWidth.current,
                                imageBitmap = bmp,
                                modifier = Modifier
                                    .padding(bottom = 72.dp)
                                    .padding(16.dp)
                                    .navigationBarsPadding()
                                    .block(RoundedCornerShape(24.dp))
                                    .padding(4.dp),
                                onEmpty = { noPalette() },
                                onColorChange = {
                                    context.copyColorIntoClipboard(
                                        context.getString(R.string.color),
                                        it.color.format()
                                    )
                                    scope.launch {
                                        toastHostState.showToast(
                                            icon = Icons.Rounded.ContentPaste,
                                            message = context.getString(R.string.color_copied)
                                        )
                                    }
                                }
                            )
                        }
                    }
                } ?: Column(Modifier.verticalScroll(scrollState)) {
                    ImageNotPickedWidget(
                        onPickImage = pickImage,
                        modifier = Modifier
                            .padding(bottom = 88.dp, top = 20.dp, start = 20.dp, end = 20.dp)
                            .navigationBarsPadding()
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = pickImage,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(12.dp)
                .align(Alignment.BottomEnd)
                .fabBorder(),
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
        ) {
            val expanded = scrollState.isScrollingUp()
            val horizontalPadding by animateDpAsState(targetValue = if (expanded) 16.dp else 0.dp)
            Row(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, null)
                AnimatedVisibility(visible = expanded) {
                    Row {
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.pick_image_alt))
                    }
                }
            }
        }
    }

    if (viewModel.isLoading) LoadingDialog()

    BackHandler {
        onGoBack()
    }
}

@Composable
fun ScrollState.isScrollingUp(): Boolean {
    var previousScrollOffset by remember(this) { mutableStateOf(value) }
    return remember(this) {
        derivedStateOf {
            (previousScrollOffset >= value).also {
                previousScrollOffset = value
            }
        }
    }.value
}