/*
Copyright (C) 2023 e:fs TechHub GmbH (sdk@efs-techhub.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.efs.sdk.accessmanager.core.space;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.efs.sdk.accessmanager.core.space.SpaceController.ENDPOINT;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpaceController.class)
@ActiveProfiles("test")
class SpaceControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RestTemplate restTemplate;

    @Test
    void givenNoAuthentication_whenCreateSpace_thenError() throws Exception {
        mvc.perform(post(ENDPOINT + "/1")).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallOk_whenCreateSpace_thenOk() throws Exception {
        Map<String, Object> create_dto = new HashMap<>();
        create_dto.put("name", "test");
        create_dto.put("description", "test description");

        Map<String, Object> dto = new HashMap<>();
        dto.put("name", "test");
        dto.put("description", "test description");

        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(dto, HttpStatus.OK);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willReturn(response);

        mvc.perform(post(ENDPOINT + "/1").with(jwt()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(create_dto))).andExpect(status().isOk());
    }

    @Test
    void givenTargetRestCallClientError_whenCreateSpace_thenError() throws Exception {
        Map<String, Object> create_dto = new HashMap<>();
        create_dto.put("name", "test");
        create_dto.put("description", "test description");

        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willThrow(except);

        mvc.perform(post(ENDPOINT + "/1").with(jwt()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(create_dto))).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallServerError_whenCreateSpace_thenError() throws Exception {
        Map<String, Object> create_dto = new HashMap<>();
        create_dto.put("name", "test");
        create_dto.put("description", "test description");

        HttpServerErrorException except = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willThrow(except);

        mvc.perform(post(ENDPOINT + "/1").with(jwt()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(create_dto))).andExpect(status().is4xxClientError());
    }

    @Test
    void givenNoAuthentication_whenUpdateSpace_thenError() throws Exception {
        mvc.perform(put(ENDPOINT + "/1/1")).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallOk_whenUpdateSpace_thenOk() throws Exception {
        Map<String, Object> update_dto = new HashMap<>();
        update_dto.put("name", "test");
        update_dto.put("description", "test description");

        Map<String, Object> dto = new HashMap<>();
        dto.put("name", "test");
        dto.put("description", "test description");

        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(dto, HttpStatus.OK);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willReturn(response);

        mvc.perform(put(ENDPOINT + "/1/1").with(jwt()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(update_dto))).andExpect(status().isOk());
    }

    @Test
    void givenTargetRestCallClientError_whenUpdateSpace_thenError() throws Exception {
        Map<String, Object> update_dto = new HashMap<>();
        update_dto.put("name", "test");
        update_dto.put("description", "test description");

        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willThrow(except);

        mvc.perform(put(ENDPOINT + "/1/1").with(jwt()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(update_dto))).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallServerError_whenUpdateSpace_thenError() throws Exception {
        Map<String, Object> update_dto = new HashMap<>();
        update_dto.put("name", "test");
        update_dto.put("description", "test description");

        HttpServerErrorException except = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willThrow(except);

        mvc.perform(put(ENDPOINT + "/1/1").with(jwt()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(update_dto))).andExpect(status().is4xxClientError());
    }

    @Test
    void givenNoAuthentication_whenGetSpaces_thenError() throws Exception {
        mvc.perform(get(ENDPOINT + "/1")).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallOk_whenGetSpaces_thenOk() throws Exception {
        Map<String, Object> dto = new HashMap<>();
        dto.put("name", "test");
        dto.put("description", "test description");

        List<Map<String, Object>> dto_list = new ArrayList<>();
        dto_list.add(dto);

        ResponseEntity<List<Map<String, Object>>> response = new ResponseEntity<>(dto_list, HttpStatus.OK);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willReturn(response);

        mvc.perform(get(ENDPOINT + "/1").with(jwt())).andExpect(status().isOk());
    }

    @Test
    void givenTargetRestCallClientError_whenGetSpaces_thenError() throws Exception {
        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1").with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallServerError_whenGetSpaces_thenError() throws Exception {
        HttpServerErrorException except = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1").with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenNoAuthentication_whenGetSpacesByPermission_thenError() throws Exception {
        mvc.perform(get(ENDPOINT + "/1").queryParam("permissions", "read")).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallOk_whenGetSpacesByPermission_thenOk() throws Exception {
        Map<String, Object> dto = new HashMap<>();
        dto.put("name", "test");
        dto.put("description", "test description");

        List<Map<String, Object>> dto_list = new ArrayList<>();
        dto_list.add(dto);

        ResponseEntity<List<Map<String, Object>>> response = new ResponseEntity<>(dto_list, HttpStatus.OK);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willReturn(response);

        mvc.perform(get(ENDPOINT + "/1").queryParam("permissions", "read").with(jwt())).andExpect(status().isOk());
    }

    @Test
    void givenTargetRestCallClientError_whenGetSpacesByPermission_thenError() throws Exception {
        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1").queryParam("permissions", "read").with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallServerError_whenGetSpacesByPermission_thenError() throws Exception {
        HttpServerErrorException except = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1").queryParam("permissions", "read").with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenNoAuthentication_whenGetSpace_thenError() throws Exception {
        mvc.perform(get(ENDPOINT + "/1/1")).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallOk_whenGetSpace_thenOk() throws Exception {
        Map<String, Object> dto = new HashMap<>();
        dto.put("name", "test");
        dto.put("description", "test description");

        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(dto, HttpStatus.OK);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willReturn(response);

        mvc.perform(get(ENDPOINT + "/1/1").with(jwt())).andExpect(status().isOk());
    }

    @Test
    void givenTargetRestCallClientError_whenGetSpace_thenError() throws Exception {
        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1/1").with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallServerError_whenGetSpace_thenError() throws Exception {
        HttpServerErrorException except = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1/1").with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenNoAuthentication_whenGetSpaceByName_thenError() throws Exception {
        mvc.perform(get(ENDPOINT + "/1/name/test")).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallOk_whenGetSpaceByName_thenOk() throws Exception {
        Map<String, Object> dto = new HashMap<>();
        dto.put("name", "test");
        dto.put("description", "test description");

        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(dto, HttpStatus.OK);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willReturn(response);

        mvc.perform(get(ENDPOINT + "/1/name/test").with(jwt())).andExpect(status().isOk());
    }

    @Test
    void givenTargetRestCallClientError_whenGetSpaceByName_thenError() throws Exception {
        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1/name/test").with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallServerError_whenGetSpaceByName_thenError() throws Exception {
        HttpServerErrorException except = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1/name/test").with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenNoAuthentication_whenListSpacesByPermission_thenError() throws Exception {
        mvc.perform(get(ENDPOINT + "/1").queryParam("permissions", "read")).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallOk_whenListSpacesByPermission_thenOk() throws Exception {
        Map<String, Object> dto = new HashMap<>();
        dto.put("name", "test");
        dto.put("description", "test description");

        List<Map<String, Object>> dto_list = new ArrayList<>();
        dto_list.add(dto);

        ResponseEntity<List<Map<String, Object>>> response = new ResponseEntity<>(dto_list, HttpStatus.OK);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willReturn(response);

        mvc.perform(get(ENDPOINT + "/1").queryParam("permissions", "read").with(jwt())).andExpect(status().isOk());
    }

    @Test
    void givenTargetRestCallClientError_whenListSpacesByPermission_thenError() throws Exception {
        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1").queryParam("permissions", "read").with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenTargetRestCallServerError_whenListSpacesByPermission_thenError() throws Exception {
        HttpServerErrorException except = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willThrow(except);

        mvc.perform(get(ENDPOINT + "/1").queryParam("permissions", "read").with(jwt())).andExpect(status().is4xxClientError());
    }
}
