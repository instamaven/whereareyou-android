package com.wlady.app.whereareyou.feature.lists;

import com.wlady.app.whereareyou.feature.models.InviteLocalModel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class InvitesLocalList implements Serializable {

    private List<InviteLocalModel> dataSet = new LinkedList<>();

    public List<InviteLocalModel> getData() {
        return dataSet;
    }

    public void clear() {
        dataSet.clear();
    }

    public void addData(InviteLocalModel inviteModel) {
        dataSet.add(inviteModel);
    }
}
