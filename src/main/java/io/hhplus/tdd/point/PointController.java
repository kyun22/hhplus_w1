package io.hhplus.tdd.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.hhplus.tdd.dto.PointHistoryListResponse;
import io.hhplus.tdd.dto.UserPointRequest;
import io.hhplus.tdd.dto.UserPointResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    @GetMapping("{id}")
    public ResponseEntity<UserPointResponse> point(
            @PathVariable long id
    ) {
        log.info("select userPoint called. user id : {}", id);
		return ResponseEntity.ok(pointService.getUserPoint(id));
    }

    @GetMapping("{id}/histories")
    public ResponseEntity<PointHistoryListResponse> history(
            @PathVariable long id
    ) {
        log.info("select pointHistory called. user id : {}", id);
		return ResponseEntity.ok(pointService.getPointHistory(id));
    }

    @PatchMapping("{id}/charge")
    public ResponseEntity<UserPointResponse> charge(
            @PathVariable long id,
            @RequestBody @Valid UserPointRequest.Charge amount
    ) {
        log.info("charge userPoint. userId : {}, amount : {}", id, amount.getAmount());
		return ResponseEntity.ok(pointService.charge(id, amount));
    }

    @PatchMapping("{id}/use")
    public ResponseEntity<UserPointResponse> use(
            @PathVariable long id,
            @RequestBody @Valid UserPointRequest.Use amount
    ) {
        log.info("use userPoint. userId : {}, amount : {}", id, amount.getAmount());
        return ResponseEntity.ok(pointService.use(id, amount));
    }
}
