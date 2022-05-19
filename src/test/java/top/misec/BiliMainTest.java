package top.misec;

import java.io.File;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import top.misec.config.ConfigLoader;
import top.misec.task.DailyTask;
import top.misec.task.ServerPush;

@Slf4j
public class BiliMainTest {

    @Test
    public void testMainHandler() {

        log.info("使用同目录下的config.json文件");
        String currentPath = System.getProperty("user.dir") + File.separator + "config.json";
        ConfigLoader.configInit(currentPath);

        //每日任务65经验

        if (!Boolean.TRUE.equals(ConfigLoader.helperConfig.getTaskConfig().getSkipDailyTask())) {
            DailyTask dailyTask = new DailyTask();
            dailyTask.doDailyTask();
        } else {
            log.info("已开启了跳过本日任务，（不会发起任何网络请求），如果需要取消跳过，请将skipDailyTask值改为false");
            ServerPush.doServerPush();
        }
    }
}
