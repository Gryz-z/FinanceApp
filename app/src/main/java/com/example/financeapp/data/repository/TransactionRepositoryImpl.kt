package com.example.financeapp.data.repository

import com.example.financeapp.data.getOrCreateCategory
import com.example.financeapp.data.local.dao.CategoryDao
import com.example.financeapp.data.local.dao.TransactionDao
import com.example.financeapp.data.toDomain
import com.example.financeapp.data.toEntity
import com.example.financeapp.data.utils.DateUtils
import com.example.financeapp.domain.model.Range
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.ports.TransactionRepository
import com.example.financeapp.security.SessionStore // <-- 5. IMPORTADO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val sessionStore: SessionStore // <-- 5. AÑADIDO
) : TransactionRepository {

    /**
     * Función ayudante para obtener el ID del usuario actual.
     * Si no hay sesión, falla.
     */
    private fun getCurrentUserId(): Result<Long> {
        val userIdString = sessionStore.getToken()
            ?: return Result.failure(Exception("No hay sesión activa"))

        val userId = userIdString.toLongOrNull()
            ?: return Result.failure(Exception("ID de sesión inválido"))

        return Result.success(userId)
    }


    override suspend fun add(tx: Transaction): Result<Unit> {
        // 5. Obtenemos el userId
        val userIdResult = getCurrentUserId()
        if (userIdResult.isFailure) return Result.failure(userIdResult.exceptionOrNull()!!)

        val userId = userIdResult.getOrThrow()

        return try {
            val categoryId = getOrCreateCategory(tx.category, categoryDao)

            // 5. Añadimos el 'userId' al 'copy'
            val entity = tx.toEntity()
                .copy(categoryId = categoryId, userId = userId)

            transactionDao.insertTransaction(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun update(tx: Transaction): Result<Unit> {
        // 5. Obtenemos el userId
        val userIdResult = getCurrentUserId()
        if (userIdResult.isFailure) return Result.failure(userIdResult.exceptionOrNull()!!)
        val userId = userIdResult.getOrThrow()

        return try {
            val categoryId = getOrCreateCategory(tx.category, categoryDao)

            // 5. Añadimos el 'userId' al 'copy'
            val entity = tx.toEntity().copy(categoryId = categoryId, userId = userId)

            transactionDao.updateTransaction(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> {
        // 5. Obtenemos el userId
        val userIdResult = getCurrentUserId()
        if (userIdResult.isFailure) return Result.failure(userIdResult.exceptionOrNull()!!)
        val userId = userIdResult.getOrThrow()

        return try {
            // 5. Pasamos el userId al DAO
            transactionDao.deleteTransactionById(id.toLong(), userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getByRange(range: Range, anchorEpoch: Long): List<Transaction> {
        // 5. Obtenemos el userId
        val userIdResult = getCurrentUserId()
        if (userIdResult.isFailure) return emptyList()
        val userId = userIdResult.getOrThrow()

        return try {
            val (startDate, endDate) = DateUtils.getStartAndEndDates(range, anchorEpoch)

            // 5. Pasamos el userId al DAO
            val entities = transactionDao.getTransactionsByRange(startDate, endDate, userId)

            // Usamos withContext para no bloquear el hilo principal
            // si hay muchas categorías que buscar
            withContext(Dispatchers.IO) {
                entities.map { entity ->
                    val category = categoryDao.getCategoryById(entity.categoryId)
                    entity.toDomain(categoryName = category?.name ?: "Desconocida")
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTotalsByCategory(range: Range, anchorEpoch: Long): Map<String, Long> {
        // 5. Obtenemos el userId
        val userIdResult = getCurrentUserId()
        if (userIdResult.isFailure) return emptyMap()
        val userId = userIdResult.getOrThrow()

        return try {
            val (startDate, endDate) = DateUtils.getStartAndEndDates(range, anchorEpoch)

            // 5. Pasamos el userId al DAO
            val spendingList = transactionDao.getSpendingByCategory(startDate, endDate, userId)

            // Usamos withContext para no bloquear el hilo principal
            withContext(Dispatchers.IO) {
                spendingList.mapNotNull { spending ->
                    val category = categoryDao.getCategoryById(spending.categoryId)
                    if (category != null) {
                        category.name to spending.total
                    } else {
                        null
                    }
                }.toMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}