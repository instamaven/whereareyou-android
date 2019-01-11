package com.wlady.app.whereareyou.feature.lists;

import com.wlady.app.whereareyou.feature.models.BlacklistModel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class BlacklistLocalList implements Serializable {

    private List<BlacklistModel> dataSet = new LinkedList<>();

    public List<BlacklistModel> getData() {
        return dataSet;
    }

    public void clear() {
        dataSet.clear();
    }

}
