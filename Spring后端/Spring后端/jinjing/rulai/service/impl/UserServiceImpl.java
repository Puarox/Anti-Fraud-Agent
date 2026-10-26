package rulai.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import rulai.constant.USER_CONSTANT;
import rulai.convertor.IUserConverter;
import rulai.dto.UserCreateDTO;
import rulai.dto.UserDetailModifyDTO;
import rulai.entity.User;
import rulai.httpstatus.exceptions.*;
import rulai.mapper.UserMapper;
import rulai.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rulai.utils.GlobalUtils;
import rulai.utils.JwtUtils;
import rulai.vo.UserLoginVO;
import rulai.vo.UserVO;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static rulai.constant.USER_CONSTANT.REDIS_JWT_BLACKLIST_PREFIX;

/**
 * <p>
 * 存储坐席用户信息 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private IUserConverter userConverter;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserLoginVO login(String username, String password) {
        String token ;
        User user = new User();
        user.setUsername(username);
        user.setCryptPassword(GlobalUtils.getCryptedPassword(password,username));
        try{
            LambdaQueryWrapper<User> lambdaWrapper = new LambdaQueryWrapper<>();
            lambdaWrapper.eq(User::getUsername, user.getUsername())
                    .eq(User::getCryptPassword, user.getCryptPassword());

            User userDB = userMapper.selectOne(lambdaWrapper);
            if(userDB == null){ throw new UnauthorizedException("账号密码错误");}
            if(userDB.getPrivilege().equals(0)){
                throw new UnauthorizedException("账号未激活，等待管理员审核。");
            }

            Map<String, String> payload = new HashMap<>();
            payload.put("id", userDB.getId());
            payload.put("username", userDB.getUsername());
            payload.put("privilege",userDB.getPrivilege().toString());
            token = JwtUtils.getToken(payload);

            userDB.setStatus(USER_CONSTANT.STATUS.BREAK.toString());
            userMapper.updateById(userDB);
            UserLoginVO userLoginVO = new UserLoginVO();
            userLoginVO.setUsername(userDB.getUsername());
            userLoginVO.setToken(token);
            userLoginVO.setUserId(userDB.getId());
            return userLoginVO;
        }
        catch (Exception e){
            e.printStackTrace();
            throw e ;
        }

    }
    /**
     * 用户登出
     * @param token
     */
    @Override
    public void logout(String token) {
        try {
            String key = REDIS_JWT_BLACKLIST_PREFIX+token;
            DecodedJWT decodedJWT = JwtUtils.verify(token);
            long ttl ;
            ttl =  decodedJWT.getExpiresAt().getTime() - System.currentTimeMillis();
            redisTemplate.opsForValue().set(key,null,ttl, TimeUnit.MILLISECONDS);

            // 设置为下线状态
            String id = JwtUtils.getTokenInfo(token).getClaim("id").asString();

            User user = userMapper.selectById(id);
            user.setStatus(USER_CONSTANT.STATUS.OFFLINE.toString());
            userMapper.updateById(user);

        }catch (Exception e){
            throw new ServiceUnavailableException("登出服务错误");
        }
    }

    @Override
    public void register(UserCreateDTO userDTO) {
        try{

//            BeanUtils.copyProperties(userDTO, user);
            User user = userConverter.fromUserCreateDTOToEntity(userDTO);
            String cryptPassword = GlobalUtils.getCryptedPassword(userDTO.getPassword(),userDTO.getUsername());
//            user.setPrivilege(32);//默认无权限"passenger"
            // MST: 这里发生了冲突，我理解这里是0于是设为了0
            user.setPrivilege(0);
            user.setCryptPassword(cryptPassword);
            // 默认下线
            user.setStatus(USER_CONSTANT.STATUS.OFFLINE.toString());
            user.setId(UUID.randomUUID().toString());
            if(userDTO.getAvatarUrl()== null){
                user.setAvatarUrl("https://c-ssl.dtstatic.com/uploads/item/202004/14/20200414210134_qbeyi.thumb.1000_0.jpg");
            }
            userMapper.insert(user);
        }
        catch(DuplicateKeyException duplicateKeyException )
        {
            throw new ServiceUnavailableException("该用户名已存在");
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public UserVO getUserDetail(String token) {
        DecodedJWT decodedJWT = JwtUtils.getTokenInfo(token);
        String id = decodedJWT.getClaim("id").asString();

        User user = userMapper.selectById(id);

//        BeanUtils.copyProperties(user, userVO);
        UserVO userVO = userConverter.toUserVO(user);
        return userVO;
    }
    @Override
    public boolean isExited(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username",username);
        return  userMapper.exists(wrapper);
    }
    @Override
    public void changePassword(String oldPassword, String newPassword, String token) {
        String userId = JwtUtils.getId(token);
        User userDb = userMapper.selectById(userId);
        String username = userDb.getUsername();
        User newUser = new User();
        newUser.setId(userDb.getId());
        String originCryptPassword = userDb.getCryptPassword();
        String oldCryptPassword = GlobalUtils.getCryptedPassword(oldPassword,username);
        if(originCryptPassword.equals(oldCryptPassword)) {
            String newCryptPassword = GlobalUtils.getCryptedPassword(newPassword,username);
            newUser.setCryptPassword(newCryptPassword);
            updateById(newUser);
        }
        else{
            throw new NotAcceptableException("原始密码输入不正确");
        }
    }
    @Override
    public User getUserById(String id) {
        try{
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id",id);
            User user = userMapper.selectOne(wrapper);
            return user;
        } catch (Exception e){
            throw new ServiceUnavailableException("查询User失败");
        }
    }

    @Override
    public User getUserByUsername(String repairUsername) {
        try {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", repairUsername);
            User user = userMapper.selectOne(wrapper);
            return user;
        } catch (Exception e) {
            throw new ServiceUnavailableException("查询User失败");
        }
    }
    /**
     * 通过username更新
     * @param userDTO
     */
    @Override
    public void updateUserMessage(UserDetailModifyDTO userDTO, String token) {
        User user = userConverter.fromUserModifyDTOToEntity(userDTO);
        String userName = JwtUtils.getUsername(token);
        User userDB = getUserByUsername(userName);
        String userId = userDB.getId();
        user.setId(userId);
        if(!updateById(user)) {
            throw new ResourceNotFoundException("目标用户不存在");
        }
    }

    @Override
    public void updateStatus(String status,String token) {
        try {
            String userId = JwtUtils.getId(token);
            User userDB = userMapper.selectById(userId);
            userDB.setStatus(status);
            userMapper.updateById(userDB);
        }catch (Exception e){
            throw new InternalServeErrorException("更新状态处理失败");
        }


        }


   /* public Boolean updateUser(UserManageModifyDTO dto) {
        User user = userConverter.fromDTO(dto);
        if(updateById(user)) {
            return true;
        } else {
            throw new ResourceNotFoundException("目标用户不存在");
        }
    }*/





  /*  @Override
    public String uploadAvatar(MultipartFile file) {
        try {
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            // 2. 将 MultipartFile 内容写入临时文件
            file.transferTo(tempFile);
            String UUID = java.util.UUID.randomUUID().toString();
            String key = "avatar"+UUID+file.getOriginalFilename();
            CosUtils.uploadFile(CloudConstants.IMG_BUCKET_NAME,key,tempFile);
            //100天有效期
            String url = CosUtils.generatePresignedUrl(key,100, KSQLWindow.TimeUnit.DAYS);
            log.info("头像上传成功"+key);
            log.info("url:  "+ url);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

}
