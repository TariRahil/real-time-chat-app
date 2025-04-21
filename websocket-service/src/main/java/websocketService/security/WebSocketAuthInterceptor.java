package websocketService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Key;
import java.util.Map;
import java.util.PrimitiveIterator;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String token = servletRequest.getParameter("token");

            try {
                Claims claims = isValidToken(token);
                if (claims != null) {
                    String username = claims.getSubject();
                    attributes.put("username", username);
                    return true;
                }
            } catch (Exception ex) {
                System.err.println("Invalid token: " + ex.getMessage());
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    private Claims isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is missing ...");
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid or expired token...", ex);
        }
    }

}
