package rulai.controller;

import jakarta.validation.Valid;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rulai.annotation.AllowAnonymous;
import rulai.common.APIResponse;
import rulai.constant.MEMO_CONSTANT;
import rulai.convertor.IMemoConverter;
import rulai.dto.MemoCreateDTO;
import rulai.dto.MemoModifyDTO;
import rulai.pojo.Memo;
import rulai.utils.RedisUtils;
import rulai.weblogger.WebLog;

import java.time.LocalDateTime;
import java.util.*;


/**
 * <p>
 * 便签控制器
 * </p>
 *
 * @author Mistry
 * @since 2025-01-08
 */
@Controller
@RequestMapping("/memo")
public class MemoController {
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private IMemoConverter memoConverter;

    /**
     * 新建便签
     * @param dto
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/create")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse createMemo(@Valid @RequestBody MemoCreateDTO dto,
                                    @RequestHeader(value = "userId") String userId) {
        Memo memo = memoConverter.fromDTO(dto);
        memo.setId(UUID.randomUUID().toString());
        memo.setCreateTime(LocalDateTime.now());

        redisUtils.set(
                MEMO_CONSTANT.REIDS_PREFIX + String.format("%s:", userId) +memo.getId(),
                memo
        );

        return APIResponse.success(memo);
    }

    /**
     * 删除话术
     * @param
     * @return
     */
    @WebLog
    @ResponseBody
    @DeleteMapping("/{memoId}")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteMemo(@PathVariable String memoId,
                                    @RequestHeader(value = "userId") String userId){
        redisUtils.delete(
                MEMO_CONSTANT.REIDS_PREFIX + String.format("%s:", userId) +memoId
        );
        return APIResponse.success();
    }


    /**
     * 新建便签
     * @param dto
     * @return
     */
    @WebLog
    @ResponseBody
    @PutMapping("/{memoId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse createMemo(@Valid @RequestBody MemoModifyDTO dto,
                                    @PathVariable String memoId,
                                    @RequestHeader(value = "userId") String userId) {
        Memo memo = memoConverter.fromDTO(dto);

        Memo oldMemo = redisUtils.get(MEMO_CONSTANT.REIDS_PREFIX + String.format("%s:", userId) +memoId, Memo.class);

        if(oldMemo != null){
            if(memo.getMemoName() == null) {
                memo.setMemoName(oldMemo.getMemoName());
            }
            if(memo.getCreateTime() == null) {
                memo.setCreateTime(oldMemo.getCreateTime());
            }
            if(memo.getContent() == null) {
                memo.setContent(oldMemo.getContent());
            }
        }
        redisUtils.set(
                MEMO_CONSTANT.REIDS_PREFIX + String.format("%s:", userId) +memoId,
                memo
        );
        return APIResponse.success();
    }

    /**
     * 查询全部话术
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/list")
    public APIResponse<List<Memo>> listMemo(@RequestHeader(value = "userId") String userId) {
        Set<String> memoKeyList = redisUtils.scanKeysWithPrefix(MEMO_CONSTANT.REIDS_PREFIX + String.format("%s:", userId));

        List<Memo> memoList = new ArrayList<>();
        for(String key : memoKeyList){
            Memo memo = redisUtils.get(key, Memo.class);
            if(memo != null){
                memoList.add(memo);
            }
        }



        return APIResponse.success(memoList);
    }

    /**
     * 删除所有的便签
     * @return
     */
    @WebLog
    @ResponseBody
    @DeleteMapping("/clear")
    public APIResponse clearMemo(@RequestHeader(value = "userId") String userId) {
        Long count = redisUtils.deleteKeysWithPrefix(MEMO_CONSTANT.REIDS_PREFIX + String.format("%s:", userId));
        return APIResponse.success(count);
    }
}
