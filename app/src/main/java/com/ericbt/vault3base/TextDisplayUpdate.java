package com.ericbt.vault3base;

public interface TextDisplayUpdate {
    void setEnabled(boolean enabled);

    void update(OutlineItem outlineItem);

    AsyncTaskActivity getAsyncTaskActivity();
}
