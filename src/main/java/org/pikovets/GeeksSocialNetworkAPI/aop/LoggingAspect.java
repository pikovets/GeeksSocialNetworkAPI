package org.pikovets.GeeksSocialNetworkAPI.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.core.CustomResponse;
import org.pikovets.GeeksSocialNetworkAPI.core.CustomStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.UUID;

@Component
@Aspect
@Slf4j
public class LoggingAspect {

    @Around("Pointcuts.allGetMethods()")
    public Object aroundGettingAdvice(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        UUID id = null;

        if (methodSignature.getName().equals("getAllUsers")) {
            log.info("Trying to get all users");
        } else if (methodSignature.getName().equals("getUserById")) {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof UUID) {
                    id = (UUID) arg;
                    log.info("Trying to get user with {} id", id);
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

        if (methodSignature.getName().equals("getAllUsers")) {
            log.info("All users received");
        } else if (methodSignature.getName().equals("getUserById")) {
            log.info("User with {} id successfully received", id);
        }

        return result;
    }

    @Around("Pointcuts.allSaveMethods()")
    public Object aroundSavingAdvice(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        User user = null;

        if (methodSignature.getName().equals("saveUser")) {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof User) {
                    user = (User) arg;
                    log.info("Trying to save user with {} email", user.getEmail());
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

        if (methodSignature.getName().equals("saveUser")) {
            log.info("User with {} email successfully saved", user.getEmail());
        }

        return result;
    }

    @Around("Pointcuts.allUpdateMethods()")
    public Object aroundUpdatingAdvice(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        UUID id = null;

        if (methodSignature.getName().equals("updateUser")) {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof UUID) {
                    id = (UUID) arg;
                    log.info("Trying to update user with {} id", id);
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

        if (methodSignature.getName().equals("updateUser")) {
            log.info("User with {} id successfully updated", id);
        }

        return result;
    }

    @Around("Pointcuts.allDeleteMethods()")
    public Object aroundDeletingAdvice(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        UUID id = null;

        if (methodSignature.getName().equals("deleteUser")) {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof UUID) {
                    id = (UUID) arg;
                    log.info("Trying to delete user with {} id", id);
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

        if (methodSignature.getName().equals("deleteUser")) {
            log.info("User with {} id successfully deleted", id);
        }

        return result;
    }
}