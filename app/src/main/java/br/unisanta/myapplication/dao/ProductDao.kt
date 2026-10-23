package br.unisanta.myapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.unisanta.myapplication.model.Product

@Dao
interface ProductDao {

    @Query("SELECT * FROM product")
    suspend fun getAll(): List<Product>

    @Insert
    suspend fun insertAll(vararg products: Product)

    // busca por nome, usar "%texto%" para nomes parecidos
    @Query("SELECT * FROM product WHERE name LIKE :nome")
    suspend fun buscarPorNome(nome: String): List<Product>

    // excluir todos
    @Query("DELETE FROM product")
    suspend fun deleteAll()

    // a parte do desafio extra, excluir um produto específico
    @Delete
    suspend fun delete(product: Product)

    // buscar um produto pelo id
    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getById(id: Int): Product?

    // atualizar produto
    @Update
    suspend fun update(product: Product)
}