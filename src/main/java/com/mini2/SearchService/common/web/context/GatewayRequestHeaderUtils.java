package com.mini2.SearchService.common.web.context;

import com.mini2.SearchService.common.exception.NotFound;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class GatewayRequestHeaderUtils {
    public static String getRequestHeaderParamAsString(String key) {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();return requestAttributes.getRequest().getHeader(key);
    }

    public static String getUserId() {
        String userId = getRequestHeaderParamAsString("X-Auth-userId");
        if (userId == null) {
            return null;
        }
        return userId;
    }

    public static String getUserIdOrThrowException() {
        String userId = getUserId();
        if (userId == null) {
            throw new NotFound("헤더에userId 정보가 없습니다.");
        }
        return userId;
    }
}
