package com.sanket.service;

import com.sanket.dto.ExpenseDTO;
import com.sanket.dto.IncomeDTO;
import com.sanket.entity.CategoryEntity;
import com.sanket.entity.ExpenseEntity;
import com.sanket.entity.IncomeEntity;
import com.sanket.entity.ProfileEntity;
import com.sanket.repository.CategoryRepository;
import com.sanket.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + dto.getCategoryId()));

        IncomeEntity newIncome = toEntity(dto, profile, category);

        newIncome = incomeRepository.save(newIncome);
        return toDTO(newIncome);
    }

    // Retrieves all incomes for the current month based on the start and end dates
    public List<IncomeDTO> getIncomesForCurrentMonth() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startOfMonth, endOfMonth);
        return incomes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // delete income by ID for the current user
    public void deleteIncomeById(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("income not found with ID: " + incomeId));
        if(!income.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Income does not belong to the current user");
        }
        incomeRepository.delete(income);
    }

    // Get latest 5 incomes for the current user
    public List<IncomeDTO> getLatest5IncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return incomes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Get total income for the current user
    public BigDecimal getTotalIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    // filter incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return incomes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // helper methods
    private IncomeEntity toEntity(IncomeDTO incomeDTO, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .date(incomeDTO.getDate())
                .category(category)
                .profile(profile)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity incomeEntity) {
        return IncomeDTO.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getName())
                .icon(incomeEntity.getIcon())
                .amount(incomeEntity.getAmount())
                .date(incomeEntity.getDate())
                .categoryId(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getId() : null)
                .categoryName(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getName() : "N/A")
                .createdAt(incomeEntity.getCreatedAt())
                .updatedAt(incomeEntity.getUpdatedAt())
                .build();
    }
}
