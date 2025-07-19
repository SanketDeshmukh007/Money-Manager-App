package com.sanket.service;

import com.sanket.dto.ExpenseDTO;
import com.sanket.entity.ProfileEntity;
import com.sanket.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "IST") // Every day at 10 PM
    // @Scheduled(cron = "0 * * * * *", zone = "IST") // every minute for testing purposes
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job started: Sending daily income and expense reminder emails");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            String body = "Hi " + profile.getFullName() + ",<br><br>"
                    + "This is a friendly reminder to add your income and expenses for today in Money Manager.<br><br>"
                    + "<a href=" + frontendUrl + " style='display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: #fff; text-decoration: none; border-radius: 5px; font-weight:bold'>Go to Money Manager</a>"
                    + "<br><br>Best regards,<br>Money Manager Team";
            emailService.sendEmail(profile.getEmail(), "Daily reminder: Add your income and expenses", body);
        }
        log.info("Job completed: Daily income and expense reminder emails sent");
    }

     @Scheduled(cron = "0 0 23 * * *", zone = "IST") // Every day at 11 PM
//     @Scheduled(cron = "0 * * * * *", zone = "IST") // every minute for testing purposes
    public void sendDailyExpenseSummary() {
        log.info("Job started: Sending daily expense summary emails");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            List<ExpenseDTO> expenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());
            if(!expenses.isEmpty()) {
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse: collapse; width: 100%;'>")
                        .append("<tr style='background-color: #f2f2f2;'>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px;'>S.No</th>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px;'>Name</th>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px;'>Amount</th>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px;'>Category</th>")
                        .append("</tr>");
                int i = 1;
                for(ExpenseDTO expenseDTO : expenses) {
                    table.append("<tr>")
                            .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(i++).append("</td>")
                            .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expenseDTO.getName()).append("</td>")
                            .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expenseDTO.getAmount()).append("</td>")
                            .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expenseDTO.getCategoryId() != null ? expenseDTO.getCategoryName() : "N/A").append("</td>")
                            .append("</tr>");
                }
                table.append("</table>");
                String body = "Hi " + profile.getFullName() + ",<br><br>"
                        + "Here is your expense summary for today:<br><br>"
                        + table.toString()
                        + "<br><br>Best regards,<br>Money Manager Team";
                emailService.sendEmail(profile.getEmail(), "Daily Expense Summary", body);
            }
        }
        log.info("Job completed: Daily expense summary emails sent");
    }
}
