package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.data.model.SaleOrder
import com.example.data.repository.StoreRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("متجر الفيصل", appName)
    }

    @Test
    fun `verify initial products and repository functions`() {
        val repo = StoreRepository()
        val products = repo.products.value
        assertEquals(10, products.size)
        assertEquals("طقم دلة القهوة العربية والضيافة الملكية", products[0].name)
    }

    @Test
    fun `verify sale calculation and customer debt update`() {
        val repo = StoreRepository()
        val customer = repo.customers.value.find { it.id == "fashion" }!!
        val initialDebt = customer.debt

        val product = repo.products.value.first()
        val order = SaleOrder(
            id = "INV-TEST",
            items = listOf(CartItem(product, 2)),
            customer = customer,
            subtotal = product.price * 2,
            vat = (product.price * 2) * 0.15,
            total = (product.price * 2) * 1.15,
            paymentMethod = "آجل"
        )

        repo.recordSale(order)
        val updatedCustomer = repo.customers.value.find { it.id == "fashion" }!!
        assertEquals(initialDebt + order.total, updatedCustomer.debt, 0.01)
    }
}
