package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

/**
 * ImageLoader configurado con decodificadores oficiales para soportar
 * imágenes estáticas (PNG, JPG, WebP), animaciones (GIF) y vectores (SVG).
 */
fun buildAppImageLoader(context: Context): ImageLoader {
  return ImageLoader.Builder(context)
    .components {
      if (Build.VERSION.SDK_INT >= 28) {
        add(ImageDecoderDecoder.Factory())
      } else {
        add(GifDecoder.Factory())
      }
      add(SvgDecoder.Factory())
    }
    .crossfade(true)
    .build()
}

/**
 * Visor interactivo de imágenes para pantallas táctiles móviles.
 * Incluye zoom multitáctil (pinch-to-zoom), desplazamiento libre (pan),
 * restablecimiento de zoom y panel de metadatos (resolución y formato).
 */
@Composable
fun ImageViewerView(
  imageFile: File,
  fileSizeFormatted: String,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val imageLoader = remember(context) { buildAppImageLoader(context) }

  var scale by remember { mutableFloatStateOf(1f) }
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }

  var imageWidth by remember { mutableStateOf<Int?>(null) }
  var imageHeight by remember { mutableStateOf<Int?>(null) }

  val extension = remember(imageFile.name) {
    imageFile.extension.uppercase(Locale.ROOT).ifEmpty { "IMG" }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SlateDark)
  ) {
    // Barra superior de herramientas: Formato, Tamaño, Zoom y Reset
    Surface(
      color = SlateNavy,
      shape = RoundedCornerShape(12.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SlateCardBorder)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Badge del formato
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(CyanPrimary)
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Text(
              text = extension,
              color = SlateDark,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }

          if (imageWidth != null && imageHeight != null) {
            Text(
              text = "${imageWidth} × ${imageHeight} px",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = CyanGlow,
              fontSize = 11.sp
            )
          } else {
            Text(
              text = fileSizeFormatted,
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        // Indicador de zoom y botones de control
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "${(scale * 100).toInt()}%",
            color = if (scale != 1f) MintSecondary else TextMuted,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )

          IconButton(
            onClick = {
              scale = (scale + 0.5f).coerceAtMost(5f)
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ZoomIn,
              contentDescription = "Aumentar zoom",
              tint = CyanPrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = {
              scale = (scale - 0.5f).coerceAtLeast(0.5f)
              if (scale == 1f) {
                offsetX = 0f
                offsetY = 0f
              }
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ZoomOut,
              contentDescription = "Reducir zoom",
              tint = CyanPrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          if (scale != 1f || offsetX != 0f || offsetY != 0f) {
            IconButton(
              onClick = {
                scale = 1f
                offsetX = 0f
                offsetY = 0f
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Replay,
                contentDescription = "Restablecer zoom",
                tint = MintSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    // Área principal de visualización de la imagen con soporte táctil
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(8.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(SlateNavy)
        .border(1.dp, SlateCardBorder, RoundedCornerShape(12.dp))
        .pointerInput(Unit) {
          detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(0.5f, 6f)
            if (scale > 1f) {
              val maxOffset = 500f * (scale - 1f)
              offsetX = (offsetX + pan.x * scale).coerceIn(-maxOffset, maxOffset)
              offsetY = (offsetY + pan.y * scale).coerceIn(-maxOffset, maxOffset)
            } else {
              offsetX = 0f
              offsetY = 0f
            }
          }
        },
      contentAlignment = Alignment.Center
    ) {
      SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
          .data(imageFile)
          .crossfade(true)
          .build(),
        imageLoader = imageLoader,
        contentDescription = "Imagen ${imageFile.name}",
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationX = offsetX
            translationY = offsetY
          }
      ) {
        val painterState = painter.state
        when (painterState) {
          is AsyncImagePainter.State.Loading -> {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(
                color = CyanPrimary,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
              )
            }
          }
          is AsyncImagePainter.State.Error -> {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No se pudo decodificar la imagen",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = painterState.result.throwable.localizedMessage ?: "Formato no compatible",
                color = TextMuted,
                fontSize = 12.sp
              )
            }
          }
          is AsyncImagePainter.State.Success -> {
            val size = painterState.painter.intrinsicSize
            if (size.width > 0 && size.height > 0) {
              imageWidth = size.width.toInt()
              imageHeight = size.height.toInt()
            }
            SubcomposeAsyncImageContent()
          }
          else -> {
            SubcomposeAsyncImageContent()
          }
        }
      }
    }

    // Pie con instrucciones táctiles
    Text(
      text = "💡 Pellizca para hacer zoom • Arrastra para explorar • Toca ↺ para reiniciar",
      style = MaterialTheme.typography.labelSmall,
      color = TextMuted,
      fontSize = 10.sp,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(bottom = 8.dp)
    )
  }
}

/**
 * Reproductor de audio integrado con AndroidX Media3 (ExoPlayer).
 * Proporciona controles táctiles de reproducción (Play/Pause/Seek),
 * barra de progreso interactiva, temporizador y liberación de memoria segura.
 */
@Composable
fun AudioPlayerView(
  audioFile: File,
  fileName: String,
  fileSizeFormatted: String,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Inicialización de ExoPlayer para Media3
  val exoPlayer = remember(audioFile.absolutePath) {
    ExoPlayer.Builder(context).build().apply {
      val mediaItem = MediaItem.fromUri(Uri.fromFile(audioFile))
      setMediaItem(mediaItem)
      prepare()
    }
  }

  var isPlaying by remember { mutableStateOf(false) }
  var currentPosition by remember { mutableLongStateOf(0L) }
  var duration by remember { mutableLongStateOf(0L) }
  var isSeeking by remember { mutableStateOf(false) }
  var seekSliderPosition by remember { mutableFloatStateOf(0f) }

  // Listener para capturar cambios de estado de reproducción en tiempo real
  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying = playing
      }

      override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
          duration = exoPlayer.duration.coerceAtLeast(0L)
        } else if (playbackState == Player.STATE_ENDED) {
          isPlaying = false
          currentPosition = duration
        }
      }
    }
    exoPlayer.addListener(listener)

    onDispose {
      exoPlayer.removeListener(listener)
      exoPlayer.stop()
      exoPlayer.release()
    }
  }

  // Bucle de actualización suave de la posición de audio cada 150ms
  LaunchedEffect(isPlaying, isSeeking) {
    while (isPlaying && !isSeeking) {
      currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
      if (duration <= 0L && exoPlayer.duration > 0L) {
        duration = exoPlayer.duration
      }
      delay(150)
    }
  }

  val extension = remember(fileName) {
    fileName.substringAfterLast('.', "AUDIO").uppercase(Locale.ROOT)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(SlateDark)
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = SlateNavy),
      border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SlateCardBorder)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Icono de carátula animado de audio
        Box(
          modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(if (isPlaying) CyanPrimary else SlateCard),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
            contentDescription = null,
            tint = if (isPlaying) SlateDark else CyanPrimary,
            modifier = Modifier.size(48.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre del archivo de audio
        Text(
          text = fileName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          maxLines = 2
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Badges de tipo y tamaño
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(MintSecondary.copy(alpha = 0.2f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "$extension Audio",
              color = MintSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(SlateCard)
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = fileSizeFormatted,
              color = TextMuted,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Barra deslizante de progreso interactiva (Seek)
        val sliderValue = if (isSeeking) {
          seekSliderPosition
        } else {
          if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
        }

        Slider(
          value = sliderValue,
          onValueChange = { newFrac ->
            isSeeking = true
            seekSliderPosition = newFrac
          },
          onValueChangeFinished = {
            val targetMs = (seekSliderPosition * duration).toLong()
            exoPlayer.seekTo(targetMs)
            currentPosition = targetMs
            isSeeking = false
          },
          colors = SliderDefaults.colors(
            thumbColor = CyanPrimary,
            activeTrackColor = CyanPrimary,
            inactiveTrackColor = SlateCard
          ),
          modifier = Modifier.fillMaxWidth()
        )

        // Contador de tiempo (actual / total)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = formatAudioTime(currentPosition),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = CyanGlow,
            fontSize = 12.sp
          )
          Text(
            text = formatAudioTime(duration),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = TextMuted,
            fontSize = 12.sp
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Controles de reproducción táctiles móviles
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Retroceder 5 segundos
          IconButton(
            onClick = {
              val target = (exoPlayer.currentPosition - 5000L).coerceAtLeast(0L)
              exoPlayer.seekTo(target)
              currentPosition = target
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = SlateCard),
            modifier = Modifier.size(48.dp)
          ) {
            Icon(
              imageVector = Icons.Default.FastRewind,
              contentDescription = "Retroceder 5s",
              tint = TextPrimary,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(20.dp))

          // Botón principal Play / Pause
          IconButton(
            onClick = {
              if (isPlaying) {
                exoPlayer.pause()
              } else {
                if (exoPlayer.playbackState == Player.STATE_ENDED) {
                  exoPlayer.seekTo(0)
                }
                exoPlayer.play()
              }
            },
            colors = IconButtonDefaults.iconButtonColors(
              containerColor = CyanPrimary,
              contentColor = SlateDark
            ),
            modifier = Modifier.size(64.dp)
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pausar" else "Reproducir",
              tint = SlateDark,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.width(20.dp))

          // Adelantar 5 segundos
          IconButton(
            onClick = {
              val target = (exoPlayer.currentPosition + 5000L).coerceAtMost(duration)
              exoPlayer.seekTo(target)
              currentPosition = target
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = SlateCard),
            modifier = Modifier.size(48.dp)
          ) {
            Icon(
              imageVector = Icons.Default.FastForward,
              contentDescription = "Adelantar 5s",
              tint = TextPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }
    }
  }
}

/** Formatea milisegundos a representación mm:ss */
private fun formatAudioTime(millis: Long): String {
  if (millis <= 0L) return "00:00"
  val totalSeconds = millis / 1000
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
