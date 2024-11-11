package com.ddubok.api.admin.service;

import com.ddubok.api.admin.dto.request.CreateSeasonReqDto;
import com.ddubok.api.admin.dto.response.CreateSeasonRes;
import com.ddubok.api.admin.dto.response.GetSeasonDetailRes;
import com.ddubok.api.admin.dto.response.GetSeasonListRes;
import java.util.List;

/**
 * 시즌 관련 처리를 위한 서비스
 */
public interface SeasonService {

    /**
     * 관리자가 시즌을 등록한다
     *
     * @param createSeasonReqDto 등록할 시즌의 정보
     * @return 등록된 시즌의 번호를 반환한다.
     */
    CreateSeasonRes createSeason(CreateSeasonReqDto createSeasonReqDto);

    /**
     * 시즌에 대한 정보를 조회한다.
     *
     * @param seasonId 조회할 시즌 번호
     * @return 해당 시즌의 정보를 받아온다.
     */
    GetSeasonDetailRes getSeasonDetail(Long seasonId);

    /**
     * 관리자가 시즌 전체 목록을 조회한다.
     *
     * @return 시즌 전체 목록을 반환한다.
     */
    List<GetSeasonListRes> getSeasonList();
}
