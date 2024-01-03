package com.octopus.core.replay;

import lombok.NonNull;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/1/11
 */
public final class ReplayFilters {

    public static ReplayFilter status(int... statusArray) {
        return or(Arrays.stream(statusArray).mapToObj(HttpStatusReplayFilter::new).toArray(ReplayFilter[]::new));
    }


    public static ReplayFilter and(@NonNull ReplayFilter... filters) {
        return (request -> {
            if (filters == null) {
                return false;
            }
            for (ReplayFilter replayFilter : filters) {
                if (!replayFilter.filter(request)) {
                    return false;
                }
            }
            return true;
        });
    }

    public static ReplayFilter or(@NonNull ReplayFilter... filters) {
        return (request -> {
            if (filters == null) {
                return false;
            }
            for (ReplayFilter replayFilter : filters) {
                if (replayFilter.filter(request)) {
                    return true;
                }
            }
            return false;
        });
    }

    public static ReplayFilter not(@NonNull ReplayFilter filter) {
        return (request -> !filter.filter(request));
    }

    public static ReplayFilter all() {
        return ((request) -> true);
    }
}
