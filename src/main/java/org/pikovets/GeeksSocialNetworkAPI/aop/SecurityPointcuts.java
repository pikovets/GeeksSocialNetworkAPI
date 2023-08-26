package org.pikovets.GeeksSocialNetworkAPI.aop;

import org.aspectj.lang.annotation.Pointcut;

public class SecurityPointcuts {
    @Pointcut("execution(* org.pikovets.GeeksSocialNetworkAPI.service.AuthService.register*(..))")
    public void allRegisterMethods() {}

    @Pointcut("execution(* org.pikovets.GeeksSocialNetworkAPI.service.AuthService.login*(..))")
    public void allLoginMethods() {}
}