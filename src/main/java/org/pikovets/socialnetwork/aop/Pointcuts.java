package org.pikovets.socialnetwork.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {
    @Pointcut("execution(* org.pikovets.socialnetwork.service.UserService.get*(..))")
    public void allGetMethods() {}

    @Pointcut("execution(* org.pikovets.socialnetwork.service.UserService.save*(..))")
    public void allSaveMethods() {}

    @Pointcut("execution(* org.pikovets.socialnetwork.service.UserService.update*(..))")
    public void allUpdateMethods() {}

    @Pointcut("execution(* org.pikovets.socialnetwork.service.UserService.delete*(..))")
    public void allDeleteMethods() {}
}