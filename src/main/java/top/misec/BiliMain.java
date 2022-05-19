package top.misec;

import java.io.File;

import lombok.extern.slf4j.Slf4j;
import top.misec.config.ConfigLoader;
import top.misec.task.DailyTask;
import top.misec.task.ServerPush;

/**
 * 入口类 .
 *
 * @author JunzhouLiu
 * @since 2020/10/11 2:29
 */

@Slf4j
public class BiliMain {

    public static void main(String[] args) {

        //每日任务65经验

        if (args.length > 0) {
            log.info("使用自定义目录的配置文件");
            ConfigLoader.configInit(args[0]);
        } else {
            log.info("使用同目录下的config.json文件");
            String currentPath = System.getProperty("user.dir") + File.separator + "config.json";
            ConfigLoader.configInit(currentPath);
        }

        if (!Boolean.TRUE.equals(ConfigLoader.helperConfig.getTaskConfig().getSkipDailyTask())) {
            DailyTask dailyTask = new DailyTask();
            dailyTask.doDailyTask();
        } else {
            log.info("skipDailyTask = true：跳过本日任务");
            ServerPush.doServerPush();
        }
    }

}
