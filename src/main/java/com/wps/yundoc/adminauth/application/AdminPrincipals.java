package com.wps.yundoc.adminauth.application;

import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;

import javax.servlet.http.HttpServletRequest;

/**
 * Admin principal access helpers.
 *
 * @author wps
 */
public final class AdminPrincipals {

    private AdminPrincipals() {
    }

    public static AdminPrincipal require(HttpServletRequest request) {
        Object principal = request.getAttribute(AdminPrincipal.REQUEST_ATTRIBUTE);
        if (principal instanceof AdminPrincipal) {
            return (AdminPrincipal) principal;
        }
        throw new YundocException(YundocErrorCode.AUTH_REQUIRED);
    }
}
