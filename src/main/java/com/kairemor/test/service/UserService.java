package com.kairemor.test.service;

import com.kairemor.test.config.Constants;
import com.kairemor.test.domain.Authority;
import com.kairemor.test.domain.User;
import com.kairemor.test.repository.AuthorityRepository;
import com.kairemor.test.repository.UserRepository;
import com.kairemor.test.security.SecurityUtils;
import com.kairemor.test.service.dto.*;
import org.apache.commons.lang.RandomStringUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final AuthorityRepository authorityRepository;

    private final KeycloakService keycloakService;

    private final int RANDOM_PASSWORD_LENGTH = 8;


    @Value("${application.domain}")
    private String appDomain;

    public UserService(UserRepository userRepository, AuthorityRepository authorityRepository, KeycloakService keycloakService) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.keycloakService = keycloakService;
    }
    /**
     * Register a new user using keycloak API
     * @param user User to create
     * @return Response The result of creation operation
     */
    public String createUser(UserDTO user){
        return keycloakService.createUser(user);
    }

    /**
     * Create a user on keycloak with password hashed with bcrypt
     *
     * @param user User to create
     * @return Id of the created user
     * @throws KeycloakCreationException if an error occurred
     */
    public UserLogDTO createUserWithHashedPassword(UserDTO user) {
        return keycloakService.createUserWithHashedPassword(user);
    }

    /**
     * Register multiple user using keycloak API
     * @param users List of Users to create
     * @return Response The result of creation operation
     */
    public int createUsers(List<UserDTO> users){
        return keycloakService.createUsers(users);
    }

    /**
     * Create multiple users on keycloak with password hashed with bcrypt
     * @param users List of Users to create
     * @return Response The result of creation operation
     */
    public List<UserLogDTO> createUsersWithHashedPassword(List<UserDTO> users){
        return keycloakService.createUsersWithHashedPassword(users);
    }

    /**
     * Generate a random password
     * @param length password length
     * @return random string password
     */
    public String generateRandomPassword(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    /**
     * Update user using keycloak API
     * @param userDTO User to be updated
     */
    public void updateUser(UserDTO userDTO) {
        keycloakService.updateUser(userDTO);
    }

    /**
     * Update user password using keycloak API
     * @param passwordResetDTO PasswordResetDTO to be updated
     */
    public void changePassword(PasswordResetDTO passwordResetDTO) {
        keycloakService.changePassword(passwordResetDTO);
    }

    /**
     * Reset user password using keycloak API
     * @param passwordForgotDTO PasswordForgotDTO to be updated
     */
    public void resetPassword(PasswordForgotDTO passwordForgotDTO) {
        keycloakService.resetPassword(passwordForgotDTO);
    }

    /**
     * Authenticate a user using keycloak API
     * @param user
     * @return AccessTokenResponse, an object containing an access token, a refresh token and expired time for tokens
     */
    public AccessTokenResponse signIn(UserDTO user){
        return keycloakService.signIn(user);
    }

    /**
     * Authenticate a user using keycloak API on mobile app
     * @param user
     * @return AccessTokenResponse, an object containing an access token, a refresh token and expired time for tokens
     */
    public AccessTokenResponse mobileSignIn(UserDTO user){
        return keycloakService.mobileSignIn(user);
    }

    /**
     * Get a refresh token to renew access token
     * @param
     * @return AuthorizationResponse
     */

    public AccessTokenResponse refreshToken(HttpServletResponse response,String refreshToken,String refreshTokenName) {
        AccessTokenResponse accessTokenResponse = keycloakService.refreshToken(refreshToken);
        return genRefreshToken(response, refreshTokenName, accessTokenResponse);
    }

    public AccessTokenResponse mobileRefreshToken(HttpServletResponse response,String refreshToken,String refreshTokenName) {
        AccessTokenResponse accessTokenResponse = keycloakService.mobileRefreshToken(refreshToken);
        return genRefreshToken(response, refreshTokenName, accessTokenResponse);
    }

    /**
     * Return all groups
     * @return List of groups representation
     */
    public HashMap<String,List<GroupDTO>> getAllGroups(){
        return keycloakService.getAllGroups();
    }

    /**
     * logOut a user using keycloak API
     */
    public void logOut(){
        keycloakService.logOut();
    }


    /***
     * Get groups for each userId
     * @param userId array of user id
     * @return userId,List groups corresponding
     */
    public List<UserWithGroupDTO> getUsersGroups(String[] userId) {
        return keycloakService.getUsersGroups(userId);
    }
    /**
     * Verify if the user have the permission
     * @param permission
     * @return true if user have the permission false if not
     */
    public boolean checkUserPermissions(String permission){
        return keycloakService.checkUserPermissions(permission);
    }

    public List<UserRepresentation> getUsersByGroup(String groupId){
        return keycloakService.getUsersByGroup(groupId);
    }

    public HashMap<String,List<GroupDTO>> updateUser(List<String> groups, String userId){
        return keycloakService.updateUser(groups,userId);
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                log.debug("Changed Information for User: {}", user);
            });
    }


    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }


    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    private User syncUserWithIdP(Map<String, Object> details, User user) {
        // save authorities in to sync user roles/groups between IdP and JHipster's local database
        Collection<String> dbAuthorities = getAuthorities();
        Collection<String> userAuthorities =
            user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toList());
        for (String authority : userAuthorities) {
            if (!dbAuthorities.contains(authority)) {
                log.debug("Saving authority '{}' in local database", authority);
                Authority authorityToSave = new Authority();
                authorityToSave.setName(authority);
                authorityRepository.save(authorityToSave);
            }
        }
        // save account in to sync users between IdP and JHipster's local database
        Optional<User> existingUser = userRepository.findOneByLogin(user.getLogin());
        if (existingUser.isPresent()) {
            // if IdP sends last updated information, use it to determine if an update should happen
            if (details.get("updated_at") != null) {
                Instant dbModifiedDate = existingUser.get().getLastModifiedDate();
                Instant idpModifiedDate = (Instant) details.get("updated_at");
                if (idpModifiedDate.isAfter(dbModifiedDate)) {
                    log.debug("Updating user '{}' in local database", user.getLogin());
                    updateUser(user.getFirstName(), user.getLastName(), user.getEmail(),
                        user.getLangKey(), user.getImageUrl());
                }
                // no last updated info, blindly update
            } else {
                log.debug("Updating user '{}' in local database", user.getLogin());
                updateUser(user.getFirstName(), user.getLastName(), user.getEmail(),
                    user.getLangKey(), user.getImageUrl());
            }
        } else {
            log.debug("Saving user '{}' in local database", user.getLogin());
            userRepository.save(user);
        }
        return user;
    }

    /**
     * Returns the user from an OAuth 2.0 login or resource server with JWT.
     * Synchronizes the user in the local repository.
     *
     * @param authToken the authentication token.
     * @return the user from the authentication.
     */
    @Transactional
    public UserDTO getUserFromAuthentication(AbstractAuthenticationToken authToken) {
        Map<String, Object> attributes;
        if (authToken instanceof OAuth2AuthenticationToken) {
            attributes = ((OAuth2AuthenticationToken) authToken).getPrincipal().getAttributes();
        } else if (authToken instanceof JwtAuthenticationToken) {
            attributes = ((JwtAuthenticationToken) authToken).getTokenAttributes();
        } else {
            throw new IllegalArgumentException("AuthenticationToken is not OAuth2 or JWT!");
        }
        User user = getUser(attributes);
        user.setAuthorities(authToken.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(authority -> {
                Authority auth = new Authority();
                auth.setName(authority);
                return auth;
            })
            .collect(Collectors.toSet()));
        return new UserDTO(syncUserWithIdP(attributes, user));
    }

    private static User getUser(Map<String, Object> details) {
        User user = new User();
        // handle resource server JWT, where sub claim is email and uid is ID
        if (details.get("uid") != null) {
            user.setId((String) details.get("uid"));
            user.setLogin((String) details.get("sub"));
        } else {
            user.setId((String) details.get("sub"));
        }
        if (details.get("preferred_username") != null) {
            user.setLogin(((String) details.get("preferred_username")).toLowerCase());
        } else if (user.getLogin() == null) {
            user.setLogin(user.getId());
        }
        if (details.get("given_name") != null) {
            user.setFirstName((String) details.get("given_name"));
        }
        if (details.get("family_name") != null) {
            user.setLastName((String) details.get("family_name"));
        }
        if (details.get("email_verified") != null) {
            user.setActivated((Boolean) details.get("email_verified"));
        }
        if (details.get("email") != null) {
            user.setEmail(((String) details.get("email")).toLowerCase());
        } else {
            user.setEmail((String) details.get("sub"));
        }
        if (details.get("langKey") != null) {
            user.setLangKey((String) details.get("langKey"));
        } else if (details.get("locale") != null) {
            // trim off country code if it exists
            String locale = (String) details.get("locale");
            if (locale.contains("_")) {
                locale = locale.substring(0, locale.indexOf('_'));
            } else if (locale.contains("-")) {
                locale = locale.substring(0, locale.indexOf('-'));
            }
            user.setLangKey(locale.toLowerCase());
        } else {
            // set langKey to default if not specified by IdP
            user.setLangKey(Constants.DEFAULT_LANGUAGE);
        }
        if (details.get("picture") != null) {
            user.setImageUrl((String) details.get("picture"));
        }
        user.setActivated(true);
        return user;
    }

    private AccessTokenResponse genRefreshToken(HttpServletResponse response, String refreshTokenName, AccessTokenResponse accessTokenResponse) {
        ResponseCookie cookie = ResponseCookie.from(refreshTokenName, accessTokenResponse.getRefreshToken())
            .maxAge(accessTokenResponse.getRefreshExpiresIn())
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .domain(appDomain)
            .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return accessTokenResponse;
    }
}
