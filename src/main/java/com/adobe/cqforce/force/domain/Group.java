package com.adobe.cqforce.force.domain;

import com.google.gson.annotations.SerializedName;

public class Group
{
    @SerializedName("GroupId")
    private String groupId;

    @SerializedName("Id")
    private String id;

    @SerializedName("UserOrGroupId")
    private String userOrGroupId;

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getUserOrGroupId()
    {
        return userOrGroupId;
    }

    public void setUserOrGroupId(String userOrGroupId)
    {
        this.userOrGroupId = userOrGroupId;
    }
}
