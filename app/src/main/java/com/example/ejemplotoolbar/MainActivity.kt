@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ejemplotoolbar
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Canvas
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.CalendarContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ejemplotoolbar.ui.theme.EjemploToolbarTheme
import java.time.LocalDateTime
import kotlin.Int
import com.example.ejemplotoolbar.data.*
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import java.util.Date

data class Jugador(
    val title: String,
    var monedas: Int,
    var rachas: Int,
    var fecha: LocalDateTime? = null
)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var nombre = IniciarDatos(this)
        setContent {
          EjemploToolbarTheme {
              MorraVirtualApp(nombre)
            }

        }
    }
}

@SuppressLint("CheckResult")
@RequiresApi(Build.VERSION_CODES.O)
fun IniciarDatos(context: Context): Jugador{
    val dbHelper = DataPartida(context)
    var db = dbHelper.writableDatabase
    return  UltimaFila(db)
}

@Composable
fun MorraVirtualApp (jugador: Jugador) {
    var currentStep by remember { mutableIntStateOf(1) }
    var ganador by remember { mutableStateOf("") }
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        when (currentStep) {
            1 -> {
                MorraScreen(
                    imageResourceId = R.drawable.lamorravirtual,
                    contentDescriptionId = R.string.welcome,
                    onImageClick = {currentStep = 2},

                    )


            }
            2 -> {
                //Thread.sleep(3000)
                InterfaceUsuario(jugador,
                    imageResourceId = R.drawable.la_morra_ui,
                    contentDescriptionId = R.string.welcome,
                    onImageClick = {currentStep = 3}
                )
            }
            3 -> {
                InterfaceJuego(jugador,
                    imageResourceId = R.drawable.la_morra_fondo,
                    contentDescriptionId = R.string.welcome,
                    onImageClick = {currentStep = 4},
                    ganador = ganador,
                    onWinnerChange = {ganador = it},

                )

            }
            4 -> {
                PantallaFinal(jugador,
                    imageResourceId = R.drawable.la_morra_final,
                    contentDescriptionId = R.string.welcome,
                    onImageClick = {currentStep = 2},
                    ganador = ganador
                )
            }
        }
    }
}

@Composable
fun MorraScreen(
    imageResourceId: Int,
    contentDescriptionId: Int,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier

){
    Box(modifier){
        Image(
            painter = painterResource(imageResourceId),
            contentDescription = stringResource(contentDescriptionId),
            contentScale = ContentScale.Crop

        )
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ){

            Spacer(modifier = Modifier.height(550.dp))
            Button( onClick = onImageClick,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(40.dp)
                //.padding(30.dp)
            ) {
                Text(
                    stringResource(R.string.clic_welcome)
                )
            }

        }
    }
}

@Composable
fun InterfaceUsuario(jugador: Jugador,
    imageResourceId: Int,
    contentDescriptionId: Int,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier){
    Scaffold(
        topBar = { Toolbar(jugador)},
        content = { innerPadding ->

            Box(modifier){
                Image(
                    painter = painterResource(imageResourceId),
                    contentDescription = stringResource(contentDescriptionId),
                    contentScale = ContentScale.Crop
                )
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ){
                    Spacer(modifier = Modifier.height(200.dp))
                    Button(
                        onClick = onImageClick,
                    ) {
                        Text(
                            text = stringResource(R.string.iniciar_juego),
                            fontSize = (24.sp),
                            fontWeight = FontWeight.Bold

                        )
                    }

                }
            }
        }
    )
}
class CaptureController {
    var bitmap: Bitmap? by mutableStateOf(null)
    var requestCapture by mutableStateOf(false)

    fun capture() {
        requestCapture = true
    }
}


@Composable
fun CaptureComposable(
    captureController: CaptureController,
    onCaptured: (Bitmap) -> Unit,
    content: @Composable () -> Unit
) {
    Log.d("CaptureDebug", "entro en captureComposable")

     AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                setContent {
                    content()
                }
            }

        },
        update = { view ->
            if (captureController.requestCapture) {
                view.post {
                    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    view.draw(canvas)
                    Log.d("CaptureDebug", "Imagen capturada, tamaño: ${bitmap.width}x${bitmap.height}")
                    captureController.bitmap = bitmap
                    captureController.requestCapture = false
                    onCaptured(bitmap)
                }
            }else{
                Log.d("CaptureDebug", "entro en el else")
            }
        },
        modifier = Modifier.fillMaxSize()
    )

}

@Composable
fun InterfaceJuego(jugador: Jugador,
                   imageResourceId: Int,
                   contentDescriptionId: Int,
                   onImageClick: () -> Unit,
                   ganador: String,
                   onWinnerChange: (String) -> Unit,
                   modifier: Modifier = Modifier,
                   onCapturaLista: (Bitmap) -> Unit ={}
                   ) {
    val context = LocalContext.current
    val dbHelper = DataPartida(context)
    var db = dbHelper.writableDatabase
    val activity = LocalActivity.current

    val captureController = remember { CaptureController() }
    var mostrarPantallaFinal by remember { mutableStateOf(false) }
    var capturaPendiente by remember { mutableStateOf(false) }
    var mostrarElegirCalendario by remember { mutableStateOf(false) }
    var calendarIdSeleccionado by remember { mutableStateOf<Long?>(null) }
        // Activa el estado de captura antes de mostrar la pantalla final
        // Componente de captura
        CaptureComposable(
            captureController = captureController,
            onCaptured = { bitmap ->
                guardarImagenEnGaleria(context, bitmap)
                Toast.makeText(context, "Imagen guardada en galería", Toast.LENGTH_SHORT).show()
                onCapturaLista(bitmap)
            }
        ) {
            Scaffold(

                topBar = { Toolbar(jugador) },

                content = { innerPadding ->

                    Box(

                        modifier = modifier.fillMaxSize()

                    )

                    {

                        Image(
                            painter = painterResource(imageResourceId),
                            contentDescription = stringResource(contentDescriptionId),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (mostrarElegirCalendario) {
                                ElegirCalendario (
                                    onDismiss = { mostrarElegirCalendario = false },
                                    onCalendarioSeleccionado = { calendarioId ->
                                        agregarVictoriaCalendario(context, calendarioId)
                                        mostrarElegirCalendario = false
                                        mostrarPantallaFinal = true
                                        capturaPendiente = true
                                    }
                                )
                            }
                            // inicio partida, puntos a 0
                            var puntos1 by remember { mutableIntStateOf(value = 0) }
                            var puntos2 by remember { mutableIntStateOf(value = 0) }
                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(15.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            )
                            {
                                val nombre = jugador.title
                                Text(text = "Contrincantes: ")
                                Column {
                                    Text(nombre)
                                    Text("$puntos1 Puntos")
                                }
                                Column {
                                    Text("Maquina")
                                    Text("${puntos2}Puntos")
                                }
                            }


                            var mano1 by remember { mutableStateOf(value = "") }
                            var mano0 by remember { mutableIntStateOf(value = 0) }
                            var mano2 by remember { mutableIntStateOf(value = 0) }
                            var total1 by remember { mutableStateOf(value = "") }
                            var total2 by remember { mutableIntStateOf(value = 0) }
                            var suma1 by remember { mutableIntStateOf(value = 0) }
                            var maxtotal by remember { mutableIntStateOf(value = 0) }
                            var resta1 by remember { mutableIntStateOf(value = 0) }
                            var resta2 by remember { mutableIntStateOf(value = 0) }
                            var gana by remember { mutableStateOf(value = "") }
                            Column(
                                modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                EntradaDeDatos(
                                    title = "Tu Mano: (del 0 al 5) ",
                                    text = mano1,
                                    onValueChange = { mano1 = it })
                                EntradaDeDatos(
                                    title = "Tu Apuesta: (del 0 al 10)",
                                    text = total1,
                                    onValueChange = { total1 = it })
                                Button(

                                    onClick = {
                                        mano0 = mano1.toInt()
                                        // comprueba si la apuesta es valida
                                        if (0 <= mano0 && mano0 <= 5 && mano0 <= total1.toInt() && total1.toInt() <= mano0 + 5) {
                                            mano2 = (0..5).random()
                                            maxtotal = mano2 + 5
                                            total2 = (mano2..maxtotal).random()
                                            suma1 = mano0 + mano2
                                            resta1 = positivoConv(suma1 - total1.toInt())
                                            resta2 = positivoConv(suma1 - total2)
                                            // revisa la solución de la apuesta
                                            if (total1.toInt() != total2) {
                                                if (total1.toInt() == suma1) {
                                                    puntos1 += 5
                                                    gana = "jugador"
                                                    jugador.monedas += 5
                                                } else if (total2 == suma1) {
                                                    puntos2 += 5
                                                    gana = "maquina"
                                                    jugador.monedas -= 5
                                                } else if (resta1 < resta2) {
                                                    puntos1 += 1
                                                    gana = "jugador"
                                                    jugador.monedas += 1
                                                } else {
                                                    puntos2 += 1
                                                    gana = "maquina"
                                                    jugador.monedas -= 1
                                                }

                                            } else {
                                                Toast.makeText(context,"Tablas, no hay vencedor",Toast.LENGTH_SHORT).show()
                                            }
                                            if (puntos1 >= 5) {
                                                // racha + 1
                                                jugador.rachas += 1
                                                Toast.makeText(context,"fin de la partida ganador jugador",Toast.LENGTH_SHORT).show()
                                                guardaPartidaDB(db,jugador.title,jugador.monedas,jugador.rachas)
                                                if(activity != null) {
                                                    captureController.capture()
                                                    solicitarPermisosCalendario(activity)
                                                    mostrarElegirCalendario = true
                                                }



                                                // Llamada final a la UI

                                            } else if (puntos2 >= 5) {
                                                // se acabo la racha
                                                jugador.rachas = 0
                                                Toast.makeText(context,"fin de la partida ganadora la maquina",Toast.LENGTH_SHORT).show()
                                                // guardo la partida
                                                guardaPartidaDB(db,jugador.title,jugador.monedas,jugador.rachas)
                                                onWinnerChange("Maquina")
                                                onImageClick.invoke()
                                            }
                                        } else {
                                            Toast.makeText(context,"Error en la apuesta, recuerda tu mano tiene de 0 a 5 dedos y tu apuesta el numero de tu apuesta hasta + 5",Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                ) {
                                    Text(
                                        text = stringResource(R.string.nueva_apuesta),
                                        fontSize = (24.sp),
                                        fontWeight = FontWeight.Bold

                                    )
                                }
                                Text(text = "Apuesta del oponente $total2")
                                Row(
                                    modifier = modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                )
                                {
                                    ImagenMano(mano0)
                                    ImagenMano(mano2)
                                }
                                Button(
                                    onClick = onImageClick,
                                    modifier = Modifier.height(70.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.salir_partida),
                                        fontSize = (24.sp),
                                        fontWeight = FontWeight.Bold

                                    )
                                }


                            }
                        }
                    }
                }
            )

        }


        // Llamamos a capture después de que se renderiza la UI
    LaunchedEffect(capturaPendiente) {
        if (capturaPendiente) {
                delay(300) // Espera un frame para la renderización
                // Realiza la captura
                captureController.capture()
                // Llamada final a la UI
                onWinnerChange.invoke("Jugador")
                onImageClick.invoke()
        }
    }

}

data class CalendarioDisponible(
    val id: Long,
    val nombre: String,
    val cuenta: String
)

fun obtenerCalendarios(context: Context): List<CalendarioDisponible> {
    val calendarios = mutableListOf<CalendarioDisponible>()

    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.ACCOUNT_NAME
    )

    val cursor = context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        null,
        null,
        null
    )

    cursor?.use {
        val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
        val nameIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
        val accountIndex = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)

        while (it.moveToNext()) {
            val id = it.getLong(idIndex)
            val nombre = it.getString(nameIndex)
            val cuenta = it.getString(accountIndex)

            Log.d("CalendarioDebug", "Calendario encontrado: Nombre=$nombre, Cuenta=$cuenta, ID=$id")

            calendarios.add(CalendarioDisponible(id, nombre, cuenta))
        }
    }

    return calendarios
}

@Composable
fun ElegirCalendario(
    onDismiss: () -> Unit,
    onCalendarioSeleccionado: (Long) -> Unit
) {
    val context = LocalContext.current
    val calendarios = remember { obtenerCalendarios(context).filter { calendario ->
        calendario.cuenta.contains("@uoc.edu")
    } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("Selecciona un calendario")},
        text= {
            Column {
                calendarios.forEach { calendario ->
                    Button(
                        onClick = {
                            onCalendarioSeleccionado(calendario.id)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(calendario.nombre)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun solicitarPermisosCalendario(activity: Activity) {
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR
            ),
            100 // código de solicitud
        )
    }
}

fun agregarVictoriaCalendario(context: Context, calendarId: Long,titulo: String = "¡Victoria en el juego!", descripcion: String = "Ganaste una partida") {
   // val calendarId = obtenerPrimerCalendarioId(context) ?: return
    obtenerEventosDelCalendario(context, calendarId)
    Log.d("CaptureDebug", "Estas en la funcion agregarvictoriacalendario")
    val inicio = Calendar.getInstance()
    val fin = Calendar.getInstance().apply{add(Calendar.HOUR, 1)}
    //fin.add(Calendar.HOUR, 1) // duración de 1 hora

    val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, inicio.timeInMillis)
        put(CalendarContract.Events.DTEND, fin.timeInMillis)
        put(CalendarContract.Events.TITLE, titulo)
        put(CalendarContract.Events.DESCRIPTION, descripcion)
        put(CalendarContract.Events.CALENDAR_ID, calendarId)
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
    }

    try {
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        if (uri != null) {
            Log.d("CALENDARIO", "Evento cuardado correctamente: $uri")
            Log.d("CaptureDebug", "Insertando evento en calendario ID: $calendarId")
        }else{
            Log.d("CALENDARIO", "Error al guardar el evento")
        }
    }catch ( e: SecurityException){
        Log.e("CaptureDebug", "Error de permisos al guardar evento: ${e.message}")
    }catch (e: Exception) {
        Log.e("CaptureDebug", "Error desconocido al guardar evento: ${e.message}")
    }
}

fun obtenerEventosDelCalendario(context: Context, calendarId: Long) {
    val projection = arrayOf(
        CalendarContract.Events._ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.DESCRIPTION
    )

    val selection = "${CalendarContract.Events.CALENDAR_ID} = ?"
    val selectionArgs = arrayOf(calendarId.toString())

    val cursor = context.contentResolver.query(
        CalendarContract.Events.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            do {
                val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                val titulo = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                val inicio = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                val fin = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                val descripcion = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
                val lugar = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))

                Log.d("EVENTOS", "ID: $id")
                Log.d("EVENTOS", "Título: $titulo")
                Log.d("EVENTOS", "Inicio: ${Date(inicio)}")
                Log.d("EVENTOS", "Fin: ${Date(fin)}")
                Log.d("EVENTOS", "Descripción: $descripcion")
                Log.d("EVENTOS", "Lugar: $lugar")
                Log.d("EVENTOS", "----------------------------")
            } while (it.moveToNext())
        } else {
            Log.d("EVENTOS", "No hay eventos en este calendario.")
        }
    }
}

fun obtenerPrimerCalendarioId(context: Context): Long? {
    Log.d("CaptureDebug", "Estas en la funcion obtenerPrimerCalendario")

    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
    )
    val uri = CalendarContract.Calendars.CONTENT_URI

    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idCol = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            return cursor.getLong(idCol)
        }
    }
    return null
}

fun guardaPartidaDB(dbpartida: SQLiteDatabase, nombre: String, monedas: Int, rachas: Int){

    val disposable = CompositeDisposable()
    disposable.add( guardarPartida(dbpartida ,nombre , monedas, rachas)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { rowId -> Log.d("DB", "Insertado con ID: $rowId") },
            { error -> Log.e("DB", "error", error)}
        )
    )
}

fun guardarImagenEnGaleria(context: Context, bitmap: Bitmap) {
    val filename = "victoria_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Morra")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
    }

}


@Composable
fun ImagenMano(a: Int){
    when(a){
        0 -> {ZeroImagen()}
        1 -> {UnoImagen()}
        2 -> {DosImagen()}
        3 -> {TresImagen()}
        4 -> {CuatroImagen()}
        5 -> {CincoImagen()}
    }
}

@Composable
fun ZeroImagen(){
    Image(
        painter = painterResource(id = R.drawable.puno),
        contentDescription = "puño cerrado 0",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun UnoImagen(){
    Image(
        painter = painterResource(id = R.drawable.uno),
        contentDescription = "un dedo 1",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun DosImagen(){
    Image(
        painter = painterResource(id = R.drawable.dos),
        contentDescription = "dos dedos 2",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun TresImagen(){
    Image(
        painter = painterResource(id = R.drawable.tres),
        contentDescription = "tres dedos 3",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun CuatroImagen(){
    Image(
        painter = painterResource(id = R.drawable.cuatro),
        contentDescription = "cuatro dedos 4",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun CincoImagen(){
    Image(
        painter = painterResource(id = R.drawable.cinco),
        contentDescription = "cinco dedos 5",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun PantallaFinal(jugador: Jugador,
                  ganador: String,
    imageResourceId: Int,
    contentDescriptionId: Int,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier) {

    Scaffold(
        topBar = { Toolbar(jugador)},
        content = { innerPadding ->

            Box(modifier) {
                Image(
                    painter = painterResource(imageResourceId),
                    contentDescription = stringResource(contentDescriptionId),
                    contentScale = ContentScale.Crop
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Text(
                        text = stringResource(R.string.ganador,ganador),
                        style = MaterialTheme.typography.displaySmall
                    )
                    Spacer(modifier = Modifier.height(200.dp))
                    Button(
                        onClick = onImageClick,
                    ) {
                        Text(
                            text = stringResource(R.string.final_oartida),
                            fontSize = (24.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun Toolbar(jugador: Jugador){
    TopAppBar(
        title = { Text("La Morra Virtual") },
        navigationIcon = {
            IconButton(onClick = { /* Acción de navegación */ }) {
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            Monedasyrachas(jugador)
            IconButton(onClick = { /* Acción de más opciones */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
            }
        },
        modifier = Modifier.fillMaxWidth(),
        //elevation = 4.dp
    )
}


@Composable
fun Monedasyrachas(jugador: Jugador){
    IconButton(onClick = { /* Acción de búsqueda */ }) {
        //Icon(Icons.Default.Search , contentDescription = "Buscar")
        Image(
            painter = painterResource(R.drawable.racha),
            contentDescription = "Racha"
        )
    }
    Text(text = "${jugador.rachas}")
    IconButton(onClick = { /* Acción de búsqueda */ }) {
        //Icon(Icons.Default.Search , contentDescription = "Buscar")
        Image(
            painter = painterResource(R.drawable.money),
            contentDescription = "Monedas"
        )
    }
    Text(text = "${jugador.monedas}")
}

@Composable
fun JuegoPartida(){
    Box(Modifier
        .fillMaxSize()
        .padding(16.dp)){
        Apostar()

    }
}

@Composable
fun Apostar(){
    Row {
        Text("prueba1")

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val nombre = Jugador("javier", 30, 4)
    EjemploToolbarTheme {
        MorraVirtualApp(nombre)
    }
}

@Composable
fun EntradaDeDatos (
    title: String,
    text: String,
    onValueChange: (String) -> Unit
){
    TextField(
        value = text,
        onValueChange = onValueChange,
        label = {Text(text = title)},
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

fun positivoConv (a: Int): Int{
    if (a < 0 ){
        return a * (-1)
    }else
        return a
}


