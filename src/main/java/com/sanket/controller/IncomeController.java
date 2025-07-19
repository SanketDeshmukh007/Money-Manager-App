package com.sanket.controller;

import com.sanket.dto.ExpenseDTO;
import com.sanket.dto.IncomeDTO;
import com.sanket.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDTO> addIncome(@RequestBody IncomeDTO incomeDTO) {
        IncomeDTO saved = incomeService.addIncome(incomeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getIncomesForCurrentMonth() {
        List<IncomeDTO> incomes = incomeService.getIncomesForCurrentMonth();
        return ResponseEntity.ok(incomes);
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<Void> deleteIncomeById(@PathVariable Long incomeId) {
        incomeService.deleteIncomeById(incomeId);
        return ResponseEntity.noContent().build();
    }
}
