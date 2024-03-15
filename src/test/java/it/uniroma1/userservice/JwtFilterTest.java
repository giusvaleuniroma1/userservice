/**
 * MIT No Attribution
 *
 *Copyright 2024 Giuseppe Valente <valentepeppe@gmail.com>
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy of this
 *software and associated documentation files (the "Software"), to deal in the Software
 *without restriction, including without limitation the rights to use, copy, modify,
 *merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *permit persons to whom the Software is furnished to do so.
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 *PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package it.uniroma1.userservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import it.uniroma1.userservice.entities.Role;
import it.uniroma1.userservice.entities.User;
import it.uniroma1.userservice.security.JwtUtil;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class JwtFilterTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtUtil jwtUtil;


    @Autowired
    private TestRestTemplate restTemplate;


    /**
     *  Test that a valid token is able to access to a privileged resource
     */
    @Test
    public void testValidJwtToken() {

        //Create a privileged User
        User user = new User();
        user.setEmail("user@email.it");
        user.setUsername("user");
        user.setEnabled(true);
        user.setSurname("User");
        user.setName("User");
        Set<Role> roles = new HashSet<Role>();
        Role r = new Role();    
        r.setAuthority("ROLE_SYSTEM_ADMINISTRATOR");
        roles.add(r);
        user.setAuthorities(roles);
        String token = null;
        try {
            token = jwtUtil.generateToken(user);
            
        } catch (Exception e) {
            assertTrue(false); //Exception fails the test
        }
        assertNotNull(token);

        ResponseEntity<String> responseEntity = sendGetHttpRequest(token);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode().value(), 200);
    
    }


    /**
     *  Test that a not valid token is not able to access to a privileged resource
     */
    @Test
    public void testWrongJwtToken() {

        String wrongToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.DVJ0H6wTvyzJzOJRGCd3lKjzn9RmWwfF_ukBQCPv46o";
        ResponseEntity<String> responseEntity = sendGetHttpRequest(wrongToken);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode().value(), 401);
    }

    /**
     * Test that a valid token doesn't allow to access to an anuthorized resource
     */
    public void testNotPrivilegedUser() {
        //Create a User without roles
        User user = new User();
        user.setEmail("user@email.it");
        user.setUsername("user");
        user.setEnabled(true);
        user.setSurname("User");
        user.setName("User");
        String token = null;
        try {
            token = jwtUtil.generateToken(user);
            
        } catch (Exception e) {
            assertTrue(false); //Exception fails the test
        }
        assertNotNull(token);

        ResponseEntity<String> responseEntity = sendGetHttpRequest(token);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode().value(), 401);
    }


    @SuppressWarnings("null")
    private  ResponseEntity<String> sendGetHttpRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> respEntity = restTemplate.exchange("http://localhost:" + port + "/api/user/hello", HttpMethod.GET, requestEntity, String.class);
        return respEntity;
    }

}
