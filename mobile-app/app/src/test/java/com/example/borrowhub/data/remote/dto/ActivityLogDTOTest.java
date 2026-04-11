package com.example.borrowhub.data.remote.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Verifies that {@link ActivityLogDTO} is compatible with the refactored backend API response.
 *
 * <p>The backend API returns {@code performed_by} and {@code target_user_name} as plain strings
 * (resolved dynamically from user relationships in the server-side resource layer). These tests
 * confirm that the DTO's {@link com.google.gson.annotations.SerializedName} annotations map both
 * fields correctly and that all other log fields deserialize as expected.
 *
 * <p>Related: issue #116 (Part of #113).
 */
public class ActivityLogDTOTest {

    private final Gson gson = new Gson();

    @Test
    public void activityLogDTO_performedByField_mapsCorrectlyFromJson() {
        String json = "{\"id\":1,\"performed_by\":\"Admin (Jane)\",\"target_user_id\":\"STU001\","
                + "\"target_user_name\":\"John Doe\",\"action\":\"created\","
                + "\"details\":\"Created item: Projector\",\"created_at\":\"2026-04-01T08:00:00Z\"}";

        ActivityLogDTO dto = gson.fromJson(json, ActivityLogDTO.class);

        assertEquals("Admin (Jane)", dto.getPerformedBy());
    }

    @Test
    public void activityLogDTO_targetUserNameField_mapsCorrectlyFromJson() {
        String json = "{\"id\":2,\"performed_by\":\"Staff (Maria)\",\"target_user_id\":\"STU002\","
                + "\"target_user_name\":\"Alice Santos\",\"action\":\"updated\","
                + "\"details\":\"Updated item: Camera\",\"created_at\":\"2026-04-02T09:30:00Z\"}";

        ActivityLogDTO dto = gson.fromJson(json, ActivityLogDTO.class);

        assertEquals("Alice Santos", dto.getTargetUserName());
    }

    @Test
    public void activityLogDTO_allFields_deserializeCorrectly() {
        String json = "{\"id\":3,\"performed_by\":\"Staff (Ana)\",\"target_user_id\":\"STU003\","
                + "\"target_user_name\":\"Bob Cruz\",\"target_type\":\"student\",\"action\":\"deleted\","
                + "\"details\":\"Deleted item: Old HDMI Cable\",\"created_at\":\"2026-04-03T10:15:00Z\"}";

        ActivityLogDTO dto = gson.fromJson(json, ActivityLogDTO.class);

        assertEquals(3L, dto.getId());
        assertEquals("Staff (Ana)", dto.getPerformedBy());
        assertEquals("STU003", dto.getTargetUserId());
        assertEquals("Bob Cruz", dto.getTargetUserName());
        assertEquals("student", dto.getTargetType());
        assertEquals("deleted", dto.getAction());
        assertEquals("Deleted item: Old HDMI Cable", dto.getDetails());
        assertEquals("2026-04-03T10:15:00Z", dto.getCreatedAt());
    }

    @Test
    public void activityLogDTO_nullPerformedBy_returnsNull() {
        String json = "{\"id\":4,\"performed_by\":null,\"target_user_id\":\"STU004\","
                + "\"target_user_name\":\"Carol Lee\",\"target_type\":\"student\",\"action\":\"updated\","
                + "\"details\":\"Updated profile\",\"created_at\":\"2026-04-04T11:00:00Z\"}";

        ActivityLogDTO dto = gson.fromJson(json, ActivityLogDTO.class);

        assertNull(dto.getPerformedBy());
    }

    @Test
    public void activityLogDTO_nullTargetUserName_returnsNull() {
        String json = "{\"id\":5,\"performed_by\":\"System\",\"target_user_id\":\"SYSTEM\","
                + "\"target_user_name\":null,\"target_type\":\"user\",\"action\":\"created\","
                + "\"details\":\"System event\",\"created_at\":\"2026-04-05T12:00:00Z\"}";

        ActivityLogDTO dto = gson.fromJson(json, ActivityLogDTO.class);

        assertNull(dto.getTargetUserName());
    }

    @Test
    public void activityLogDTO_paginatedApiResponse_parsesPerformedByAndTargetUserName() {
        String json = "{\"status\":\"success\",\"message\":\"Activity logs retrieved successfully.\","
                + "\"data\":{\"current_page\":1,\"data\":["
                + "{\"id\":10,\"performed_by\":\"Admin (John)\",\"target_user_id\":\"STU010\","
                + "\"target_user_name\":\"Diana Prince\",\"target_type\":\"student\",\"action\":\"borrowed\","
                + "\"details\":\"Borrowed Laptop\",\"created_at\":\"2026-04-06T08:00:00Z\"},"
                + "{\"id\":11,\"performed_by\":\"Staff (Sara)\",\"target_user_id\":\"STU011\","
                + "\"target_user_name\":\"Clark Kent\",\"target_type\":\"student\",\"action\":\"returned\","
                + "\"details\":\"Returned Projector\",\"created_at\":\"2026-04-06T09:00:00Z\"}"
                + "],\"last_page\":1,\"total\":2}}";

        Type responseType = new TypeToken<ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>>>() {}.getType();
        ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>> response = gson.fromJson(json, responseType);

        assertTrue(response.isSuccess());
        List<ActivityLogDTO> logs = response.getData().getData();
        assertEquals(2, logs.size());

        assertEquals("Admin (John)", logs.get(0).getPerformedBy());
        assertEquals("Diana Prince", logs.get(0).getTargetUserName());
        assertEquals("student", logs.get(0).getTargetType());

        assertEquals("Staff (Sara)", logs.get(1).getPerformedBy());
        assertEquals("Clark Kent", logs.get(1).getTargetUserName());
        assertEquals("student", logs.get(1).getTargetType());
    }
}
