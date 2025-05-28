package com.example.healingapp.common;

public final class Consts {
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 10001;
    public static final String EXTRA_TASK_TYPE = "task_type";
    public static final String EXTRA_DATA_PAYLOAD = "data_payload";
    public static final String EXTRA_DATA_ID = "data_id";
    public static final String EXTRA_TARGET_ACTIVITY_CLASS_NAME = "target_activity_class_name";
    public static final String EXTRA_RESULT_MESSAGE = "result_message";
    public static final double MAP_ZOOM_DEFAULT = 17.0;

    // key sleep manager:
    public static final String PREFS_NAME = "SleepAppPreferences";
    public static final String KEY_ACTIVE_SLEEP_SESSION_ID = "active_sleep_session_id";
    public static final String EXTRA_SESSION_ID = "extra_session_id";
    public static final String ALARM_SERVICE_CHANNEL_ID = "AlarmServiceChannel";
    public static final int ALARM_NOTIFICATION_ID = 1;
    // Request codes for permissions
    public static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;
    public static final int REQUEST_CODE_SCHEDULE_EXACT_ALARM = 102;
}
