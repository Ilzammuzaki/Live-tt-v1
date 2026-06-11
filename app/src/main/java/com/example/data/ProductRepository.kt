package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
        .onStart {
            // Check if there are no items, and insert defaults
            ensureInitialProducts()
        }

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteProductById(id: Int) {
        productDao.deleteProductById(id)
    }

    private suspend fun ensureInitialProducts() {
        val currentList = productDao.getAllProducts().firstOrNull()
        if (currentList == null || currentList.isEmpty()) {
            val defaults = listOf(
                Product(
                    name = "Minyak Telon Fit Plus Original",
                    price = "Rp 35.000",
                    description = "Minyak telon beraroma lavender menenangkan yang memberikan kehangatan ekstra untuk si kecil, meringankan kembung perut, serta melindungi kulit lembut dari gigitan nyamuk hingga 8 jam.",
                    imageUrl = "preset_telon",
                    bpomStatus = "BPOM TR213600121 & Halal Certified"
                ),
                Product(
                    name = "Kapsul Kunyit Temulawak Fit",
                    price = "Rp 85.000",
                    description = "Suplemen herbal alami ekstrak temulawak dan kunyit murni tanpa campuran kimia. Membantu memelihara kesehatan fungsi pencernaan, nafsu makan, dan memperkuat daya tahan tubuh harian.",
                    imageUrl = "preset_kunyit",
                    bpomStatus = "BPOM TR193301011 & Halal ID00110000"
                ),
                Product(
                    name = "Madu Hutan Multifloral Berkah",
                    price = "Rp 120.000",
                    description = "Madu murni alami 100% dipanen langsung dari hutan tropis Nusantara. Kaya antioksidan aktif dan asam amino esensial untuk memelihara kebugaran fisik, vitalitas, serta daya tahan tubuh dari cuaca ekstrem.",
                    imageUrl = "preset_madu",
                    bpomStatus = "BPOM MD208316200 & Halal MUI"
                ),
                Product(
                    name = "Collagen Glow Berry Drink",
                    price = "Rp 150.000",
                    description = "Formula kolagen ikan laut dengan ekstrak buah beri tinggi Vitamin C alami. Bermanfaat menjaga kelembaban kulit Kakak tercinta, memelihara elastisitas sel kulit wajah, dan kaya penangkal radikal bebas.",
                    imageUrl = "preset_collagen",
                    bpomStatus = "BPOM MD867012015 & Halal Certified"
                ),
                Product(
                    name = "Teh Hijau Diet Detoks Alami",
                    price = "Rp 45.000",
                    description = "Teh hijau pilihan premium rendah kafein yang berpadu dengan rempah herbal alami Indonesia. Bermanfaat membantu mengurangi kolesterol, melancarkan metabolisme pencernaan, dan menyegarkan kebugaran jasmani.",
                    imageUrl = "preset_teh",
                    bpomStatus = "BPOM TR183215161 & Halal MUI"
                )
            )
            for (product in defaults) {
                productDao.insertProduct(product)
            }
        }
    }
}
