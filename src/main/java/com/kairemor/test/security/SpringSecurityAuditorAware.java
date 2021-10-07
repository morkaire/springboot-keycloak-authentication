package com.kairemor.test.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairemor.test.domain.User;
import com.kairemor.test.repository.UserRepository;

import java.util.Base64;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Implementation of {@link AuditorAware} based on Spring Security.
 */
@Service
public class SpringSecurityAuditorAware implements AuditorAware<User> {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final Logger log = LoggerFactory.getLogger(SpringSecurityAuditorAware.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> getCurrentAuditor() {
        User user = getUserFromToken(getTokenFromHttpRequest());
        return Optional.ofNullable(user);
    }

    private String getTokenFromHttpRequest(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            return request.getHeader(AUTHORIZATION_HEADER);
        }
        log.info("Not called in the context of an HTTP request");
        return null;
    }

    private User getUserFromToken(String token) {
        User user = null;

        if (token == null) {
            return user;
        }

        String jwtToken = token.replaceFirst("Bearer ", "");
        String[] splitString = jwtToken.split("\\.");
        String base64EncodedBody = splitString[1];
        Base64.Decoder decoder = Base64.getDecoder();
        String body = new String(decoder.decode(base64EncodedBody));

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            String openId = jsonNode.get("sub").asText();

            if (jsonNode.get("email") == null) {
              log.error("email Field missing on token, cannot save user information.");
              return user;
            }

            Optional<User> optionalUser =  userRepository.findById(openId);
            if(optionalUser.isPresent()){
                  return optionalUser.get();
            }
            user = new User();
            user.setFirstName(jsonNode.get("given_name") != null ? jsonNode.get("given_name").asText() : null);
            user.setLastName(jsonNode.get("family_name") != null ? jsonNode.get("family_name").asText() : null);
            user.setLogin(jsonNode.get("email").asText());
            user.setId(openId);
            userRepository.save(user);
            return user;
        } catch (JsonProcessingException e) {
            log.error("Error when parsing token as json");
        }
        return user;
    }
}
