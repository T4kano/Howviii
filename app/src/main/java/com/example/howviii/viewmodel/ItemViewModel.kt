package com.example.howviii.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howviii.model.Campus
import com.example.howviii.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ItemViewModel(firestore: FirebaseFirestore) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // -------------------------------
    // 🔥 ESTADOS PARA A UI
    // -------------------------------

    private val _campusMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val campusMap = _campusMap.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items = _items.asStateFlow()

    private val _currentItem = MutableStateFlow<Item?>(null)
    val currentItem = _currentItem.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _endReached = MutableStateFlow(false)
    val endReached = _endReached.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null

    // -------------------------------------
    // 1️⃣ AUTENTICAÇÃO ANÔNIMA
    // -------------------------------------
    fun ensureAnonymousAuth() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
        }
    }

    // -------------------------------------
    // 2️⃣ CARREGAR CAMPUS DO FIRESTORE
    // -------------------------------------
    fun loadCampus() {
        viewModelScope.launch {
            val result = db.collection("campus").get().await()

            val map = result.documents.associate { doc ->
                val campus = doc.toObject(Campus::class.java)
                doc.id to (campus?.name.orEmpty())
            }

            _campusMap.value = map
        }
    }

    // -------------------------------------
    // 3️⃣ PAGINAÇÃO DE ITENS
    // -------------------------------------
    fun resetPagination() {
        lastDocument = null
        _endReached.value = false
        _items.value = emptyList()
    }

    fun loadItems(
        campusId: String?,
        search: String?
    ) {
        viewModelScope.launch {
            _loading.value = true

            var query: Query = db.collection("items")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)

            if (!campusId.isNullOrEmpty()) {
                query = query.whereEqualTo("campusId", campusId)
            }

            if (!search.isNullOrEmpty()) {
                query = query
                    .whereGreaterThanOrEqualTo("title", search)
                    .whereLessThanOrEqualTo("title", search + "\uf8ff")
            }

            if (lastDocument != null) {
                query = query.startAfter(lastDocument!!)
            }

            val result = query.get().await()
            val newItems = result.toObjects(Item::class.java)

            if (result.documents.isNotEmpty()) {
                lastDocument = result.documents.last()
            } else {
                _endReached.value = true
            }

            _items.value = _items.value + newItems
            _loading.value = false
        }
    }

    // -------------------------------------
    // 4️⃣ SALVAR ITEM NO FIRESTORE
    // -------------------------------------
    fun saveItem(
        type: String,
        title: String,
        description: String,
        local: String,
        contact: String,
        campusId: String,
        date: Date,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val doc = db.collection("items").document()

                val item = Item(
                    title = title,
                    description = description,
                    local = local,
                    contact = contact,
                    imageUrl = "https://picsum.photos/200",
                    campusId = campusId,
                    createdBy = auth.currentUser?.uid ?: "anonymous",
                    createdAt = date,
                    updatedAt = null,
                    status = "ativo",
                    type = type
                )

                doc.set(item).await()

                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun loadItem(uuid: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("items")
                    .document(uuid)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val item = snapshot.toObject(Item::class.java)
                    _currentItem.value = item?.copy(id = snapshot.id)
                } else {
                    _currentItem.value = null
                }

            } catch (e: Exception) {
                _currentItem.value = null
            }
        }
    }

    fun markAsReturned(uuid: String) {
        viewModelScope.launch {
            try {
                db.collection("items")
                    .document(uuid)
                    .update("type", "recuperado")
                    .await()

                // Atualiza o estado local
                _currentItem.value = _currentItem.value?.copy(type = "recuperado")

            } catch (e: Exception) {
                Log.e("ITEM_VM", "Erro ao marcar como recuperado", e)
            }
        }
    }

    fun deleteItem(uuid: String) {
        viewModelScope.launch {
            try {
                db.collection("items")
                    .document(uuid)
                    .delete()
                    .await()

                // Remove o item atual da memória
                _currentItem.value = null

            } catch (e: Exception) {
                Log.e("ITEM_VM", "Erro ao deletar item", e)
            }
        }
    }
}
