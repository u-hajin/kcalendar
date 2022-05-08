package com.kkwakjavacoding.kcalendar.fooddatabase

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// DB에 직접 접근x, Repository에서 데이터 통신
class FoodViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<Food>>
    private val repository: FoodRepository

    init {
        val foodDao = FoodDatabase.getDatabase(application)!!.foodDao()
        repository = FoodRepository(foodDao)
        readAllData = repository.readAllData.asLiveData()
    }

    fun addFood(food: Food) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFood(food)
        }
    }

    fun updateUser(food: Food) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFood(food)
        }
    }

    fun deleteUser(food: Food) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFood(food)
        }
    }

    // ViewModel에 파라미터를 넘기기 위해서, 파라미터를 포함한 Factory 객체를 생성하기 위한 클래스
    class Factory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FoodViewModel(application) as T
        }
    }

    fun searchName(searchQuery: String): LiveData<List<Food>> {
        return repository.searchName(searchQuery).asLiveData()
    }

    fun searchClassification(searchQuery: String): LiveData<List<Food>> {
        return repository.searchClassification(searchQuery).asLiveData()
    }

}