package com.ylli.api.leaf;

import com.ylli.api.leaf.service.LeafService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leaf")
public class LeafController {

    LeafService leafService;

    public LeafController(LeafService leafService) {
        this.leafService = leafService;
    }

    @GetMapping
    public Object getId(@RequestParam(required = false, defaultValue = "default") String bizTag) {
        return leafService.generateId(bizTag);
    }
}
