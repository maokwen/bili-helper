package top.misec.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import top.misec.config.ConfigLoader;
import top.misec.utils.HttpUtils;

/**
 * B站直播送出即将过期的礼物.
 *
 * @author srcrs
 * @since 2020-10-13
 */

@Slf4j
public class GiveGift implements Task {

    @Override
    public void run() {
        try {
            /* 从配置类中读取是否需要执行赠送礼物 */
            if (!Boolean.TRUE.equals(ConfigLoader.helperConfig.getTaskConfig().getGiveGift())) {
                log.info("未开启自动送出即将过期礼物功能");
                return;
            }
            /* 直播间 id */
            String roomId = "";
            /* 直播间 uid 即 up 的 id*/
            String uid = "";
            /* B站后台时间戳为10位 */
            long nowTime = System.currentTimeMillis() / 1000;
            /* 获得礼物列表 */
            JsonArray jsonArray = xliveGiftBagList();
            /* 判断是否有过期礼物出现 */
            boolean flag = true;
            for (Object object : jsonArray) {
                JsonObject json = (JsonObject) object;
                long expireAt = Long.parseLong(json.get("expire_at").getAsString());
                /* 礼物还剩 1 天送出 */
                /* 永久礼物到期时间为 0 */
                if ((expireAt - nowTime) < 60 * 60 * 25 && expireAt != 0) {
                    /* 如果有未送出的礼物，则获取一个直播间 */
                    if ("".equals(roomId)) {
                        JsonObject uidAndRid = getuidAndRid();
                        uid = uidAndRid.get("uid").getAsString();
                        roomId = uidAndRid.get("roomid").getAsString();
                    }

                    String requestBody = "biz_id=" + roomId
                            + "&ruid=" + uid
                            + "&bag_id=" + json.get("bag_id")
                            + "&gift_id=" + json.get("gift_id")
                            + "&gift_num=" + json.get("gift_num");
                    JsonObject jsonObject3 = xliveBagSend(requestBody);
                    if ("0".equals(jsonObject3.get("code").getAsString())) {
                        /* 礼物的名字 */
                        String giftName = jsonObject3.get("data").getAsJsonObject().get("gift_name").getAsString();
                        /* 礼物的数量 */
                        String giftNum = jsonObject3.get("data").getAsJsonObject().get("gift_num").getAsString();
                        log.info("给直播间 - {} - {} - 数量: {}✔", roomId, giftName, giftNum);
                        flag = false;
                    } else {
                        log.warn("送礼失败, 原因 : {}", jsonObject3);
                    }
                }
            }
            if (flag) {
                log.info("当前无即将过期礼物");
            }
        } catch (Exception e) {
            log.error("赠送礼物异常 : ", e);
        }
    }

    /**
     * 获取一个直播间的room_id.
     *
     * @return String
     * @author srcrs
     * @since 2020-10-13
     */
    public String xliveGetRecommend() {
        return HttpUtils.doGet("https://api.live.bilibili.com/relation/v1/AppWeb/getRecommendList")
                .get("data").getAsJsonObject()
                .get("list").getAsJsonArray()
                .get(6).getAsJsonObject()
                .get("roomid").getAsString();
    }

    /**
     * B站获取直播间的uid.
     *
     * @param roomId up 主的 uid.
     * @return String.
     * @author srcrs.
     * @since 2020-10-13.
     */
    public String xliveGetRoomUid(String roomId) {
        JsonObject pJson = new JsonObject();
        pJson.addProperty("room_id", roomId);
        String urlPram = "?room_id=" + roomId;
        return HttpUtils.doGet("https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom" + urlPram)
                .get("data").getAsJsonObject()
                .get("room_info").getAsJsonObject()
                .get("uid").getAsString();
    }

    /**
     * 根据 uid 获取其 roomid
     *
     * @param mid 即 uid.
     * @return String 返回一个直播间id.
     * @author srcrs.
     * @since 2020-11-20.
     */
    public String getRoomInfoOld(String mid) {
        JsonObject pJson = new JsonObject();
        pJson.addProperty("mid", Integer.parseInt(mid));
        String urlPram = "?mid=" + mid;
        return HttpUtils.doGet("https://api.bilibili.com/x/space/acc/info" + urlPram)
                .get("data").getAsJsonObject()
                .get("live_room").getAsJsonObject()
                .get("roomid").getAsString();
    }

    /**
     * B站直播获取背包礼物.
     *
     * @return JsonArray
     * @author srcrs
     * @since 2020-10-13
     */
    public JsonArray xliveGiftBagList() {
        JsonObject obj = HttpUtils.doGet("https://api.live.bilibili.com/xlive/web-room/v1/gift/bag_list")
                .get("data").getAsJsonObject();
        if (obj.get("list").isJsonArray()) {
            return obj.get("list").getAsJsonArray();
        } else {
            return new JsonArray();
        }
    }

    /**
     * B站直播送出背包的礼物.
     *
     * @return JsonObject
     * @author srcrs
     * @since 2020-10-13
     */
    public JsonObject xliveBagSend(String requestBody) {
        requestBody += "&uid=" + ConfigLoader.helperConfig.getBiliVerify().getDedeUserId()
                + "&csrf=" + ConfigLoader.helperConfig.getBiliVerify().getBiliJct()
                + "&send_ruid=" + "0"
                + "&storm_beat_id=" + "0"
                + "&price=" + "0"
                + "&platform=" + "pc"
                + "&biz_code=" + "live";
        return HttpUtils.doPost("https://api.live.bilibili.com/gift/v2/live/bag_send", requestBody);
    }

    /**
     * 获取一个包含 uid 和 RooId 的 json 对象.
     *
     * @return JsonObject 返回一个包含 uid 和 RooId 的 json 对象.
     * @author srcrs
     * @since 2020-11-20
     */
    public JsonObject getuidAndRid() {
        /* 直播间 id */
        String roomId;
        /* 直播间 uid 即 up 的 id*/
        String uid;
        if (!"0".equals(ConfigLoader.helperConfig.getTaskConfig().getUpLive())) {
            /* 获取指定up的id */
            uid = ConfigLoader.helperConfig.getTaskConfig().getUpLive();
            roomId = getRoomInfoOld(uid);
            String status = "0";
            if (status.equals(roomId)) {
                log.info("自定义up {} 无直播间", uid);
                /* 随机获取一个直播间 */
                roomId = xliveGetRecommend();
                uid = xliveGetRoomUid(roomId);
                log.info("随机直播间");
            } else {
                log.info("自定义up {} 的直播间", uid);
            }

        } else {
            /* 随机获取一个直播间 */
            roomId = xliveGetRecommend();
            uid = xliveGetRoomUid(roomId);
            log.info("随机直播间");
        }
        JsonObject json = new JsonObject();
        json.addProperty("uid", uid);
        json.addProperty("roomid", roomId);
        return json;
    }

    @Override
    public String getName() {
        return "送出即将过期的直播礼物";
    }
}
