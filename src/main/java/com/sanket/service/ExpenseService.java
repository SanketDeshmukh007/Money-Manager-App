package com.sanket.service;

import com.sanket.dto.ExpenseDTO;
import com.sanket.entity.CategoryEntity;
import com.sanket.entity.ExpenseEntity;
import com.sanket.repository.ExpenseRepository;
import com.sanket.entity.ProfileEntity;
import com.sanket.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + dto.getCategoryId()));

        ExpenseEntity newExpense = toEntity(dto, profile, category);

        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }

    // Retrieves all expenses for the current month based on the start and end dates
    public List<ExpenseDTO> getExpensesForCurrentMonth() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startOfMonth, endOfMonth);
        return expenses.stream().map(this::toDTO).toList();
    }

    // delete expense by ID for the current user
    public void deleteExpenseById(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with ID: " + expenseId));
        if(!expense.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Expense does not belong to the current user");
        }
        expenseRepository.delete(expense);
    }

    // Get latest 5 expenses for the current user
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return expenses.stream().map(this::toDTO).toList();
    }

    // Get total expenses for the current user
    public BigDecimal getTotalExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    // filter expenses
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return expenses.stream().map(this::toDTO).toList();
    }

    // Notifications
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDate(profileId, date);
        return expenses.stream().map(this::toDTO).toList();
    }

    // helper methods
    private ExpenseEntity toEntity(ExpenseDTO expenseDTO, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate())
                .category(category)
                .profile(profile)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity expenseEntity) {
        return ExpenseDTO.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getName())
                .icon(expenseEntity.getIcon())
                .amount(expenseEntity.getAmount())
                .date(expenseEntity.getDate())
                .categoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getId() : null)
                .categoryName(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getName() : "N/A")
                .createdAt(expenseEntity.getCreatedAt())
                .updatedAt(expenseEntity.getUpdatedAt())
                .build();
    }
}
