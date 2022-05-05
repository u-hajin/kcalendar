package com.kkwakjavacoding.kcalendar.weightdatabase

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<Weight>>
    private val repository: WeightRepository

    init {
        val weightDao = WeightDatabase.getDatabase(application)!!.weightDao()
        repository = WeightRepository(weightDao)
        readAllData = repository.readAllData.asLiveData()
    }

    fun addWeight(weight: Weight) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addWeight(weight)
        }
    }

    fun updateWeight(weight: Weight) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWeight(weight)
        }
    }

    fun deleteWeight(weight: Weight) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWeight(weight)
        }
    }

    class Factory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeightViewModel(application) as T
        }
    }

    fun searchDatabase(searchQuery: String): LiveData<List<Weight>> {
        return repository.searchDatabase(searchQuery).asLiveData()
    }

    fun getSameMonth(compare: String): LiveData<List<Weight>> {
        return repository.getSameMonth(compare).asLiveData()
    }

}