package com.example.eas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Tambahkan import ini
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.eas.data.AppDatabase
import com.example.eas.ui.theme.EASTheme
import com.example.eas.viewmodel.CoffeeBlissViewModel

class MainActivity : ComponentActivity() {

    // Inisialisasi ViewModel menggunakan Factory
    private val viewModel: CoffeeBlissViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(applicationContext)
                return CoffeeBlissViewModel(db.coffeeBlissDao()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EASTheme {
                // Teruskan ViewModel ke UI
                CoffeeBlissApp(viewModel)
            }
        }
    }
}

enum class AppDestinations(val label: String, val icon: Int) {
    REGISTER("Daftar", R.drawable.ic_account_box),
    CARD("Kartu", R.drawable.ic_home),
    TRANSACTION("Transaksi", R.drawable.ic_favorite),
    REDEEM("Redeem", R.drawable.ic_favorite)
}

@Composable
fun CoffeeBlissApp(viewModel: CoffeeBlissViewModel) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.REGISTER) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(painterResource(it.icon), contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                when (currentDestination) {
                    AppDestinations.REGISTER -> RegistrationScreen(viewModel)
                    AppDestinations.CARD -> DigitalCardScreen()
                    AppDestinations.TRANSACTION -> TransactionScreen(viewModel)
                    AppDestinations.REDEEM -> RedeemScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun RegistrationScreen(viewModel: CoffeeBlissViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Daftar Member Coffee Bliss", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("No HP") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { viewModel.registerMember(name, email, phone) }, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan & Daftar")
        }
            }
        }

@Composable
fun DigitalCardScreen(viewModel: CoffeeBlissViewModel) {
    // Mock Data untuk tampilan UI
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text("COFFEE BLISS", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Nama: Budi Santoso", style = MaterialTheme.typography.bodyLarge)
            Text("Status: Gold Member", style = MaterialTheme.typography.bodyLarge)
            Text("Total Poin: 15 Pts", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun TransactionScreen(viewModel: CoffeeBlissViewModel) {
    var inputAmount by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tambah Transaksi Baru", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = inputAmount, 
            onValueChange = { inputAmount = it }, 
            label = { Text("Nominal (Rp)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { 
                val amount = inputAmount.toDoubleOrNull() ?: 0.0
                if(amount > 0) viewModel.addTransaction(amount) 
            }, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan Transaksi")
        }
        
        HorizontalDivider()
        Text("Riwayat Transaksi", style = MaterialTheme.typography.titleMedium)
        
        // Mock List
        LazyColumn {
            items(2) { // Contoh 2 item
                ListItem(
                    headlineContent = { Text("Pembelian Rp 150.000") },
                    supportingContent = { Text("15 Poin didapatkan") },
                    trailingContent = { Text("12 Okt 2023") }
                )
            }
        }
    }
}

@Composable
fun RedeemScreen(viewModel: CoffeeBlissViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tukar Poin Anda", style = MaterialTheme.typography.headlineMedium)
        Text("Poin Anda Saat Ini: 15 Poin", style = MaterialTheme.typography.bodyLarge)
        
        Button(onClick = { viewModel.redeemPoints(50) }, modifier = Modifier.fillMaxWidth()) {
            Text("Tukar 50 Poin - Gratis Espresso")
        }
        Button(onClick = { viewModel.redeemPoints(100) }, modifier = Modifier.fillMaxWidth()) {
            Text("Tukar 100 Poin - Gratis Cappuccino")
        }
        Button(onClick = { viewModel.redeemPoints(150) }, modifier = Modifier.fillMaxWidth()) {
            Text("Tukar 150 Poin - Gratis Latte")
        }
    }
}