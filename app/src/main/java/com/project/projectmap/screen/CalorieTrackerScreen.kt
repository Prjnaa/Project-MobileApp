package com.project.projectmap.screen

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.projectmap.R
import com.project.projectmap.scanKamera.MainActivity

@Composable
fun CalorieTrackerScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Kurangi jarak antar elemen
    ) {
        item {
            TopBar()
        }
        item {
            MacronutrientRow()
        }
        item {
            BunnyImage()
        }
        item {
            CalorieProgress() // Kurangi jarak antar elemen
        }
        item {
            TrackEatButton() // Sesuaikan tombol Track Eat
        }
        item {
            SetNewTarget() // Tambahkan teks set new target
        }
        item {
            ChallengesSection()
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween, // Jarak merata antara ikon
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fire Icon with Points
        IconWithArrowAndPoints(
            iconRes = R.drawable.fire_icon, // Ganti dengan ID resource untuk ikon api
            contentDescription = "Fire",
            points = 1200 // Jumlah poin api
        )

        // Coin Icon with Points
        IconWithArrowAndPoints(
            iconRes = R.drawable.coin_icon, // Ganti dengan ID resource untuk ikon koin
            contentDescription = "Coin",
            points = 350 // Jumlah poin koin
        )
    }
}

@Composable
fun IconWithArrowAndPoints(iconRes: Int, contentDescription: String, points: Int) {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0B0FF)) // Warna ungu muda
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon (misalnya api atau koin)
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Teks Poin
            Text(
                text = "$points",  // Menampilkan poin
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Ikon panah ke kanan
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right), // Ikon panah kanan
                contentDescription = "Arrow",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray // Warna panah
            )
        }
    }
}

@Composable
fun BunnyImage() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bunny),
            contentDescription = "Cute bunny",
            modifier = Modifier.size(180.dp) // Ukuran gambar Bunny tetap
        )
    }
}

@Composable
fun CalorieProgress(
    progress: Float = 0.5f,  // progress antara 0 hingga 1
    totalCalories: Int = 2000,
    currentCalories: Int = 1000
) {
    val sweepAngle = 180 * progress
    val color = Color(0xFFE0B0FF)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Menggunakan rasio aspek 2f untuk proporsi lebih baik
            .padding(vertical = 0.dp) // Kurangi padding untuk mendekatkan elemen
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(0.8f) // Menyesuaikan ukuran speedometer
        ) {
            drawArc(
                color = Color.LightGray,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx()) // Ketebalan arc
            )

            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx()) // Ketebalan arc progress
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-50).dp)  // Kurangi offset untuk mendekatkan teks
        ) {
            Text(
                text = "$currentCalories",
                fontSize = 40.sp,  // Ukuran teks sedikit lebih kecil
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "of $totalCalories calories",
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun TrackEatButton() {
    val context = LocalContext.current
    Button(
        onClick = {
            // Intent untuk memulai CameraActivity
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent) // Memulai aktivitas Camera
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 32.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0B0FF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Add",
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Track Eat", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun SetNewTarget() {
    Text(
        text = "set new target",
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun MacronutrientRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround // Jarak yang merata antar item
    ) {
        MacronutrientItem("Carb", 0.6f, Color.Blue)
        MacronutrientItem("Protein", 0.4f, Color.Red)
        MacronutrientItem("Fat", 0.3f, Color.Green)
    }
}

@Composable
fun MacronutrientItem(name: String, progress: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp)
        ) {
            Canvas(modifier = Modifier.size(60.dp)) {
                drawCircle(
                    color = Color.LightGray,
                    radius = size.minDimension / 2,
                    style = Stroke(width = 8.dp.toPx())
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360 * progress,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx()),
                    size = size
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 14.sp,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 14.sp)
    }
}

@Composable
fun ChallengesSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0B0FF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Complete today's challenges x/3",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            repeat(3) { index ->
                ChallengeItem(isCompleted = index == 0)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChallengeItem(isCompleted: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD08BFF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Challenge 1",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Drink milk today!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+10 coins",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            if (isCompleted) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Completed",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
