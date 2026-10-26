package rulai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rulai.annotation.AllowAnonymous;
import rulai.annotation.Dev_Permission;
import rulai.common.APIResponse;
import rulai.dto.*;
import rulai.entity.User;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.mapper.UserMapper;
import rulai.service.impl.CostServiceImpl;
import rulai.service.impl.UserManageServiceImpl;
import rulai.vo.UserVO;
import rulai.vo.table.CostTableVO;
import rulai.vo.table.UserTableVO;
import rulai.weblogger.WebLog;

import java.util.List;

import static rulai.utils.ReflectionUtils.isEmpty;

/**
 * 管理坐席 前端控制器
 */
@Controller
@RequestMapping("/users")
public class UserManageController {
    @Autowired
    private UserManageServiceImpl userManageService;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CostServiceImpl costServiceImpl;

    /**
     * 创建用户（管理端）
     * @param createDTO
     * @return
     */
    @WebLog
    @ResponseBody
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public APIResponse<String> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        User user = userManageService.createUser(createDTO);
        return APIResponse.success(user.getUsername());
    }

    @WebLog
    @ResponseBody
    @GetMapping("/{userId}")
    public APIResponse<UserVO> getUser(@PathVariable String userId) {
        UserVO userVo = userManageService.getById(userId);
        return APIResponse.success(userVo);
    }

    @WebLog
    @ResponseBody
    @PutMapping("/{userId}")
    @SuppressWarnings({"rawtypes"})
    public APIResponse updateUser(@PathVariable String userId, @Valid @RequestBody UserModifyDTO dto) {
        if(isEmpty(dto)) {
            throw new BadRequestException("请求中不含任何有效属性");
        }
        userManageService.updateUser(userId,dto);
        return APIResponse.success();
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @GetMapping("/page")
    @ResponseBody
    @WebLog
    @Dev_Permission
    public IPage<UserTableVO> getUserPage(UserQueryDTO dto) {
        return userManageService.pageUsers(dto);
    }

    @WebLog
    @ResponseBody
    @PostMapping("/list")
    public APIResponse<List<UserTableVO>> listUser(UserQueryDTO dto) {
        return APIResponse.success(userManageService.listCosts(dto));
    }

    @WebLog
    @ResponseBody
    @DeleteMapping("/{userId}")
    public APIResponse<String> deleteUser(@PathVariable String userId) {
        userManageService.deleteUser(userId);
        return APIResponse.success("用户已删除");
    }

}
