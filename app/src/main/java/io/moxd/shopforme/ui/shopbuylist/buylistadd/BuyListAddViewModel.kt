package io.moxd.shopforme.ui.shopbuylist.buylistadd

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.api.ApiBuyListAdd
import io.moxd.shopforme.data.model.*
import io.moxd.shopforme.utils.requireAuthManager
import kotlinx.coroutines.launch
import retrofit2.Response


class BuyListAddViewModel @AssistedInject constructor(
        @Assisted savedStateHandle: SavedStateHandle,
        val apiBuyListAdd: ApiBuyListAdd

) : ViewModel() {
    val sessionId: String = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()

    private val _items = MutableLiveData<Response<List<ItemGSON>>>()

    val Items: LiveData<Response<List<ItemGSON>>> = _items

    private val _buylist = MutableLiveData<Response<BuyListGSON>>()
    val BuyList: LiveData<Response<BuyListGSON>> = _buylist

    private val _articles = MutableLiveData<Response<List<ArticleAddGson>>>()
    val Articles: LiveData<Response<List<ArticleAddGson>>> = _articles


    init {
        getItems()
    }


    fun createBuylist() {
        viewModelScope.launch {
            val list = mutableListOf<ArticleGson>()
            for (item in Items.value?.body()!!.filter { it.anzahl.value!! > 0 })
                list.add(ArticleGson(item.id, item.anzahl.value!!))

            _buylist.value = apiBuyListAdd.createBuyList(BuyListCreate(sessionId, list))
        }
    }

    fun getItems() {
        viewModelScope.launch {
            _items.value = apiBuyListAdd.getItems()
        }
    }
}