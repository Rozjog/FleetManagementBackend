package ru.mirea.project.dto;

import java.util.List;
import java.util.UUID;

public class BulkRequestDTO {
    private List<UUID> ids;
    private String action; // ARCHIVE, RESTORE, DELETE

    public List<UUID> getIds() { return ids; }
    public void setIds(List<UUID> ids) { this.ids = ids; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}