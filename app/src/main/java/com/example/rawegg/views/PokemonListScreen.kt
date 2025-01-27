package com.example.rawegg.views

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.rawegg.R
import com.example.rawegg.models.PokedexListEntry
import com.example.rawegg.ui.theme.RobotoCondensed
import com.example.rawegg.viewModels.PokemonListViewModel
import timber.log.Timber


@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_international_pok_mon_logo),
                contentDescription = "Pokemon",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
            SearchBar(
                hint = "Search...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                //Log.d(TAG, "안녕 ${it}")
                viewModel.searchPokemonList(it)
            }
            Spacer(modifier = Modifier.height(16.dp))
            PokemonList(navController = navController)
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    onSearch: (String) -> Unit = {}
) {
    var text by remember { mutableStateOf("") }
    var isHintDisplayed by remember { mutableStateOf(hint != "") }

    Box(modifier = modifier) {
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onSearch(it)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, CircleShape)
                .background(Color.White, CircleShape)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .onFocusChanged {
                    isHintDisplayed = (!it.isFocused && text.isEmpty())
                }
        )
        if(isHintDisplayed) {
            Text(
                text = hint,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}


@Composable
fun PokemonList(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val pokemonList by remember { viewModel.pokemonList }
    val endReached by remember { viewModel.endReached }
    val loadError by remember { viewModel.loadError }
    val isLoading by remember { viewModel.isLoading }
    val isSearching by remember { viewModel.isSearching }

    LazyColumn(
        contentPadding = PaddingValues(16.dp)
    ) {
        val itemCount = if(pokemonList.size % 2 == 0) {
                            pokemonList.size / 2
                        } else {
                            pokemonList.size / 2 + 1
                        }
        items(itemCount) {
            if(it >= itemCount - 1 &&
               !endReached &&
               !isLoading && !isSearching) {
                viewModel.loadPokemonPaginated()
            }
            PokedexRow(
                rowIndex = it,
                entries = pokemonList,
                navController = navController
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if(isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colors.primary
            )
        }
        if(loadError.isNotEmpty()) {
            RetrySection(error = loadError) {
                viewModel.loadPokemonPaginated()
            }
        }
    }
}


@Composable
fun PokedexRow(
    rowIndex: Int,
    entries: List<PokedexListEntry>,
    navController: NavController
) {
    Column {
        Row {
            PokedexEntry(
                entry = entries[rowIndex * 2],
                navController = navController,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            if(entries.size >= rowIndex * 2 + 2) {
                PokedexEntry(
                    entry = entries[rowIndex * 2 + 1],
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/*
@Composable
fun PokedexEntryTmp(
    entry: PokedexListEntry,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val defaultDominantColor = MaterialTheme.colors.surface
    var dominantColor by remember { mutableStateOf(defaultDominantColor) }
    val context = LocalContext.current

    val imageRequest = ImageRequest
        .Builder(context)
        .data(entry.imageUrl)
        .target {
            viewModel.calcDominantColor(it) { color ->
                //Toast.makeText(context, "dominantColor....", Toast.LENGTH_SHORT).show()
                Log.d(TAG,"dominantColor::Coil...")

                dominantColor = color
            }
        }
        .target(
            onStart = {
                // Handle the placeholder drawable.
                CircularProgressIndicator(
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.scale(0.5f)
                )
            },
            onSuccess = { result ->
                // Handle the successful result.
                viewModel.calcDominantColor(result) { color ->
                    dominantColor = color
                }
            },
            onError = {
                // Handle the error drawable.
                R.drawable.photo_architecture
            }
        )
        //.placeholder(R.drawable.photo_architecture)
        .build()

    val painter = rememberCoilPainter(
        request = imageRequest,
        fadeIn = true
    )

    Timber.tag(TAG).d("dominantColor::Coil...start...")
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominantColor,
                        defaultDominantColor
                    )
                )
            )
            .clickable {
                Timber.tag(TAG).d("dominantColor::Coil...Clickable...")
                navController.navigate(
                    "pokemon_detail_screen/${dominantColor.toArgb()}/${entry.pokemonName}"
                )
            }
    ) {
        Column {
            Box {
                Timber.i("로그::rememberCoilPainter Called")
                Image(
                    painter = rememberCoilPainter(
                        request = ImageRequest
                            .Builder(context)
                            .data(entry.imageUrl)
                            .target {
                                viewModel.calcDominantColor(it) { color ->
                                    //2021.07.01 suchang 아무리 해봐도 이부분이 처리안된다. 그래서 Glide 로 대체 했다.
                                    //Toast.makeText(context, "dominantColor....", Toast.LENGTH_SHORT).show()
                                    Log.d(TAG,"dominantColor::Coil...")
                                    dominantColor = color
                                }
                            }
                            .build(),
                        fadeIn = true
                    ),
                    contentDescription = entry.pokemonName,
                    contentScale = ContentScale.FillBounds
                )
                Text(
                    text = entry.pokemonName,
                    fontFamily = RobotoCondensed,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
*/



@Composable
fun PokedexEntry(
    entry: PokedexListEntry,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val defaultDominantColor = MaterialTheme.colors.surface
    var dominantColor by remember { mutableStateOf(defaultDominantColor) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominantColor,
                        defaultDominantColor
                    )
                )
            )
            .clickable {
                navController.navigate(
                    "pokemon_detail_screen/${dominantColor.toArgb()}/${entry.pokemonName}"
                )
            }
    ) {
        Column {
            Box {
                PokedexEntryImage (
                    imgUrl = entry.imageUrl,
                    viewModel =  viewModel
                ){
                    dominantColor = it
                }
                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = entry.pokemonName,
                    fontFamily = RobotoCondensed,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun PokedexEntryImage (
    imgUrl: String,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel(),
    onSetDominantColor: (Color) -> Unit
) {
    val bitmap : MutableState<Bitmap?> = mutableStateOf(null)
    val imageModifier = modifier
        .size(200.dp, 200.dp)
//        .clip(RoundedCornerShape(10.dp))
        .clip(CircleShape)

    Glide.with(LocalContext.current)
        .asBitmap()
        .load(imgUrl)
        .into(
            object : CustomTarget<Bitmap>() {
                override fun onResourceReady (
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    viewModel.calcDominantColor(resource) { color ->
                        onSetDominantColor(color)
                    }
                    bitmap.value = resource
                }
                override fun onLoadCleared (
                    placeholder: Drawable?
                ) { }
            }
        )
    // 2021.06.25 suchang If it's not working you should check INTERNET Permition.
    // 2021.06.25 suchang How find R.drawable at the sub packages!!!
    //            import import com.example.rawegg.R
    Timber.i("로그::Glide Called asImageBitmap")
    bitmap.value?.asImageBitmap()?.let { fetchedBitmap ->
        Image (
            bitmap = fetchedBitmap,
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = imageModifier
        )
    } ?: Image (
        painter = painterResource(id = R.drawable.ic_empty_user_img),
        contentScale = ContentScale.Fit,
        contentDescription = null,
        modifier = imageModifier
    )
}


@Composable
fun RetrySection(
    error: String,
    onRetry: () -> Unit
) {
    Column {
        Text(error, color = Color.Red, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onRetry() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Retry")
        }
    }
}
