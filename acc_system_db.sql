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

 Date: 25/11/2025 16:50:23
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
  `doctor_certificate_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `employment_proof_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `full_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `institution` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reviewed_at` datetime(6) NULL DEFAULT NULL,
  `reviewed_by` bigint NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of membership_applications
-- ----------------------------
INSERT INTO `membership_applications` VALUES (1, '', '2025-11-25 11:17:05.670354', '/photos/membership/8c11983b-4a8b-41cf-bd4e-77e9bf7d1ccc.png', '/photos/membership/03af85b9-c2e7-4e6b-8b36-098ceb4d1eea.png', '罗巍', '42102220031126151X', '123', '', '19979401086', '博士', '2025-11-25 11:18:19.714890', 1, 'APPROVED', '2025-11-25 11:18:19.718227', 2);

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
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_sessions
-- ----------------------------
INSERT INTO `user_sessions` VALUES (25, '2025-11-25 15:50:44.062514', '2025-11-25 16:50:44.062514', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI6NCwidXNlcm5hbWUiOiIxMTEiLCJzdWIiOiIxMTEiLCJpYXQiOjE3NjQwNTcwNDQsImV4cCI6MTc2NDA2MDY0NH0._63_Y_EZXRWl0IbHDx6bX7oHMArXZ6dHlN-2ZMpwiMo', 4);
INSERT INTO `user_sessions` VALUES (26, '2025-11-25 16:23:44.795033', '2025-11-25 17:23:44.795033', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI6NSwidXNlcm5hbWUiOiIxMTEiLCJzdWIiOiIxMTEiLCJpYXQiOjE3NjQwNTkwMjQsImV4cCI6MTc2NDA2MjYyNH0.nfhOcFv2cfZOvmvmu31frmnlIQNxZL_f4Rqp6pF-IkU', 5);
INSERT INTO `user_sessions` VALUES (27, '2025-11-25 16:23:55.710489', '2025-11-25 17:23:55.710489', b'1', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI6NSwidXNlcm5hbWUiOiIxMTEiLCJzdWIiOiIxMTEiLCJpYXQiOjE3NjQwNTkwMzUsImV4cCI6MTc2NDA2MjYzNX0.FMprQvn20XVjJdQ0-tHYvwtCU3uCT-Q281Lwu66_88M', 5);

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
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `membership` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '罗巍', '2650090110@qq.com', '$2a$10$tvYHQ5kWZN/ud0jVl9M4TOEdnBk1rihK122vhOd/9DZ..QVGfkgY6', 'ADMIN', NULL, 'member');
INSERT INTO `users` VALUES (3, 'bilibili', '2843765089@qq.com', '$2a$10$y3ZtjSZduLckerbBhd0SAOW88vd5Nn2IX4SS5kV9dHI8E8kRW73vu', 'USER', '2025-11-24 21:17:47', NULL);

-- ----------------------------
-- Table structure for verification_codes
-- ----------------------------
DROP TABLE IF EXISTS `verification_codes`;
CREATE TABLE `verification_codes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `identifier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` enum('CAPTCHA','REGISTER','FORGET_PASSWORD') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `used` bit(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_identifier_type`(`identifier` ASC, `type` ASC) USING BTREE,
  INDEX `idx_expires_at`(`expires_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_posts
-- ----------------------------
DROP TABLE IF EXISTS `blog_posts`;
CREATE TABLE `blog_posts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `author_id` bigint NOT NULL,
  `author_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_published` bit(1) NOT NULL DEFAULT b'0',
  `view_count` bigint NULL DEFAULT 0,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `published_at` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_is_published`(`is_published` ASC) USING BTREE,
  INDEX `idx_published_at`(`published_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of verification_codes
-- ----------------------------
INSERT INTO `verification_codes` VALUES (1, '943995', '2025-11-25 16:46:12.456230', '2025-11-25 16:51:12.455230', '2650090110@qq.com', 'FORGET_PASSWORD', b'0');
INSERT INTO `verification_codes` VALUES (2, '898730', '2025-11-25 16:41:22.521110', '2025-11-25 16:46:22.521110', '3054467021@qq.com', 'REGISTER', b'0');
INSERT INTO `verification_codes` VALUES (3, '791959', '2025-11-25 16:44:22.402518', '2025-11-25 16:49:22.401680', '3054467021@qq.com', 'FORGET_PASSWORD', b'0');

SET FOREIGN_KEY_CHECKS = 1;
