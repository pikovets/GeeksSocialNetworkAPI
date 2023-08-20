package org.pikovets.GeeksSocialNetworkAPI.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {
    @Pointcut("execution(* org.pikovets.GeeksSocialNetworkAPI.service.UserService.get*(..))")
    public void allGetMethods() {}

    @Pointcut("execution(* org.pikovets.GeeksSocialNetworkAPI.service.UserService.save*(..))")
    public void allSaveMethods() {}

    @Pointcut("execution(* org.pikovets.GeeksSocialNetworkAPI.service.UserService.update*(..))")
    public void allUpdateMethods() {}

    @Pointcut("execution(* org.pikovets.GeeksSocialNetworkAPI.service.UserService.delete*(..))")
    public void allDeleteMethods() {}
}