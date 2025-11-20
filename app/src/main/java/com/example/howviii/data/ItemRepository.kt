package com.example.howviii.data

import android.util.Log
import com.example.howviii.model.Item
import com.example.howviii.model.Campus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ItemRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private var lastDocumentSnapshot: com.google.firebase.firestore.DocumentSnapshot? = null

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

        return result.toObjects(Item::class.java)
    }

    // 🔥 CARREGAR LISTA DE CAMPUS
    suspend fun loadCampus(): Map<String, String> {
        val result = db.collection("campus").get().await()

        Log.d("CAMPUS", "Campus: $result")

        // Retornamos um MAPA: uuid → name
        return result.documents.associate { doc ->
            val name = doc.getString("name") ?: ""
            doc.id to name
        }
    }

    fun resetPagination() {
        lastDocumentSnapshot = null
    }
}
