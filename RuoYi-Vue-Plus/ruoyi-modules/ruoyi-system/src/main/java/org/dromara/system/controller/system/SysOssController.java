package org.dromara.system.controller.system;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.bo.SysOssBo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传 控制层
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource/oss")
public class SysOssController extends BaseController {

    private final ISysOssService ossService;

    /**
     * 分页查询 OSS 对象存储列表。
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return OSS 分页结果
     */
    @SaCheckPermission("system:oss:list")
    @GetMapping("/list")
    public R<PageResult<SysOssVo>> list(@Validated(QueryGroup.class) SysOssBo bo, PageQuery pageQuery) {
        return R.ok(ossService.queryPageList(bo, pageQuery));
    }

    /**
     * 查询OSS对象基于id串
     *
     * @param ossIds OSS对象ID串
     * @return OSS 对象列表
     */
    @SaCheckPermission("system:oss:query")
    @GetMapping("/listByIds/{ossIds}")
    public R<List<SysOssVo>> listByIds(@NotEmpty(message = "主键不能为空")
                                       @PathVariable Long[] ossIds) {
        List<SysOssVo> list = ossService.listByIds(Arrays.asList(ossIds));
        return R.ok(list);
    }

    /**
     * 上传OSS对象存储
     *
     * @param file 文件
     * @return 上传结果
     */
    @SaCheckPermission("system:oss:upload")
    @Log(title = "OSS对象存储", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssUploadVo> upload(@RequestPart("file") MultipartFile file, @RequestParam(value = "ossExt", required = false) String ossExtJson) {
        SysOssVo oss = ossService.upload(file, JsonUtils.parseObject(ossExtJson, SysOssExt.class));
        SysOssUploadVo uploadVo = new SysOssUploadVo(oss.getUrl(), oss.getOriginalName(), oss.getOssId().toString());
        return R.ok(uploadVo);
    }

    /**
     * 下载OSS对象
     *
     * @param ossId OSS对象ID
     * @throws IOException IO 异常
     */
    @SaCheckPermission("system:oss:download")
    @GetMapping("/download/{ossId}")
    public ResponseEntity<byte[]> download(@PathVariable Long ossId) throws IOException {
        return ossService.download(ossId);
    }

    /**
     * 预览OSS对象（图片/文件内联展示）。
     * <p>
     * 通过后端代理从对象存储读取字节并返回，浏览器无需直连对象存储，
     * 从根本上解决 MinIO 内网 endpoint 浏览器不可达、私有桶无匿名访问权限等问题。
     * 鉴权通过 query 参数中的 token 完成，因为 img 标签无法携带请求头。
     *
     * @param ossId        OSS对象ID
     * @param authorization 携带 Bearer 前缀的 token（通过 query 参数传递）
     * @return 文件字节流响应
     */
    @SaIgnore
    @GetMapping("/preview/{ossId}")
    public ResponseEntity<byte[]> preview(@PathVariable Long ossId,
                                          @RequestParam(value = "Authorization", required = false) String authorization) {
        String token = resolveToken(authorization);
        Object loginId;
        try {
            loginId = StpUtil.getLoginIdByToken(token);
        } catch (NotLoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<byte[]>body(null);
        }
        // 头像等用户资料图片需对全体已登录用户可见，仅校验是否已登录即可，不再要求 OSS 列表权限。
        // 预览仅按 ossId 返回单文件字节，不暴露 OSS 列表/元数据，已登录用户均可访问。
        if (loginId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<byte[]>body(null);
        }
        return ossService.preview(ossId);
    }

    /**
     * 从 Authorization 参数中解析出脱去 Bearer 前缀的 token。
     *
     * @param authorization 原始 Authorization 值
     * @return 纯 token，或 null
     */
    private String resolveToken(String authorization) {
        if (StringUtils.isBlank(authorization)) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return authorization;
    }

    /**
     * 删除OSS对象存储
     *
     * @param ossIds OSS对象ID串
     * @return 操作结果
     */
    @SaCheckPermission("system:oss:remove")
    @Log(title = "OSS对象存储", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ossIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ossIds) {
        return toAjax(ossService.deleteWithValidByIds(List.of(ossIds), true));
    }

    /**
     * OSS 上传响应对象。
     *
     * @param url      文件访问地址
     * @param fileName 原始文件名
     * @param ossId    OSS 对象 ID
     */
    public record SysOssUploadVo(String url, String fileName, String ossId) {
    }
}
