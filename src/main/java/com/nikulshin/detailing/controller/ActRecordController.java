package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.domain.ActRecord;
import com.nikulshin.detailing.service.ActRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/acts")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*") // При необходимости настройте CORS под ваш фронтенд
public class ActRecordController {

    private final ActRecordService actRecordService;

    // GET /api/acts?year=2026&month=6
    @GetMapping
    public ResponseEntity<List<ActRecord>> getActsSummary(
            @RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month) {
        try {
            List<ActRecord> acts = actRecordService.getActsSummary(year, month);
            return ResponseEntity.ok(acts);
        } catch (Exception e) {
            e.printStackTrace(); // <-- Это залогает реальную ошибку в консоль!
            throw e;
        }
    }

    // POST /api/acts
    @PostMapping
    public ResponseEntity<ActRecord> saveActRecord(@RequestBody ActRecord actRecord) {
        ActRecord savedAct = actRecordService.saveOrUpdateAct(actRecord);
        return ResponseEntity.ok(savedAct);
    }

    // DELETE /api/acts/{id}
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteActRecord(@PathVariable Long id) {
        actRecordService.deleteAct(id);
        return ResponseEntity.noContent().build();
    }
}