CREATE DATABASE  IF NOT EXISTS `quickquack`;
USE `quickquack`;

DROP TABLE IF EXISTS `follow`;
DROP TABLE IF EXISTS `block`;
DROP TABLE IF EXISTS `quacklike`;
DROP TABLE IF EXISTS `quacktag`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `rolepermission`;
DROP TABLE IF EXISTS `userrole`;
DROP TABLE IF EXISTS `quack`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `hashtag`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `permission`;
DROP TABLE IF EXISTS `log`;

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(64) NOT NULL,
  `alias` varchar(45) DEFAULT NULL,
  `password` varchar(128) NOT NULL,
  `password_salt` varchar(45) DEFAULT NULL,
  `lastest_activity` timestamp NULL DEFAULT NULL,
  `account_active` tinyint(4) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `alias_UNIQUE` (`alias`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `user` WRITE;
INSERT INTO `user` VALUES (1,'superadmin@quickquack.com',NULL,'AhD+BMPttye5omv6LGqvcSYXitLHLZKUZZetNiM+eRUuS9J90NSINIpLLcz2rgCJ+8jP7ZFw5aZpOjN1D6qJfA==','gOtI4BTZ7pf8FRljAKthbA==',NULL,1);
UNLOCK TABLES;

CREATE TABLE `block` (
  `blocker_id` int(11) NOT NULL,
  `blocked_id` int(11) NOT NULL,
  PRIMARY KEY (`blocker_id`,`blocked_id`),
  KEY `fk_block_2_idx` (`blocked_id`),
  CONSTRAINT `fk_block_1` FOREIGN KEY (`blocker_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_block_2` FOREIGN KEY (`blocked_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `follow` (
  `follower_id` int(11) NOT NULL,
  `followed_id` int(11) NOT NULL,
  PRIMARY KEY (`follower_id`,`followed_id`),
  KEY `fk_follow_2_idx` (`followed_id`),
  CONSTRAINT `fk_follow_1` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_follow_2` FOREIGN KEY (`followed_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `quack` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `author_id` int(11) NOT NULL,
  `content` varchar(1000) NOT NULL,
  `post_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `scope` tinyint(4) NOT NULL DEFAULT '2',
  PRIMARY KEY (`id`),
  KEY `sdgsdg_idx` (`author_id`),
  CONSTRAINT `author_fkey` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `quacklike` (
  `quack_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`quack_id`,`user_id`),
  KEY `fk_like_2_idx` (`user_id`),
  CONSTRAINT `fk_like_1` FOREIGN KEY (`quack_id`) REFERENCES `quack` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_like_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `hashtag` (
  `identifier` varchar(32) NOT NULL,
  PRIMARY KEY (`identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `quacktag` (
  `tag_identifier` varchar(64) NOT NULL,
  `quack_id` int(11) NOT NULL,
  PRIMARY KEY (`tag_identifier`,`quack_id`),
  KEY `fk_quacktag_2_idx` (`quack_id`),
  CONSTRAINT `fk_quacktag_1` FOREIGN KEY (`tag_identifier`) REFERENCES `hashtag` (`identifier`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_quacktag_2` FOREIGN KEY (`quack_id`) REFERENCES `quack` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `quack_id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL,
  `content` varchar(600) NOT NULL,
  `post_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_comment_1_idx` (`quack_id`),
  KEY `fk_comment_2_idx` (`author_id`),
  CONSTRAINT `fk_comment_1` FOREIGN KEY (`quack_id`) REFERENCES `quack` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_comment_2` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `identifier_UNIQUE` (`name`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `role` WRITE;
INSERT INTO `role` VALUES (2,'Admin'),(1,'SuperAdmin');
UNLOCK TABLES;

CREATE TABLE `permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `permission` WRITE;
INSERT INTO `permission` VALUES (1,'MANAGE_USERS'),(2,'MANAGE_ROLES'),(3,'MANAGE_QUACKS'), (4,'RESET_DATABASE');
UNLOCK TABLES;

CREATE TABLE `rolepermission` (
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY (`permission_id`,`role_id`),
  KEY `fk_rolepermission_1_idx` (`role_id`),
  CONSTRAINT `fk_rolepermission_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rolepermission_2` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `rolepermission` WRITE;
INSERT INTO `rolepermission` VALUES (1,1),(1,2),(1,3),(1,4),(2,1),(2,3);
UNLOCK TABLES;

CREATE TABLE `userrole` (
  `user_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_userroles_2_idx` (`role_id`),
  CONSTRAINT `fk_userroles_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_userroles_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `userrole` WRITE;
INSERT INTO `userrole` VALUES (1,1);
UNLOCK TABLES;

CREATE TABLE `log` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `info` VARCHAR(200) NOT NULL,
  `time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;
