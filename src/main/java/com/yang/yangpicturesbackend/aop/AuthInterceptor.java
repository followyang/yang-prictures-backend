package com.yang.yangpicturesbackend.aop;

import com.yang.yangpicturesbackend.annotation.AuthCheck;
import com.yang.yangpicturesbackend.model.entity.User;
import com.yang.yangpicturesbackend.model.enums.UserRoleEnum;
import com.yang.yangpicturesbackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        //获取上下文
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        User loginUser = userService.getLoginUser(request);

        //必须登录
        if(loginUser == null){
            throw  new RuntimeException("必须必须登录");
        }
        //如果无此权限字段将其放行
        if ("".equals(mustRole)) {
            return joinPoint.proceed();
        }
        //仅管理员，必须有管理员权限
        if("admin".equals(mustRole)){
            if(!isAdmin(loginUser)){
                throw  new RuntimeException("无权限");
            }
            return joinPoint.proceed();
        }

        //普通角色校验
        String userRole= loginUser.getUserRole();
        if(!mustRole.equals(userRole)){
            throw new RuntimeException("无权限");
        }
        return joinPoint.proceed();
    }
    private boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}
