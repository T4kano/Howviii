package com.example.howviii.data

import android.util.Log
import com.example.howviii.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ItemRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private var lastDocumentSnapshot: com.google.firebase.firestore.DocumentSnapshot? = null

    // 🔥 CRIAR ITEM
    suspend fun createItem(item: Item): Boolean {
        return try {
            db.collection("items").add(item).await()
            true
        } catch (e: Exception) {
            Log.e("ITEM_REPO", "Erro ao criar item", e)
            false
        }
    }

    // 🔥 ATUALIZAR ITEM
    suspend fun updateItem(id: String, data: Map<String, Any>): Boolean {
        return try {
            db.collection("items").document(id).update(data).await()
            true
        } catch (e: Exception) {
            Log.e("ITEM_REPO", "Erro ao atualizar item", e)
            false
        }
    }

    // 🔥 APAGAR ITEM
    suspend fun deleteItem(id: String): Boolean {
        return try {
            db.collection("items").document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e("ITEM_REPO", "Erro ao deletar item", e)
            false
        }
    }

    // 🔥 OBTER ITEM POR ID (substitui loadItem)
    suspend fun getItemById(id: String): Item? {
        return try {
            val doc = db.collection("items").document(id).get().await()
            doc.toObject(Item::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("ITEM_REPO", "Erro ao buscar item por ID", e)
            null
        }
    }

    // 🔥 CARREGAR ITEMS COM PAGINAÇÃO + FILTROS
    suspend fun loadItemsPaged(
        campusId: String?,
        search: String?,
        limit: Long = 10
    ): List<Item> {

        var query: Query = db.collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)

        if (!campusId.isNullOrEmpty()) {
            query = query.whereEqualTo("campusId", campusId)
        }

        if (!search.isNullOrEmpty()) {
            query = query.whereGreaterThanOrEqualTo("title", search)
                .whereLessThanOrEqualTo("title", search + "\uf8ff")
        }

        lastDocumentSnapshot?.let {
            query = query.startAfter(it)
        }

        val result = query.get().await()

        if (!result.isEmpty) {
            lastDocumentSnapshot = result.documents.last()
        }

        return result.documents.map { doc ->
            doc.toObject(Item::class.java)!!.copy(id = doc.id)
        }
    }

    // 🔥 CARREGAR LISTA DE CAMPUS
    suspend fun loadCampus(): Map<String, String> {
        val result = db.collection("campus").get().await()

        Log.d("CAMPUS", "Campus: $result")

        return result.documents.associate { doc ->
            val name = doc.getString("name") ?: ""
            doc.id to name
        }
    }

    fun resetPagination() {
        lastDocumentSnapshot = null
    }
}
