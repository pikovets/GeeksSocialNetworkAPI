package org.pikovets.GeeksSocialNetworkAPI.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.pikovets.GeeksSocialNetworkAPI.core.CustomResponse;
import org.pikovets.GeeksSocialNetworkAPI.core.CustomStatus;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.NoSuchElementException;

@Component
@Aspect
@Slf4j
public class SecurityAspect {
    @Around("SecurityPointcuts.allRegisterMethods()")
    public Object aroundRegisterAdvice(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        String username = null;

        if (methodSignature.getName().equals("registerUser")) {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof User) {
                    username = ((User) arg).getUsername();
                    log.info("Trying to register a user with {} username", username);
                }
            }
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (NoSuchElementException e) {
            log.error(e.getMessage(), e);
            result = new CustomResponse<>(Collections.emptyList(), CustomStatus.NOT_FOUND);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            result = new CustomResponse<>(Collections.emptyList(), CustomStatus.EXCEPTION);
        }

        if (methodSignature.getName().equals("registerUser")) {
            log.info("User with {} username successfully registered", username);
        }

        return result;
    }

    @Around("SecurityPointcuts.allLoginMethods()")
    public Object aroundLoginAdvice(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        String username = null;

        if (methodSignature.getName().equals("loginUser")) {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof String) {
                    username = (String) arg;
                    log.info("Attempting to log in a user named {}", username);
                }
            }
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (NoSuchElementException e) {
            log.error(e.getMessage(), e);
            result = new CustomResponse<>(Collections.emptyList(), CustomStatus.NOT_FOUND);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            result = new CustomResponse<>(Collections.emptyList(), CustomStatus.EXCEPTION);
        }

        if (methodSignature.getName().equals("loginUser")) {
            log.info("User with {} username successfully logged in", username);
        }

        return result;
    }
}
