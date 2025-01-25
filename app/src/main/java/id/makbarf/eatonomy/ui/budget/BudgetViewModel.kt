package id.makbarf.eatonomy.ui.budget

import androidx.lifecycle.*
import id.makbarf.eatonomy.data.*
import id.makbarf.eatonomy.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val budgetManager: BudgetManager,
    private val calculator: BudgetCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val uiState: StateFlow<BudgetUiState> = _uiState

    private val _activeBudget = MutableLiveData<Budget?>()
    val activeBudget: LiveData<Budget?> = _activeBudget

    private val _budgetWarnings = MutableLiveData<List<String>>()
    val budgetWarnings: LiveData<List<String>> = _budgetWarnings

    private val _spendingProgress = MutableLiveData<Double>()
    val spendingProgress: LiveData<Double> = _spendingProgress

    private val _dailyAllowance = MutableLiveData<Double>()
    val dailyAllowance: LiveData<Double> = _dailyAllowance

    init {
        loadActiveBudget()
    }

    private fun loadActiveBudget() {
        viewModelScope.launch {
            try {
                _uiState.value = BudgetUiState.Loading
                val budget = budgetRepository.getActiveBudgetForDate(LocalDate.now()).value
                if (budget != null) {
                    _activeBudget.value = budget
                    updateBudgetAnalysis(budget)
                    _uiState.value = BudgetUiState.Success(budget)
                } else {
                    _uiState.value = BudgetUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = BudgetUiState.Error(e.message ?: "Failed to load budget")
            }
        }
    }

    private fun updateBudgetAnalysis(budget: Budget) {
        _spendingProgress.value = calculator.calculateSpendingProgress(budget)
        _dailyAllowance.value = calculator.calculateDailyAllowance(budget)
        _budgetWarnings.value = budgetManager.getBudgetWarnings(budget)
    }

    fun createBudget(
        name: String,
        amount: Double,
        currency: String,
        startDate: LocalDate,
        endDate: LocalDate,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = BudgetUiState.Loading
                val budget = Budget(
                    name = name,
                    amount = amount,
                    currency = currency,
                    startDate = startDate,
                    endDate = endDate,
                    status = BudgetStatus.ACTIVE,
                    notes = notes
                )
                budgetManager.createBudget(budget).fold(
                    onSuccess = {
                        _uiState.value = BudgetUiState.Success(it)
                        loadActiveBudget()
                    },
                    onFailure = { e ->
                        _uiState.value = BudgetUiState.Error(e.message ?: "Failed to create budget")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = BudgetUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateBudgetAmount(newAmount: Double) {
        viewModelScope.launch {
            _activeBudget.value?.let { budget ->
                try {
                    budgetManager.updateBudgetAmount(budget.id, newAmount).fold(
                        onSuccess = { loadActiveBudget() },
                        onFailure = { e ->
                            _uiState.value = BudgetUiState.Error(e.message ?: "Failed to update budget")
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = BudgetUiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }

    fun recordExpense(amount: Double) {
        viewModelScope.launch {
            _activeBudget.value?.let { budget ->
                try {
                    budgetManager.recordExpense(budget.id, amount).fold(
                        onSuccess = { loadActiveBudget() },
                        onFailure = { e ->
                            _uiState.value = BudgetUiState.Error(e.message ?: "Failed to record expense")
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = BudgetUiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }

    fun closeBudget() {
        viewModelScope.launch {
            _activeBudget.value?.let { budget ->
                try {
                    budgetManager.closeBudget(budget.id).fold(
                        onSuccess = { loadActiveBudget() },
                        onFailure = { e ->
                            _uiState.value = BudgetUiState.Error(e.message ?: "Failed to close budget")
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = BudgetUiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }
}

sealed class BudgetUiState {
    object Loading : BudgetUiState()
    object Empty : BudgetUiState()
    data class Success(val budget: Budget) : BudgetUiState()
    data class Error(val message: String) : BudgetUiState()
} 