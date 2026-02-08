package ljc.service;

import ljc.entity.*;
import ljc.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 数据一致性校验器
 * 在应用启动时检查数据配置的完整性，防止运行时出现"找不到配置"的错误
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataIntegrityChecker implements CommandLineRunner {

    private final StoryStageConfigMapper stageConfigMapper;
    private final StoryUnlockConfigMapper unlockConfigMapper;
    private final GeneralTemplateMapper generalTemplateMapper;
    private final DropPoolMapper dropPoolMapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("========== 开始数据一致性校验 ==========");
        
        try {
            checkStoryStageCompleteness();
            checkUnlockConfigIntegrity();
            checkDropPoolExistence();
            
            log.info("========== 数据一致性校验通过 ✅ ==========");
        } catch (Exception e) {
            log.error("========== 数据一致性校验失败 ❌ ==========", e);
            throw new RuntimeException("数据一致性校验失败，服务启动终止", e);
        }
    }

    /**
     * 校验1: 四国每国都有1-10关的配置
     */
    private void checkStoryStageCompleteness() {
        log.info("校验1: 检查四国关卡配置完整性...");
        
        String[] civs = {"CN", "JP", "KR", "GB"};
        for (String civ : civs) {
            for (int stageNo = 1; stageNo <= 10; stageNo++) {
                StoryStageConfigTbl config = stageConfigMapper.selectByCivAndStage(civ, stageNo);
                if (config == null) {
                    throw new RuntimeException(String.format(
                        "缺失关卡配置: %s-%d (story_stage_config表中找不到)", civ, stageNo
                    ));
                }
            }
            log.info("  ✓ {} 关卡配置完整 (1-10)", civ);
        }
    }

    /**
     * 校验2: story_unlock_config的完整性
     * - unlock的stage必须在story_stage_config中存在
     * - unlock的general模板必须存在
     */
    private void checkUnlockConfigIntegrity() {
        log.info("校验2: 检查解锁配置完整性...");
        
        String[] civs = {"CN", "JP", "KR", "GB"};
        int[] unlockStages = {1, 5, 10};
        
        for (String civ : civs) {
            for (int stageNo : unlockStages) {
                StoryUnlockConfigTbl unlockConfig = unlockConfigMapper.selectByCivAndStage(civ, stageNo);
                
                if (unlockConfig == null) {
                    throw new RuntimeException(String.format(
                        "缺失解锁配置: %s-%d (story_unlock_config表中找不到)", civ, stageNo
                    ));
                }
                
                // 检查对应的stage配置是否存在
                StoryStageConfigTbl stageConfig = stageConfigMapper.selectByCivAndStage(civ, stageNo);
                if (stageConfig == null) {
                    throw new RuntimeException(String.format(
                        "解锁配置指向的关卡不存在: %s-%d", civ, stageNo
                    ));
                }
                
                // 检查解锁的英雄模板是否存在
                if (unlockConfig.getUnlockGeneralTemplateId() != null) {
                    GeneralTemplateTbl generalTemplate = generalTemplateMapper.selectById(
                        unlockConfig.getUnlockGeneralTemplateId()
                    );
                    if (generalTemplate == null) {
                        throw new RuntimeException(String.format(
                            "解锁配置指向的英雄模板不存在: %s-%d -> templateId=%d",
                            civ, stageNo, unlockConfig.getUnlockGeneralTemplateId()
                        ));
                    }
                }
            }
            log.info("  ✓ {} 解锁配置完整 (1/5/10)", civ);
        }
    }

    /**
     * 校验3: 关卡配置中使用的drop_pool必须存在
     */
    private void checkDropPoolExistence() {
        log.info("校验3: 检查掉落池配置...");
        
        // data.sql中使用的pool_id
        List<Integer> requiredPoolIds = Arrays.asList(1, 2, 3, 5);
        
        for (Integer poolId : requiredPoolIds) {
            DropPoolTbl dropPool = dropPoolMapper.selectById(poolId);
            if (dropPool == null) {
                throw new RuntimeException(String.format(
                    "缺失掉落池配置: pool_id=%d (drop_pool表中找不到)", poolId
                ));
            }
        }
        log.info("  ✓ 掉落池配置完整 (pool_id: {})", requiredPoolIds);
    }
}
