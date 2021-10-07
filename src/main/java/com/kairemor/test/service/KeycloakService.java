package com.kairemor.test.service;


import com.kairemor.test.service.dto.*;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.HashMap;
import java.util.List;

public interface KeycloakService {

    String createUser(UserDTO user);

    UserLogDTO createUserWithHashedPassword(UserDTO user);

    List<UserLogDTO> createUsersWithHashedPassword(List<UserDTO> users);

    void updateUser(UserDTO user);

    void changePassword(PasswordResetDTO passwordResetDTO);

    void resetPassword(PasswordForgotDTO passwordForgotDTO);

    int createUsers(List<UserDTO> users);

    AccessTokenResponse signIn(UserDTO user);

    AccessTokenResponse mobileSignIn(UserDTO user);

    AccessTokenResponse refreshToken(String refreshToken);

    AccessTokenResponse mobileRefreshToken(String refreshToken);

    void logOut();

    List<UserWithGroupDTO> getUsersGroups(String[] usersId);

    boolean checkUserPermissions(String permission);

    List<UserRepresentation> getUsersByGroup(String groupId);

    HashMap<String,List<GroupDTO>> getAllGroups();

    HashMap<String,List<GroupDTO>> updateUser(List<String> groups, String userId);
}
