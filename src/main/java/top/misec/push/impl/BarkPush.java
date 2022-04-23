package top.misec.push.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import top.misec.api.ApiList;
import top.misec.push.AbstractPush;
import top.misec.push.model.PushMetaInfo;

/**
 * @author JunzhouLiu
 */
public class BarkPush extends AbstractPush {

    @Override
    protected String generatePushUrl(PushMetaInfo metaInfo) {
        return ApiList.BARK_PUSH + metaInfo.getToken();
    }

    @Override
    protected boolean checkPushStatus(JsonObject jsonObject) {
        if (null == jsonObject) {
            return false;
        }

        JsonElement code = jsonObject.get("code");
        return code.getAsInt() == 200;
    }

    @Override
    protected String generatePushBody(PushMetaInfo metaInfo, String content) {
        return "title=" + "BILIBILI-HELPER任务简报" + "&body=" + content;
    }
}
