package my.maleva.api.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum UserRoles {
    SUPRERADMIN(100),
    ADMIN(200),
    CUSTOMERSERVICE(300),
    OPERATIONADMIN(400),
    BOARDINGOFFICER(500),
    WAREHOUSE(600),
    DRIVER(700),
    HR(800),
    ACCOUNTS(900),
    PAYABLE(1100),
    RECEIVABLE(1200),
    MAINTENANCE(1300);

    private final int roleId;

    UserRoles(int roleId) {
        this.roleId = roleId;
    }

    public int getRoleId() {
        return roleId;
    }

    // Reverse lookup map for efficient id -> enum resolution
    private static final Map<Integer, UserRoles> BY_ID = Arrays.stream(values())
            .collect(Collectors.toMap(UserRoles::getRoleId, Function.identity()));

    public static Optional<UserRoles> fromId(int id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    @Override
    public String toString() {
        return name() + "(" + roleId + ")";
    }

}
