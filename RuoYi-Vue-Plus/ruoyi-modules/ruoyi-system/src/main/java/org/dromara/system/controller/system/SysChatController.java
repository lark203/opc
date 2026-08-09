package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.SysChatMessageBo;
import org.dromara.system.domain.vo.SysChatConversationVo;
import org.dromara.system.domain.vo.SysChatMessageVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysChatService;
import org.dromara.system.service.ISysOssService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 聊天信息控制器
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource/chat")
public class SysChatController extends BaseController {

    private final ISysChatService chatService;
    private final ISysOssService ossService;

    /**
     * 获取或创建单聊会话
     *
     * @param targetUserId 对端用户ID
     * @return 会话信息
     */
    @SaCheckLogin
    @GetMapping("/conversation/{targetUserId}")
    public R<SysChatConversationVo> getOrCreateConversation(@PathVariable Long targetUserId) {
        return R.ok(chatService.getOrCreateConversation(LoginHelper.getUserId(), targetUserId));
    }

    /**
     * 分页查询当前用户的会话列表
     *
     * @param pageQuery 分页参数
     * @return 会话分页结果
     */
    @SaCheckLogin
    @GetMapping("/conversation/list")
    public R<PageResult<SysChatConversationVo>> conversationList(PageQuery pageQuery) {
        return R.ok(chatService.selectConversationList(LoginHelper.getUserId(), pageQuery));
    }

    /**
     * 统计当前用户所有会话的未读消息总数（用于消息角标）
     *
     * @return 未读消息总数
     */
    @SaCheckLogin
    @GetMapping("/conversation/unread")
    public R<Long> conversationUnread() {
        return R.ok(chatService.selectUnreadTotal(LoginHelper.getUserId()));
    }

    /**
     * 分页查询会话消息列表
     *
     * @param conversationId 会话ID
     * @param pageQuery      分页参数
     * @return 消息分页结果
     */
    @SaCheckLogin
    @GetMapping("/message/list/{conversationId}")
    public R<PageResult<SysChatMessageVo>> messageList(@PathVariable Long conversationId, PageQuery pageQuery) {
        return R.ok(chatService.selectMessageList(conversationId, pageQuery, LoginHelper.getUserId()));
    }

    /**
     * 发送消息
     *
     * @param bo 消息参数
     * @return 消息信息
     */
    @SaCheckLogin
    @Log(title = "聊天消息", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/message/send")
    public R<SysChatMessageVo> sendMessage(@Validated @RequestBody SysChatMessageBo bo) {
        return R.ok(chatService.sendMessage(bo, LoginHelper.getUserId()));
    }

    /**
     * 标记会话消息为已读
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @SaCheckLogin
    @PutMapping("/message/read/{conversationId}")
    public R<Void> markRead(@PathVariable Long conversationId) {
        chatService.markRead(conversationId, LoginHelper.getUserId());
        return R.ok();
    }

    /**
     * 删除会话（当前用户侧）
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @SaCheckLogin
    @Log(title = "聊天会话", businessType = BusinessType.DELETE)
    @DeleteMapping("/conversation/{conversationId}")
    public R<Void> deleteConversation(@PathVariable Long conversationId) {
        chatService.deleteConversation(conversationId, LoginHelper.getUserId());
        return R.ok();
    }

    /**
     * 分页查询联系人列表（排除当前用户）
     *
     * @param keyword   搜索关键字（用户名/昵称/手机号）
     * @param pageQuery 分页参数
     * @return 联系人分页结果
     */
    @SaCheckLogin
    @GetMapping("/contacts")
    public R<PageResult<SysUserVo>> contactList(@RequestParam(required = false) String keyword, PageQuery pageQuery) {
        return R.ok(chatService.selectContactPage(LoginHelper.getUserId(), keyword, pageQuery));
    }

    /**
     * 聊天内文件/图片上传（仅校验登录，无需 system:oss:upload 管理员权限）
     * <p>复用 OSS 上传能力，返回 ossId 供消息以 OSS id 形式携带。
     *
     * @param file 文件
     * @return 上传结果（url / 原始文件名 / ossId）
     */
    @SaCheckLogin
    @Log(title = "聊天文件", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssController.SysOssUploadVo> upload(@RequestPart("file") MultipartFile file) {
        SysOssVo oss = ossService.upload(file, null);
        SysOssController.SysOssUploadVo uploadVo = new SysOssController.SysOssUploadVo(oss.getUrl(), oss.getOriginalName(), oss.getOssId().toString());
        return R.ok(uploadVo);
    }

    /**
     * 解析聊天消息中图片/文件对应的 OSS 原始信息（文件名等）
     * <p>仅需登录即可访问，避免普通用户因缺少 system:oss:query 权限而无法显示文件名。
     *
     * @param ossIds OSS 对象 ID 串
     * @return OSS 对象列表（含原始文件名）
     */
    @SaCheckLogin
    @GetMapping("/oss/info/{ossIds}")
    public R<List<SysOssVo>> ossInfo(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ossIds) {
        return R.ok(ossService.listByIds(Arrays.asList(ossIds)));
    }

    /**
     * 聊天内文件下载（仅校验登录，无需 system:oss:download 管理员权限）
     * <p>与图片预览一致，登录用户即可下载会话中的文件。
     *
     * @param ossId OSS 对象 ID
     * @return 文件字节流
     */
    @SaCheckLogin
    @GetMapping("/download/{ossId}")
    public ResponseEntity<byte[]> download(@PathVariable Long ossId) throws IOException {
        return ossService.download(ossId);
    }

}
