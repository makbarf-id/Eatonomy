package id.makbarf.eatonomy.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.makbarf.eatonomy.data.BudgetRepository
import id.makbarf.eatonomy.utils.BudgetCalculator
import id.makbarf.eatonomy.utils.BudgetManager

class BudgetViewModelFactory(
    private val repository: BudgetRepository,
    private val manager: BudgetManager,
    private val calculator: BudgetCalculator
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository, manager, calculator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 