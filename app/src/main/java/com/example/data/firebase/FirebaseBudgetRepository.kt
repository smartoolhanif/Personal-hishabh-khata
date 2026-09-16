package com.example.data.firebase

import com.example.domain.model.BudgetGoal
import com.example.domain.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseBudgetRepository : BudgetRepository {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }

    private val currentUserId: String?
        get() = auth?.currentUser?.uid

    private fun getBudgetsCollection() = currentUserId?.let { uid ->
        firestore?.collection("users")?.document(uid)?.collection("budgets")
    }

    override fun getBudgets(monthYear: String): Flow<List<BudgetGoal>> {
        val collection = getBudgetsCollection() ?: return emptyFlow()
        return collection.whereEqualTo("monthYear", monthYear).snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(BudgetGoal::class.java) }
        }
    }

    override suspend fun setBudget(budget: BudgetGoal): Result<Unit> {
        val collection = getBudgetsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val docId = if (budget.id.isNotBlank()) budget.id else "${budget.monthYear}_${budget.categoryId}"
            collection.document(docId).set(budget.copy(id = docId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBudget(budgetId: String): Result<Unit> {
        val collection = getBudgetsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(budgetId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
