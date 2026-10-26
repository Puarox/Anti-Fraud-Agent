package rulai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import rulai.annotation.AllowAnonymous;
import rulai.annotation.Dev_Permission;
import rulai.common.APIResponse;
import rulai.common.PageParam;
import rulai.convertor.IUserConverter;
import rulai.dto.UserCreateDTO;
import rulai.dto.UserDetailModifyDTO;
import rulai.dto.UserLoginDTO;
import rulai.entity.User;
import rulai.httpstatus.exceptions.UnauthorizedException;
import rulai.service.impl.UserServiceImpl;
import rulai.utils.QueryHelper;
import rulai.vo.UserLoginVO;
import rulai.vo.UserVO;
import rulai.weblogger.WebLog;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 存储坐席用户信息
 * @author Mistry
 * @since 2025-12-26
 */
@Controller
@RequestMapping("/user")
public class UserController {
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    @Autowired
    IUserConverter userConverter;
    
    @Autowired
    private UserServiceImpl userService;
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png","image/jpg");
    /**
     * 用户账号密码登录
     * @param userLoginDTO
     * @return
     */
    @NotBlank
    @PostMapping("/login")
    @ResponseBody
    @WebLog
    public APIResponse<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO)
    {
        try{
            UserLoginVO userLoginVO = userService.login(userLoginDTO.getUsername(),userLoginDTO.getPassword());
            return APIResponse.success(userLoginVO);
        } catch (Exception e){
            throw new UnauthorizedException(e.getMessage());
        }
    }

    /**
     * 用户登出
     * @param
     * @return
     */
    @WebLog

    @GetMapping("/logout")
    @ResponseBody
    @NotBlank
    public APIResponse<Integer> logout(@RequestHeader String token) {
        userService.logout(token);
        return APIResponse.success();
    }

    /**
     * 用户注册
     * @param userDTO
     * @return
     */
    @WebLog
    @Dev_Permission
    @AllowAnonymous
    @PostMapping("/register")
    @ResponseBody
    public APIResponse<String> register(@RequestBody UserCreateDTO userDTO) {
        userService.register(userDTO);
        return APIResponse.success("账号注册成功，等待管理员审核");
    }

/*    @Dev_Permission
    @GetMapping("/get_user")*/
    /**
     * 用户名是否存在
     * @param username
     * @return
     */
    @WebLog
    @AllowAnonymous
    @Dev_Permission
    @GetMapping("/checkUserExists")
    @ResponseBody
    public APIResponse<Boolean> isExist(@NotBlank @RequestParam String username) {
        boolean isExist = userService.isExited(username);
        return APIResponse.success(isExist);
    }

    /**
     * 获取用户像信息
     * @param
     * @return
     */
    @WebLog
    @Dev_Permission
    @GetMapping("/getUserDetail")
    @AllowAnonymous
    @ResponseBody
    public APIResponse<UserVO> getUserInfo(@NotBlank @RequestHeader String token) {
        UserVO userVO ;
        userVO = userService.getUserDetail(token);
        return APIResponse.success(userVO);
    }

    /**
     * 修改用户信息
     * @param userDTO
     * @return
     */
    @WebLog
    @Dev_Permission
    @PutMapping ("/updateUserDetail")
    @ResponseBody
    @NotBlank
    public APIResponse updateUserDetail(@Valid @RequestBody UserDetailModifyDTO userDTO, @RequestHeader String token)
    {
        userService.updateUserMessage(userDTO,token);
        return APIResponse.success();
    }

    /**
     * 上传头像获取url
     * @param file
     * @return
     */
    @WebLog
    @Dev_Permission
    @PostMapping("/uploadAvatar")
    @ResponseBody
    @NotNull(message = "文件不能为空")
    @Size(max = MAX_FILE_SIZE, message = "文件大小不能超过5MB")
    public APIResponse<String> uploadAvatar(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的图片不能为空");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("仅支持JPEG或PNG格式的图片");
        }
        String url = "";
        return APIResponse.success(url);
    }

    /**
     * 改变坐席状态
     * @return
     */
    @WebLog
    @Dev_Permission
    @PutMapping("/status")
    @ResponseBody
    @NotNull(message="状态不能为空")
    public APIResponse changeStatus(@RequestParam String status,@NotBlank @RequestHeader String token)
    {
        userService.updateStatus(status,token);
        return APIResponse.success();
    }

    /**
     * 话术分页查询
     * @param pageParam
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/page")
    public APIResponse<List<UserVO>> listPageUser(@RequestBody PageParam pageParam) {
        Page<User> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        HashMap<String, Object> param = pageParam.getParam();

        QueryHelper.handle(queryWrapper, param);

        IPage<User> result = userService.page(page, queryWrapper);
        List<UserVO> userVOList = result.getRecords().stream().map(userConverter::toUserVO).collect(Collectors.toList());
        return APIResponse.paginated(
                result.getTotal(),
                (int)result.getCurrent() ,
                (int)result.getSize(), userVOList
        );
    }

    /**
     * 查询全部话术
     * @param param
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/list")
    public APIResponse<List<UserVO>> listUser(@RequestBody HashMap<String, Object> param) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        QueryHelper.handle(queryWrapper, param);
        List<UserVO> result = userService.list(queryWrapper).stream().map(userConverter::toUserVO).collect(Collectors.toList());
        return APIResponse.success(result);
    }

    @WebLog
    @ResponseBody
    @PutMapping("/password")
    public APIResponse<String> changePassword(@RequestParam String oldPassword,@RequestParam String newPassword,@NotBlank @RequestHeader String token) {
        userService.changePassword(oldPassword,newPassword,token);
        return APIResponse.success();
    }


}
