package com.doordash.auth_service.repositories;

import com.doordash.auth_service.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * 
 * Provides data access methods for user management including
 * authentication, account status management, and user queries.
 * Extends JpaRepository for basic CRUD operations and includes
 * custom query methods for specific business requirements.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email address.
     * 
     * @param email the email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by username.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email or username.
     * Useful for login where users can use either identifier.
     * 
     * @param email the email address to search for
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :username")
    Optional<User> findByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    /**
     * Find a user by email and check if email is verified.
     * 
     * @param email the email address to search for
     * @param isEmailVerified the email verification status
     * @return Optional containing the user if found
     */
    Optional<User> findByEmailAndIsEmailVerified(String email, Boolean isEmailVerified);

    /**
     * Check if an email address already exists.
     * 
     * @param email the email address to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a username already exists.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Find all active users.
     * 
     * @param pageable pagination information
     * @return Page of active users
     */
    Page<User> findByIsActiveTrue(Pageable pageable);

    /**
     * Find users by role name.
     * 
     * @param roleName the name of the role
     * @param pageable pagination information
     * @return Page of users with the specified role
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    /**
     * Find users created within a date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return Page of users created within the date range
     */
    Page<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find users with accounts locked until after the specified time.
     * 
     * @param dateTime the time to compare against
     * @return List of locked users
     */
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :dateTime")
    Page<User> findLockedUsers(@Param("dateTime") LocalDateTime dateTime, Pageable pageable);

    /**
     * Find users who haven't verified their email within the specified time.
     * 
     * @param dateTime the cutoff time
     * @param pageable pagination information
     * @return Page of users with unverified emails
     */
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false AND u.createdAt < :dateTime")
    Page<User> findUnverifiedEmailUsers(@Param("dateTime") LocalDateTime dateTime, Pageable pageable);

    /**
     * Update user's last login timestamp.
     * 
     * @param userId the user ID
     * @param lastLoginAt the last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLoginAt(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    /**
     * Reset failed login attempts for a user.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.accountLockedUntil = null WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);

    /**
     * Increment failed login attempts for a user.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);

    /**
     * Lock user account until specified time.
     * 
     * @param userId the user ID
     * @param lockUntil the time until which the account is locked
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLockedUntil = :lockUntil WHERE u.id = :userId")
    void lockUserAccount(@Param("userId") Long userId, @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * Update user's email verification status.
     * 
     * @param userId the user ID
     * @param isVerified the verification status
     */
    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = :isVerified WHERE u.id = :userId")
    void updateEmailVerificationStatus(@Param("userId") Long userId, @Param("isVerified") Boolean isVerified);

    /**
     * Update user's password hash.
     * 
     * @param userId the user ID
     * @param passwordHash the new password hash
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :passwordHash WHERE u.id = :userId")
    void updatePasswordHash(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);

    /**
     * Deactivate user account.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") Long userId);

    /**
     * Find users by first name and last name (case insensitive).
     * 
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @param pageable pagination information
     * @return Page of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) " +
           "AND LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    Page<User> findByFirstNameAndLastNameContainingIgnoreCase(
            @Param("firstName") String firstName, 
            @Param("lastName") String lastName, 
            Pageable pageable);

    /**
     * Search users by email, username, first name, or last name.
     * 
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return Page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}
