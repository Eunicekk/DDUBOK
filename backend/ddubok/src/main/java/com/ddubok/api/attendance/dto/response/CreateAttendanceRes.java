package com.ddubok.api.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateAttendanceRes {

    FortuneRes fortune;
    AttendanceHistoryRes attendanceHistory;
}
