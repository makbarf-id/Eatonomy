package id.makbarf.eatonomy.ui.household

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import id.makbarf.eatonomy.data.FoodDatabase
import id.makbarf.eatonomy.data.HouseholdMember
import id.makbarf.eatonomy.data.HouseholdMemberRepository
import kotlinx.coroutines.launch

class HouseholdMemberViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HouseholdMemberRepository
    val allMembers: LiveData<List<HouseholdMember>>

    init {
        val householdMemberDao = FoodDatabase.getDatabase(application).householdMemberDao()
        repository = HouseholdMemberRepository(householdMemberDao)
        allMembers = repository.allMembers
    }

    fun delete(member: HouseholdMember) = viewModelScope.launch {
        repository.delete(member)
    }

    fun getMemberById(id: Int): LiveData<HouseholdMember> {
        return repository.getMemberById(id)
    }

    fun insert(member: HouseholdMember) = viewModelScope.launch {
        repository.insert(member)
    }

    fun update(member: HouseholdMember) = viewModelScope.launch {
        repository.update(member)
    }
} 