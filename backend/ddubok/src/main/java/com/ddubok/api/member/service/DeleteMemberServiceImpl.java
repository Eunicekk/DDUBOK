package com.ddubok.api.member.service;

import com.ddubok.api.member.entity.Member;
import com.ddubok.api.member.exception.MemberNotFoundException;
import com.ddubok.api.member.repository.MemberRepository;
import com.ddubok.common.auth.exception.InvalidDeleteMemberException;
import com.ddubok.common.auth.exception.InvalidRefreshTokenException;
import com.ddubok.common.auth.exception.SoicalAccessTokenNotFoundExcpetion;
import com.ddubok.common.auth.jwt.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteMemberServiceImpl implements DeleteMemberService {

    @Value("${spring.social.naver-id}")
    private String naverId;
    @Value("${spring.social.naver-secret}")
    private String naverSecret;
    @Value("${spring.social.x-id}")
    private String xId;
    @Value("${spring.social.x-secret}")
    private String xSecret;

    private final JwtTokenUtil jwtTokenUtil;
    private final MemberRepository memberRepository;
    private final String REDIS_REFRESH_TOKEN_PREFIX = "RT:";
    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMember(Long memberId, HttpServletResponse httpResponse) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException());
        String socialProvider = member.getSocialProvider().toLowerCase();
        try {
            ResponseEntity<?> response = sendUnlinkRequestByProvider(socialProvider,
                member.getId());

            if (response == null) {
                throw new InvalidDeleteMemberException("연동 해제 중 오류가 발생했습니다.");
            }

            member.deleteMember();
            httpResponse.addCookie(createCookie());
        } catch (Exception e) {
            throw new InvalidDeleteMemberException("연동 해제 중 오류가 발생했습니다.");
        }
    }

    private Cookie createCookie() {
        Cookie refreshCookie = new Cookie("refresh", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        refreshCookie.setSecure(true);
        refreshCookie.setHttpOnly(true);
        return refreshCookie;
    }

    private ResponseEntity<Object> sendUnlinkRequestByProvider(String socialProvider,
        Long memberId) {

        String token = redisTemplate.opsForValue().get(REDIS_REFRESH_TOKEN_PREFIX + memberId);
        if (token == null) {
            throw new InvalidRefreshTokenException("리프레시 토큰을 찾을 수 없습니다.");
        }

        String socialAccessToken = jwtTokenUtil.getSocialAccessToken(token);
        if (socialAccessToken == null) {
            throw new SoicalAccessTokenNotFoundExcpetion("소셜 액세스 토큰을 찾을 수 없습니다.");
        }

        try {
            return switch (socialProvider) {
                case "kakao" -> unlinkKakao(socialAccessToken);
                case "naver" -> unlinkNaver(socialAccessToken);
                case "google" -> unlinkGoogle(socialAccessToken);
                case "x" -> unlinkX(socialAccessToken);
                default -> null;
            };
        } catch (RestClientException e) {
            throw new InvalidDeleteMemberException("지원하지 않는 소셜 프로바이더: " + socialProvider);
        }
    }

    private ResponseEntity<Object> unlinkKakao(String socialAccessToken) {
        String kakaoUrl = "https://kapi.kakao.com/v1/user/unlink";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + socialAccessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>("", headers);
        return restTemplate.exchange(kakaoUrl, HttpMethod.POST, entity, Object.class);
    }

    private ResponseEntity<Object> unlinkNaver(String socialAccessToken) {
        String naverUrl = "https://nid.naver.com/oauth2.0/token";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(naverUrl)
            .queryParam("grant_type", "delete")
            .queryParam("client_id", naverId)
            .queryParam("client_secret", naverSecret)
            .queryParam("access_token", socialAccessToken);

        return restTemplate.exchange(
            builder.toUriString(),
            HttpMethod.POST,
            null,
            Object.class
        );
    }

    private ResponseEntity<Object> unlinkGoogle(String socialAccessToken) {
        String googleUrl = "https://oauth2.googleapis.com/revoke";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("token", socialAccessToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        return restTemplate.exchange(googleUrl, HttpMethod.POST, entity, Object.class);
    }

    private ResponseEntity<Object> unlinkX(String socialAccessToken) {
        String disconnectEndpoint = "https://api.twitter.com/2/oauth2/revoke";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(socialAccessToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token", socialAccessToken);
        body.add("token_type_hint", "access_token");
        body.add("client_id", xId);

        String credentials = Base64.getEncoder()
            .encodeToString((xId + ":" + xSecret).getBytes());
        headers.set("Authorization", "Basic " + credentials);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                disconnectEndpoint,
                HttpMethod.POST,
                request,
                Object.class
            );
    }

}
