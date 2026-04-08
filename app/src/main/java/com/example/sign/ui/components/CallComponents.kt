package com.example.sign.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sign.R

@Composable
fun QuickPhraseChip(text: String, onClick: () -> Unit) {
    val imageRes = when (text.lowercase()) {
        "emergency", "आपातकालीन", "ತುರ್ತು" -> R.drawable.img_emergency
        "hospital", "अस्पताल", "ಆಸ್ಪತ್ರೆ" -> R.drawable.img_hospital
        "food", "भोजन", "ಆಹಾರ" -> R.drawable.img_food
        "water", "पानी", "ನೀರು" -> R.drawable.img_water
        "help", "मदद", "ಸಹಾಯ" -> R.drawable.img_help
        "toilet", "शौचालय", "ಶೌಚಾಲಯ" -> R.drawable.img_toilet
        "police", "पुलिस", "ಪೊಲೀಸ್" -> R.drawable.img_police
        "thank you", "धन्यवाद", "ಧನ್ಯವಾದಗಳು" -> R.drawable.img_thank_you
        else -> null
    }

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (imageRes != null) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = text,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

@Composable
fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    imageRes: Int? = null,
    containerColor: Color = Color.White.copy(alpha = 0.1f),
    iconColor: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (imageRes != null) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
        }
    }
}
