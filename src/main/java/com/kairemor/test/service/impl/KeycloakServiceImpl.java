package com.kairemor.test.service.impl;

import com.kairemor.test.exception.KeycloakCreationException;
import com.kairemor.test.exception.KeycloakForbiddenAction;
import com.kairemor.test.security.SecurityUtils;
import com.kairemor.test.service.KeycloakService;
import com.kairemor.test.service.dto.*;
import com.kairemor.test.service.keycloak.KeycloakServiceInitializer;
import org.apache.commons.lang.WordUtils;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.*;

@Service
public class KeycloakServiceImpl implements KeycloakService {
    private final Logger log = LoggerFactory.getLogger(KeycloakService.class);
    private final KeycloakServiceInitializer keycloakServiceInitializer;
    private final String PREFIX = "OLD_";

    public KeycloakServiceImpl(KeycloakServiceInitializer keycloakServiceInitializer) {
        this.keycloakServiceInitializer = keycloakServiceInitializer;
    }

    /**
     * Register a new user using keycloak API
     *
     * @param user User to create
     * @return Id of the created user
     * @throws KeycloakCreationException if an error occurred
     */
    public String createUser(UserDTO user) {
        UsersResource usersResource = keycloakServiceInitializer.getUsersResource();
        List<UserRepresentation> list = usersResource.search(user.getEmail());
        if (!list.isEmpty()) {
            return list.get(0).getId();
        }
        UserRepresentation userRepresentation = buildUserRepresentation(user);
        userRepresentation.setGroups(new ArrayList<>());
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> userRoles = user.getRoles();
            userRepresentation.setGroups(userRoles);
        }
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phone_number", new ArrayList<>(Collections.singletonList(user.getPhoneNumber())));
        userRepresentation.setAttributes(attributes);
        Response response = usersResource.create(userRepresentation);
        if (response.getStatus() != 201) {
            throw new KeycloakCreationException(user, response.getStatusInfo());
        }
        //Return userID
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        log.info("User creation end ok userId: {}", userId);
        return userId;
    }

    /**
     * Create a user on keycloak with password hashed with bcrypt
     *
     * @param user User to create
     * @return Id of the created user
     * @throws KeycloakCreationException if an error occurred
     */
    public UserLogDTO createUserWithHashedPassword(UserDTO user) {
        Long failureId = 0000000000001L;
        UsersResource usersResource = keycloakServiceInitializer.getUsersResource();
        List<UserRepresentation> list = usersResource.search(user.getEmail());
        if (list.size() != 0) {
            user.setId(list.get(0).getId());
            updateUserWithGroup(user);
            return new UserLogDTO(list.get(0).getId(), user.getEmail(), 200);
        }
        UserRepresentation userRepresentation = bcryptUserRepresentation(user);
        userRepresentation.setGroups(new ArrayList<String>());
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> userRoles = user.getRoles();
            userRepresentation.setGroups(userRoles);
        }
        Map<String, List<String>> attributes = new HashMap<String, List<String>>();
        attributes.put("phone_number", new ArrayList<>(Collections.singletonList(user.getPhoneNumber())));
        userRepresentation.setAttributes(attributes);
        Response response = usersResource.create(userRepresentation);

        if (response.getStatus() != 201) {
            String exception = new KeycloakCreationException(user, response.getStatusInfo()).getMessage();
            log.error(exception);
            return new UserLogDTO(user.getPhoneNumber()+(++failureId), user.getEmail(), 400, exception);
        }

        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        return new UserLogDTO(userId, user.getEmail(), 201);
    }

    /**
     * Update all informations about a user
     * @param userDTO
     */
    private void updateUserWithGroup(UserDTO userDTO) {
        UsersResource usersResource = keycloakServiceInitializer.getUsersResource();
        UserResource userResource = usersResource.get(userDTO.getId());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phone_number", new ArrayList<>(Collections.singletonList(userDTO.getPhoneNumber())));
        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setFirstName(userDTO.getFirstName());
        userRepresentation.setLastName(userDTO.getLastName());
        userRepresentation.setAttributes(attributes);
        userRepresentation.setGroups(userDTO.getRoles());
        userRepresentation.setCredentials(Collections.singletonList(buildCredentials(userDTO.getPassword())));
        userResource.update(userRepresentation);
        log.info("Updated successfuly user {} groups {}", userDTO.getId(), userDTO.getRoles());
    }

    /**
     * CReate a group of user with their roles and having bcrypt password encryption
     * @param users
     * @return NUmber of users created
     */
    public List<UserLogDTO> createUsersWithHashedPassword(List<UserDTO> users) {
        List<UserLogDTO> userLogs = new ArrayList<>();
        for (UserDTO userDTO : users) {
            userLogs.add(this.createUserWithHashedPassword(userDTO));
        }
        return userLogs;
    }

    /**
     * Register a new user using keycloak API
     *
     * @param users List of User to create
     * @return Number of the user insertion
     * @throws KeycloakCreationException if an error occurred
     */
    public int createUsers(List<UserDTO> users) {
        int insertions = 0;
        for (UserDTO userDTO : users) {
            String userId = this.createUser(userDTO);
            if (userId != null) {
                insertions += 1;
            }
        }
        return insertions;
    }

    /**
     * update user
     *
     * @param userDTO User to be updated
     * @throws KeycloakForbiddenAction if an error occurred
     */
    public void updateUser(UserDTO userDTO) {
        UsersResource usersResource = keycloakServiceInitializer.getUsersResource();
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new KeycloakForbiddenAction("You need to signIn first in order to update your profile infos."));
        List<UserRepresentation> list = usersResource.search(login);
        if (list.size() == 1) {
            UserResource userResource = usersResource.get(list.get(0).getId());
            UserRepresentation userRepresentation = userResource.toRepresentation();
            userRepresentation.setFirstName(userDTO.getFirstName());
            userRepresentation.setLastName(userDTO.getLastName());
            userRepresentation.setEmail(userDTO.getEmail());
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("phone_number", new ArrayList<>(Collections.singletonList(userDTO.getPhoneNumber())));
            userRepresentation.setAttributes(attributes);
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                CredentialRepresentation passwordCredential = this.buildPasswordRepresentation(userDTO.getPassword());
                userResource.resetPassword(passwordCredential);
            }
            userResource.update(userRepresentation);
        }
    }

    /**
     * update user password
     *
     * @param passwordResetDTO Password to be updated
     * @throws KeycloakForbiddenAction if an error occurred
     */
    public void changePassword(PasswordResetDTO passwordResetDTO) {
        UsersResource usersResource = keycloakServiceInitializer.getUsersResource();
        UserRepresentation userRepresentation = this.getCurrentUserRepresentation(usersResource);
        UserResource userResource = usersResource.get(userRepresentation.getId());
        CredentialRepresentation newPasswordCredentials = this.buildPasswordRepresentation(passwordResetDTO.getNewPassword());
        userResource.resetPassword(newPasswordCredentials);
        userResource.update(userRepresentation);
    }

    /**
     * update user password
     *
     * @param passwordForgotDTO Password to reset
     * @throws KeycloakForbiddenAction, BadRequestException if an error occurred
     */
    public void resetPassword(PasswordForgotDTO passwordForgotDTO) {
        UsersResource usersResource = keycloakServiceInitializer.getUsersResource();
        List<UserRepresentation> list = usersResource.search(passwordForgotDTO.getEmail());
        if (list.size() == 1) {
            UserResource userResource = usersResource.get(list.get(0).getId());
            UserRepresentation userRepresentation = userResource.toRepresentation();
            CredentialRepresentation newPasswordCredentials = this.buildPasswordRepresentation(passwordForgotDTO.getNewPassword());
            userResource.resetPassword(newPasswordCredentials);
            userResource.update(userRepresentation);
        } else {
            throw new BadRequestException("Invalid user infos !");
        }
    }

    /**
     * @param user object
     * @return AccessTokenResponse
     */
    @Override
    public AccessTokenResponse signIn(UserDTO user) {
        AuthzClient authzClient = keycloakServiceInitializer.getAuthzClient();
        return authzClient.obtainAccessToken(user.getLogin(), user.getPassword());
    }

    /**
     * @param user object
     * @return AccessTokenResponse
     */
    @Override
    public AccessTokenResponse mobileSignIn(UserDTO user) {
        AuthzClient authzMobileClient = keycloakServiceInitializer.getAuthzMobileClient();
        return authzMobileClient.obtainAccessToken(user.getLogin(), user.getPassword());
    }

    /**
     * Get a refresh token to renew access token
     * @return AuthorizationResponse
     */
    public AccessTokenResponse refreshToken(String refreshToken) {
        MultiValueMap<String, String> requestBody = keycloakServiceInitializer.getRefreshTokenConfig();
        return genToken(refreshToken, requestBody);
    }

    @Override
    public AccessTokenResponse mobileRefreshToken(String refreshToken) {
        MultiValueMap<String, String> requestBody = keycloakServiceInitializer.getMobileRefreshTokenConfig();
        return genToken(refreshToken, requestBody);
    }

    @Override
    public void logOut() {
        UsersResource usersResource = keycloakServiceInitializer.getUsersResource();
        String currentAuthenticatedUserId = this.getCurrentUserRepresentation(usersResource).getId();
        usersResource.get(currentAuthenticatedUserId).logout();
        log.info("User logOut end ok ID: {}", currentAuthenticatedUserId);
    }


    /**
     * Build a keycloak user representation
     *
     * @param user UserDTO object for which we will build representation
     * @return UserRepresentation
     */
    private UserRepresentation buildUserRepresentation(UserDTO user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(user.getEmail());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setCredentials(Collections.singletonList(buildPasswordRepresentation(user.getPassword())));
        return userRepresentation;
    }

    private CredentialRepresentation buildPasswordRepresentation(String password) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);
        return credentialRepresentation;
    }

    private UserRepresentation bcryptUserRepresentation(UserDTO user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(user.getEmail());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setCredentials(Collections.singletonList(buildCredentials(user.getPassword())));
        return userRepresentation;
    }

    private CredentialRepresentation buildCredentials(String password) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setCredentialData("{\"hashIterations\":10,\"algorithm\":\"bcrypt\"}");
        credentialRepresentation.setSecretData("{\"value\":\""+password+"\"}");
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        return credentialRepresentation;
    }

    /**
     * Verify if the user have the permission
     *
     * @param permission The permission to check
     * @return true if user have the permission false if not
     */
    public boolean checkUserPermissions(String permission) {
        String currentUserId = this.getCurrentUserRepresentation(keycloakServiceInitializer.getUsersResource()).getId();
        List<RoleRepresentation> roleRepresentationList = keycloakServiceInitializer.getUsersResource().get(currentUserId).roles().realmLevel().listAll();
        return roleRepresentationList.stream().anyMatch(roleRepresentation -> match(roleRepresentation.getName(), permission));
    }

    @Override
    public List<UserRepresentation> getUsersByGroup(String groupName) {
        RealmResource realmResource = keycloakServiceInitializer.getRealmResource();
        GroupsResource groupsResource = realmResource.groups();
        GroupRepresentation group = groupsResource.groups()
            .stream()
            .filter(groupRepresentation -> groupRepresentation.getName().equals(groupName))
            .findFirst()
            .orElseThrow(() -> new KeycloakForbiddenAction("This group doest not exist"));
        return realmResource.groups().group(group.getId()).members();
    }

    private boolean match(String keycloakRole, String roleToCheck) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] composite = keycloakRole.split("_");
        if (composite.length == 1) {
            return false;
        }
        stringBuilder.append(composite[1]);
        for (int i = 2; i < composite.length; i++) {
            stringBuilder.append("_");
            stringBuilder.append(composite[i]);
        }
        return roleToCheck.equalsIgnoreCase(stringBuilder.toString());
    }

    /**
     * Get group of a user given his keycloak userId
     *
     * @param userId String
     * @return List<String> User groups arrays
     */
    public HashMap<String, List<GroupDTO>> getUserGroups(String userId) {
        UsersResource usersResource = keycloakServiceInitializer.getUsersResource();
        HashMap<String, List<GroupDTO>> userGroups = new HashMap<>();
        try {
            UserResource userResource = usersResource.get(userId);
            List<GroupRepresentation> groupRepresentationList = userResource.groups();
            userGroups = this.parseGroup(groupRepresentationList);
        } catch (NotFoundException notFoundException) {
            log.debug("User with id {} not found. Nested error :{}", userId, notFoundException.getMessage());
        }
        return userGroups;
    }

    /**
     * Return an Hashmap containing for each user his groups
     *
     * @param usersId Array of user Id
     * @return HashMap of userId and corresponding groups
     */
    public List<UserWithGroupDTO> getUsersGroups(String[] usersId) {
        List<UserWithGroupDTO> userWithGroupDTOS = new ArrayList<>();
        HashMap<String, List<GroupDTO>> usersGroups;
        for (String userId : usersId) {
            usersGroups = getUserGroups(userId);
            userWithGroupDTOS.add(new UserWithGroupDTO(userId, usersGroups.get("oldRoles"), usersGroups.get("newRoles")));
        }
        return userWithGroupDTOS;
    }

    /**
     * Get All possible groups/roles
     *
     * @return Groups
     */
    @Override
    public HashMap<String, List<GroupDTO>> getAllGroups() {
        RealmResource realmResource = keycloakServiceInitializer.getRealmResource();
        GroupsResource groupsResource = realmResource.groups();
        return parseGroup(groupsResource.groups());
    }

    private HashMap<String, List<GroupDTO>> parseGroup(List<GroupRepresentation> groupRepresentationList) {
        List<GroupDTO> oldGroups = new ArrayList<>();
        List<GroupDTO> newGroups = new ArrayList<>();
        HashMap<String, List<GroupDTO>> groups = new HashMap<>();

        groupRepresentationList.stream()
            .filter(groupRepresentation -> groupRepresentation.getName().contains(PREFIX))
            .forEach(groupRepresentation -> {
                    String displayedName = this.parseGroupName(groupRepresentation.getName(), PREFIX);
                    oldGroups.add(new GroupDTO(groupRepresentation.getId(), displayedName));
                }
            );
        groupRepresentationList.stream()
            .filter(groupRepresentation -> !groupRepresentation.getName().contains(PREFIX))
            .forEach(groupRepresentation -> {
                    String displayedName = this.parseGroupName(groupRepresentation.getName(), "");
                    newGroups.add(new GroupDTO(groupRepresentation.getId(), displayedName));
                }
            );
        groups.put("oldRoles", oldGroups);
        groups.put("newRoles", newGroups);
        return groups;
    }

    private String parseGroupName(String groupName, String prefix) {
        String rawGroupName = groupName.substring(groupName.indexOf(prefix) + prefix.length());
        return WordUtils.capitalizeFully(String.join(" ", rawGroupName.split("_")));
    }

    public HashMap<String, List<GroupDTO>> updateUser(List<String> groups, String userId) {
        UsersResource usersResource = keycloakServiceInitializer.getUsersResource();
        HashMap<String, List<GroupDTO>> userGroups;
        try {
            UserResource userResource = usersResource.get(userId);
            List<GroupRepresentation> groupRepresentations = userResource.groups();
            for (GroupRepresentation groupRepresentation : groupRepresentations) {
                if (!groups.contains(groupRepresentation.getId())) {
                    userResource.leaveGroup(groupRepresentation.getId());
                } else {
                    groups.remove(groupRepresentation.getId());
                }
            }

            for (String group : groups) {
                userResource.joinGroup(group);
            }
            String[] id = {userId};
            userGroups = getUserGroups(id[0]);
            return userGroups;
        } catch (NotFoundException notFoundException) {
            log.error("User with id {} not found. Nested error :{}", userId, notFoundException.getMessage());
        }
        return null;
    }

    private UserRepresentation getCurrentUserRepresentation(UsersResource usersResource) {
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new KeycloakForbiddenAction("You need to signIn first in order to perform this operation."));
        return usersResource.search(login).get(0);
    }

    private AccessTokenResponse genToken(String refreshToken, MultiValueMap<String, String> requestBody) {
        RestTemplate restTemplate = new RestTemplate();
        requestBody.add("refresh_token",refreshToken);
        String refreshTokenUrl = requestBody.getFirst("url");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<MultiValueMap> request = new HttpEntity<>(requestBody,headers);
        ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(refreshTokenUrl, request, AccessTokenResponse.class);
        return response.getBody();
    }

}
