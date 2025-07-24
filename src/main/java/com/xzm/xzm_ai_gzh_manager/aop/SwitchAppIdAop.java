package com.xzm.xzm_ai_gzh_manager.aop;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.xzm.xzm_ai_gzh_manager.common.ErrorCode;
import com.xzm.xzm_ai_gzh_manager.exception.BusinessException;
import com.xzm.xzm_ai_gzh_manager.service.WxAccountService;
import com.xzm.xzm_ai_gzh_manager.model.entity.WxAccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class SwitchAppIdAop {
    private final WxMpService wxMpService;
    private final WxAccountService wxAccountService;

    /**
     * 拦截控制器方法，提取路径中的appId并切换公众号服务
     *
     * @param joinPoint 连接点
     * @return 原方法的执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("execution(* com.xzm.xzm_ai_gzh_manager.controller..*.*(..)))")
    public Object extractAppId(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String appId = null;
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            PathVariable pathVariable = AnnotationUtils.findAnnotation(parameter, PathVariable.class);
            if (pathVariable != null) {
                String variableName = pathVariable.value().isEmpty() ? parameter.getName() : pathVariable.value();
                if ("appId".equalsIgnoreCase(variableName)) {
                    appId = (String) args[i];
                    log.info("当前使用的 appId：{}", appId);
                    // 切换公众号，并提供从数据库查询配置的回调函数
                    wxMpService.switchover(appId, mpAppId -> wxAccountService.lambdaQuery()
                            .eq(WxAccount::getAppId, mpAppId)
                            .oneOpt()
                            .orElseThrow(() -> new BusinessException(ErrorCode.OPERATION_ERROR,
                                    String.format("当前公众号服务【%s】不存在，请稍后再试", mpAppId)))
                            .toWxMpConfigStorage()
                    );
                    break;
                }
            }
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        // 请求结束后清理资源
        if (ObjectUtils.isNotEmpty(appId)) {
            // 如果存在 appId，则在线程执行完毕后移除配置，避免空间浪费
            wxMpService.removeConfigStorage(appId);
        }
        return result;
    }
}
