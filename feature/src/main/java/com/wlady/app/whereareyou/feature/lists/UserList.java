package com.wlady.app.whereareyou.feature.lists;

import com.wlady.app.whereareyou.feature.models.UserModel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class UserList implements Serializable {

    private List<UserModel> dataSet = new LinkedList<>();

    public List<UserModel> getData() {
        return dataSet;
    }
}
