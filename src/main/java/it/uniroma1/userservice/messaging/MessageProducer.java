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

package it.uniroma1.userservice.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.uniroma1.userservice.InvalidInputParameter;
import it.uniroma1.userservice.entities.User;

@Service
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DirectExchange directExchange;

    @Value("${binding.rabbitmq.key}")
    private String keyBinding;

    public String sendMessage(User user) throws InvalidInputParameter, JsonProcessingException {
        boolean isValidUser = userValidator(user);
        if(isValidUser) {
            ObjectMapper om = new ObjectMapper();
            String jsonMessage = om.writeValueAsString(user);
            String response = (String) rabbitTemplate.convertSendAndReceive(directExchange.getName(), keyBinding, jsonMessage);
            return response;
        } else {
            throw new InvalidInputParameter("User is not valid");
        }
    }

    private boolean userValidator(User u) {
        
        if(u != null) {
            //Username not valid
            if(u.getUsername() == null || u.getUsername().trim().equals("")) {
                return false;
            }

            //Password not valid
            if(u.getPassword() == null || u.getPassword().trim().equals("")) {
                return false;
            }

            //Email
            if(u.getEmail() == null || u.getEmail().trim().equals("")) {
                return false;
            }

            //Name
            if(u.getName() == null || u.getName().trim().equals("")) {
                return false;
            }

            //Surname
            if(u.getSurname() == null || u.getSurname().trim().equals("")) {
                return false;
            }
            return true;
        }
        return false;
    }




}
