package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.domain.ActRecord;
import com.nikulshin.detailing.service.ActRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/acts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // При необходимости настройте CORS под ваш фронтенд
public class ActRecordController {

    private final ActRecordService actRecordService;

    // GET /api/reports/acts?year=2026&month=6
    @GetMapping
    public ResponseEntity<List<ActRecord>> getActsSummary(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        List<ActRecord> acts = actRecordService.getActsSummary(year, month);
        return ResponseEntity.ok(acts);
    }

    // POST /api/reports/acts
    @PostMapping
    public ResponseEntity<ActRecord> saveActRecord(@RequestBody ActRecord actRecord) {
        ActRecord savedAct = actRecordService.saveOrUpdateAct(actRecord);
        return ResponseEntity.ok(savedAct);
    }

    // DELETE /api/reports/acts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActRecord(@PathVariable("id") Long id) {
        actRecordService.deleteAct(id);
        return ResponseEntity.noContent().build();
    }
}