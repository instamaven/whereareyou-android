package com.wlady.app.whereareyou.feature.models;

import java.io.Serializable;

public class FCMPushNotification {

    final public static Integer INVITE_DEEP_LINK = 1;
    final public static Integer INVITE_MESSAGE = 2;
    final public static Integer CONFIRMED_MESSAGE = 3;
    final public static Integer ACCEPTED_MESSAGE = 4;
    final public static Integer PING_MESSAGE = 5;

    public static class Notification {

        private String title = "";
        private String body = "";

        Notification() {}

        public void setBody(String body) {
            this.body = body;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class Data implements Serializable {
        private String name = "";
        private String avatar = "";
        private String uId = "";
        private String token = "";
        private Integer type = 0;

        public void setFrom(UserModel from) {
            name = !from.getAlias().equals("") ? from.getAlias() : from.getName();
            avatar = from.getAvatar();
            uId = from.getuId();
            token = from.getDevice().getToken();
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public void setUid(String uId) {
            this.uId = uId;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getToken() {
            return token;
        }

        public String getUid() {
            return uId;
        }

        public Integer getType() {
            return type;
        }
    }

    public static class DataMessage {

        private String to = "";
        private Data data = null;

        public DataMessage setTo(String to) {
            this.to = to;
            return this;
        }

        public DataMessage setFrom(UserModel userModel) {
            if (this.data == null) {
                this.data = new Data();
            }
            this.data.setFrom(userModel);
            return this;
        }

        public DataMessage setType(Integer type) {
            if (this.data == null) {
                this.data = new Data();
            }
            this.data.setType(type);
            return this;
        }

        public Data getData() {
            return data;
        }
    }

    public static class Message {

        private String to = "";
        private Notification notification = new Notification();
        private Data data = null;

        public Message setTo(String to) {
            this.to = to;
            return this;
        }

        public Message setNotification(Notification notification) {
            this.notification = notification;
            return this;
        }

        public Notification getNotification() {
            return notification;
        }

        public Message setTitle(String title) {
            notification.setTitle(title);
            return this;
        }

        public Message setBody(String body) {
            notification.setBody(body);
            return this;
        }
        public Message setFrom(UserModel userModel) {
            if (this.data == null) {
                this.data = new Data();
            }
            this.data.setFrom(userModel);
            return this;
        }

        public Message setType(Integer type) {
            if (this.data == null) {
                this.data = new Data();
            }
            this.data.setType(type);
            return this;
        }

        public Data getData() {
            return data;
        }
    }
}
