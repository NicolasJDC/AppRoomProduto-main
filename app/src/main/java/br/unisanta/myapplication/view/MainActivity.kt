package br.unisanta.myapplication.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import br.unisanta.myapplication.dao.ProductDao
import br.unisanta.myapplication.databinding.ActivityMainBinding
import br.unisanta.myapplication.db.AppDatabase
import br.unisanta.myapplication.model.Product
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .fallbackToDestructiveMigration() // recria o banco se o schema mudar
            .build()

        productDao = db.productDao()

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnInserirProduto.setOnClickListener { inserirProduto() }
        binding.btnListarProdutos.setOnClickListener { listarProdutos() }
        binding.btnBuscar.setOnClickListener { buscarPorNome() }
        binding.btnExcluirTodos.setOnClickListener { excluirTodos() }
        binding.btnExcluirPorId.setOnClickListener { excluirPorId() }
        binding.btnAtualizarPreco.setOnClickListener { atualizarPreco() }
    }

    // inserir
    private fun inserirProduto() {
        val name = binding.editProductName.text.toString().trim()
        val priceText = binding.editProductPrice.text.toString()
        val description = binding.editProductDescription.text.toString()
        val quantity = binding.editProductQuantity.text.toString().toIntOrNull() ?: 0

        if (name.isEmpty()) {
            binding.editProductName.error = "Informe o nome"
            return
        }

        // validacao preco
        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            binding.editProductPrice.error = "Informe um preço válido"
            binding.editProductPrice.requestFocus()
            return
        }

        val product = Product(
            name = name,
            price = price,
            description = description,
            quantity = quantity
        )

        lifecycleScope.launch {
            productDao.insertAll(product)
            Log.i("TAG", "Produto inserido: $name - R$$price")
            Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()
            limparCampos() // A1
        }
    }

    // limpa os campos
    private fun limparCampos() {
        binding.editProductName.text.clear()
        binding.editProductPrice.text.clear()
        binding.editProductDescription.text.clear()
        binding.editProductQuantity.text.clear()
        binding.editProductName.requestFocus()
    }

    // listar
    private fun listarProdutos() {
        lifecycleScope.launch {
            val products = productDao.getAll()
            products.forEach {
                Log.i("TAG", "Produto = ${it.name}, Preço = R$${it.price}, Descrição = ${it.description}")
            }
            mostrarNaTela(products)
        }
    }

    // mostra a lista no TextView com a quantidade e a soma dos preços
    private fun mostrarNaTela(products: List<Product>) {
        if (products.isEmpty()) {
            binding.txtResumo.text = "Nenhum produto encontrado."
            binding.txtListaProdutos.text = ""
            return
        }

        val total = products.sumOf { it.price }
        binding.txtResumo.text =
            "Produtos cadastrados: ${products.size}  |  Soma dos preços: ${formatarReais(total)}"

        binding.txtListaProdutos.text = products.joinToString("\n\n") {
            "#${it.id} - ${it.name}\n" +
                    "Preço: ${formatarReais(it.price)}  |  Qtd: ${it.quantity}\n" +
                    "Descrição: ${it.description.orEmpty().ifEmpty { "-" }}"
        }
    }

    private fun formatarReais(valor: Double): String =
        "R$ " + String.format(Locale("pt", "BR"), "%.2f", valor)

    // buscar
    private fun buscarPorNome() {
        val texto = binding.editBusca.text.toString().trim()
        lifecycleScope.launch {
            val resultado = productDao.buscarPorNome("%$texto%")
            mostrarNaTela(resultado)
        }
    }

    // excluir
    private fun excluirTodos() {
        lifecycleScope.launch {
            productDao.deleteAll()
            Toast.makeText(this@MainActivity, "Todos os produtos foram excluídos", Toast.LENGTH_SHORT).show()
            mostrarNaTela(emptyList())
        }
    }

    // desafio extra: excluir um produto pelo id usando @Delete
    private fun excluirPorId() {
        val id = binding.editId.text.toString().toIntOrNull()
        if (id == null) {
            binding.editId.error = "Informe um ID"
            return
        }
        lifecycleScope.launch {
            val product = productDao.getById(id)
            if (product == null) {
                Toast.makeText(this@MainActivity, "Produto #$id não encontrado", Toast.LENGTH_SHORT).show()
            } else {
                productDao.delete(product)
                Toast.makeText(this@MainActivity, "Produto #$id excluído", Toast.LENGTH_SHORT).show()
                mostrarNaTela(productDao.getAll())
            }
        }
    }

    // atualizar
    private fun atualizarPreco() {
        val id = binding.editId.text.toString().toIntOrNull()
        val novoPreco = binding.editNovoPreco.text.toString().toDoubleOrNull()

        if (id == null) {
            binding.editId.error = "Informe um ID"
            return
        }
        if (novoPreco == null || novoPreco <= 0.0) {
            binding.editNovoPreco.error = "Informe um preço válido"
            return
        }

        lifecycleScope.launch {
            val product = productDao.getById(id)
            if (product == null) {
                Toast.makeText(this@MainActivity, "Produto #$id não encontrado", Toast.LENGTH_SHORT).show()
            } else {
                productDao.update(product.copy(price = novoPreco))
                Toast.makeText(this@MainActivity, "Preço atualizado!", Toast.LENGTH_SHORT).show()
                binding.editId.text.clear()
                binding.editNovoPreco.text.clear()
                mostrarNaTela(productDao.getAll())
            }
        }
    }
}