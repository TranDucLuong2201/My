package com.android.myapplication


import android.widget.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Cần import thư viện lifecycle-viewmodel-compose

@Composable
fun MainScreen(
    // Inject ViewModel ở đây để nó sống cùng vòng đời của MainScreen
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()

    // Lấy state hiện tại từ ViewModel, chuyển đổi từ Flow sang State của Compose
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "start",
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Màn hình 1: Chọn số lượng
        composable("start") {
            OneCupcakeScreen(
                // Truyền hành động thay đổi số lượng xuống dưới
                onSelectQuantity = { quantity ->
                    viewModel.setQuantity(quantity)
                    viewModel.setFlavor("Vanilla") // Set mặc định cho ví dụ
                    navController.navigate("summary")
                }
            )
        }

        // Màn hình 2: Xem tổng kết
        composable("summary") {
            AnotherScreen(
                uiState = uiState, // Truyền dữ liệu để hiển thị
                onCancel = {
                    viewModel.resetOrder() // Reset dữ liệu trong VM

                    // Quay về trang đầu và xóa sạch stack cũ
                    navController.navigate("start") {
                        popUpTo("start") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun OneCupcakeScreen(
    onSelectQuantity: (Int) -> Unit // Callback nhận vào số lượng
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(text = "Order Cupcakes", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        // Nút chọn 1 bánh
        Button(onClick = { onSelectQuantity(1) }) {
            Text("Order 1 Cupcake")
        }

        // Nút chọn 6 bánh
        Button(onClick = { onSelectQuantity(6) }) {
            Text("Order 6 Cupcakes")
        }

        // Nút chọn 12 bánh
        Button(onClick = { onSelectQuantity(12) }) {
            Text("Order 12 Cupcakes")
        }
    }
}

@Composable
fun AnotherScreen(
    uiState: OrderUiState, // Nhận dữ liệu trực tiếp
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "Order Summary",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        // Hiển thị dữ liệu từ ViewModel
        Text(text = "Quantity: ${uiState.quantity}")
        Text(text = "Flavor: ${uiState.flavor}")
        Text(
            text = "Total: ${uiState.price}",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = { onCancel() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Cancel Order")
        }
    }
}
class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()
    /**
     * Set the quantity [numberCupcakes] of cupcakes for this order's state and update the price
     */
    fun setQuantity(numberCupcakes: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                quantity = numberCupcakes,
                price = calculatePrice(quantity = numberCupcakes)
            )
        }
    }

    /**
     * Set the [desiredFlavor] of cupcakes for this order's state.
     * Only 1 flavor can be selected for the whole order.
     */
    fun setFlavor(desiredFlavor: String) {
        _uiState.update { currentState ->
            currentState.copy(flavor = desiredFlavor)
        }
    }

    /**
     * Set the [pickupDate] for this order's state and update the price
     */
    fun setDate(pickupDate: String) {
        _uiState.update { currentState ->
            currentState.copy(
                date = pickupDate,
                price = calculatePrice(pickupDate = pickupDate)
            )
        }
    }

    /**
     * Reset the order state
     */
    fun resetOrder() {
        _uiState.value = OrderUiState(pickupOptions = emptyList())
    }

    /**
     * Returns the calculated price based on the order details.
     */
    private fun calculatePrice(
        quantity: Int = _uiState.value.quantity,
        pickupDate: String = _uiState.value.date
    ): String {
        var calculatedPrice = quantity * 2.00
        // If the user selected the first option (today) for pickup, add the surcharge
        if (pickupOptions()[0] == pickupDate) {
            calculatedPrice += 3.00
        }
        val formattedPrice = NumberFormat.getCurrencyInstance().format(calculatedPrice)
        return formattedPrice
    }

    private fun pickupOptions(): List<String> {
        val dateOptions = mutableListOf<String>()
        val formatter = SimpleDateFormat("E MMM d", Locale.getDefault())
        val calendar = Calendar.getInstance()
        repeat(4) {
            dateOptions.add(formatter.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
        return dateOptions
    }
}

