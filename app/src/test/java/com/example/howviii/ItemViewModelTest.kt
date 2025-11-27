package com.example.howviii

import com.example.howviii.data.ItemRepository
import com.example.howviii.model.Item
import com.example.howviii.viewmodel.ItemViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ItemViewModelTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var documentRef: DocumentReference
    private lateinit var documentSnapshot: DocumentSnapshot
    private lateinit var viewModel: ItemViewModel
    private lateinit var repo: ItemRepository

    @Before
    fun setup() {
        repo = mock()
        firestore = mock()
        documentRef = mock()
        documentSnapshot = mock()

        // Mock da coleção
        whenever(firestore.collection("items")).thenReturn(mock {
            on { document(any()) } doReturn documentRef
        })

        viewModel = ItemViewModel(firestore)
    }

    // -------------------------------
    // TEST loadItem
    // -------------------------------
    @Test
    fun `loadItem - deve preencher o currentItem corretamente`() = runTest {
        val fakeItem = Item(
            title = "Carteira",
            description = "preta",
            local = "Biblioteca",
            imageUrl = "",
            contact = "(00) 00000-0000",
            createdAt = null,
            campusId = "l1AA8qJ58dUPU1VuKhlQbzZtsjc2",
            type = "perdido",
            status = "ativo",
            createdBy = "QoDU0OoPtzO6Nd8dvJiHTNour0X2",
            id = "RrIRSE4ikVSGPFr2duSeJQzxlGt1"
        )

        whenever(documentSnapshot.exists()).thenReturn(true)
        whenever(documentSnapshot.toObject(Item::class.java)).thenReturn(fakeItem)

        // Mock da task get()
        whenever(documentRef.get()).thenReturn(mock {
            on { addOnSuccessListener(any()) } doAnswer {
                val listener = it.arguments[0] as (DocumentSnapshot) -> Unit
                listener(documentSnapshot)
                null
            }
        })

        viewModel.loadItem("RrIRSE4ikVSGPFr2duSeJQzxlGt1")

        assertNotNull(viewModel.currentItem.value)
        assertEquals("Carteira", viewModel.currentItem.value?.title)
    }

    @Test
    fun `saveItem - sucesso chama onSuccess`() = runTest {
        // arrange
        val slotItem = argumentCaptor<Item>()
        whenever(repo.createItem(any())).thenReturn(true)

        var successCalled = false
        var errorCalled = false

        // act
        viewModel.saveItem(
            type = "perdido",
            title = "Teste",
            description = "desc",
            local = "local",
            contact = "contato",
            campusId = "l1AA8qJ58dUPU1VuKhlQbzZtsjc2",
            date = java.util.Date(),
            onSuccess = { successCalled = true },
            onError = { _ -> errorCalled = true }
        )

        // assert
        verify(repo, times(1)).createItem(slotItem.capture())

        assert(successCalled)
        assert(!errorCalled)

        // conferir campos do item passado ao repo
        assert(slotItem.firstValue.title == "Teste")
        assert(slotItem.firstValue.campusId == "l1AA8qJ58dUPU1VuKhlQbzZtsjc2")
    }

    // -------------------------------
    // TEST deleteItem
    // -------------------------------
    @Test
    fun `deleteItem - deve chamar firestore delete`() = runTest {
        whenever(documentRef.delete()).thenReturn(mock())

        viewModel.deleteItem("XYZ")

        verify(documentRef, times(1)).delete()
    }

    // -------------------------------
    // TEST markAsReturned
    // -------------------------------
    @Test
    fun `markAsReturned - deve atualizar campo status`() = runTest {
        whenever(documentRef.update("status", "recuperado")).thenReturn(mock())

        viewModel.markAsReturned("XYZ")

        verify(documentRef, times(1)).update("status", "recuperado")
    }
}
