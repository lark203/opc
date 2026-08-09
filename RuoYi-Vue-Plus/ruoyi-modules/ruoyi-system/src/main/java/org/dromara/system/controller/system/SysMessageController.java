package org.dromara.system.controller.system;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.enums.PushSourceEnum;
import org.dromara.common.core.enums.PushTypeEnum;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.api.domain.PushPayloadDTO;
import org.dromara.system.domain.bo.SysMessageBo;
import org.dromara.system.domain.bo.SysMessageSendBo;
import org.dromara.system.domain.vo.SysMessageBoxVo;
import org.dromara.system.domain.vo.SysMessageVo;
import org.dromara.system.service.ISysMessageService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 消息记录控制器
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource/message")
public class SysMessageController extends BaseController {

    private final ISysMessageService messageService;

    /**
     * 查询当前用户消息盒子数据
     *
     * @return 消息盒子数据
     */
    @GetMapping("/box")
    public R<SysMessageBoxVo> getBox() {
        return R.ok(messageService.queryMessageBox(LoginHelper.getUserId()));
    }

    /**
     * 分页查询当前用户消息列表
     *
     * @param notice    查询条件
     * @param pageQuery 分页参数
     * @return 消息分页结果
     */
    @GetMapping("/list")
    public R<PageResult<SysMessageVo>> list(SysMessageBo notice, PageQuery pageQuery) {
        return R.ok(messageService.selectPageMessageList(notice, pageQuery, LoginHelper.getUserId()));
    }

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @return 操作结果
     */
    @PutMapping("/read/{messageId}")
    public R<Void> markRead(@PathVariable Long messageId) {
        messageService.markRead(LoginHelper.getUserId(), messageId);
        return R.ok();
    }

    /**
     * 批量标记消息为已读
     *
     * @param messageIds 消息ID集合
     * @return 操作结果
     */
    @PutMapping("/read")
    public R<Void> markReadBatch(@RequestBody List<Long> messageIds) {
        messageService.markReadBatch(LoginHelper.getUserId(), messageIds);
        return R.ok();
    }

    /**
     * 标记所有消息为已读
     *
     * @return 操作结果
     */
    @PutMapping("/read/all")
    public R<Void> markReadAll() {
        messageService.markReadAll(LoginHelper.getUserId());
        return R.ok();
    }

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @return 操作结果
     */
    @DeleteMapping("/{messageId}")
    public R<Void> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(LoginHelper.getUserId(), messageId);
        return R.ok();
    }

    /**
     * 批量删除消息
     *
     * @param messageIds 消息ID集合
     * @return 操作结果
     */
    @DeleteMapping
    public R<Void> deleteMessageBatch(@RequestBody List<Long> messageIds) {
        messageService.deleteMessageBatch(LoginHelper.getUserId(), messageIds);
        return R.ok();
    }

    /**
     * 发送消息（系统内 OA 消息）
     * 支持指定用户或全局广播
     *
     * @param bo 消息发送参数
     * @return 操作结果
     */
    @PostMapping("/send")
    public R<Void> sendMessage(@RequestBody SysMessageSendBo bo) {
        PushPayloadDTO payload = new PushPayloadDTO();
        payload.setTitle(bo.getTitle());
        payload.setCategory(bo.getCategory());
        payload.setMessage(bo.getContent());
        payload.setPath(bo.getPath());
        payload.setData(Collections.singletonMap("noticeContent", bo.getContent()));

        String type = StrUtil.blankToDefault(bo.getType(),
            StrUtil.equals(bo.getCategory(), PushTypeEnum.NOTICE.getType())
                ? PushTypeEnum.NOTICE.getType() : PushTypeEnum.MESSAGE.getType());
        payload.setType(type);

        String source = StrUtil.blankToDefault(bo.getSource(),
            StrUtil.equals(bo.getCategory(), PushTypeEnum.NOTICE.getType()) ? PushSourceEnum.NOTICE.getSource()
                : StrUtil.equals(bo.getCategory(), PushSourceEnum.WORKFLOW.getSource()) ? PushSourceEnum.WORKFLOW.getSource()
                  : PushSourceEnum.BACKEND.getSource());
        payload.setSource(source);

        if (Boolean.TRUE.equals(bo.getBroadcast())) {
            messageService.publishAll(payload);
        } else {
            if (CollUtil.isEmpty(bo.getUserIds())) {
                return R.fail("请选择接收用户");
            }
            messageService.publishMessage(bo.getUserIds(), payload);
        }
        return R.ok();
    }
}
