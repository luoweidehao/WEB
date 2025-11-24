/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80032 (8.0.32)
 Source Host           : localhost:3306
 Source Schema         : acc_system_db

 Target Server Type    : MySQL
 Target Server Version : 80032 (8.0.32)
 File Encoding         : 65001

 Date: 24/11/2025 19:26:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for membership_applications
-- ----------------------------
DROP TABLE IF EXISTS `membership_applications`;
CREATE TABLE `membership_applications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `education_background` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `full_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `institution` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `motivation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `research_interests` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `reviewed_at` datetime(6) NULL DEFAULT NULL,
  `reviewed_by` bigint NULL DEFAULT NULL,
  `specialization` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `years_of_experience` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of membership_applications
-- ----------------------------
INSERT INTO `membership_applications` VALUES (1, '', '2025-11-24 08:16:09.179837', '', '罗巍', '南昌大学', '321', '19979401086', '', '', '2025-11-24 08:22:51.327816', 1, '', 'APPROVED', '2025-11-24 08:22:51.332352', 2, NULL);
INSERT INTO `membership_applications` VALUES (2, '', '2025-11-24 08:31:27.745244', '', '罗巍', '南昌大学', 'tests', '19979401086', '博士', '', '2025-11-24 08:31:59.910779', 1, '心血管内科', 'APPROVED', '2025-11-24 08:31:59.912800', 2, 5);

-- ----------------------------
-- Table structure for user_sessions
-- ----------------------------
DROP TABLE IF EXISTS `user_sessions`;
CREATE TABLE `user_sessions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `token` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_dyv200n8t8bn2vt6pu071b5l0`(`token` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_sessions
-- ----------------------------
INSERT INTO `user_sessions` VALUES (1, '2025-11-24 11:15:32.341847', '2025-11-24 12:15:32.337845', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoi572X5beNIiwic3ViIjoi572X5beNIiwiaWF0IjoxNzYzOTU0MTMyLCJleHAiOjE3NjM5NTc3MzJ9._8ZTJ_y1kZ7Rd9CNY2CsZIkRN3ciD3hXWhjA0OOxPJ0', 1);
INSERT INTO `user_sessions` VALUES (2, '2025-11-24 11:15:45.609097', '2025-11-24 12:15:45.609097', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6MiwidXNlcm5hbWUiOiJ0ZXN0Iiwic3ViIjoidGVzdCIsImlhdCI6MTc2Mzk1NDE0NSwiZXhwIjoxNzYzOTU3NzQ1fQ.J1lvow7w5h3PkdCTuRNqndTzET1D0H7kZ2u0D8Edi90', 2);
INSERT INTO `user_sessions` VALUES (3, '2025-11-24 11:20:17.569438', '2025-11-24 12:20:17.565438', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6MiwidXNlcm5hbWUiOiJ0ZXN0Iiwic3ViIjoidGVzdCIsImlhdCI6MTc2Mzk1NDQxNywiZXhwIjoxNzYzOTU4MDE3fQ.-WBnlRzCbvLjtGqmR6dUyMtCswUmQUbq4yybNUPhexo', 2);
INSERT INTO `user_sessions` VALUES (4, '2025-11-24 11:21:12.376482', '2025-11-24 12:21:12.376482', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6MiwidXNlcm5hbWUiOiJ0ZXN0Iiwic3ViIjoidGVzdCIsImlhdCI6MTc2Mzk1NDQ3MiwiZXhwIjoxNzYzOTU4MDcyfQ.r-kD5SBuEHWCUKkrB3BLn2QknGT3s1BRTOdEBR8vfmM', 2);
INSERT INTO `user_sessions` VALUES (5, '2025-11-24 11:25:55.017120', '2025-11-24 12:25:55.014120', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6MiwidXNlcm5hbWUiOiJ0ZXN0Iiwic3ViIjoidGVzdCIsImlhdCI6MTc2Mzk1NDc1NCwiZXhwIjoxNzYzOTU4MzU0fQ.hbDyiR4ntVx_7xxStIhmcQlPvOxvgnyXPIGdPVZ_vuY', 2);
INSERT INTO `user_sessions` VALUES (6, '2025-11-24 11:37:18.953824', '2025-11-24 12:37:18.950822', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6MiwidXNlcm5hbWUiOiJ0ZXN0Iiwic3ViIjoidGVzdCIsImlhdCI6MTc2Mzk1NTQzOCwiZXhwIjoxNzYzOTU5MDM4fQ.lK_YZJ00YI4i71JB5ZYBRhCPO4t6R9I6_wWywftD_oI', 2);
INSERT INTO `user_sessions` VALUES (7, '2025-11-24 11:37:34.882430', '2025-11-24 12:37:34.882430', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoi572X5beNIiwic3ViIjoi572X5beNIiwiaWF0IjoxNzYzOTU1NDU0LCJleHAiOjE3NjM5NTkwNTR9.2jrTBXEgwNpaaQiieer2F9SMKIuOxTy5jHMdwYXFn4Q', 1);
INSERT INTO `user_sessions` VALUES (8, '2025-11-24 11:37:39.358165', '2025-11-24 12:37:39.357154', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoi572X5beNIiwic3ViIjoi572X5beNIiwiaWF0IjoxNzYzOTU1NDU5LCJleHAiOjE3NjM5NTkwNTl9.2SCwsaokaobyadrN2KbEWBXlP32KEaWxfEA7tcZ1lus', 1);
INSERT INTO `user_sessions` VALUES (9, '2025-11-24 15:07:26.063154', '2025-11-24 16:07:26.060153', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoi572X5beNIiwic3ViIjoi572X5beNIiwiaWF0IjoxNzYzOTY4MDQ2LCJleHAiOjE3NjM5NzE2NDZ9.bMKWnpE5PP0ejzIeh-qOjg6CzJ9Um1hFB1vO4yxCC2o', 1);
INSERT INTO `user_sessions` VALUES (10, '2025-11-24 15:08:31.977145', '2025-11-24 16:08:31.973144', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoi572X5beNIiwic3ViIjoi572X5beNIiwiaWF0IjoxNzYzOTY4MTExLCJleHAiOjE3NjM5NzE3MTF9.3bF0clx9uza_yp7kA0qsvJxvpCXEF1E7yqcCTz9i5Yw', 1);
INSERT INTO `user_sessions` VALUES (11, '2025-11-24 15:12:47.002753', '2025-11-24 16:12:47.000753', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoi572X5beNIiwic3ViIjoi572X5beNIiwiaWF0IjoxNzYzOTY4MzY2LCJleHAiOjE3NjM5NzE5NjZ9.OsglXj6xkY23K27ECo8ausouYvD-6JIJvXoFQkk3jCY', 1);
INSERT INTO `user_sessions` VALUES (12, '2025-11-24 15:12:55.970676', '2025-11-24 16:12:55.970676', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoi572X5beNIiwic3ViIjoi572X5beNIiwiaWF0IjoxNzYzOTY4Mzc1LCJleHAiOjE3NjM5NzE5NzV9.H0qgI2wjszS7yTJqh50QjvGjs3d8bI-COtMOhJu0ZQ4', 1);
INSERT INTO `user_sessions` VALUES (13, '2025-11-24 15:19:16.274089', '2025-11-24 16:19:16.272089', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoi572X5beNIiwic3ViIjoi572X5beNIiwiaWF0IjoxNzYzOTY4NzU2LCJleHAiOjE3NjM5NzIzNTZ9.ZWIQrk-fpRtwehlJAE24gbN5iVordt-QAjrKtIj61lY', 1);
INSERT INTO `user_sessions` VALUES (14, '2025-11-24 15:19:46.644958', '2025-11-24 16:19:46.643958', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoi572X5beNIiwic3ViIjoi572X5beNIiwiaWF0IjoxNzYzOTY4Nzg2LCJleHAiOjE3NjM5NzIzODZ9.S7voeqSYaVDxPqCsbXiZfGEXCRDNCCYa3clx18sO7fs', 1);
INSERT INTO `user_sessions` VALUES (15, '2025-11-24 16:02:24.499548', '2025-11-24 17:02:24.499548', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6MiwidXNlcm5hbWUiOiJ0ZXN0Iiwic3ViIjoidGVzdCIsImlhdCI6MTc2Mzk3MTM0NCwiZXhwIjoxNzYzOTc0OTQ0fQ.VpYkWuf4aNP3euCk3ardN8ZQeb6_0m06W8EW6FDvokE', 2);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'user',
  `verification_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `code_expiry_time` datetime NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `membership` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '罗巍', '2650090110@qq.com', '$2a$10$tvYHQ5kWZN/ud0jVl9M4TOEdnBk1rihK122vhOd/9DZ..QVGfkgY6', 'ADMIN', NULL, NULL, NULL, 'member');
INSERT INTO `users` VALUES (2, 'test', '3054467021@qq.com', '$2a$10$uWgAilaF2PRrK99AC2ZzUONRIyB12Tcp9kNvprsb7SBcVFDRKf3E2', 'user', NULL, NULL, '2025-11-24 07:58:44', '');

SET FOREIGN_KEY_CHECKS = 1;
