package com.ylli.api.leaf;

import com.ylli.api.leaf.service.LeafService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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

    @GetMapping("/test")
    public Object test() throws InterruptedException {
//        AsyncLoadingCache<String, String> asyncLoadingCache = Caffeine.newBuilder()
//                .expireAfterWrite(1, java.util.concurrent.TimeUnit.SECONDS)
//                .buildAsync((key, executor) -> {
//                    return CompletableFuture.supplyAsync(() -> {
//                        return "hello";
//                    }, executor);
//                });
//
//        return asyncLoadingCache.get("2");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(4,
                8,
                100,
                java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>());

        for (int i = 0; i < 10000; i++) {
            CompletableFuture.runAsync(() -> {
                System.out.println(leafService.generateId("default"));
            }, executor);
        }
        return "ok";
    }
}
