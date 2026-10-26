package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rulai.convertor.IUserConverter;
import rulai.dto.UserCreateDTO;
import rulai.dto.UserModifyDTO;
import rulai.dto.UserQueryDTO;
import rulai.entity.User;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.UserMapper;
import rulai.service.IUserManageService;
import rulai.utils.ReflectionUtils;
import rulai.vo.UserVO;
import rulai.vo.table.UserTableVO;

import java.util.List;

@Service
public class UserManageServiceImpl implements IUserManageService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IUserConverter userConverter;

    /**
     * 创建用户
     *
     * @param createDTO
     * @return
     */
    @Override
    public User createUser(UserCreateDTO createDTO) {
        User user = userConverter.fromUserCreateDTOToEntity(createDTO);
        userMapper.insert(user);
        return user;
    }

    @Override
    public UserVO getById(String userId) {

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("未找到用户");
        }
        return userConverter.toUserVO(user);
    }

    @Override
    public void updateUser(String userId, UserModifyDTO dto) {
        User modify = userConverter.fromUserModifyDTOToEntityManage(dto);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("目标用户不存在");
        } else {
            ReflectionUtils.update(user, modify);
        }
        userMapper.updateById(user);
    }

    @Override
    public IPage<UserTableVO> pageUsers(UserQueryDTO dto) {
        Page<UserTableVO> userVOPage = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<UserTableVO> queryWrapper = buildUserQueryWrapper(dto);
        return userMapper.selectUserPage(userVOPage, queryWrapper);
    }

    @Override
    public List<UserTableVO> listCosts(UserQueryDTO dto) {
        QueryWrapper<UserTableVO> wrapper = buildUserQueryWrapper(dto);
        return userMapper.selectUserList(wrapper);
    }

    @Override
    public void deleteUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户未找到");
        }
        userMapper.deleteById(user);
    }


    private QueryWrapper<UserTableVO> buildUserQueryWrapper(UserQueryDTO queryDTO) {
        QueryWrapper<UserTableVO> wrapper = new QueryWrapper<>();

        // 1. 精确查询条件
        if (StringUtils.isNotBlank(queryDTO.getId())) {
            wrapper.eq("u.id", queryDTO.getId());
        }

        if (StringUtils.isNotBlank(queryDTO.getUsername())) {
            wrapper.eq("u.username", queryDTO.getUsername());
        }

        if (queryDTO.getPrivilege() != null) {
            wrapper.eq("u.privilege", queryDTO.getPrivilege());
        }

        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq("u.status", queryDTO.getStatus());
        }

        if (StringUtils.isNotBlank(queryDTO.getPhone())) {
            wrapper.eq("u.phone", queryDTO.getPhone());
        }

        if (StringUtils.isNotBlank(queryDTO.getEmail())) {
            wrapper.eq("u.email", queryDTO.getEmail());
        }

        // 2. 模糊查询条件
        if (StringUtils.isNotBlank(queryDTO.getAlias())) {
            wrapper.like("u.alias", queryDTO.getAlias());
        }

        // 3. 时间范围查询
        if (queryDTO.getCreatedAt() != null) {
            // 如果需要按创建时间范围查询，可以添加类似逻辑
            // wrapper.ge("u.created_at", queryDTO.getCreatedAt());
        }

        // 4. 组合查询条件（可根据需要添加）
        // 例如：查询特定状态的非管理员用户
        if ("active".equals(queryDTO.getStatus()) && queryDTO.getPrivilege() != null) {
            wrapper.ne("u.privilege", 1); // 假设1是管理员权限
        }
        if (wrapper.isEmptyOfWhere()) {
            wrapper.apply("1=1");
        }
        // 5. 排序逻辑
        applyUserSorting(wrapper, queryDTO);

        return wrapper;
    }

    private void applyUserSorting(QueryWrapper<UserTableVO> wrapper, UserQueryDTO dto) {
        if (StringUtils.isNotBlank(dto.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(dto.getSortOrder());
            if (isAsc) {
                wrapper.orderByAsc(dto.getSortField());
            } else {
                wrapper.orderByDesc(dto.getSortField());
            }
        }
    }
}
