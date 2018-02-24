package br.com.fclug.financialaid.adapter;

import java.util.List;

import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.UniqueObject;

/**
 * Created by Fabioclug on 2018-02-22.
 */

public class GroupListItem implements UniqueObject {
    public static final int HEADER = 0;
    public static final int CHILD = 1;

    public static long incrementalId = 1;
    public long id;
    public int type;
    public String title;
    public Group group;
    public List<GroupListItem> hiddenChildren;

    public GroupListItem(String title) {
        type = HEADER;
        this.title = title;
        setId(incrementalId);
    }

    public GroupListItem(Group group) {
        type = CHILD;
        this.group = group;
        setId(incrementalId);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
        incrementalId++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupListItem item = (GroupListItem) o;

        if (type != item.type) return false;
        if (title != null ? !title.equals(item.title) : item.title != null) return false;
        return group != null ? group.equals(item.group) : item.group == null;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        return result;
    }
}
