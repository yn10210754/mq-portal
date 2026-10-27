package io.github.mqportal.cluster.controller;

import io.github.mqportal.cluster.dto.ClusterOverviewVO;
import io.github.mqportal.cluster.service.ClusterService;
import io.github.mqportal.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cluster")
@Tag(name = "cluster")
public class ClusterController {

    private final ClusterService clusterService;

    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @GetMapping("/overview")
    public Result<ClusterOverviewVO> overview() {
        return Result.ok(clusterService.overview());
    }
}
