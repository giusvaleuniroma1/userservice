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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.uniroma1.userservice.entities.ACK;
import it.uniroma1.userservice.entities.Role;
import it.uniroma1.userservice.entities.User;
import it.uniroma1.userservice.messaging.MessageProducer;

@SpringBootTest
public class MessageProducerTest {

    @Autowired
    private MessageProducer messageProducer;

    @Test
    public void testProducerRightMessage() throws JsonProcessingException, InvalidInputParameter {

        User u = new User();
        u.setAuthorities(null);
        u.setEmail("email");
        u.setEnabled(true);
        u.setId(54L);
        u.setName("Name");
        u.setSurname("Surname");
        u.setUsername("username");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        u.setPassword(bCryptPasswordEncoder.encode("aaa"));
        Role r1 = new Role();
        Role r2 = new Role();
        r1.setAuthority("SYSTEM_ADMINISTRATOR");
        r2.setAuthority("SUPERADMIN");
        Set<Role> roles = new HashSet<Role>();
        roles.add(r1);
        roles.add(r2);
        u.setAuthorities(roles);

        String response = messageProducer.sendMessage(u);
        assertNotNull(response);

        //Convert the response to a User
        ObjectMapper om = new ObjectMapper();
        ACK<User> ack = om.readValue(response, new TypeReference<ACK<User>>() {});
        
        assertNotNull(ack);
        assertEquals(ack.isSuccess(), true);
        assertEquals("Ok", ack.getMessage());
    
        User resultUser = ack.getPayload();
        assertNotNull(resultUser);

        assertEquals(u.getEmail(), resultUser.getEmail());
        assertEquals(u.getUsername(), resultUser.getUsername());
        assertEquals(u.getPassword(), resultUser.getPassword());
        assertEquals(u.getId(), resultUser.getId());
        assertEquals(u.getName(), resultUser.getName());
        assertEquals(u.getSurname(), resultUser.getSurname());
        assertEquals(u.isEnabled(), resultUser.isEnabled());
        for(Role tmp : resultUser.getAuthorities()) {
            assertNotNull(tmp);

        }        
    }

    @Test
    public void testNullEmail() throws JsonProcessingException, InvalidInputParameter {
        User u = new User();
        u.setEmail(null);
        u.setEnabled(true);
        u.setId(54L);
        u.setName("Name");
        u.setSurname("Surname");
        u.setUsername("username");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        u.setPassword(bCryptPasswordEncoder.encode("aaa"));
        Role r1 = new Role();
        Role r2 = new Role();
        r1.setAuthority("SYSTEM_ADMINISTRATOR");
        r2.setAuthority("SUPERADMIN");
        Set<Role> roles = new HashSet<Role>();
        roles.add(r1);
        roles.add(r2);
        u.setAuthorities(roles);
        assertThrows(InvalidInputParameter.class, () -> messageProducer.sendMessage(u));      
    }

    @Test
    public void testNullUsername() throws JsonProcessingException, InvalidInputParameter {
        User u = new User();
        u.setEmail("Email");
        u.setEnabled(true);
        u.setId(54L);
        u.setName("Name");
        u.setSurname("Surname");
        u.setUsername(null);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        u.setPassword(bCryptPasswordEncoder.encode("aaa"));
        Role r1 = new Role();
        Role r2 = new Role();
        r1.setAuthority("SYSTEM_ADMINISTRATOR");
        r2.setAuthority("SUPERADMIN");
        Set<Role> roles = new HashSet<Role>();
        roles.add(r1);
        roles.add(r2);
        u.setAuthorities(roles);
        assertThrows(InvalidInputParameter.class, () -> messageProducer.sendMessage(u));      
    }

    @Test
    public void testNullPassword() throws JsonProcessingException, InvalidInputParameter {
        User u = new User();
        u.setEmail("Email");
        u.setEnabled(true);
        u.setId(54L);
        u.setName("Name");
        u.setSurname("Surname");
        u.setUsername("username");
        u.setPassword(null);
        Role r1 = new Role();
        Role r2 = new Role();
        r1.setAuthority("SYSTEM_ADMINISTRATOR");
        r2.setAuthority("SUPERADMIN");
        Set<Role> roles = new HashSet<Role>();
        roles.add(r1);
        roles.add(r2);
        u.setAuthorities(roles);
        assertThrows(InvalidInputParameter.class, () -> messageProducer.sendMessage(u));      
    }

    @Test
    public void testNullName() throws JsonProcessingException, InvalidInputParameter {
        User u = new User();
        u.setEmail("Email");
        u.setEnabled(true);
        u.setId(54L);
        u.setName(null);
        u.setSurname("Surname");
        u.setUsername("username");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        u.setPassword(bCryptPasswordEncoder.encode("aaa"));
        Role r1 = new Role();
        Role r2 = new Role();
        r1.setAuthority("SYSTEM_ADMINISTRATOR");
        r2.setAuthority("SUPERADMIN");
        Set<Role> roles = new HashSet<Role>();
        roles.add(r1);
        roles.add(r2);
        u.setAuthorities(roles);
        assertThrows(InvalidInputParameter.class, () -> messageProducer.sendMessage(u));      
    }

    @Test
    public void testNullSurname() throws JsonProcessingException, InvalidInputParameter {
        User u = new User();
        u.setEmail("Email");
        u.setEnabled(true);
        u.setId(54L);
        u.setName("Name");
        u.setSurname(null);
        u.setUsername("username");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        u.setPassword(bCryptPasswordEncoder.encode("aaa"));
        Role r1 = new Role();
        Role r2 = new Role();
        r1.setAuthority("SYSTEM_ADMINISTRATOR");
        r2.setAuthority("SUPERADMIN");
        Set<Role> roles = new HashSet<Role>();
        roles.add(r1);
        roles.add(r2);
        u.setAuthorities(roles);
        assertThrows(InvalidInputParameter.class, () -> messageProducer.sendMessage(u));      
    }





    /**
     * Simulate a message consumer, and sand back the message received to the
     * direct exchange
     * 
     * @param message The message received from the publisher
     * @return the loopback of the message sent by publisher
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws InvalidInputParameter 
     */
    @RabbitListener(queues = {"${queue.rabbitmq.listener.name}"})
    @SendTo("user_exchange/${binding.rabbitmq.key}")
    public String receiveMessage(String message) throws JsonMappingException, JsonProcessingException, InvalidInputParameter {


        ACK<User> replyMessage = new ACK<>();
        ObjectMapper om = new ObjectMapper();
        User u = (User) om.readValue(message, User.class);

        replyMessage.setMessage("Ok");
        replyMessage.setPayload(u);
        replyMessage.setSuccess(true);

        String response = om.writeValueAsString(replyMessage);

        //loopback the message to the exchange
        return response;
    }


}
