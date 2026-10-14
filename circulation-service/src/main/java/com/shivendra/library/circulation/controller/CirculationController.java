package com.shivendra.library.circulation.controller;

import com.shivendra.library.circulation.dto.IssueRequest;
import com.shivendra.library.circulation.dto.IssueResponse;
import com.shivendra.library.circulation.service.CirculationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/circulation")
public class CirculationController {

    private final CirculationService service;

    public CirculationController(CirculationService service) {
        this.service = service;
    }

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponse issue(@Valid @RequestBody IssueRequest request) {
        return service.issue(request);
    }

    @PostMapping("/return/{issueId}")
    public IssueResponse returnBook(@PathVariable Long issueId) {
        return service.returnBook(issueId);
    }

    @GetMapping("/member/{memberId}")
    public List<IssueResponse> history(@PathVariable Long memberId) {
        return service.historyForMember(memberId);
    }
}
