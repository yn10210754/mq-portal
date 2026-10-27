package io.github.mqportal.cluster.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ClusterOverviewVO {

    private List<ClusterVO> clusters;
}
