package rulai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import rulai.annotation.AllowAnonymous;
import rulai.common.APIResponse;
import rulai.common.PageParam;
import rulai.constant.AttachmentConstants;
import rulai.convertor.IAttachmentConverter;
import rulai.dto.AttachmentModifyDTO;
import rulai.entity.Attachment;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.service.IAttachmentService;
import rulai.service.IFileService;
import rulai.utils.QueryHelper;
import rulai.vo.table.AttachmentTableVO;
import rulai.weblogger.WebLog;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static rulai.utils.ReflectionUtils.isEmpty;

/**
 * <p>
 * 系统附件信息 前端控制器
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Controller
@RequestMapping("/attachment")
public class AttachmentController {

    @Autowired
    private IFileService fileService;

    @Autowired
    private IAttachmentService attachmentService;

    @Autowired
    private IAttachmentConverter attachmentConverter;

    @WebLog
    @ResponseBody
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<AttachmentTableVO> upload(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("associateTable") String associateTable,
                                                 @RequestParam("associateId") String associateId) {
        // 处理文件名
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String pureFileName = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf("."))
                : "";
        String fileExtension = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf("."))
                : "";

        Attachment attachment = new Attachment();
        attachment.setAssociateTable(associateTable);
        attachment.setAssociateId(associateId);
        attachment.setAttachmentName(pureFileName);
        attachment.setAttachmentType(fileExtension);
        attachment.setAttachmentSize((int) file.getSize());
        Attachment result = attachmentService.uploadAttachment(attachment, file);

        return APIResponse.success(attachmentConverter.toVO(result));
    }

    @WebLog
    @ResponseBody
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable String attachmentId) {

        Attachment attachment = attachmentService.getById(attachmentId);
        if(attachment == null) {
            throw new ResourceNotFoundException("附件不存在");
        }

        Resource resource = fileService.loadFileAsResource(attachment.getFileId());

        if (!resource.exists()) {
            throw new ResourceNotFoundException("文件不存在，fileId: " + attachment.getFileId());
        }
        if (!resource.isReadable()) {
            throw new RuntimeException("文件不可读，fileId: " + attachment.getFileId());
        }

        // 设置下载文件名
        String fileName = attachment.getAttachmentName();
        String fileExtension = attachment.getAttachmentType();

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName + fileExtension, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.valueOf(contentDisposition))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(resource);
    }

    /*
    这个功能还没有做
     */
    @WebLog
    @ResponseBody
    @GetMapping("/{attachmentId}/preview")
    public APIResponse preview(@PathVariable String attachmentId) {
        return APIResponse.success();
    }

    @WebLog
    @ResponseBody
    @DeleteMapping("/{attachmentId}")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteAttachment(@PathVariable String attachmentId){
        attachmentService.deleteAttachment(attachmentId);
        return APIResponse.success();
    }

    @WebLog
    @ResponseBody
    @GetMapping("/{attachmentId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getAttachment(@PathVariable String attachmentId) {
        Attachment attachment = attachmentService.getAttachment(attachmentId);
        return APIResponse.success(attachmentConverter.toVO(attachment));
    }

    @WebLog
    @ResponseBody
    @PutMapping("/{attachmentId}")
    @SuppressWarnings({"rawtypes"})
    public APIResponse updateAttachment(@PathVariable String attachmentId, @Valid @RequestBody AttachmentModifyDTO dto) {
        if(isEmpty(dto)) {
            throw new BadRequestException("请求中不含任何有效属性");
        }
        dto.setAttachmentId(attachmentId);
        Attachment attachment = attachmentConverter.fromDTO(dto);
        if(attachmentService.updateById(attachment)) {
            return APIResponse.success();
        } else {
            throw new ResourceNotFoundException("目标对象不存在");
        }
    }

    /**
     * 根据请求中的信息进行分页查询
     * 按照 restful 风格，理论上应该用 GET 请求
     * 但是参考网上部分帖子，对于含参的查询，此处POST更加适合
     */
    @WebLog
    @ResponseBody
    @PostMapping("/page")
    public APIResponse<List<AttachmentTableVO>> listPageAttachment(@RequestBody PageParam pageParam) {
        Page<Attachment> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        QueryWrapper<Attachment> queryWrapper = new QueryWrapper<>();
        HashMap<String, Object> param = pageParam.getParam();

        QueryHelper.handle(queryWrapper, param);

        IPage<Attachment> result = attachmentService.page(page, queryWrapper);
        return APIResponse.paginated(
                result.getTotal(),
                (int)result.getCurrent() ,
                (int)result.getSize(), attachmentConverter.toVO(result.getRecords())
        );
    }

    /**
     * 根据请求中的信息查询全部记录
     */
    @WebLog
    @ResponseBody
    @PostMapping("/list")
    public APIResponse<List<AttachmentTableVO>> listAttachment(@RequestBody HashMap<String, Object> param) {
        QueryWrapper<Attachment> queryWrapper = new QueryWrapper<>();
        QueryHelper.handle(queryWrapper, param);
        List<Attachment> result = attachmentService.list(queryWrapper);
        return APIResponse.success(attachmentConverter.toVO(result));
    }

    @WebLog
    @ResponseBody
    @GetMapping("/attachment-types")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getAttachmentTypes() {
        List<String> enumList = Stream.of(AttachmentConstants.ASSOCIATE_TYPE.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return APIResponse.success(enumList);
    }
}
