package com.kairemor.test.web.rest;

import com.kairemor.test.service.UserService;
import com.kairemor.test.service.dto.UserDTO;
import com.kairemor.test.service.dto.UserLogDTO;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserResource {
    private final Logger log = LoggerFactory.getLogger(UserResource.class);
    private final UserService userService;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     *
     * @param user UserDto
     * @return created user id
     */
    @PostMapping("/register")
    public String creatUser(@Valid @RequestBody UserDTO user) {
        return userService.createUser(user);
    }

    @PostMapping("/register-user")
    /**
     * Create a user on keycloak with password hashed with bcrypt
     *
     * @param user User to create
     * @return Id of the created user
     * @throws KeycloakCreationException if an error occurred
     */
    public UserLogDTO createUserWithHashedPassword(@Valid @RequestBody UserDTO user) {
        return userService.createUserWithHashedPassword(user);
    }

    /**
     * @param userDTO
     * @return ResponseEntity containing the Response and the status
     */
    @PostMapping(path = "/signin")
    public ResponseEntity<?> signIn(@RequestBody UserDTO userDTO) {
        AccessTokenResponse accessToken;
        try {
            accessToken = userService.signIn(userDTO);
        } catch (HttpResponseException e) {
            return new ResponseEntity("Authentication Failed, Wrong credentials", HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(accessToken);
    }

    @GetMapping(path = "/logOut")
    public void logOut() {
        userService.logOut();
    }
}
