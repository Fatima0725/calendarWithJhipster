package com.groupeisi.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.time.DayOfWeek;

@RestController
@RequestMapping("/services/calendar")
public class CalendarDayFinderResource {
    @GetMapping("/services/calendar/dayfinder")
    public ResponseEntity<Map<String, String>> findDayOfWeek(@RequestParam("date") String dateString) {
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String dayOfWeekString = dayOfWeek.toString();
        // Map pour stocker la réponse JSON
        Map<String, String> response = new HashMap<>();
        response.put("date", date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        response.put("dayOfWeek", dayOfWeekString);
        return ResponseEntity.ok(response);
    }
}
