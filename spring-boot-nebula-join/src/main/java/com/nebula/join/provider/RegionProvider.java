package com.nebula.join.provider;

import java.util.List;

public interface RegionProvider {

    List<Long> getRegionIds(Long userId);


    Long getCurrentUserId();
    
}
