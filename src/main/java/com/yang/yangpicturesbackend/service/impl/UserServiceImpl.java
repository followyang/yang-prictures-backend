package com.yang.yangpicturesbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.yangpicturesbackend.exception.BusinessException;
import com.yang.yangpicturesbackend.exception.ErrorCode;
import com.yang.yangpicturesbackend.exception.ThrowUtils;
import com.yang.yangpicturesbackend.mapper.UserMapper;
import com.yang.yangpicturesbackend.model.dto.UserQueryRequest;
import com.yang.yangpicturesbackend.model.entity.User;
import com.yang.yangpicturesbackend.model.enums.UserRoleEnum;
import com.yang.yangpicturesbackend.model.vo.LoginUserVO;
import com.yang.yangpicturesbackend.model.vo.UserVO;
import com.yang.yangpicturesbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yang.yangpicturesbackend.constant.UserConstant.USER_LOGIN_STATE;


/**
* @author liuya
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-10-25 17:54:55
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
     UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        //1.1 用户基本信息校验
        if(StrUtil.hasBlank(userAccount, userPassword, checkPassword)){
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR,"用户请求信息错误");
        }
        if(userAccount.length()<2){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短="+userAccount );
        }
        //1.2 校验密码
        if (userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短="+userPassword );
        }
        if (checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户确认密码过短="+checkPassword );
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        User user;

        //2.判断用户注册是否重复
         QueryWrapper<User> queryWrapper = new QueryWrapper<>();
         queryWrapper.eq("userAccount", userAccount);
         user = userMapper.selectOne(queryWrapper);

         if(user != null){
             ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "账户已存在");
         }
        //3.生成用户密码
        String encryptPassword = getEncryptPassword(userPassword);

        //4.插入数据库
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());

        Boolean isInsert = this.save( user);
        if(!isInsert){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入用户失败");
        }

        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {

        String salt = "yang-pictures";
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());

    }
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StrUtil.hasBlank(userAccount, userPassword)){
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR,"用户请求信息错误");
        }
        if(userAccount.length()<1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短="+userAccount );
        }
        if(userPassword.length() < 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短="+userPassword );
        }
        //2.查询用户
        User user;
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        //获取盐值密码
        String encryptPassword = getEncryptPassword(userPassword);
        queryWrapper.eq("userPassword",encryptPassword);
        user = userMapper.selectOne(queryWrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在,重新输入");
        }
        //存入用户登录信息
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        //3.返回脱敏信息
        return getLoginUserVO(user);
    }


    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }


    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        //1.查询参数错误
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR,"查询参数错误");

        //查询参数
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        //分页参数
        int pageSize = userQueryRequest.getPageSize();
        int current = userQueryRequest.getCurrent();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
       queryWrapper.orderBy(StrUtil.isNotBlank(sortField),sortOrder.equals("ascend"), sortField);
       return queryWrapper;
    }


    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
   if(CollUtil.isEmpty(userList)){
       return new ArrayList<>();
   }
   return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

}




