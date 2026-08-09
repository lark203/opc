package org.dromara.common.license.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.license.config.properties.LicenseProperties;
import org.dromara.common.license.core.LicenseState;
import org.dromara.common.license.core.LicenseVerifier;
import org.dromara.common.license.utils.MachineFingerprint;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 授权管理接口（仅超级管理员可访问）。
 *
 * <p>提供机器指纹查看、授权状态查询、授权文件上传续期能力。
 * 上传接口即使授权已过期也可访问，便于管理员续期。
 *
 * @author your-name
 */
@RestController
@RequestMapping("/license")
@Tag(name = "授权管理", description = "机器指纹查看、授权状态查询、授权文件上传续期（仅超级管理员可访问，且续期接口在授权过期时仍可调用）")
public class LicenseController {

    private final LicenseVerifier verifier;
    private final LicenseProperties properties;

    public LicenseController(LicenseVerifier verifier, LicenseProperties properties) {
        this.verifier = verifier;
        this.properties = properties;
    }

    /**
     * 获取当前服务器机器指纹，供厂商签发授权文件使用。
     *
     * @return 机器指纹
     */
    @SaCheckRole(SystemConstants.SUPER_ADMIN_ROLE_KEY)
    @GetMapping("/fingerprint")
    @Operation(summary = "获取机器指纹", description = "返回当前服务器的机器指纹，供厂商签发授权文件（.lic）使用")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功返回机器指纹字符串")
    })
    public R<String> fingerprint() {
        // 注意：必须使用 msg + data 双参重载，否则 R.ok(String) 会被解析为
        // ok(String msg)，导致指纹被写入 msg 字段而非 data 字段，前端取不到数据。
        return R.ok("获取机器指纹成功", MachineFingerprint.getFingerprint());
    }

    /**
     * 获取当前授权状态。
     *
     * @return 授权状态
     */
    @SaCheckRole(SystemConstants.SUPER_ADMIN_ROLE_KEY)
    @GetMapping("/info")
    @Operation(summary = "获取授权状态", description = "返回当前授权是否有效、过期时间、机器指纹等状态信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功返回授权状态")
    })
    public R<LicenseState> info() {
        return R.ok(verifier.getState());
    }

    /**
     * 上传授权文件并立即重新加载，实现续期（无需重新部署）。
     *
     * <p>上传前会先对文件做完整校验（签名 / 过期 / 机器指纹），
     * 仅当校验通过才落盘并刷新内存状态，避免无效或伪造的授权文件
     * 覆盖当前有效的授权而导致系统被锁死。
     *
     * @param file 授权文件（.lic）
     * @return 续期后的授权状态
     */
    @SaCheckRole(SystemConstants.SUPER_ADMIN_ROLE_KEY)
    @PostMapping("/upload")
    @Operation(summary = "上传授权文件（续期）", description = "上传 .lic 授权文件，先校验（签名/过期/机器指纹）通过后落盘并刷新内存状态；"
        + "该接口在授权已过期时仍可被超级管理员调用，用于续期。")
    @Parameter(name = "file", description = "授权文件（.lic）", required = true,
        schema = @Schema(type = "string", format = "binary"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "校验通过并已生效，返回最新授权状态"),
        @ApiResponse(responseCode = "500", description = "校验失败（签名/过期/指纹不匹配）、文件格式错误或写入失败，返回失败原因")
    })
    public R<LicenseState> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.fail("上传文件为空");
        }
        String original = file.getOriginalFilename();
        if (original != null && !original.toLowerCase().endsWith(".lic")) {
            return R.fail("授权文件格式不正确，请上传 .lic 文件");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            return R.fail("读取上传文件失败");
        }
        if (bytes.length > 64 * 1024) {
            return R.fail("授权文件过大（不得超过 64KB）");
        }

        LicenseState check = verifier.validate(bytes);
        if (!check.isValid()) {
            return R.fail("授权文件校验失败：" + check.getMessage());
        }

        try {
            Path path = Paths.get(properties.getLicensePath());
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, bytes);
            verifier.reload();
            return R.ok("授权更新成功", verifier.getState());
        } catch (Exception e) {
            return R.fail("写入授权文件失败：" + e.getMessage());
        }
    }

}
