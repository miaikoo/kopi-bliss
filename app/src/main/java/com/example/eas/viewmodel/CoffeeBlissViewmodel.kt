package com.example.eas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eas.data.CoffeeBlissDao
import com.example.eas.data.Member
import com.example.eas.data.TransactionHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CoffeeBlissViewModel(private val dao: CoffeeBlissDao) : ViewModel() {

    // Menyimpan ID member yang sedang aktif/login
    private val _currentMemberId = MutableStateFlow<Int?>(null)
    val currentMemberId: StateFlow<Int?> = _currentMemberId

    // 1. Registrasi Member
    fun registerMember(name: String, email: String, phone: String) {
        viewModelScope.launch {
            val member = Member(name = name, email = email, phone = phone)
            val id = dao.insertMember(member)
            _currentMemberId.value = id.toInt() // Auto-login setelah daftar
        }
    }

    // 2. Fetch Data Member Aktif
    fun getCurrentMember() = _currentMemberId.value?.let { dao.getMemberById(it) }

    // 3. Fetch Riwayat Transaksi
    fun getTransactions() = _currentMemberId.value?.let { dao.getTransactionsByMember(it) }

    // 4. Tambah Transaksi & Hitung Poin (Rp 10.000 = 1 Point)
    fun addTransaction(amount: Double) {
        val memberId = _currentMemberId.value ?: return
        val points = (amount / 10000).toInt()
        val date = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val transaction = TransactionHistory(
                memberId = memberId,
                date = date,
                amount = amount,
                pointsEarned = points
            )
            dao.insertTransaction(transaction)
            dao.addPoints(memberId, points)
        }
    }

    // 5. Redeem Poin
    fun redeemPoints(pointsToRedeem: Int) {
        val memberId = _currentMemberId.value ?: return
        viewModelScope.launch {
            dao.deductPoints(memberId, pointsToRedeem)
        }
    }
}